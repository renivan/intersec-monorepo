use crate::error::UseCasesError;
use crate::models::{CaptureContext, FlowListItem, FlowListResult};

pub fn list_flows(ctx: &CaptureContext) -> Result<FlowListResult, UseCasesError> {
    let items = ctx
        .flows
        .iter()
        .cloned()
        .map(|flow| {
            let summary = flow.summary();
            FlowListItem {
                key: flow.key.clone(),
                label: summary.label,
                total_packets: flow.packet_numbers.len(),
                total_payload_bytes: flow.total_payload_bytes,
            }
        })
        .collect::<Vec<_>>();

    Ok(FlowListResult {
        total_items: items.len(),
        items,
    })
}