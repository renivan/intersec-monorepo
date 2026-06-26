use mobile_bridge::{
    BridgeFlowQuery,
    BridgeFlowSearchResult,
    BridgePacketQuery,
    BridgePacketSearchHit,
    BridgePacketSearchResult,
    BridgeSessionSnapshot,
    BridgeStoredSession,
};

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FfiResponse<T> {
    pub ok: bool,
    pub message: String,
    pub data: Option<T>,
}

impl<T> FfiResponse<T> {
    pub fn success(message: impl Into<String>, data: T) -> Self {
        Self {
            ok: true,
            message: message.into(),
            data: Some(data),
        }
    }

    pub fn success_without_data(message: impl Into<String>) -> Self {
        Self {
            ok: true,
            message: message.into(),
            data: None,
        }
    }

    pub fn failure(message: impl Into<String>) -> Self {
        Self {
            ok: false,
            message: message.into(),
            data: None,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Default)]
pub struct FfiPacketQuery {
    pub protocol: Option<String>,
    pub host: Option<String>,
    pub text: Option<String>,
    pub packet_number: Option<u64>,
    pub offset: usize,
    pub limit: usize,
}

impl From<FfiPacketQuery> for BridgePacketQuery {
    fn from(value: FfiPacketQuery) -> Self {
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
pub struct FfiFlowQuery {
    pub protocol: Option<String>,
    pub host: Option<String>,
    pub port: Option<u16>,
    pub text: Option<String>,
    pub offset: usize,
    pub limit: usize,
}

impl From<FfiFlowQuery> for BridgeFlowQuery {
    fn from(value: FfiFlowQuery) -> Self {
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
pub struct FfiSessionSnapshot {
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

impl From<BridgeSessionSnapshot> for FfiSessionSnapshot {
    fn from(value: BridgeSessionSnapshot) -> Self {
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
pub struct FfiPacketSearchHit {
    pub packet_number: u64,
    pub timestamp_epoch_micros: Option<u64>,
    pub highest_protocol: Option<String>,
    pub summary: String,
}

impl From<BridgePacketSearchHit> for FfiPacketSearchHit {
    fn from(value: BridgePacketSearchHit) -> Self {
        Self {
            packet_number: value.packet_number,
            timestamp_epoch_micros: value.timestamp_epoch_micros,
            highest_protocol: value.highest_protocol,
            summary: value.summary,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FfiPacketSearchResult {
    pub items: Vec<FfiPacketSearchHit>,
    pub total_items: usize,
}

impl From<BridgePacketSearchResult> for FfiPacketSearchResult {
    fn from(value: BridgePacketSearchResult) -> Self {
        Self {
            items: value
                .items
                .into_iter()
                .map(|item| FfiPacketSearchHit {
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
pub struct FfiFlowSearchHit {
    pub label: String,
    pub endpoints: String,
    pub total_packets: usize,
    pub total_payload_bytes: usize,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FfiFlowSearchResult {
    pub items: Vec<FfiFlowSearchHit>,
    pub total_items: usize,
}

impl From<BridgeFlowSearchResult> for FfiFlowSearchResult {
    fn from(value: BridgeFlowSearchResult) -> Self {
        Self {
            items: value
                .items
                .into_iter()
                .map(|item| FfiFlowSearchHit {
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
pub struct FfiSecurityCounts {
    pub safe: usize,
    pub unusual: usize,
    pub suspicious: usize,
    pub active_alerts: usize,
}

#[derive(Debug, Clone, PartialEq)]
pub struct FfiGeoPoint {
    pub latitude: f64,
    pub longitude: f64,
    pub country_name: String,
    pub country_code: String,
    pub flow_count: usize,
}

#[derive(Debug, Clone, PartialEq)]
pub struct FfiCaptureOverview {
    pub total_packets: usize,
    pub total_flows: usize,
    pub total_volume_bytes: u64,
    pub average_risk_score: u8,
    pub top_protocols: Vec<(String, usize)>,
    pub top_hosts: Vec<(String, usize)>,
    pub security_counts: FfiSecurityCounts,
    pub events: Vec<String>,
    pub geo_points: Vec<FfiGeoPoint>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FfiStoredSession {
    pub session_id: String,
    pub source_name: String,
    pub total_packets: usize,
    pub total_flows: usize,
    pub tags: Vec<String>,
    pub notes: Option<String>,
}

impl From<mobile_bridge::dto::BridgeCaptureOverview> for FfiCaptureOverview {
    fn from(value: mobile_bridge::dto::BridgeCaptureOverview) -> Self {
        Self {
            total_packets: value.total_packets,
            total_flows: value.total_flows,
            total_volume_bytes: value.total_volume_bytes,
            average_risk_score: value.average_risk_score,
            top_protocols: value.top_protocols,
            top_hosts: value.top_hosts,
            security_counts: FfiSecurityCounts {
                safe: value.security_counts.safe,
                unusual: value.security_counts.unusual,
                suspicious: value.security_counts.suspicious,
                active_alerts: value.security_counts.active_alerts,
            },
            events: value.events,
            geo_points: value.geo_points.into_iter().map(|g| FfiGeoPoint {
                latitude: g.latitude,
                longitude: g.longitude,
                country_name: g.country_name,
                country_code: g.country_code,
                flow_count: g.flow_count,
            }).collect(),
        }
    }
}

impl From<BridgeStoredSession> for FfiStoredSession {
    fn from(value: BridgeStoredSession) -> Self {
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
