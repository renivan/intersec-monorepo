use facade_api::FacadeApi;

use crate::dto::{
    BridgeFlowQuery,
    BridgeFlowSearchResult,
    BridgePacketQuery,
    BridgePacketSearchResult,
    BridgeSessionSnapshot,
    BridgeStoredSession,
};
use crate::error::MobileBridgeError;

pub struct MobileBridge {
    facade: FacadeApi,
}

impl MobileBridge {
    pub fn new() -> Self {
        Self {
            facade: FacadeApi::new(),
        }
    }

    pub fn open_capture(
        &mut self,
        path: &str,
        now_epoch_micros: u64,
    ) -> Result<BridgeSessionSnapshot, MobileBridgeError> {
        let snapshot = self.facade.open_capture(path, now_epoch_micros)?;
        Ok(snapshot.into())
    }

    pub fn snapshot_active(&self) -> Result<BridgeSessionSnapshot, MobileBridgeError> {
        let snapshot = self.facade.snapshot_active()?;
        Ok(snapshot.into())
    }

    pub fn persist_active(
        &mut self,
        tags: Vec<String>,
        notes: Option<String>,
    ) -> Result<(), MobileBridgeError> {
        self.facade.persist_active(tags, notes)?;
        Ok(())
    }

    pub fn query_packets(
        &self,
        query: BridgePacketQuery,
    ) -> Result<BridgePacketSearchResult, MobileBridgeError> {
        let result = self.facade.query_packets(&query.into())?;
        Ok(result.into())
    }

    pub fn query_flows(
        &self,
        query: BridgeFlowQuery,
    ) -> Result<BridgeFlowSearchResult, MobileBridgeError> {
        let result = self.facade.query_flows(&query.into())?;
        Ok(result.into())
    }

    pub fn list_stored_sessions(&self) -> Result<Vec<BridgeStoredSession>, MobileBridgeError> {
        let items = self.facade.list_stored_sessions()?;
        Ok(items.into_iter().map(Into::into).collect())
    }

    pub fn get_capture_overview(&self) -> Result<crate::dto::BridgeCaptureOverview, MobileBridgeError> {
        let ov = self.facade.get_capture_overview()?;
        Ok(ov.into())
    }

    pub fn start_capture(&mut self, iface: &str, filter: &str) -> Result<String, MobileBridgeError> {
        Ok(self.facade.start_capture(iface, filter)?)
    }

    pub fn stop_capture(&mut self, session_id: &str) -> Result<BridgeSessionSnapshot, MobileBridgeError> {
        let snapshot = self.facade.stop_capture(session_id)?;
        Ok(snapshot.into())
    }

    pub fn get_latest_packets(&self, limit: usize) -> Result<Vec<crate::dto::BridgePacketSearchHit>, MobileBridgeError> {
        let res = self.facade.get_latest_packets(limit)?;
        Ok(res.into_iter().map(|item| crate::dto::BridgePacketSearchHit {
            packet_number: item.packet_number,
            timestamp_epoch_micros: item.timestamp_epoch_micros,
            highest_protocol: item.highest_protocol,
            summary: item.summary,
        }).collect())
    }

    pub fn push_live_packet(&mut self, packet: core_types::ParsedPacket) -> Result<(), MobileBridgeError> {
        Ok(self.facade.push_live_packet(packet)?)
    }

    pub fn attach_vpn_tunnel(&mut self, fd: i32) -> Result<(), MobileBridgeError> {
        Ok(self.facade.attach_vpn_tunnel(fd)?)
    }

    pub fn update_threat_database(&mut self, data: Vec<u8>) -> Result<(), MobileBridgeError> {
        Ok(self.facade.update_threat_database(data)?)
    }

    pub fn update_security_settings(&self, level: u8, smart_shield: bool, kill_switch: bool) -> Result<bool, MobileBridgeError> {
        Ok(self.facade.update_security_settings(level, smart_shield, kill_switch)?)
    }

    pub fn simulate_attack(&self, data: Vec<u8>) -> String {
        self.facade.simulate_attack(data)
    }

    pub fn get_neural_snapshot(&self) -> Vec<neural_intelligence::NeuralLink3D> {
        self.facade.get_neural_snapshot()
    }

    pub fn push_neural_event(&mut self, ip: &str, proto: &str, lat: f64, lon: f64, volume: u64) {
        self.facade.push_neural_event(ip, proto, lat, lon, volume);
    }
}
