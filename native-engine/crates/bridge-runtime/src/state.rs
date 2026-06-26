#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum BridgePlatform {
    Android,
    Ios,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct BridgeRuntimeSnapshot {
    pub platform: BridgePlatform,
    pub initialized: bool,
    pub active_capture_loaded: bool,
    pub stored_sessions_count: u64,
    pub last_opened_source: Option<String>,
}