use ffi_contracts::{
    FfiContracts,
    FfiResponse,
};

use crate::error::AndroidAdapterError;
use crate::mapper::{
    AndroidFlowQueryInput,
    AndroidFlowSearchResultOutput,
    AndroidPacketQueryInput,
    AndroidPacketSearchResultOutput,
    AndroidSessionSnapshotOutput,
    AndroidStoredSessionOutput,
};

pub struct AndroidAdapter {
    ffi: FfiContracts,
}

impl AndroidAdapter {
    pub fn new() -> Self {
        Self {
            ffi: FfiContracts::new(),
        }
    }

    pub fn ping(&self) -> String {
        let response = self.ffi.ping();
        if response.ok {
            response.message
        } else {
            "error".into()
        }
    }

    pub fn open_capture(
        &mut self,
        path: &str,
        now_epoch_micros: i64,
    ) -> Result<AndroidSessionSnapshotOutput, AndroidAdapterError> {
        let response = self
            .ffi
            .open_capture(path, now_epoch_micros as u64)?;

        Ok(map_required(response)?.into())
    }

    pub fn snapshot_active(&self) -> Result<AndroidSessionSnapshotOutput, AndroidAdapterError> {
        let response = self.ffi.snapshot_active()?;
        Ok(map_required(response)?.into())
    }

    pub fn persist_active(
        &mut self,
        tags_csv: &str,
        notes: Option<String>,
    ) -> Result<(), AndroidAdapterError> {
        let _ = self.ffi.persist_active(tags_csv, notes)?;
        Ok(())
    }

    pub fn query_packets(
        &self,
        query: AndroidPacketQueryInput,
    ) -> Result<AndroidPacketSearchResultOutput, AndroidAdapterError> {
        let response = self.ffi.query_packets(query.into())?;
        Ok(map_required(response)?.into())
    }

    pub fn query_flows(
        &self,
        query: AndroidFlowQueryInput,
    ) -> Result<AndroidFlowSearchResultOutput, AndroidAdapterError> {
        let response = self.ffi.query_flows(query.into())?;
        Ok(map_required(response)?.into())
    }

    pub fn list_stored_sessions(
        &self,
    ) -> Result<Vec<AndroidStoredSessionOutput>, AndroidAdapterError> {
        let response = self.ffi.list_stored_sessions()?;
        Ok(map_required(response)?
            .into_iter()
            .map(Into::into)
            .collect())
    }

    pub fn get_capture_overview(&self) -> Result<crate::mapper::AndroidCaptureOverviewOutput, AndroidAdapterError> {
        let response = self.ffi.get_capture_overview()?;
        Ok(map_required(response)?.into())
    }

    pub fn start_capture(&mut self, iface: &str, filter: &str) -> Result<String, AndroidAdapterError> {
        let response = self.ffi.start_capture(iface, filter)?;
        Ok(map_required(response)?)
    }

    pub fn stop_capture(&mut self, session_id: &str) -> Result<AndroidSessionSnapshotOutput, AndroidAdapterError> {
        let response = self.ffi.stop_capture(session_id)?;
        Ok(map_required(response)?.into())
    }

    pub fn get_latest_packets(&self, limit: usize) -> Result<Vec<crate::mapper::AndroidPacketSearchHitOutput>, AndroidAdapterError> {
        let response = self.ffi.get_latest_packets(limit)?;
        Ok(map_required(response)?.into_iter().map(Into::into).collect())
    }

    pub fn attach_vpn_tunnel(&mut self, fd: i32) -> Result<(), AndroidAdapterError> {
        let _ = self.ffi.attach_vpn_tunnel(fd)?;
        Ok(())
    }

    pub fn update_threat_database(&mut self, data: Vec<u8>) -> Result<(), AndroidAdapterError> {
        let _ = self.ffi.update_threat_database(data)?;
        Ok(())
    }

    pub fn update_security_settings(&self, level: u8, smart_shield: bool, kill_switch: bool) -> Result<bool, AndroidAdapterError> {
        let response = self.ffi.update_security_settings(level, smart_shield, kill_switch)?;
        Ok(map_required(response)?)
    }

    pub fn simulate_attack(&self, data: Vec<u8>) -> String {
        self.ffi.simulate_attack(data)
    }
}

fn map_required<T>(response: FfiResponse<T>) -> Result<T, AndroidAdapterError> {
    // A resposta FFI já indica sucesso semântico.
    // Nesta fase, se `ok=true` e não houver `data`, isso é tratado como bug de contrato.
    match (response.ok, response.data) {
        (true, Some(data)) => Ok(data),
        (true, None) => unreachable!("ffi response declared success but returned no data"),
        (false, _) => unreachable!("ffi errors are propagated as Result before this point"),
    }
}