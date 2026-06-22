use std::fs;
use std::path::PathBuf;
use std::time::{SystemTime, UNIX_EPOCH};

use application_services::ApplicationServices;
use storage::{InMemoryStorage, StorageRepository};

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
        0x00,0x11,0x22,0x33,0x44,0x55,
        0x66,0x77,0x88,0x99,0xAA,0xBB,
        0x08,0x00,
        0x45,0x00,0x00,0x20,
        0x00,0x01,0x00,0x00,
        0x40,0x11,0x00,0x00,
        0xC0,0xA8,0x00,0x01,
        0x08,0x08,0x08,0x08,
        0x30,0x39,0x00,0x35,
        0x00,0x0C,0x00,0x00,
        0x12,0x34,0x01,0x00,
    ];

    out.extend_from_slice(&1u32.to_le_bytes());
    out.extend_from_slice(&0u32.to_le_bytes());
    out.extend_from_slice(&(packet.len() as u32).to_le_bytes());
    out.extend_from_slice(&(packet.len() as u32).to_le_bytes());
    out.extend_from_slice(&packet);

    out
}

#[test]
fn opens_capture_and_persists_active_session() {
    let file_path = unique_temp_file("app_services_open_store");
    fs::write(&file_path, build_test_pcap()).unwrap();

    let storage = InMemoryStorage::new();
    let mut app = ApplicationServices::new(storage);

    let snapshot = app.open_capture(file_path.to_str().unwrap(), 1000).unwrap();
    assert_eq!(snapshot.total_packets, 1);
    assert_eq!(snapshot.total_flows, 1);

    app.persist_active(vec!["baseline".into()], Some("stored".into()))
        .unwrap();

    let stored = app.storage.list_sessions().unwrap();
    assert_eq!(stored.len(), 1);
    assert_eq!(stored[0].snapshot.total_packets, 1);

    let _ = fs::remove_file(&file_path);
}
