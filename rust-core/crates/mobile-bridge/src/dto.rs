use facade_api::{
    FlowQuery,
    FlowSearchResultDto,
    PacketQuery,
    PacketSearchResultDto,
    SessionSnapshotDto,
    StoredSessionDto,
};

#[derive(Debug, Clone, PartialEq, Eq, Default)]
pub struct BridgePacketQuery {
    pub protocol: Option<String>,
    pub host: Option<String>,
    pub text: Option<String>,
    pub packet_number: Option<u64>,
    pub offset: usize,
    pub limit: usize,
}

impl From<BridgePacketQuery> for PacketQuery {
    fn from(value: BridgePacketQuery) -> Self {
        Self {
            protocol: value.protocol,
            host: value.host,
            text: value.text,
            packet_number: value.packet_number,
            offset: value.offset,
            limit: value.limit,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Default)]
pub struct BridgeFlowQuery {
    pub protocol: Option<String>,
    pub host: Option<String>,
    pub port: Option<u16>,
    pub text: Option<String>,
    pub offset: usize,
    pub limit: usize,
}

impl From<BridgeFlowQuery> for FlowQuery {
    fn from(value: BridgeFlowQuery) -> Self {
        Self {
            protocol: value.protocol,
            host: value.host,
            port: value.port,
            text: value.text,
            offset: value.offset,
            limit: value.limit,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct BridgeSessionSnapshot {
    pub session_id: String,
    pub source_name: String,
    pub total_packets: usize,
    pub total_flows: usize,
    pub active_packet_number: Option<u64>,
    pub active_flow_label: Option<String>,
    pub search_text: Option<String>,
    pub applied_filters: Vec<String>,
    pub created_at_epoch_micros: u64,
    pub updated_at_epoch_micros: u64,
}

impl From<SessionSnapshotDto> for BridgeSessionSnapshot {
    fn from(value: SessionSnapshotDto) -> Self {
        Self {
            session_id: value.session_id,
            source_name: value.source_name,
            total_packets: value.total_packets,
            total_flows: value.total_flows,
            active_packet_number: value.active_packet_number,
            active_flow_label: value.active_flow_label,
            search_text: value.search_text,
            applied_filters: value.applied_filters,
            created_at_epoch_micros: value.created_at_epoch_micros,
            updated_at_epoch_micros: value.updated_at_epoch_micros,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct BridgePacketSearchHit {
    pub packet_number: u64,
    pub timestamp_epoch_micros: Option<u64>,
    pub highest_protocol: Option<String>,
    pub summary: String,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct BridgePacketSearchResult {
    pub items: Vec<BridgePacketSearchHit>,
    pub total_items: usize,
}

impl From<PacketSearchResultDto> for BridgePacketSearchResult {
    fn from(value: PacketSearchResultDto) -> Self {
        Self {
            items: value
                .items
                .into_iter()
                .map(|item| BridgePacketSearchHit {
                    packet_number: item.packet_number,
                    timestamp_epoch_micros: item.timestamp_epoch_micros,
                    highest_protocol: item.highest_protocol,
                    summary: item.summary,
                })
                .collect(),
            total_items: value.total_items,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct BridgeFlowSearchHit {
    pub label: String,
    pub endpoints: String,
    pub total_packets: usize,
    pub total_payload_bytes: usize,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct BridgeFlowSearchResult {
    pub items: Vec<BridgeFlowSearchHit>,
    pub total_items: usize,
}

impl From<FlowSearchResultDto> for BridgeFlowSearchResult {
    fn from(value: FlowSearchResultDto) -> Self {
        Self {
            items: value
                .items
                .into_iter()
                .map(|item| BridgeFlowSearchHit {
                    label: item.label,
                    endpoints: item.endpoints,
                    total_packets: item.total_packets,
                    total_payload_bytes: item.total_payload_bytes,
                })
                .collect(),
            total_items: value.total_items,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct BridgeSecurityCounts {
    pub safe: usize,
    pub unusual: usize,
    pub suspicious: usize,
    pub active_alerts: usize,
}

#[derive(Debug, Clone, PartialEq)]
pub struct BridgeGeoPoint {
    pub latitude: f64,
    pub longitude: f64,
    pub country_name: String,
    pub country_code: String,
    pub city: Option<String>,
    pub flow_count: usize,
}

#[derive(Debug, Clone, PartialEq)]
pub struct BridgeCaptureOverview {
    pub total_packets: usize,
    pub total_flows: usize,
    pub total_volume_bytes: u64,
    pub average_risk_score: u8,
    pub top_protocols: Vec<(String, usize)>,
    pub top_hosts: Vec<(String, usize)>,
    pub security_counts: BridgeSecurityCounts,
    pub events: Vec<String>,
    pub geo_points: Vec<BridgeGeoPoint>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct BridgeStoredSession {
    pub session_id: String,
    pub source_name: String,
    pub total_packets: usize,
    pub total_flows: usize,
    pub tags: Vec<String>,
    pub notes: Option<String>,
}

impl From<use_cases::models::CaptureOverview> for BridgeCaptureOverview {
    fn from(value: use_cases::models::CaptureOverview) -> Self {
        Self {
            total_packets: value.total_packets,
            total_flows: value.total_flows,
            total_volume_bytes: value.total_volume_bytes,
            average_risk_score: value.average_risk_score,
            top_protocols: value.top_protocols,
            top_hosts: value.top_hosts,
            security_counts: BridgeSecurityCounts {
                safe: value.security_counts.safe,
                unusual: value.security_counts.unusual,
                suspicious: value.security_counts.suspicious,
                active_alerts: value.security_counts.active_alerts,
            },
            events: value.events,
            geo_points: value.geo_points.into_iter().map(|g| BridgeGeoPoint {
                latitude: g.latitude,
                longitude: g.longitude,
                country_name: g.country_name,
                country_code: g.country_code,
                city: g.city,
                flow_count: g.flow_count,
            }).collect(),
        }
    }
}

impl From<StoredSessionDto> for BridgeStoredSession {
    fn from(value: StoredSessionDto) -> Self {
        Self {
            session_id: value.session_id,
            source_name: value.source_name,
            total_packets: value.total_packets,
            total_flows: value.total_flows,
            tags: value.tags,
            notes: value.notes,
        }
    }
}
