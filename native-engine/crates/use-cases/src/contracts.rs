use crate::error::UseCasesError;
use crate::models::{
    CaptureContext, CaptureOverview, FlowDetail, FlowListResult, PacketDetail, PacketListResult,
};
use flow_engine::FlowKey;

pub trait CaptureInputPort {
    fn open_capture(&self, path: &str) -> Result<CaptureContext, UseCasesError>;
}

pub trait CaptureOverviewPort {
    fn get_capture_overview(&self, ctx: &CaptureContext) -> Result<CaptureOverview, UseCasesError>;
}

pub trait PacketQueryPort {
    fn list_packets(
        &self,
        ctx: &CaptureContext,
        page: usize,
        page_size: usize,
    ) -> Result<PacketListResult, UseCasesError>;

    fn get_packet_detail(
        &self,
        ctx: &CaptureContext,
        packet_number: u64,
    ) -> Result<PacketDetail, UseCasesError>;
}

pub trait FlowQueryPort {
    fn list_flows(&self, ctx: &CaptureContext) -> Result<FlowListResult, UseCasesError>;

    fn get_flow_detail(
        &self,
        ctx: &CaptureContext,
        key: &FlowKey,
    ) -> Result<FlowDetail, UseCasesError>;
}