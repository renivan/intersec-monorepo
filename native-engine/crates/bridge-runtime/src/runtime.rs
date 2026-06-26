use android_adapter::{
    AndroidAdapter,
    AndroidFlowQueryInput,
    AndroidPacketQueryInput,
    AndroidPacketSearchResultOutput,
    AndroidFlowSearchResultOutput,
    AndroidSessionSnapshotOutput,
    AndroidStoredSessionOutput,
};
use ios_adapter::{
    IosAdapter,
    IosFlowQueryInput,
    IosPacketQueryInput,
    IosPacketSearchResultOutput,
    IosFlowSearchResultOutput,
    IosSessionSnapshotOutput,
    IosStoredSessionOutput,
};

use crate::error::BridgeRuntimeError;
use crate::state::{BridgePlatform, BridgeRuntimeSnapshot};

pub struct BridgeRuntime {
    android: AndroidAdapter,
    ios: IosAdapter,
    initialized: bool,
    active_capture_loaded: bool,
    last_opened_source: Option<String>,
}

impl BridgeRuntime {
    pub fn new() -> Self {
        Self {
            android: AndroidAdapter::new(),
            ios: IosAdapter::new(),
            initialized: true,
            active_capture_loaded: false,
            last_opened_source: None,
        }
    }

    pub fn ping_android(&self) -> Result<String, BridgeRuntimeError> {
        if !self.initialized {
            return Err(BridgeRuntimeError::InvalidRuntimeState(
                "runtime not initialized".into(),
            ));
        }

        Ok(self.android.ping())
    }

    pub fn ping_ios(&self) -> Result<String, BridgeRuntimeError> {
        if !self.initialized {
            return Err(BridgeRuntimeError::InvalidRuntimeState(
                "runtime not initialized".into(),
            ));
        }

        Ok(self.ios.ping())
    }

    pub fn open_capture_android(
        &mut self,
        path: &str,
        now_epoch_micros: i64,
    ) -> Result<AndroidSessionSnapshotOutput, BridgeRuntimeError> {
        let snapshot = self.android.open_capture(path, now_epoch_micros)?;
        self.active_capture_loaded = true;
        self.last_opened_source = Some(snapshot.source_name.clone());
        Ok(snapshot)
    }

    pub fn open_capture_ios(
        &mut self,
        path: &str,
        now_epoch_micros: u64,
    ) -> Result<IosSessionSnapshotOutput, BridgeRuntimeError> {
        let snapshot = self.ios.open_capture(path, now_epoch_micros)?;
        self.active_capture_loaded = true;
        self.last_opened_source = Some(snapshot.source_name.clone());
        Ok(snapshot)
    }

    pub fn snapshot_android(&self) -> Result<AndroidSessionSnapshotOutput, BridgeRuntimeError> {
        self.ensure_active()?;
        Ok(self.android.snapshot_active()?)
    }

    pub fn snapshot_ios(&self) -> Result<IosSessionSnapshotOutput, BridgeRuntimeError> {
        self.ensure_active()?;
        Ok(self.ios.snapshot_active()?)
    }

    pub fn query_packets_android(
        &self,
        query: AndroidPacketQueryInput,
    ) -> Result<AndroidPacketSearchResultOutput, BridgeRuntimeError> {
        self.ensure_active()?;
        Ok(self.android.query_packets(query)?)
    }

    pub fn query_packets_ios(
        &self,
        query: IosPacketQueryInput,
    ) -> Result<IosPacketSearchResultOutput, BridgeRuntimeError> {
        self.ensure_active()?;
        Ok(self.ios.query_packets(query)?)
    }

    pub fn query_flows_android(
        &self,
        query: AndroidFlowQueryInput,
    ) -> Result<AndroidFlowSearchResultOutput, BridgeRuntimeError> {
        self.ensure_active()?;
        Ok(self.android.query_flows(query)?)
    }

    pub fn query_flows_ios(
        &self,
        query: IosFlowQueryInput,
    ) -> Result<IosFlowSearchResultOutput, BridgeRuntimeError> {
        self.ensure_active()?;
        Ok(self.ios.query_flows(query)?)
    }

