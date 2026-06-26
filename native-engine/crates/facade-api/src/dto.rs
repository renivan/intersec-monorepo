use query_engine::{
    FlowSearchHit, FlowSearchResult, PacketSearchHit, PacketSearchResult,
};
use session_manager::SessionSnapshot;
use storage::StoredSessionRecord;

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct SessionSnapshotDto {
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

impl From<SessionSnapshot> for SessionSnapshotDto {
    fn from(value: SessionSnapshot) -> Self {
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
pub struct PacketSearchHitDto {
    pub packet_number: u64,
    pub timestamp_epoch_micros: Option<u64>,
    pub highest_protocol: Option<String>,
    pub summary: String,
}

impl From<PacketSearchHit> for PacketSearchHitDto {
    fn from(value: PacketSearchHit) -> Self {
        Self {
            packet_number: value.packet_number,
            timestamp_epoch_micros: value.timestamp_epoch_micros,
            highest_protocol: value.highest_protocol,
            summary: value.summary,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct PacketSearchResultDto {
    pub items: Vec<PacketSearchHitDto>,
    pub total_items: usize,
}

impl From<PacketSearchResult> for PacketSearchResultDto {
    fn from(value: PacketSearchResult) -> Self {
        Self {
            items: value.items.into_iter().map(Into::into).collect(),
            total_items: value.total_items,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FlowSearchHitDto {
    pub label: String,
    pub endpoints: String,
    pub total_packets: usize,
    pub total_payload_bytes: usize,
}

impl From<FlowSearchHit> for FlowSearchHitDto {
    fn from(value: FlowSearchHit) -> Self {
        Self {
            label: value.label,
            endpoints: value.endpoints,
            total_packets: value.total_packets,
            total_payload_bytes: value.total_payload_bytes,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FlowSearchResultDto {
    pub items: Vec<FlowSearchHitDto>,
    pub total_items: usize,
}

impl From<FlowSearchResult> for FlowSearchResultDto {
    fn from(value: FlowSearchResult) -> Self {
        Self {
            items: value.items.into_iter().map(Into::into).collect(),
            total_items: value.total_items,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct StoredSessionDto {
    pub session_id: String,
    pub source_name: String,
    pub total_packets: usize,
    pub total_flows: usize,
    pub tags: Vec<String>,
    pub notes: Option<String>,
}

impl From<StoredSessionRecord> for StoredSessionDto {
    fn from(value: StoredSessionRecord) -> Self {
        Self {
            session_id: value.snapshot.session_id,
            source_name: value.snapshot.source_name,
            total_packets: value.snapshot.total_packets,
            total_flows: value.snapshot.total_flows,
            tags: value.tags,
            notes: value.notes,
        }
    }
}