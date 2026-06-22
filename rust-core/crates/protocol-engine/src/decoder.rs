use core_types::{PacketRecord, ParsedPacket, ProtocolKind, ProtocolNode};

use crate::dissectors::application::{detect_application_tcp, detect_application_udp};
use crate::dissectors::link::{parse_ethernet_frame, parse_null_link};
use crate::dissectors::network::{
    parse_arp_packet, parse_icmp_packet, parse_icmpv6_packet, parse_ipv4_packet, parse_ipv6_packet,
};
use crate::dissectors::transport::{parse_tcp_segment, parse_udp_datagram};
use crate::summary::build_summary;
use crate::ProtocolEngineError;

pub fn decode_packet(record: &PacketRecord) -> Result<ParsedPacket, ProtocolEngineError> {
    let mut nodes = Vec::<ProtocolNode>::new();
    let mut highest_protocol = None;

    let link_type = record
        .link_type
        .ok_or_else(|| ProtocolEngineError::MalformedPacket("missing link_type".into()))?;

    match link_type {
        1 => { // Ethernet
            let ethernet = parse_ethernet_frame(&record.raw_data)?;
            nodes.push(ethernet.node);
            decode_from_ether_type(ethernet.ether_type, ethernet.payload, &mut nodes, &mut highest_protocol)?;
        },
        0 | 108 => { // Loopback (BSD NULL)
            let null_link = parse_null_link(&record.raw_data)?;
            nodes.push(null_link.node);
            // Mapeia família do protocolo Loopback para EtherType equivalente
            let pseudo_ether_type = match null_link.protocol_family {
                2 => 0x0800, // IPv4
                24 | 28 | 30 => 0x86DD, // IPv6
                _ => 0x0000,
            };
            decode_from_ether_type(pseudo_ether_type, null_link.payload, &mut nodes, &mut highest_protocol)?;
        },
        other => return Err(ProtocolEngineError::UnsupportedLinkType(other)),
    }

    let summary = build_summary(&nodes);

    Ok(ParsedPacket {
        packet_number: record.packet_number,
        timestamp_epoch_micros: record.timestamp_epoch_micros,
        link_type: record.link_type,
        highest_protocol,
        nodes,
        summary,
        warnings: Vec::new(),
    })
}

fn decode_from_ether_type(
    ether_type: u16,
    payload: &[u8],
    nodes: &mut Vec<ProtocolNode>,
    highest_protocol: &mut Option<ProtocolKind>
) -> Result<(), ProtocolEngineError> {
    match ether_type {
        0x0800 => {
            let ipv4 = parse_ipv4_packet(payload)?;
            nodes.push(ipv4.node);

            *highest_protocol = match ipv4.protocol {
                6 => {
                    match parse_tcp_segment(ipv4.payload) {
                        Ok(tcp) => {
                            nodes.push(tcp.node);
                            if let Some(app_node) = detect_application_tcp(tcp.src_port, tcp.dst_port, tcp.payload) {
                                let kind = app_node.kind.clone();
                                nodes.push(app_node);
                                Some(kind)
                            } else {
                                Some(ProtocolKind::Tcp)
                            }
                        },
                        Err(_) => Some(ProtocolKind::Unknown("Malformed TCP".into())),
                    }
                }
                17 => {
                    match parse_udp_datagram(ipv4.payload) {
                        Ok(udp) => {
                            nodes.push(udp.node);
                            if let Some(app_node) = detect_application_udp(udp.src_port, udp.dst_port, udp.payload) {
                                let kind = app_node.kind.clone();
                                nodes.push(app_node);
                                Some(kind)
                            } else {
                                Some(ProtocolKind::Udp)
                            }
                        },
                        Err(_) => Some(ProtocolKind::Unknown("Malformed UDP".into())),
                    }
                }
                1 => {
                    let icmp = parse_icmp_packet(ipv4.payload)?;
                    nodes.push(icmp.node);
                    Some(ProtocolKind::Icmp)
                }
                other => Some(ProtocolKind::Unknown(format!("ipv4_protocol_{other}"))),
            };
        }
        0x86DD => {
            let ipv6 = parse_ipv6_packet(payload)?;
            nodes.push(ipv6.node);

            *highest_protocol = match ipv6.next_header {
                6 => {
                    match parse_tcp_segment(ipv6.payload) {
                        Ok(tcp) => {
                            nodes.push(tcp.node);
                            if let Some(app_node) = detect_application_tcp(tcp.src_port, tcp.dst_port, tcp.payload) {
                                let kind = app_node.kind.clone();
                                nodes.push(app_node);
                                Some(kind)
                            } else {
                                Some(ProtocolKind::Tcp)
                            }
                        },
                        Err(_) => Some(ProtocolKind::Unknown("Malformed TCP".into())),
                    }
                }
                17 => {
                    match parse_udp_datagram(ipv6.payload) {
                        Ok(udp) => {
                            nodes.push(udp.node);
                            if let Some(app_node) = detect_application_udp(udp.src_port, udp.dst_port, udp.payload) {
                                let kind = app_node.kind.clone();
                                nodes.push(app_node);
                                Some(kind)
                            } else {
                                Some(ProtocolKind::Udp)
                            }
                        },
                        Err(_) => Some(ProtocolKind::Unknown("Malformed UDP".into())),
                    }
                }
                58 => {
                    let icmpv6 = parse_icmpv6_packet(ipv6.payload)?;
                    nodes.push(icmpv6.node);
                    Some(ProtocolKind::Icmpv6)
                }
                other => Some(ProtocolKind::Unknown(format!("ipv6_next_header_{other}"))),
            };
        }
        0x0806 => {
            let arp = parse_arp_packet(payload)?;
            nodes.push(arp.node);
            *highest_protocol = Some(ProtocolKind::Arp);
        }
        other => {
            *highest_protocol = Some(ProtocolKind::Unknown(format!("ether_type_{other:04X}")));
        }
    }
    Ok(())
}