    pub fn persist_active_android(
        &mut self,
        tags_csv: &str,
        notes: Option<String>,
    ) -> Result<(), BridgeRuntimeError> {
        self.ensure_active()?;
        self.android.persist_active(tags_csv, notes)?;
        Ok(())
    }

    pub fn persist_active_ios(
        &mut self,
        tags_csv: &str,
        notes: Option<String>,
    ) -> Result<(), BridgeRuntimeError> {
        self.ensure_active()?;
        self.ios.persist_active(tags_csv, notes)?;
        Ok(())
    }

    pub fn list_stored_sessions_android(
        &self,
    ) -> Result<Vec<AndroidStoredSessionOutput>, BridgeRuntimeError> {
        Ok(self.android.list_stored_sessions()?)
    }

    pub fn list_stored_sessions_ios(
        &self,
    ) -> Result<Vec<IosStoredSessionOutput>, BridgeRuntimeError> {
        Ok(self.ios.list_stored_sessions()?)
    }

    pub fn get_capture_overview_android(
        &self,
    ) -> Result<android_adapter::AndroidCaptureOverviewOutput, BridgeRuntimeError> {
        self.ensure_active()?;
        Ok(self.android.get_capture_overview()?)
    }

    pub fn start_capture_android(&mut self, iface: &str, filter: &str) -> Result<String, BridgeRuntimeError> {
        let id = self.android.start_capture(iface, filter)?;
        self.active_capture_loaded = true;
        Ok(id)
    }

    pub fn stop_capture_android(&mut self, session_id: &str) -> Result<AndroidSessionSnapshotOutput, BridgeRuntimeError> {
        let snapshot = self.android.stop_capture(session_id)?;
        Ok(snapshot)
    }

    pub fn get_latest_packets_android(&self, limit: usize) -> Result<Vec<android_adapter::AndroidPacketSearchHitOutput>, BridgeRuntimeError> {
        self.ensure_active()?;
        Ok(self.android.get_latest_packets(limit)?)
    }

    pub fn runtime_snapshot_android(&self) -> Result<BridgeRuntimeSnapshot, BridgeRuntimeError> {
        let stored_count = self.android.list_stored_sessions()?.len() as u64;

        Ok(BridgeRuntimeSnapshot {
            platform: BridgePlatform::Android,
            initialized: self.initialized,
            active_capture_loaded: self.active_capture_loaded,
            stored_sessions_count: stored_count,
            last_opened_source: self.last_opened_source.clone(),
        })
    }

    pub fn runtime_snapshot_ios(&self) -> Result<BridgeRuntimeSnapshot, BridgeRuntimeError> {
        let stored_count = self.ios.list_stored_sessions()?.len() as u64;

        Ok(BridgeRuntimeSnapshot {
            platform: BridgePlatform::Ios,
            initialized: self.initialized,
            active_capture_loaded: self.active_capture_loaded,
            stored_sessions_count: stored_count,
            last_opened_source: self.last_opened_source.clone(),
        })
    }

    pub fn clear_runtime_state(&mut self) {
        self.active_capture_loaded = false;
        self.last_opened_source = None;
    }

    fn ensure_active(&self) -> Result<(), BridgeRuntimeError> {
        if !self.initialized {
            return Err(BridgeRuntimeError::InvalidRuntimeState(
                "runtime not initialized".into(),
            ));
        }

        if !self.active_capture_loaded {
            return Err(BridgeRuntimeError::InvalidRuntimeState(
                "no active capture loaded".into(),
            ));
        }

        Ok(())
    }

    pub fn attach_vpn_tunnel(&mut self, fd: i32) -> Result<(), BridgeRuntimeError> {
        self.android.attach_vpn_tunnel(fd)?;
        self.active_capture_loaded = true;
        Ok(())
    }

    pub fn update_threat_database(&mut self, data: Vec<u8>) -> Result<(), BridgeRuntimeError> {
        self.android.update_threat_database(data)?;
        Ok(())
    }

    pub fn update_security_settings(&self, level: u8, smart_shield: bool, kill_switch: bool) -> Result<bool, BridgeRuntimeError> {
        Ok(self.android.update_security_settings(level, smart_shield, kill_switch)?)
    }

    pub fn simulate_attack(&self, data: Vec<u8>) -> String {
        self.android.simulate_attack(data)
    }
}
