/**
 * DTOs Master de Contrato (Zero-Dependencies)
 * Define a estrutura de dados que trafega entre Native e Kotlin sem depender de implementaÃ§Ãµes.
 */

#[derive(Debug, Clone, PartialEq, Eq, Default)]
pub struct KotlinPacketQuery {
    pub protocol: Option<String>,
    pub host: Option<String>,
    pub text: Option<String>,
    pub packet_number: Option<i64>,
    pub offset: i64,
    pub limit: i32,
}

#[derive(Debug, Clone, PartialEq, Eq, Default)]
pub struct KotlinFlowQuery {
    pub protocol: Option<String>,
    pub host: Option<String>,
    pub port: Option<i32>,
    pub text: Option<String>,
    pub offset: i64,
    pub limit: i32,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct KotlinSessionSnapshot {
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

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct KotlinPacketSearchHit {
    pub packet_number: i64,
    pub timestamp_epoch_micros: Option<i64>,
    pub highest_protocol: Option<String>,
    pub summary: String,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct KotlinPacketSearchResult {
    pub total_items: i64,
    pub items: Vec<KotlinPacketSearchHit>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct KotlinFlowSearchHit {
    pub label: String,
    pub endpoints: String,
    pub total_packets: i64,
    pub total_payload_bytes: i64,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct KotlinFlowSearchResult {
    pub total_items: i64,
    pub items: Vec<KotlinFlowSearchHit>,
}

#[derive(Debug, Clone, PartialEq, Eq, Default)]
pub struct KotlinSecurityCounts {
    pub safe: i64,
    pub unusual: i64,
    pub suspicious: i64,
    pub active_alerts: i64,
}

#[derive(Debug, Clone, PartialEq, Default)]
pub struct KotlinGeoPoint {
    pub latitude: f64,
    pub longitude: f64,
    pub country_name: String,
    pub country_code: String,
    pub flow_count: i64,
}

#[derive(Debug, Clone, PartialEq, Default)]
pub struct KotlinCaptureOverview {
    pub total_packets: i64,
    pub total_flows: i64,
    pub total_volume_bytes: i64,
    pub average_risk_score: i32,
    pub top_protocols: Vec<(String, i64)>,
    pub top_hosts: Vec<(String, i64)>,
    pub security_counts: KotlinSecurityCounts,
    pub events: Vec<String>,
    pub geo_points: Vec<KotlinGeoPoint>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct KotlinStoredSession {
    pub session_id: String,
    pub source_name: String,
    pub total_packets: i64,
    pub total_flows: i64,
    pub tags_csv: String,
    pub notes: Option<String>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct KotlinRuntimeSnapshot {
    pub initialized: bool,
    pub active_capture_loaded: bool,
    pub stored_sessions_count: i64,
    pub last_opened_source: Option<String>,
    pub platform_label: String,
}

