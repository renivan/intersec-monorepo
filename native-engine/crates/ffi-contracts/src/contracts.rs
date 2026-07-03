use mobile_bridge::MobileBridge;

use crate::codecs::decode_csv_tags;
use crate::dto::{
    FfiFlowQuery,
    FfiFlowSearchResult,
    FfiPacketQuery,
    FfiPacketSearchResult,
    FfiResponse,
    FfiSessionSnapshot,
    FfiStoredSession,
};
use crate::error::FfiContractsError;

pub struct FfiContracts {
    bridge: MobileBridge,
}

impl FfiContracts {
    pub fn new() -> Self {
        Self {
            bridge: MobileBridge::new(),
        }
    }

    pub fn ping(&self) -> FfiResponse<String> {
        FfiResponse::success("ok", "ffi-contracts-ready".into())
    }

    pub fn open_capture(
        &mut self,
        path: &str,
        now_epoch_micros: u64,
    ) -> Result<FfiResponse<FfiSessionSnapshot>, FfiContractsError> {
        if path.trim().is_empty() {
            return Err(FfiContractsError::InvalidInput(
                "path must not be empty".into(),
            ));
        }

        let snapshot = self.bridge.open_capture(path, now_epoch_micros)?;
        Ok(FfiResponse::success("capture_opened", snapshot.into()))
    }

    pub fn snapshot_active(
        &self,
    ) -> Result<FfiResponse<FfiSessionSnapshot>, FfiContractsError> {
        let snapshot = self.bridge.snapshot_active()?;
        Ok(FfiResponse::success("active_snapshot", snapshot.into()))
    }

    pub fn persist_active(
        &mut self,
        tags_csv: &str,
        notes: Option<String>,
    ) -> Result<FfiResponse<()>, FfiContractsError> {
        let tags = decode_csv_tags(tags_csv);
        self.bridge.persist_active(tags, notes)?;
        Ok(FfiResponse::success_without_data("active_session_persisted"))
    }

    pub fn query_packets(
        &self,
        query: FfiPacketQuery,
    ) -> Result<FfiResponse<FfiPacketSearchResult>, FfiContractsError> {
        let result = self.bridge.query_packets(query.into())?;
        Ok(FfiResponse::success("packet_search_done", result.into()))
    }

    pub fn query_flows(
        &self,
        query: FfiFlowQuery,
    ) -> Result<FfiResponse<FfiFlowSearchResult>, FfiContractsError> {
        let result = self.bridge.query_flows(query.into())?;
        Ok(FfiResponse::success("flow_search_done", result.into()))
    }

    pub fn list_stored_sessions(
        &self,
    ) -> Result<FfiResponse<Vec<FfiStoredSession>>, FfiContractsError> {
        let items = self.bridge.list_stored_sessions()?;
        Ok(FfiResponse::success(
            "stored_sessions_listed",
            items.into_iter().map(Into::into).collect(),
        ))
    }

    pub fn get_capture_overview(&self) -> Result<FfiResponse<crate::dto::FfiCaptureOverview>, FfiContractsError> {
        let ov = self.bridge.get_capture_overview()?;
        Ok(FfiResponse::success("capture_overview_done", ov.into()))
    }

    pub fn start_capture(&mut self, iface: &str, filter: &str) -> Result<FfiResponse<String>, FfiContractsError> {
        let session_id = self.bridge.start_capture(iface, filter)?;
        Ok(FfiResponse::success("capture_started", session_id))
    }

    pub fn stop_capture(&mut self, session_id: &str) -> Result<FfiResponse<crate::dto::FfiSessionSnapshot>, FfiContractsError> {
        let snapshot = self.bridge.stop_capture(session_id)?;
        Ok(FfiResponse::success("capture_stopped", snapshot.into()))
    }

    pub fn get_latest_packets(&self, limit: usize) -> Result<FfiResponse<Vec<crate::dto::FfiPacketSearchHit>>, FfiContractsError> {
        let items = self.bridge.get_latest_packets(limit)?;
        Ok(FfiResponse::success("latest_packets_fetched", items.into_iter().map(Into::into).collect()))
    }

    pub fn attach_vpn_tunnel(&mut self, fd: i32) -> Result<FfiResponse<()>, FfiContractsError> {
        self.bridge.attach_vpn_tunnel(fd)?;
        Ok(FfiResponse::success_without_data("vpn_tunnel_attached"))
    }

    pub fn update_threat_database(&mut self, data: Vec<u8>) -> Result<FfiResponse<()>, FfiContractsError> {
        self.bridge.update_threat_database(data)?;
        Ok(FfiResponse::success_without_data("threat_database_updated"))
    }

    pub fn update_security_settings(&self, level: u8, smart_shield: bool, kill_switch: bool) -> Result<FfiResponse<bool>, FfiContractsError> {
        let result = self.bridge.update_security_settings(level, smart_shield, kill_switch)?;
        Ok(FfiResponse::success("security_settings_updated", result))
    }

    pub fn simulate_attack(&self, data: Vec<u8>) -> String {
        self.bridge.simulate_attack(data)
    }

    pub fn get_neural_snapshot(&self) -> Vec<neural_intelligence::NeuralLink3D> {
        self.bridge.get_neural_snapshot()
    }

    pub fn push_neural_event(&mut self, ip: &str, proto: &str, lat: f64, lon: f64, volume: u64) {
        self.bridge.push_neural_event(ip, proto, lat, lon, volume);
    }
}
