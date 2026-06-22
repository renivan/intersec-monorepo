use flow_engine::FlowKey;
use use_cases::CaptureContext;

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct SessionId(pub String);

#[derive(Debug, Clone)]
pub struct SessionState {
    pub session_id: SessionId,
    pub capture: CaptureContext,
    pub active_packet_number: Option<u64>,
    pub active_flow_key: Option<FlowKey>,
    pub search_text: Option<String>,
    pub applied_filters: Vec<String>,
    pub created_at_epoch_micros: u64,
    pub updated_at_epoch_micros: u64,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct SessionSnapshot {
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