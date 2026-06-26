use ffi_contracts::{
    FfiFlowQuery,
    FfiFlowSearchResult,
    FfiPacketQuery,
    FfiPacketSearchResult,
    FfiSessionSnapshot,
    FfiStoredSession,
};

#[derive(Debug, Clone, PartialEq, Eq, Default)]
pub struct IosPacketQueryInput {
    pub protocol: Option<String>,
    pub host: Option<String>,
    pub text: Option<String>,
    pub packet_number: Option<u64>,
    pub offset: usize,
    pub limit: usize,
}

impl From<IosPacketQueryInput> for FfiPacketQuery {
    fn from(value: IosPacketQueryInput) -> Self {
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
pub struct IosFlowQueryInput {
    pub protocol: Option<String>,
    pub host: Option<String>,
    pub port: Option<u16>,
    pub text: Option<String>,
    pub offset: usize,
    pub limit: usize,
}

impl From<IosFlowQueryInput> for FfiFlowQuery {
    fn from(value: IosFlowQueryInput) -> Self {
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
pub struct IosSessionSnapshotOutput {
    pub session_id: String,
    pub source_name: String,
    pub total_packets: u64,
    pub total_flows: u64,
    pub active_packet_number: Option<u64>,
    pub active_flow_label: Option<String>,
    pub search_text: Option<String>,
    pub applied_filters: Vec<String>,
    pub created_at_epoch_micros: u64,
    pub updated_at_epoch_micros: u64,
}

impl From<FfiSessionSnapshot> for IosSessionSnapshotOutput {
    fn from(value: FfiSessionSnapshot) -> Self {
        Self {
            session_id: value.session_id,
            source_name: value.source_name,
            total_packets: value.total_packets as u64,
            total_flows: value.total_flows as u64,
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
pub struct IosPacketSearchHitOutput {
    pub packet_number: u64,
    pub timestamp_epoch_micros: Option<u64>,
    pub highest_protocol: Option<String>,
    pub summary: String,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct IosPacketSearchResultOutput {
    pub total_items: u64,
    pub items: Vec<IosPacketSearchHitOutput>,
}

impl From<FfiPacketSearchResult> for IosPacketSearchResultOutput {
    fn from(value: FfiPacketSearchResult) -> Self {
        Self {
            total_items: value.total_items as u64,
            items: value
                .items
                .into_iter()
                .map(|item| IosPacketSearchHitOutput {
                    packet_number: item.packet_number,
                    timestamp_epoch_micros: item.timestamp_epoch_micros,
                    highest_protocol: item.highest_protocol,
                    summary: item.summary,
                })
                .collect(),
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct IosFlowSearchHitOutput {
    pub label: String,
    pub endpoints: String,
    pub total_packets: u64,
    pub total_payload_bytes: u64,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct IosFlowSearchResultOutput {
    pub total_items: u64,
    pub items: Vec<IosFlowSearchHitOutput>,
}

impl From<FfiFlowSearchResult> for IosFlowSearchResultOutput {
    fn from(value: FfiFlowSearchResult) -> Self {
        Self {
            total_items: value.total_items as u64,
            items: value
                .items
                .into_iter()
                .map(|item| IosFlowSearchHitOutput {
                    label: item.label,
                    endpoints: item.endpoints,
                    total_packets: item.total_packets as u64,
                    total_payload_bytes: item.total_payload_bytes as u64,
                })
                .collect(),
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct IosStoredSessionOutput {
    pub session_id: String,
    pub source_name: String,
    pub total_packets: u64,
    pub total_flows: u64,
    pub tags: Vec<String>,
    pub notes: Option<String>,
}

impl From<FfiStoredSession> for IosStoredSessionOutput {
    fn from(value: FfiStoredSession) -> Self {
        Self {
            session_id: value.session_id,
            source_name: value.source_name,
            total_packets: value.total_packets as u64,
            total_flows: value.total_flows as u64,
            tags: value.tags,
            notes: value.notes,
        }
    }
}
