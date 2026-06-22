use core_types::{ProtocolField, ProtocolKind, ProtocolNode};

use crate::ProtocolEngineError;

pub struct Ipv4Packet<'a> {
    pub protocol: u8,
    pub src_ip: String,
    pub dst_ip: String,
    pub payload: &'a [u8],
    pub node: ProtocolNode,
}

pub struct Ipv6Packet<'a> {
    pub next_header: u8,
    pub src_ip: String,
    pub dst_ip: String,
    pub payload: &'a [u8],
    pub node: ProtocolNode,
}

pub struct IcmpPacket {
    pub icmp_type: u8,
    pub icmp_code: u8,
    pub node: ProtocolNode,
}

pub struct ArpPacket {
    pub opcode: u16,
    pub sender_ip: String,
    pub target_ip: String,
    pub node: ProtocolNode,
}

pub fn parse_ipv4_packet(data: &[u8]) -> Result<Ipv4Packet<'_>, ProtocolEngineError> {
    if data.len() < 20 {
        return Err(ProtocolEngineError::MalformedPacket(
            "ipv4 header shorter than 20 bytes".into(),
        ));
    }

    let version = data[0] >> 4;
    let ihl_words = data[0] & 0x0F;
    if version != 4 || ihl_words < 5 {
        return Err(ProtocolEngineError::MalformedPacket(
            "invalid ipv4 version or ihl".into(),
        ));
    }

    let header_len = (ihl_words as usize) * 4;
    if data.len() < header_len {
        return Err(ProtocolEngineError::MalformedPacket(
            "ipv4 shorter than declared ihl".into(),
        ));
    }

    let protocol = data[9];
    let src_ip = format_ipv4(&data[12..16]);
    let dst_ip = format_ipv4(&data[16..20]);

    let total_length = u16::from_be_bytes([data[2], data[3]]) as usize;
    let payload_end = std::cmp::min(total_length, data.len());
    let payload = if payload_end >= header_len {
        &data[header_len..payload_end]
    } else {
        &[]
    };

    let node = ProtocolNode {
        kind: ProtocolKind::Ipv4,
        label: format!("IPv4 {src_ip} -> {dst_ip}"),
        fields: vec![
            ProtocolField {
                name: "src_ip".into(),
                value: src_ip.clone(),
            },
            ProtocolField {
                name: "dst_ip".into(),
                value: dst_ip.clone(),
            },
            ProtocolField {
                name: "protocol".into(),
                value: protocol.to_string(),
            },
        ],
    };

    Ok(Ipv4Packet {
        protocol,
        src_ip,
        dst_ip,
        payload,
        node,
    })
}

pub fn parse_ipv6_packet(data: &[u8]) -> Result<Ipv6Packet<'_>, ProtocolEngineError> {
    if data.len() < 40 {
        return Err(ProtocolEngineError::MalformedPacket(
            "ipv6 header shorter than 40 bytes".into(),
        ));
    }

    let version = data[0] >> 4;
    if version != 6 {
        return Err(ProtocolEngineError::MalformedPacket(
            "invalid ipv6 version".into(),
        ));
    }

    let next_header = data[6];
    let payload_length = u16::from_be_bytes([data[4], data[5]]) as usize;
    let payload_end = std::cmp::min(40 + payload_length, data.len());
    let payload = &data[40..payload_end];

    let src_ip = format_ipv6(&data[8..24]);
    let dst_ip = format_ipv6(&data[24..40]);

    let node = ProtocolNode {
        kind: ProtocolKind::Ipv6,
        label: format!("IPv6 {src_ip} -> {dst_ip}"),
        fields: vec![
            ProtocolField {
                name: "src_ip".into(),
                value: src_ip.clone(),
            },
            ProtocolField {
                name: "dst_ip".into(),
                value: dst_ip.clone(),
            },
            ProtocolField {
                name: "next_header".into(),
                value: next_header.to_string(),
            },
        ],
    };

    Ok(Ipv6Packet {
        next_header,
        src_ip,
        dst_ip,
        payload,
        node,
    })
}

pub fn parse_icmp_packet(data: &[u8]) -> Result<IcmpPacket, ProtocolEngineError> {
    parse_icmp_like_packet(data, ProtocolKind::Icmp, "ICMP")
}

pub fn parse_icmpv6_packet(data: &[u8]) -> Result<IcmpPacket, ProtocolEngineError> {
    parse_icmp_like_packet(data, ProtocolKind::Icmpv6, "ICMPv6")
}

pub fn parse_arp_packet(data: &[u8]) -> Result<ArpPacket, ProtocolEngineError> {
    if data.len() < 28 {
        return Err(ProtocolEngineError::MalformedPacket(
            "arp packet shorter than 28 bytes".into(),
        ));
    }

    let hardware_type = u16::from_be_bytes([data[0], data[1]]);
    let protocol_type = u16::from_be_bytes([data[2], data[3]]);
    let hardware_size = data[4];
    let protocol_size = data[5];
    let opcode = u16::from_be_bytes([data[6], data[7]]);

    if hardware_type != 1 || protocol_type != 0x0800 || hardware_size != 6 || protocol_size != 4 {
        return Err(ProtocolEngineError::MalformedPacket(
            "unsupported arp format".into(),
        ));
    }

    let sender_ip = format_ipv4(&data[14..18]);
    let target_ip = format_ipv4(&data[24..28]);

    let node = ProtocolNode {
        kind: ProtocolKind::Arp,
        label: format!("ARP {} -> {}", sender_ip, target_ip),
        fields: vec![
            ProtocolField {
                name: "opcode".into(),
                value: opcode.to_string(),
            },
            ProtocolField {
                name: "sender_ip".into(),
                value: sender_ip.clone(),
            },
            ProtocolField {
                name: "target_ip".into(),
                value: target_ip.clone(),
            },
        ],
    };

    Ok(ArpPacket {
        opcode,
        sender_ip,
        target_ip,
        node,
    })
}

fn parse_icmp_like_packet(
    data: &[u8],
    kind: ProtocolKind,
    label: &str,
) -> Result<IcmpPacket, ProtocolEngineError> {
    if data.len() < 2 {
        return Err(ProtocolEngineError::MalformedPacket(
            "icmp packet shorter than 2 bytes".into(),
        ));
    }

    let icmp_type = data[0];
    let icmp_code = data[1];

    let node = ProtocolNode {
        kind,
        label: format!("{label} type={icmp_type} code={icmp_code}"),
        fields: vec![
            ProtocolField {
                name: "type".into(),
                value: icmp_type.to_string(),
            },
            ProtocolField {
                name: "code".into(),
                value: icmp_code.to_string(),
            },
        ],
    };

    Ok(IcmpPacket {
        icmp_type,
        icmp_code,
        node,
    })
}

fn format_ipv4(bytes: &[u8]) -> String {
    format!("{}.{}.{}.{}", bytes[0], bytes[1], bytes[2], bytes[3])
}

fn format_ipv6(bytes: &[u8]) -> String {
    let mut parts = Vec::<String>::new();
    for chunk in bytes.chunks(2) {
        parts.push(format!("{:02X}{:02X}", chunk[0], chunk[1]));
    }
    parts.join(":")
}