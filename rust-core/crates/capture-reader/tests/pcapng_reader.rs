use std::fs;
use std::path::PathBuf;
use std::time::{SystemTime, UNIX_EPOCH};

use capture_reader::{open_file_capture, CaptureFormat, CaptureReader};

fn unique_temp_file(name: &str) -> PathBuf {
    let mut path = std::env::temp_dir();
    let ts = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_nanos();
    path.push(format!("{}_{}.pcapng", name, ts));
    path
}

fn build_test_pcapng() -> Vec<u8> {
    let mut out = Vec::<u8>::new();

    out.extend_from_slice(&0x0A0D0D0Au32.to_le_bytes());
    out.extend_from_slice(&28u32.to_le_bytes());
    out.extend_from_slice(&[0x4D, 0x3C, 0x2B, 0x1A]);
    out.extend_from_slice(&1u16.to_le_bytes());
    out.extend_from_slice(&0u16.to_le_bytes());
    out.extend_from_slice(&(-1i64).to_le_bytes());
    out.extend_from_slice(&28u32.to_le_bytes());

    let idb_total_length = 32u32;
    out.extend_from_slice(&0x00000001u32.to_le_bytes());
    out.extend_from_slice(&idb_total_length.to_le_bytes());
    out.extend_from_slice(&1u16.to_le_bytes());
    out.extend_from_slice(&0u16.to_le_bytes());
    out.extend_from_slice(&65535u32.to_le_bytes());
    out.extend_from_slice(&9u16.to_le_bytes());
    out.extend_from_slice(&1u16.to_le_bytes());
    out.push(6u8);
    out.extend_from_slice(&[0, 0, 0]);
    out.extend_from_slice(&0u16.to_le_bytes());
    out.extend_from_slice(&0u16.to_le_bytes());
    out.extend_from_slice(&idb_total_length.to_le_bytes());

    let payload = [0xDE, 0xAD, 0xBE, 0xEF];
    let epb_total_length = 36u32;
    out.extend_from_slice(&0x00000006u32.to_le_bytes());
    out.extend_from_slice(&epb_total_length.to_le_bytes());
    out.extend_from_slice(&0u32.to_le_bytes());
    out.extend_from_slice(&0u32.to_le_bytes());
    out.extend_from_slice(&42u32.to_le_bytes());
    out.extend_from_slice(&(payload.len() as u32).to_le_bytes());
    out.extend_from_slice(&(payload.len() as u32).to_le_bytes());
    out.extend_from_slice(&payload);
    out.extend_from_slice(&epb_total_length.to_le_bytes());

    out
}

#[test]
fn opens_and_reads_basic_pcapng_file() {
    let file_path = unique_temp_file("capture_reader_basic_pcapng");
    let bytes = build_test_pcapng();
    fs::write(&file_path, bytes).unwrap();

    let (mut reader, result) = open_file_capture(&file_path).unwrap();
    assert_eq!(result.metadata.format, CaptureFormat::PcapNg);
    assert_eq!(result.metadata.interface_count, 1);
    assert_eq!(result.metadata.default_link_type, Some(1));

    let packet = match &mut reader {
        CaptureReader::PcapNg(_, _, _) => reader.next_packet().unwrap().unwrap(),
        _ => panic!("expected pcapng reader"),
    };

    assert_eq!(packet.packet_number, 1);
    assert_eq!(packet.interface_id, Some(0));
    assert_eq!(packet.captured_length, 4);
    assert_eq!(packet.original_length, 4);
    assert_eq!(packet.link_type, Some(1));
    assert_eq!(packet.raw_data, vec![0xDE, 0xAD, 0xBE, 0xEF]);
    assert_eq!(packet.timestamp_epoch_micros, Some(42));

    let _ = fs::remove_file(&file_path);
}
