use core_types::PacketRecord;
use protocol_engine::decode_packet;

#[test]
fn decodes_ipv4_udp_ntp_packet() {
    let bytes = vec![
        // Ethernet
        0x00,0x11,0x22,0x33,0x44,0x55,
        0x66,0x77,0x88,0x99,0xAA,0xBB,
        0x08,0x00,
        // IPv4
        0x45,0x00,0x00,0x20,
        0x00,0x01,0x00,0x00,
        0x40,0x11,0x00,0x00,
        0xC0,0xA8,0x00,0x01,
        0x81,0x6F,0x00,0x01,
        // UDP
        0x30,0x39,0x00,0x7B, // 12345 -> 123
        0x00,0x0C,0x00,0x00,
        // payload
        0x23,0x00,0x00,0x00,
    ];

    let record = PacketRecord {
        packet_number: 4,
        file_offset: 0,
        interface_id: Some(0),
        timestamp_epoch_micros: Some(4),
        captured_length: bytes.len() as u32,
        original_length: bytes.len() as u32,
        link_type: Some(1),
        raw_data: bytes,
        read_warnings: vec![],
    };

    let parsed = decode_packet(&record).unwrap();
    assert!(parsed.summary.contains("NTP"));
}