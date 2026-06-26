use application_services::ApplicationServices;
use query_engine::{FlowQuery, PacketQuery};
use storage::{InMemoryStorage, StorageRepository};

use crate::dto::{
    FlowSearchResultDto, PacketSearchResultDto, SessionSnapshotDto, StoredSessionDto,
};
use crate::error::FacadeApiError;

pub struct FacadeApi {
    app: ApplicationServices<InMemoryStorage>,
}

impl FacadeApi {
    pub fn new() -> Self {
        Self {
            app: ApplicationServices::new(InMemoryStorage::new()),
        }
    }

    pub fn open_capture(
        &mut self,
        path: &str,
        now_epoch_micros: u64,
    ) -> Result<SessionSnapshotDto, FacadeApiError> {
        let snapshot = self.app.open_capture(path, now_epoch_micros)?;
        Ok(snapshot.into())
    }

    pub fn snapshot_active(&self) -> Result<SessionSnapshotDto, FacadeApiError> {
        let snapshot = self.app.snapshot_active()?;
        Ok(snapshot.into())
    }

    pub fn persist_active(
        &mut self,
        tags: Vec<String>,
        notes: Option<String>,
    ) -> Result<(), FacadeApiError> {
        self.app.persist_active(tags, notes)?;
        Ok(())
    }

    pub fn query_packets(
        &self,
        query: &PacketQuery,
    ) -> Result<PacketSearchResultDto, FacadeApiError> {
        let result = self.app.query_active_packets(query)?;
        Ok(result.into())
    }

    pub fn query_flows(
        &self,
        query: &FlowQuery,
    ) -> Result<FlowSearchResultDto, FacadeApiError> {
        let result = self.app.query_active_flows(query)?;
        Ok(result.into())
    }

    pub fn list_stored_sessions(&self) -> Result<Vec<StoredSessionDto>, FacadeApiError> {
        let records = self.app.storage.list_sessions()?;
        Ok(records.into_iter().map(Into::into).collect())
    }

    pub fn get_capture_overview(&self) -> Result<use_cases::models::CaptureOverview, FacadeApiError> {
        let overview = self.app.get_capture_overview()?;
        Ok(overview)
    }

    pub fn start_capture(&mut self, iface: &str, filter: &str) -> Result<String, FacadeApiError> {
        Ok(self.app.start_capture(iface, filter)?)
    }

    pub fn stop_capture(&mut self, session_id: &str) -> Result<SessionSnapshotDto, FacadeApiError> {
        let snapshot = self.app.stop_capture(session_id)?;
        Ok(snapshot.into())
    }

    pub fn get_latest_packets(&self, limit: usize) -> Result<Vec<use_cases::models::PacketListItem>, FacadeApiError> {
        Ok(self.app.get_latest_packets(limit)?)
    }

    pub fn push_live_packet(&mut self, packet: core_types::ParsedPacket) -> Result<(), FacadeApiError> {
        Ok(self.app.push_live_packet(packet)?)
    }

    pub fn attach_vpn_tunnel(&mut self, fd: i32) -> Result<(), FacadeApiError> {
        Ok(self.app.attach_vpn_tunnel(fd)?)
    }

    pub fn update_threat_database(&mut self, data: Vec<u8>) -> Result<(), FacadeApiError> {
        // Encaminha a base de dados para o módulo de serviços
        Ok(self.app.update_threat_database(data)?)
    }

    pub fn update_security_settings(&self, level: u8, smart_shield: bool, kill_switch: bool) -> Result<bool, FacadeApiError> {
        Ok(self.app.update_security_settings(level, smart_shield, kill_switch)?)
    }

    pub fn simulate_attack(&self, data: Vec<u8>) -> String {
        self.app.simulate_attack_verdict(data)
    }
}
