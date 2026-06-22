use core_types::{ParsedPacket, ProtocolKind, ProtocolNode};

use crate::FlowEngineError;

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub enum TransportProtocol {
    Tcp,
    Udp,
    Icmp,
    Other,
}

#[derive(Debug, Clone, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub struct FlowEndpoint {
    pub address: String,
    pub port: Option<u16>,
}

#[derive(Debug, Clone, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub struct FlowKey {
    pub transport: TransportProtocol,
    pub a: FlowEndpoint,
    pub b: FlowEndpoint,
}

pub fn identify_flow(packet: &ParsedPacket) -> Result<FlowKey, FlowEngineError> {
    let (src_ip, dst_ip) = extract_ip_pair(&packet.nodes).ok_or(FlowEngineError::MissingAddressLayer)?;

    // TCP e UDP usam portas.
    if let Some((src_port, dst_port, transport)) = extract_ports_and_transport(&packet.nodes)? {
        let left = FlowEndpoint {
            address: src_ip,
            port: Some(src_port),
        };
        let right = FlowEndpoint {
            address: dst_ip,
            port: Some(dst_port),
        };

        return Ok(canonicalize(FlowKey {
            transport,
            a: left,
            b: right,
        }));
    }

    // ICMP e outros fluxos baseados apenas em IP.
    let transport = if has_protocol(&packet.nodes, ProtocolKind::Icmp) || has_protocol(&packet.nodes, ProtocolKind::Icmpv6) {
        TransportProtocol::Icmp
    } else {
        TransportProtocol::Other
    };

    let left = FlowEndpoint {
        address: src_ip,
        port: None,
    };
    let right = FlowEndpoint {
        address: dst_ip,
        port: None,
    };

    Ok(canonicalize(FlowKey {
        transport,
        a: left,
        b: right,
    }))
}

fn canonicalize(mut key: FlowKey) -> FlowKey {
    // Ordenação canônica para agrupar ida/volta no mesmo fluxo.
    if key.b < key.a {
        std::mem::swap(&mut key.a, &mut key.b);
    }
    key
}

fn has_protocol(nodes: &[ProtocolNode], kind: ProtocolKind) -> bool {
    nodes.iter().any(|n| n.kind == kind)
}

fn extract_ip_pair(nodes: &[ProtocolNode]) -> Option<(String, String)> {
    for node in nodes {
        match node.kind {
            ProtocolKind::Ipv4 | ProtocolKind::Ipv6 => {
                let src = field_value(node, "src_ip")?;
                let dst = field_value(node, "dst_ip")?;
                return Some((src, dst));
            }
            _ => {}
        }
    }
    None
}

fn extract_ports_and_transport(
    nodes: &[ProtocolNode],
) -> Result<Option<(u16, u16, TransportProtocol)>, FlowEngineError> {
    for node in nodes {
        match node.kind {
            ProtocolKind::Tcp => {
                let src = parse_port(
                    field_value(node, "src_port")
                        .ok_or_else(|| FlowEngineError::InvalidPort("missing tcp src_port".into()))?,
                )?;
                let dst = parse_port(
                    field_value(node, "dst_port")
                        .ok_or_else(|| FlowEngineError::InvalidPort("missing tcp dst_port".into()))?,
                )?;
                return Ok(Some((src, dst, TransportProtocol::Tcp)));
            }
            ProtocolKind::Udp => {
                let src = parse_port(
                    field_value(node, "src_port")
                        .ok_or_else(|| FlowEngineError::InvalidPort("missing udp src_port".into()))?,
                )?;
                let dst = parse_port(
                    field_value(node, "dst_port")
                        .ok_or_else(|| FlowEngineError::InvalidPort("missing udp dst_port".into()))?,
                )?;
                return Ok(Some((src, dst, TransportProtocol::Udp)));
            }
            _ => {}
        }
    }

    Ok(None)
}

fn parse_port(value: String) -> Result<u16, FlowEngineError> {
    value.parse::<u16>().map_err(|_| FlowEngineError::InvalidPort(value))
}

fn field_value(node: &ProtocolNode, name: &str) -> Option<String> {
    node.fields
        .iter()
        .find(|field| field.name == name)
        .map(|field| field.value.clone())
}
