use core_types::{CaptureMetadata, ParsedPacket, ProtocolKind};
use use_cases::{list_packets, CaptureContext};

#[test]
fn lists_packets_with_pagination() {
    let ctx = CaptureContext {
        source_name: "sample".into(),
        metadata: CaptureMetadata::default(),
        packets: vec![
            ParsedPacket {
                packet_number: 1,
                timestamp_epoch_micros: Some(1),
                link_type: Some(1),
                highest_protocol: Some(ProtocolKind::Dns),
                nodes: vec![],
                summary: "packet 1".into(),
                warnings: vec![],
            },
            ParsedPacket {
                packet_number: 2,
                timestamp_epoch_micros: Some(2),
                link_type: Some(1),
                highest_protocol: Some(ProtocolKind::Http),
                nodes: vec![],
                summary: "packet 2".into(),
                warnings: vec![],
            },
        ],
        flows: vec![],
    };

    let page = list_packets(&ctx, 1, 1).unwrap();

    assert_eq!(page.total_items, 2);
    assert_eq!(page.items.len(), 1);
    assert_eq!(page.items[0].packet_number, 1);
}