use core_types::PacketRecord;
use protocol_engine::decode_packet;

#[test]
fn decodes_ipv4_icmp_packet() {
    let bytes = vec![
        0x00,0x11,0x22,0x33,0x44,0x55,
        0x66,0x77,0x88,0x99,0xAA,0xBB,
        0x08,0x00,
        0x45,0x00,0x00,0x1E,
        0x00,0x01,0x00,0x00,
        0x40,0x01,0x00,0x00,
        0x0A,0x00,0x00,0x01,
        0x08,0x08,0x08,0x08,
        0x08,0x00,0x00,0x00,
        0x12,0x34,
    ];

    let record = PacketRecord {
        packet_number: 7,
        file_offset: 0,
        interface_id: Some(0),
        timestamp_epoch_micros: Some(7),
        captured_length: bytes.len() as u32,
        original_length: bytes.len() as u32,
        link_type: Some(1),
        raw_data: bytes,
        read_warnings: vec![],
    };

    let parsed = decode_packet(&record).unwrap();
    assert!(parsed.summary.contains("Ethernet"));
    assert!(parsed.summary.contains("IPv4"));
    assert!(parsed.summary.contains("ICMP"));
}
