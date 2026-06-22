use ffi_contracts::{FfiContracts, FfiResponse};

use crate::error::IosAdapterError;
use crate::mapper::{
    IosFlowQueryInput,
    IosFlowSearchResultOutput,
    IosPacketQueryInput,
    IosPacketSearchResultOutput,
    IosSessionSnapshotOutput,
    IosStoredSessionOutput,
};

pub struct IosAdapter {
    ffi: FfiContracts,
}

impl IosAdapter {
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
        now_epoch_micros: u64,
    ) -> Result<IosSessionSnapshotOutput, IosAdapterError> {
        let response = self.ffi.open_capture(path, now_epoch_micros)?;
        Ok(map_required(response)?.into())
    }

    pub fn snapshot_active(&self) -> Result<IosSessionSnapshotOutput, IosAdapterError> {
        let response = self.ffi.snapshot_active()?;
        Ok(map_required(response)?.into())
    }

    pub fn persist_active(
        &mut self,
        tags_csv: &str,
        notes: Option<String>,
    ) -> Result<(), IosAdapterError> {
        let _ = self.ffi.persist_active(tags_csv, notes)?;
        Ok(())
    }

    pub fn query_packets(
        &self,
        query: IosPacketQueryInput,
    ) -> Result<IosPacketSearchResultOutput, IosAdapterError> {
        let response = self.ffi.query_packets(query.into())?;
        Ok(map_required(response)?.into())
    }

    pub fn query_flows(
        &self,
        query: IosFlowQueryInput,
    ) -> Result<IosFlowSearchResultOutput, IosAdapterError> {
        let response = self.ffi.query_flows(query.into())?;
        Ok(map_required(response)?.into())
    }

    pub fn list_stored_sessions(
        &self,
    ) -> Result<Vec<IosStoredSessionOutput>, IosAdapterError> {
        let response = self.ffi.list_stored_sessions()?;
        Ok(map_required(response)?
            .into_iter()
            .map(Into::into)
            .collect())
    }
}

fn map_required<T>(response: FfiResponse<T>) -> Result<T, IosAdapterError> {
    match (response.ok, response.data) {
        (true, Some(data)) => Ok(data),
        (true, None) => unreachable!("ffi response declared success but returned no data"),
        (false, _) => unreachable!("ffi errors are propagated as Result before this point"),
    }
}