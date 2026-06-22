use std::fs;
use std::path::PathBuf;
use std::time::{SystemTime, UNIX_EPOCH};

use session_manager::SessionManager;

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
fn opens_session_from_capture_file() {
    let file_path = unique_temp_file("session_manager_open");
    fs::write(&file_path, build_test_pcap()).unwrap();

    let mut manager = SessionManager::new();
    let session = manager
        .open_capture_session(file_path.to_str().unwrap(), 1000)
        .unwrap();

    assert_eq!(session.capture.packets.len(), 1);
    assert_eq!(session.capture.flows.len(), 1);

    let _ = fs::remove_file(&file_path);
}