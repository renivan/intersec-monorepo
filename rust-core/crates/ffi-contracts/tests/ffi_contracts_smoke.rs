use std::fs;
use std::path::PathBuf;
use std::time::{SystemTime, UNIX_EPOCH};

use ffi_contracts::{
    encode_flow_search_result,
    encode_packet_search_result,
    encode_session_list,
    encode_session_snapshot,
    FfiContracts,
    FfiFlowQuery,
    FfiPacketQuery,
};

fn unique_temp_file(name: &str) -> PathBuf {
    let mut path = std::env::temp_dir();
    let ts = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_nanos();
    path.push(format!("{}_{}.pcap", name, ts));
    path
}

fn build_test_pcap() -> Vec<u8> {
    let mut out = Vec::<u8>::new();

    out.extend_from_slice(&[0xD4, 0xC3, 0xB2, 0xA1]);
    out.extend_from_slice(&2u16.to_le_bytes());
    out.extend_from_slice(&4u16.to_le_bytes());
    out.extend_from_slice(&0i32.to_le_bytes());
    out.extend_from_slice(&0u32.to_le_bytes());
    out.extend_from_slice(&65535u32.to_le_bytes());
    out.extend_from_slice(&1u32.to_le_bytes());

    let packet = vec![
        0x00, 0x11, 0x22, 0x33, 0x44, 0x55,
        0x66, 0x77, 0x88, 0x99, 0xAA, 0xBB,
        0x08, 0x00,
        0x45, 0x00, 0x00, 0x20,
        0x00, 0x01, 0x00, 0x00,
        0x40, 0x11, 0x00, 0x00,
        0xC0, 0xA8, 0x00, 0x01,
        0x08, 0x08, 0x08, 0x08,
        0x30, 0x39, 0x00, 0x35,
        0x00, 0x0C, 0x00, 0x00,
        0x12, 0x34, 0x01, 0x00,
    ];

    out.extend_from_slice(&1u32.to_le_bytes());
    out.extend_from_slice(&0u32.to_le_bytes());
    out.extend_from_slice(&(packet.len() as u32).to_le_bytes());
    out.extend_from_slice(&(packet.len() as u32).to_le_bytes());
    out.extend_from_slice(&packet);

    out
}

#[test]
fn opens_queries_persists_lists_and_serializes() {
    let file_path = unique_temp_file("ffi_contracts_smoke");
    fs::write(&file_path, build_test_pcap()).unwrap();

    let mut ffi = FfiContracts::new();

    let ping = ffi.ping();
    assert!(ping.ok);

    let opened = ffi
        .open_capture(file_path.to_str().unwrap(), 1000)
        .unwrap();
    assert!(opened.ok);

    let snapshot = ffi.snapshot_active().unwrap();
    assert!(snapshot.ok);
    let snapshot_text = encode_session_snapshot(snapshot.data.as_ref().unwrap());
    assert!(snapshot_text.contains("total_packets=1"));

    let packets = ffi
        .query_packets(FfiPacketQuery {
            protocol: Some("DNS".into()),
            host: Some("8.8.8.8".into()),
            text: None,
            packet_number: None,
        })
        .unwrap();
    assert!(packets.ok);
    let packet_text = encode_packet_search_result(packets.data.as_ref().unwrap());
    assert!(packet_text.contains("total_items=1"));

    let flows = ffi
        .query_flows(FfiFlowQuery {
            protocol: Some("DNS".into()),
            host: Some("8.8.8.8".into()),
            port: Some(53),
            text: None,
        })
        .unwrap();
    assert!(flows.ok);
    let flow_text = encode_flow_search_result(flows.data.as_ref().unwrap());
    assert!(flow_text.contains("total_items=1"));

    let persisted = ffi
        .persist_active("ffi,mobile,android", Some("bridge-ready".into()))
        .unwrap();
    assert!(persisted.ok);

    let listed = ffi.list_stored_sessions().unwrap();
    assert!(listed.ok);
    let list_text = encode_session_list(listed.data.as_ref().unwrap());
    assert!(list_text.contains("total_items=1"));

    let _ = fs::remove_file(&file_path);
}
