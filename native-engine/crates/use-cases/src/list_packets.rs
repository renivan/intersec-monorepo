use crate::error::UseCasesError;
use crate::models::{CaptureContext, PacketListItem, PacketListResult};

pub fn list_packets(
    ctx: &CaptureContext,
    page: usize,
    page_size: usize,
) -> Result<PacketListResult, UseCasesError> {
    if page == 0 {
        return Err(UseCasesError::InvalidPagination(
            "page must start at 1".into(),
        ));
    }

    if page_size == 0 {
        return Err(UseCasesError::InvalidPagination(
            "page_size must be greater than 0".into(),
        ));
    }

    let total_items = ctx.packets.len();
    let start = (page - 1) * page_size;
    let end = std::cmp::min(start + page_size, total_items);

    let items = if start >= total_items {
        Vec::new()
    } else {
        ctx.packets[start..end]
            .iter()
            .map(|packet| PacketListItem {
                packet_number: packet.packet_number,
                timestamp_epoch_micros: packet.timestamp_epoch_micros,
                highest_protocol: packet.highest_protocol.as_ref().map(|p| p.label()),
                summary: packet.summary.clone(),
            })
            .collect()
    };

    Ok(PacketListResult {
        items,
        total_items,
        current_page: page,
        page_size,
    })
}