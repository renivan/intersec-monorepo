use core_types::PacketRecord;
use protocol_engine::decode_packet;

#[test]
fn rejects_unsupported_link_type() {
    let record = PacketRecord {
        packet_number: 1,
        file_offset: 0,
        interface_id: Some(0),
        timestamp_epoch_micros: Some(1),
        captured_length: 0,
        original_length: 0,
        link_type: Some(999),
        raw_data: vec![],
        read_warnings: vec![],
    };

    let err = decode_packet(&record).unwrap_err();
    assert!(err.to_string().contains("unsupported link type"));
}
