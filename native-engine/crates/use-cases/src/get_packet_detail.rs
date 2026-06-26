use crate::error::UseCasesError;
use crate::models::{CaptureContext, PacketDetail};

pub fn get_packet_detail(
    ctx: &CaptureContext,
    packet_number: u64,
) -> Result<PacketDetail, UseCasesError> {
    let packet = ctx
        .packets
        .iter()
        .find(|packet| packet.packet_number == packet_number)
        .cloned()
        .ok_or(UseCasesError::PacketNotFound(packet_number))?;

    Ok(PacketDetail { packet })
}