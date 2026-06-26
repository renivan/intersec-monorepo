use core_types::{CaptureMetadata, ParsedPacket, ProtocolField, ProtocolKind, ProtocolNode};
use use_cases::{get_capture_overview, CaptureContext};

#[test]
fn builds_capture_overview() {
    let ctx = CaptureContext {
        source_name: "sample".into(),
        metadata: CaptureMetadata::default(),
        packets: vec![
            ParsedPacket {
                packet_number: 1,
                timestamp_epoch_micros: Some(100),
                link_type: Some(1),
                highest_protocol: Some(ProtocolKind::Dns),
                nodes: vec![ProtocolNode {
                    kind: ProtocolKind::Ipv4,
                    label: "IPv4".into(),
                    fields: vec![
                        ProtocolField {
                            name: "src_ip".into(),
                            value: "10.0.0.1".into(),
                        },
                        ProtocolField {
                            name: "dst_ip".into(),
                            value: "8.8.8.8".into(),
                        },
                    ],
                }],
                summary: "IPv4 -> UDP -> DNS".into(),
                warnings: vec![],
            },
            ParsedPacket {
                packet_number: 2,
                timestamp_epoch_micros: Some(200),
                link_type: Some(1),
                highest_protocol: Some(ProtocolKind::Http),
                nodes: vec![ProtocolNode {
                    kind: ProtocolKind::Ipv4,
                    label: "IPv4".into(),
                    fields: vec![
                        ProtocolField {
                            name: "src_ip".into(),
                            value: "10.0.0.1".into(),
                        },
                        ProtocolField {
                            name: "dst_ip".into(),
                            value: "93.184.216.34".into(),
                        },
                    ],
                }],
                summary: "IPv4 -> TCP -> HTTP".into(),
                warnings: vec![],
            },
        ],
        flows: vec![],
    };

    let overview = get_capture_overview(&ctx).unwrap();

    assert_eq!(overview.total_packets, 2);
    assert_eq!(overview.first_timestamp_epoch_micros, Some(100));
    assert_eq!(overview.last_timestamp_epoch_micros, Some(200));
    assert!(!overview.top_protocols.is_empty());
}