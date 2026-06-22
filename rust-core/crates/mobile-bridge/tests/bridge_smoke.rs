use std::fs;
use std::path::PathBuf;
use std::time::{SystemTime, UNIX_EPOCH};

use mobile_bridge::{
    serialize_flow_result_text,
    serialize_packet_result_text,
    serialize_session_snapshot_text,
    serialize_stored_sessions_text,
    BridgeFlowQuery,
    BridgePacketQuery,
    MobileBridge,
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
fn opens_queries_persists_and_serializes() {
    let file_path = unique_temp_file("mobile_bridge_smoke");
    fs::write(&file_path, build_test_pcap()).unwrap();

    let mut bridge = MobileBridge::new();

    let snapshot = bridge
        .open_capture(file_path.to_str().unwrap(), 1000)
        .unwrap();

    let snapshot_text = serialize_session_snapshot_text(&snapshot);
    assert!(snapshot_text.contains("total_packets=1"));

    let packets = bridge
        .query_packets(BridgePacketQuery {
            protocol: Some("DNS".into()),
            host: Some("8.8.8.8".into()),
            text: None,
            packet_number: None,
        })
        .unwrap();

    let packet_text = serialize_packet_result_text(&packets);
    assert!(packet_text.contains("total_items=1"));

    let flows = bridge
        .query_flows(BridgeFlowQuery {
            protocol: Some("DNS".into()),
            host: Some("8.8.8.8".into()),
            port: Some(53),
            text: None,
        })
        .unwrap();

    let flow_text = serialize_flow_result_text(&flows);
    assert!(flow_text.contains("total_items=1"));

    bridge
        .persist_active(vec!["mobile".into()], Some("bridge".into()))
        .unwrap();

    let stored = bridge.list_stored_sessions().unwrap();
    let stored_text = serialize_stored_sessions_text(&stored);
    assert!(stored_text.contains("total_items=1"));

    let _ = fs::remove_file(&file_path);
}