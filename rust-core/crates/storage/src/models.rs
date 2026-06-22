use session_manager::SessionSnapshot;
use use_cases::CaptureOverview;

#[derive(Debug, Clone, PartialEq)]
pub struct StoredSessionRecord {
    pub snapshot: SessionSnapshot,
    pub overview: Option<CaptureOverview>,
    pub tags: Vec<String>,
    pub notes: Option<String>,
}

impl StoredSessionRecord {
    pub fn session_id(&self) -> &str {
        &self.snapshot.session_id
    }

    pub fn source_name(&self) -> &str {
        &self.snapshot.source_name
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct StorageStats {
    pub total_sessions: usize,
    pub total_packets_indexed: usize,
    pub total_flows_indexed: usize,
}