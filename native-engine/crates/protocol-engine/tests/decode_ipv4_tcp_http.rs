use core_types::PacketRecord;
use protocol_engine::decode_packet;

#[test]
fn decodes_ipv4_tcp_http_packet() {
    // Ethernet + IPv4 + TCP(80) + payload HTTP.
    let mut bytes = vec![
        // Ethernet
        0x00,0x11,0x22,0x33,0x44,0x55,
        0x66,0x77,0x88,0x99,0xAA,0xBB,
        0x08,0x00,
        // IPv4
        0x45,0x00,0x00,0x3B,
        0x00,0x01,0x00,0x00,
        0x40,0x06,0x00,0x00,
        0xC0,0xA8,0x00,0x01,
        0x5D,0xB8,0xD8,0x22,
        // TCP
        0x30,0x39,0x00,0x50,
        0x00,0x00,0x00,0x01,
        0x00,0x00,0x00,0x00,
        0x50,0x18,0x20,0x00,
        0x00,0x00,0x00,0x00,
    ];
    bytes.extend_from_slice(b"GET / HTTP/1.1
");

    let record = PacketRecord {
        packet_number: 1,
        file_offset: 0,
        interface_id: Some(0),
        timestamp_epoch_micros: Some(1),
        captured_length: bytes.len() as u32,
        original_length: bytes.len() as u32,
        link_type: Some(1),
        raw_data: bytes,
        read_warnings: vec![],
    };

    let parsed = decode_packet(&record).unwrap();
    assert!(parsed.summary.contains("Ethernet"));
    assert!(parsed.summary.contains("IPv4"));
    assert!(parsed.summary.contains("TCP"));
    assert!(parsed.summary.contains("HTTP"));
}
