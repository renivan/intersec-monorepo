use flow_engine::FlowSummary;
use use_cases::CaptureContext;

use crate::error::QueryEngineError;
use crate::filters::{FlowQuery, PacketQuery};

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct PacketSearchHit {
    pub packet_number: u64,
    pub timestamp_epoch_micros: Option<u64>,
    pub highest_protocol: Option<String>,
    pub summary: String,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct PacketSearchResult {
    pub items: Vec<PacketSearchHit>,
    pub total_items: usize,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FlowSearchHit {
    pub label: String,
    pub endpoints: String,
    pub total_packets: usize,
    pub total_payload_bytes: usize,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FlowSearchResult {
    pub items: Vec<FlowSearchHit>,
    pub total_items: usize,
}

pub fn query_packets(
    ctx: &CaptureContext,
    query: &PacketQuery,
) -> Result<PacketSearchResult, QueryEngineError> {
    validate_packet_query(query)?;

    let filtered_items: Vec<_> = ctx.packets.iter().filter(|packet| {
        if !matches_packet_number(packet.packet_number, query.packet_number) {
            return false;
        }

        if !matches_protocol(
            packet.highest_protocol.as_ref().map(|p| p.label()),
            query.protocol.as_ref(),
        ) {
            return false;
        }

        if !matches_packet_host(packet, query.host.as_ref()) {
            return false;
        }

        if !matches_text(&packet.summary, query.text.as_ref())
            && !packet_contains_text(packet, query.text.as_ref())
        {
            return false;
        }

        true
    }).collect();

    let total_items = filtered_items.len();

    let paginated_items = filtered_items.into_iter()
        .skip(query.offset)
        .take(query.limit)
        .map(|packet| PacketSearchHit {
            packet_number: packet.packet_number,
            timestamp_epoch_micros: packet.timestamp_epoch_micros,
            highest_protocol: packet.highest_protocol.as_ref().map(|p| p.label()),
            summary: packet.summary.clone(),
        })
        .collect();

    Ok(PacketSearchResult {
        total_items,
        items: paginated_items,
    })
}

pub fn query_flows(
    ctx: &CaptureContext,
    query: &FlowQuery,
) -> Result<FlowSearchResult, QueryEngineError> {
    validate_flow_query(query)?;

    let filtered_items: Vec<_> = ctx.flows.iter().filter(|flow| {
        let summary: FlowSummary = flow.summary();

        if !matches_flow_protocol(flow, query.protocol.as_ref()) {
            return false;
        }

        if !matches_flow_host(flow, query.host.as_ref()) {
            return false;
        }

        if !matches_flow_port(flow, query.port) {
            return false;
        }

        if !matches_text(&summary.label, query.text.as_ref())
            && !matches_text(&summary.endpoints, query.text.as_ref())
        {
            return false;
        }

        true
    }).collect();

    let total_items = filtered_items.len();

    let paginated_items = filtered_items.into_iter()
        .skip(query.offset)
        .take(query.limit)
        .map(|flow| {
            let summary = flow.summary();
            FlowSearchHit {
                label: summary.label.clone(),
                endpoints: summary.endpoints.clone(),
                total_packets: summary.total_packets,
                total_payload_bytes: summary.total_payload_bytes,
            }
        })
        .collect();

    Ok(FlowSearchResult {
        total_items,
        items: paginated_items,
    })
}

fn validate_packet_query(query: &PacketQuery) -> Result<(), QueryEngineError> {
    if let Some(protocol) = &query.protocol {
        if protocol.trim().is_empty() {
            return Err(QueryEngineError::InvalidQuery(
                "protocol must not be empty".into(),
            ));
        }
    }

    if let Some(host) = &query.host {
        if host.trim().is_empty() {
            return Err(QueryEngineError::InvalidQuery(
                "host must not be empty".into(),
            ));
        }
    }

    if let Some(text) = &query.text {
        if text.trim().is_empty() {
            return Err(QueryEngineError::InvalidQuery(
                "text must not be empty".into(),
            ));
        }
    }

    Ok(())
}

fn validate_flow_query(query: &FlowQuery) -> Result<(), QueryEngineError> {
    if let Some(protocol) = &query.protocol {
        if protocol.trim().is_empty() {
            return Err(QueryEngineError::InvalidQuery(
                "protocol must not be empty".into(),
            ));
        }
    }

    if let Some(host) = &query.host {
        if host.trim().is_empty() {
            return Err(QueryEngineError::InvalidQuery(
                "host must not be empty".into(),
            ));
        }
    }

    if let Some(text) = &query.text {
        if text.trim().is_empty() {
            return Err(QueryEngineError::InvalidQuery(
                "text must not be empty".into(),
            ));
        }
    }

    Ok(())
}

fn matches_packet_number(packet_number: u64, filter: Option<u64>) -> bool {
    match filter {
        Some(value) => packet_number == value,
        None => true,
    }
}

fn matches_protocol(protocol_label: Option<String>, filter: Option<&String>) -> bool {
    match filter {
        Some(expected) => protocol_label
            .map(|value| value.eq_ignore_ascii_case(expected))
            .unwrap_or(false),
        None => true,
    }
}

fn matches_packet_host(packet: &core_types::ParsedPacket, filter: Option<&String>) -> bool {
    match filter {
        Some(expected) => {
            let expected = expected.to_ascii_lowercase();

            packet.nodes.iter().any(|node| {
                node.fields.iter().any(|field| {
                    (field.name == "src_ip" || field.name == "dst_ip")
                        && field.value.to_ascii_lowercase().contains(&expected)
                })
            })
        }
        None => true,
    }
}

fn packet_contains_text(packet: &core_types::ParsedPacket, filter: Option<&String>) -> bool {
    match filter {
        Some(expected) => {
            let expected = expected.to_ascii_lowercase();

            packet.nodes.iter().any(|node| {
                node.label.to_ascii_lowercase().contains(&expected)
                    || node.fields.iter().any(|field| {
                    field.name.to_ascii_lowercase().contains(&expected)
                        || field.value.to_ascii_lowercase().contains(&expected)
                })
            })
        }
        None => true,
    }
}

fn matches_flow_protocol(flow: &flow_engine::FlowAggregate, filter: Option<&String>) -> bool {
    match filter {
        Some(expected) => {
            let expected = expected.to_ascii_lowercase();

            flow.highest_protocols
                .iter()
                .any(|protocol| protocol.to_ascii_lowercase() == expected)
                || flow
                .key
                .transport
                .label()
                .to_ascii_lowercase()
                .contains(&expected)
        }
        None => true,
    }
}

fn matches_flow_host(flow: &flow_engine::FlowAggregate, filter: Option<&String>) -> bool {
    match filter {
        Some(expected) => {
            let expected = expected.to_ascii_lowercase();
            flow.key.a.address.to_ascii_lowercase().contains(&expected)
                || flow.key.b.address.to_ascii_lowercase().contains(&expected)
        }
        None => true,
    }
}

fn matches_flow_port(flow: &flow_engine::FlowAggregate, filter: Option<u16>) -> bool {
    match filter {
        Some(expected) => flow.key.a.port == Some(expected) || flow.key.b.port == Some(expected),
        None => true,
    }
}

fn matches_text(value: &str, filter: Option<&String>) -> bool {
    match filter {
        Some(expected) => value
            .to_ascii_lowercase()
            .contains(&expected.to_ascii_lowercase()),
        None => true,
    }
}

trait TransportLabel {
    fn label(&self) -> String;
}

impl TransportLabel for flow_engine::TransportProtocol {
    fn label(&self) -> String {
        match self {
            flow_engine::TransportProtocol::Tcp => "TCP".into(),
            flow_engine::TransportProtocol::Udp => "UDP".into(),
            flow_engine::TransportProtocol::Icmp => "ICMP".into(),
            flow_engine::TransportProtocol::Other => "OTHER".into(),
        }
    }
}
