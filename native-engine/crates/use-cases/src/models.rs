use core_types::{CaptureMetadata, ParsedPacket};
pub use flow_engine::{FlowAggregate, FlowKey, FlowSummary, FlowSecurityLevel};

#[derive(Debug, Clone)]
pub struct CaptureContext {
    pub source_name: String,
    pub metadata: CaptureMetadata,
    pub packets: Vec<ParsedPacket>,
    pub flows: Vec<FlowAggregate>,
}

#[derive(Debug, Clone, PartialEq, Eq, Default)]
pub struct SecurityCounts {
    pub safe: usize,
    pub unusual: usize,
    pub suspicious: usize,
    pub active_alerts: usize,
}

#[derive(Debug, Clone, PartialEq)]
pub struct GeoPoint {
    pub latitude: f64,
    pub longitude: f64,
    pub country_name: String,
    pub country_code: String,
    pub city: Option<String>,
    pub flow_count: usize,
}

#[derive(Debug, Clone, PartialEq)]
pub struct CaptureOverview {
    pub total_packets: usize,
    pub total_flows: usize,
    pub total_volume_bytes: u64,
    pub average_risk_score: u8,
    pub first_timestamp_epoch_micros: Option<u64>,
    pub last_timestamp_epoch_micros: Option<u64>,
    pub top_protocols: Vec<(String, usize)>,
    pub top_hosts: Vec<(String, usize)>,
    pub security_counts: SecurityCounts,
    pub events: Vec<String>,
    pub geo_points: Vec<GeoPoint>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct PacketListItem {
    pub packet_number: u64,
    pub timestamp_epoch_micros: Option<u64>,
    pub highest_protocol: Option<String>,
    pub summary: String,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct PacketListResult {
    pub items: Vec<PacketListItem>,
    pub total_items: usize,
    pub current_page: usize,
    pub page_size: usize,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct PacketDetail {
    pub packet: ParsedPacket,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FlowListItem {
    pub key: FlowKey,
    pub label: String,
    pub total_packets: usize,
    pub total_payload_bytes: usize,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FlowListResult {
    pub items: Vec<FlowListItem>,
    pub total_items: usize,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FlowDetail {
    pub flow: FlowAggregate,
    pub summary: FlowSummary,
}