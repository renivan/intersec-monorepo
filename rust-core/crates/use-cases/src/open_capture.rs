use capture_reader::open_file_capture;
use flow_engine::track_flows;
use protocol_engine::decode_packet;

use crate::error::UseCasesError;
use crate::models::CaptureContext;

pub fn open_capture(path: &str) -> Result<CaptureContext, UseCasesError> {
    let (mut reader, read_result) = open_file_capture(path)?;

    let mut packets = Vec::new();

    // Materializa os pacotes já decodificados para os demais casos de uso.
    while let Some(packet_record) = reader.next_packet()? {
        let parsed = decode_packet(&packet_record)?;
        packets.push(parsed);
    }

    let flows = track_flows(&packets)?;

    Ok(CaptureContext {
        source_name: read_result.metadata.capture_id.clone(),
        metadata: read_result.metadata,
        packets,
        flows,
    })
}