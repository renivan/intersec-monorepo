use flow_engine::FlowKey;

use crate::error::UseCasesError;
use crate::models::{CaptureContext, FlowDetail};

pub fn get_flow_detail(
    ctx: &CaptureContext,
    key: &FlowKey,
) -> Result<FlowDetail, UseCasesError> {
    let flow = ctx
        .flows
        .iter()
        .find(|flow| &flow.key == key)
        .cloned()
        .ok_or_else(|| UseCasesError::FlowNotFound(format!("{key:?}")))?;

    let summary = flow.summary();

    Ok(FlowDetail { flow, summary })
}