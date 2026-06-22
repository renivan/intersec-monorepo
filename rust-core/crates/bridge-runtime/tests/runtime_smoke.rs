use std::fs;
use std::path::PathBuf;
use std::time::{SystemTime, UNIX_EPOCH};

use android_adapter::{AndroidFlowQueryInput, AndroidPacketQueryInput};
use bridge_runtime::{BridgePlatform, BridgeRuntime};
use ios_adapter::{IosFlowQueryInput, IosPacketQueryInput};

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
fn opens_queries_persists_and_reports_runtime_state() {
    let file_path = unique_temp_file("bridge_runtime_smoke");
    fs::write(&file_path, build_test_pcap()).unwrap();

    let mut runtime = BridgeRuntime::new();

    let ping_android = runtime.ping_android().unwrap();
    let ping_ios = runtime.ping_ios().unwrap();

    assert_eq!(ping_android, "ok");
    assert_eq!(ping_ios, "ok");

    let android_snapshot = runtime
        .open_capture_android(file_path.to_str().unwrap(), 1000)
        .unwrap();
    assert_eq!(android_snapshot.total_packets, 1);
    assert_eq!(android_snapshot.total_flows, 1);

    let android_packets = runtime
        .query_packets_android(AndroidPacketQueryInput {
            protocol: Some("DNS".into()),
            host: Some("8.8.8.8".into()),
            text: None,
            packet_number: None,
        })
        .unwrap();
    assert_eq!(android_packets.total_items, 1);

    let android_flows = runtime
        .query_flows_android(AndroidFlowQueryInput {
            protocol: Some("DNS".into()),
            host: Some("8.8.8.8".into()),
            port: Some(53),
            text: None,
        })
        .unwrap();
    assert_eq!(android_flows.total_items, 1);

    runtime
        .persist_active_android("runtime,android", Some("stored".into()))
        .unwrap();

    let android_stored = runtime.list_stored_sessions_android().unwrap();
    assert_eq!(android_stored.len(), 1);

    let android_runtime_snapshot = runtime.runtime_snapshot_android().unwrap();
    assert_eq!(android_runtime_snapshot.platform, BridgePlatform::Android);
    assert!(android_runtime_snapshot.active_capture_loaded);
    assert_eq!(android_runtime_snapshot.stored_sessions_count, 1);

    runtime.clear_runtime_state();
    let ios_snapshot = runtime
        .open_capture_ios(file_path.to_str().unwrap(), 2000)
        .unwrap();
    assert_eq!(ios_snapshot.total_packets, 1);
    assert_eq!(ios_snapshot.total_flows, 1);

    let ios_packets = runtime
        .query_packets_ios(IosPacketQueryInput {
            protocol: Some("DNS".into()),
            host: Some("8.8.8.8".into()),
            text: None,
            packet_number: None,
        })
        .unwrap();
    assert_eq!(ios_packets.total_items, 1);

    let ios_flows = runtime
        .query_flows_ios(IosFlowQueryInput {
            protocol: Some("DNS".into()),
            host: Some("8.8.8.8".into()),
            port: Some(53),
            text: None,
        })
        .unwrap();
    assert_eq!(ios_flows.total_items, 1);

    runtime
        .persist_active_ios("runtime,ios", Some("stored".into()))
        .unwrap();

    let ios_stored = runtime.list_stored_sessions_ios().unwrap();
    assert_eq!(ios_stored.len(), 1);

    let ios_runtime_snapshot = runtime.runtime_snapshot_ios().unwrap();
    assert_eq!(ios_runtime_snapshot.platform, BridgePlatform::Ios);
    assert!(ios_runtime_snapshot.active_capture_loaded);
    assert_eq!(ios_runtime_snapshot.stored_sessions_count, 1);

    let _ = fs::remove_file(&file_path);
}