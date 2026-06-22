use crate::{FlowAggregate, TransportProtocol};

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FlowSummary {
    pub label: String,
    pub total_packets: usize,
    pub total_payload_bytes: usize,
    pub endpoints: String,
}

pub fn build_flow_summary(flow: &FlowAggregate) -> FlowSummary {
    let transport = match flow.key.transport {
        TransportProtocol::Tcp => "TCP",
        TransportProtocol::Udp => "UDP",
        TransportProtocol::Icmp => "ICMP",
        TransportProtocol::Other => "OTHER",
    };

    let endpoints = match (flow.key.a.port, flow.key.b.port) {
        (Some(a_port), Some(b_port)) => format!(
            "{}:{} <-> {}:{}",
            flow.key.a.address, a_port, flow.key.b.address, b_port
        ),
        _ => format!("{} <-> {}", flow.key.a.address, flow.key.b.address),
    };

    FlowSummary {
        label: format!("{transport} {endpoints}"),
        total_packets: flow.packet_numbers.len(),
        total_payload_bytes: flow.total_payload_bytes,
        endpoints,
    }
}