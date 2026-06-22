use std::fs;
use std::path::PathBuf;
use std::time::{SystemTime, UNIX_EPOCH};

use kotlin_bridge_contracts::{
    encode_flow_result_text,
    encode_packet_result_text,
    encode_runtime_snapshot_text,
    encode_session_list_text,
    encode_session_snapshot_text,
    KotlinBridgeContracts,
    KotlinFlowQuery,
    KotlinPacketQuery,
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
fn opens_queries_persists_lists_and_serializes_with_kotlin_contracts() {
    let file_path = unique_temp_file("kotlin_contracts_smoke");
    fs::write(&file_path, build_test_pcap()).unwrap();

    let mut contracts = KotlinBridgeContracts::new();

    let ping = contracts.ping().unwrap();
    assert_eq!(ping, "ok");

    let opened = contracts
        .open_capture(file_path.to_str().unwrap(), 1000)
        .unwrap();
    assert_eq!(opened.total_packets, 1);
    assert_eq!(opened.total_flows, 1);

    let snapshot = contracts.snapshot_active().unwrap();
    let snapshot_text = encode_session_snapshot_text(&snapshot);
    assert!(snapshot_text.contains("total_packets=1"));

    let packets = contracts
        .query_packets(KotlinPacketQuery {
            protocol: Some("DNS".into()),
            host: Some("8.8.8.8".into()),
            text: None,
            packet_number: None,
        })
        .unwrap();
    let packet_text = encode_packet_result_text(&packets);
    assert!(packet_text.contains("total_items=1"));

    let flows = contracts
        .query_flows(KotlinFlowQuery {
            protocol: Some("DNS".into()),
            host: Some("8.8.8.8".into()),
            port: Some(53),
            text: None,
        })
        .unwrap();
    let flow_text = encode_flow_result_text(&flows);
    assert!(flow_text.contains("total_items=1"));

    contracts
        .persist_active("kotlin,android,bridge", Some("stored".into()))
        .unwrap();

    let stored = contracts.list_stored_sessions().unwrap();
    let stored_text = encode_session_list_text(&stored);
    assert!(stored_text.contains("total_items=1"));

    let runtime = contracts.runtime_snapshot().unwrap();
    let runtime_text = encode_runtime_snapshot_text(&runtime);
    assert!(runtime_text.contains("initialized=true"));

    let _ = fs::remove_file(&file_path);
}