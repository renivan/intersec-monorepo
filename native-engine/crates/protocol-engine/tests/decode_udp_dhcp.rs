use core_types::PacketRecord;
use protocol_engine::decode_packet;

#[test]
fn decodes_ipv4_udp_dhcp_packet() {
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
        0xFF,0xFF,0xFF,0xFF,
        // UDP
        0x00,0x44,0x00,0x43, // 68 -> 67
        0x00,0x0C,0x00,0x00,
        // payload
        0x01,0x01,0x06,0x00,
    ];

    let record = PacketRecord {
        packet_number: 3,
        file_offset: 0,
        interface_id: Some(0),
        timestamp_epoch_micros: Some(3),
        captured_length: bytes.len() as u32,
        original_length: bytes.len() as u32,
        link_type: Some(1),
        raw_data: bytes,
        read_warnings: vec![],
    };

    let parsed = decode_packet(&record).unwrap();
    assert!(parsed.summary.contains("DHCP"));
}