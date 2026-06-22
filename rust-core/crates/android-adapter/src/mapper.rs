use ffi_contracts::{
    FfiFlowQuery,
    FfiFlowSearchResult,
    FfiPacketQuery,
    FfiPacketSearchResult,
    FfiSessionSnapshot,
    FfiStoredSession,
};
use kotlin_bridge_contracts::dto::*;

// --- DTOs de Saída do Adaptador ---

#[derive(Debug, Clone, PartialEq, Eq, Default)]
pub struct AndroidPacketQueryInput {
    pub protocol: Option<String>,
    pub host: Option<String>,
    pub text: Option<String>,
    pub packet_number: Option<u64>,
    pub offset: usize,
    pub limit: usize,
}

impl From<AndroidPacketQueryInput> for FfiPacketQuery {
    fn from(value: AndroidPacketQueryInput) -> Self {
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
pub struct AndroidFlowQueryInput {
    pub protocol: Option<String>,
    pub host: Option<String>,
    pub port: Option<u16>,
    pub text: Option<String>,
    pub offset: usize,
    pub limit: usize,
}

impl From<AndroidFlowQueryInput> for FfiFlowQuery {
    fn from(value: AndroidFlowQueryInput) -> Self {
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
pub struct AndroidSessionSnapshotOutput {
    pub session_id: String,
    pub source_name: String,
    pub total_packets: i64,
    pub total_flows: i64,
    pub active_packet_number: Option<i64>,
    pub active_flow_label: Option<String>,
    pub search_text: Option<String>,
    pub applied_filters_csv: String,
    pub created_at_epoch_micros: i64,
    pub updated_at_epoch_micros: i64,
}

impl From<FfiSessionSnapshot> for AndroidSessionSnapshotOutput {
    fn from(value: FfiSessionSnapshot) -> Self {
        Self {
            session_id: value.session_id,
            source_name: value.source_name,
            total_packets: value.total_packets as i64,
            total_flows: value.total_flows as i64,
            active_packet_number: value.active_packet_number.map(|v| v as i64),
            active_flow_label: value.active_flow_label,
            search_text: value.search_text,
            applied_filters_csv: value.applied_filters.join(","),
            created_at_epoch_micros: value.created_at_epoch_micros as i64,
            updated_at_epoch_micros: value.updated_at_epoch_micros as i64,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct AndroidPacketSearchHitOutput {
    pub packet_number: i64,
    pub timestamp_epoch_micros: Option<i64>,
    pub highest_protocol: Option<String>,
    pub summary: String,
}

impl From<ffi_contracts::dto::FfiPacketSearchHit> for AndroidPacketSearchHitOutput {
    fn from(value: ffi_contracts::dto::FfiPacketSearchHit) -> Self {
        Self {
            packet_number: value.packet_number as i64,
            timestamp_epoch_micros: value.timestamp_epoch_micros.map(|v| v as i64),
            highest_protocol: value.highest_protocol,
            summary: value.summary,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct AndroidPacketSearchResultOutput {
    pub total_items: i64,
    pub items: Vec<AndroidPacketSearchHitOutput>,
}

impl From<FfiPacketSearchResult> for AndroidPacketSearchResultOutput {
    fn from(value: FfiPacketSearchResult) -> Self {
        Self {
            total_items: value.total_items as i64,
            items: value
                .items
                .into_iter()
                .map(|item| AndroidPacketSearchHitOutput {
                    packet_number: item.packet_number as i64,
                    timestamp_epoch_micros: item.timestamp_epoch_micros.map(|v| v as i64),
                    highest_protocol: item.highest_protocol,
                    summary: item.summary,
                })
                .collect(),
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct AndroidFlowSearchHitOutput {
    pub label: String,
    pub endpoints: String,
    pub total_packets: i64,
    pub total_payload_bytes: i64,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct AndroidFlowSearchResultOutput {
    pub total_items: i64,
    pub items: Vec<AndroidFlowSearchHitOutput>,
}

impl From<FfiFlowSearchResult> for AndroidFlowSearchResultOutput {
    fn from(value: FfiFlowSearchResult) -> Self {
        Self {
            total_items: value.total_items as i64,
            items: value
                .items
                .into_iter()
                .map(|item| AndroidFlowSearchHitOutput {
                    label: item.label,
                    endpoints: item.endpoints,
                    total_packets: item.total_packets as i64,
                    total_payload_bytes: item.total_payload_bytes as i64,
                })
                .collect(),
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct AndroidSecurityCountsOutput {
    pub safe: i64,
    pub unusual: i64,
    pub suspicious: i64,
    pub active_alerts: i64,
}

#[derive(Debug, Clone, PartialEq)]
pub struct AndroidCaptureOverviewOutput {
    pub total_packets: i64,
    pub total_flows: i64,
    pub total_volume_bytes: i64,
    pub average_risk_score: i32,
    pub top_protocols: Vec<(String, i64)>,
    pub top_hosts: Vec<(String, i64)>,
    pub security_counts: AndroidSecurityCountsOutput,
    pub events: Vec<String>,
    pub geo_points: Vec<KotlinGeoPoint>,
}

impl From<ffi_contracts::dto::FfiCaptureOverview> for AndroidCaptureOverviewOutput {
    fn from(value: ffi_contracts::dto::FfiCaptureOverview) -> Self {
        Self {
            total_packets: value.total_packets as i64,
            total_flows: value.total_flows as i64,
            total_volume_bytes: value.total_volume_bytes as i64,
            average_risk_score: value.average_risk_score as i32,
            top_protocols: value.top_protocols.into_iter().map(|(s, n)| (s, n as i64)).collect(),
            top_hosts: value.top_hosts.into_iter().map(|(s, n)| (s, n as i64)).collect(),
            security_counts: AndroidSecurityCountsOutput {
                safe: value.security_counts.safe as i64,
                unusual: value.security_counts.unusual as i64,
                suspicious: value.security_counts.suspicious as i64,
                active_alerts: value.security_counts.active_alerts as i64,
            },
            events: value.events,
            geo_points: value.geo_points.into_iter().map(|g| KotlinGeoPoint {
                latitude: g.latitude,
                longitude: g.longitude,
                country_name: g.country_name,
                country_code: g.country_code,
                flow_count: g.flow_count as i64,
            }).collect(),
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct AndroidStoredSessionOutput {
    pub session_id: String,
    pub source_name: String,
    pub total_packets: i64,
    pub total_flows: i64,
    pub tags_csv: String,
    pub notes: Option<String>,
}

impl From<FfiStoredSession> for AndroidStoredSessionOutput {
    fn from(value: FfiStoredSession) -> Self {
        Self {
            session_id: value.session_id,
            source_name: value.source_name,
            total_packets: value.total_packets as i64,
            total_flows: value.total_flows as i64,
            tags_csv: value.tags.join(","),
            notes: value.notes,
        }
    }
}

// --- Conversões Master para Kotlin Bridge Contracts ---

impl From<AndroidSessionSnapshotOutput> for KotlinSessionSnapshot {
    fn from(value: AndroidSessionSnapshotOutput) -> Self {
        Self {
            session_id: value.session_id,
            source_name: value.source_name,
            total_packets: value.total_packets,
            total_flows: value.total_flows,
            active_packet_number: value.active_packet_number,
            active_flow_label: value.active_flow_label,
            search_text: value.search_text,
            applied_filters_csv: value.applied_filters_csv,
            created_at_epoch_micros: value.created_at_epoch_micros,
            updated_at_epoch_micros: value.updated_at_epoch_micros,
        }
    }
}

impl From<AndroidCaptureOverviewOutput> for KotlinCaptureOverview {
    fn from(value: AndroidCaptureOverviewOutput) -> Self {
        Self {
            total_packets: value.total_packets,
            total_flows: value.total_flows,
            total_volume_bytes: value.total_volume_bytes,
            average_risk_score: value.average_risk_score,
            top_protocols: value.top_protocols,
            top_hosts: value.top_hosts,
            security_counts: KotlinSecurityCounts {
                safe: value.security_counts.safe,
                unusual: value.security_counts.unusual,
                suspicious: value.security_counts.suspicious,
                active_alerts: value.security_counts.active_alerts,
            },
            events: value.events,
            geo_points: value.geo_points,
        }
    }
}

impl From<AndroidPacketSearchResultOutput> for KotlinPacketSearchResult {
    fn from(value: AndroidPacketSearchResultOutput) -> Self {
        Self {
            total_items: value.total_items,
            items: value.items.into_iter().map(Into::into).collect(),
        }
    }
}

impl From<AndroidPacketSearchHitOutput> for KotlinPacketSearchHit {
    fn from(value: AndroidPacketSearchHitOutput) -> Self {
        Self {
            packet_number: value.packet_number,
            timestamp_epoch_micros: value.timestamp_epoch_micros,
            highest_protocol: value.highest_protocol,
            summary: value.summary,
        }
    }
}

impl From<AndroidFlowSearchResultOutput> for KotlinFlowSearchResult {
    fn from(value: AndroidFlowSearchResultOutput) -> Self {
        Self {
            total_items: value.total_items,
            items: value.items.into_iter().map(Into::into).collect(),
        }
    }
}

impl From<AndroidFlowSearchHitOutput> for KotlinFlowSearchHit {
    fn from(value: AndroidFlowSearchHitOutput) -> Self {
        Self {
            label: value.label,
            endpoints: value.endpoints,
            total_packets: value.total_packets,
            total_payload_bytes: value.total_payload_bytes,
        }
    }
}

impl From<AndroidStoredSessionOutput> for KotlinStoredSession {
    fn from(value: AndroidStoredSessionOutput) -> Self {
        Self {
            session_id: value.session_id,
            source_name: value.source_name,
            total_packets: value.total_packets,
            total_flows: value.total_flows,
            tags_csv: value.tags_csv,
            notes: value.notes,
        }
    }
}
