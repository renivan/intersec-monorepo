use core_types::{CaptureMetadata, ParsedPacket, ProtocolField, ProtocolKind, ProtocolNode};
use query_engine::{query_packets, PacketQuery};
use use_cases::CaptureContext;

#[test]
fn filters_packets_by_protocol_and_host() {
    let ctx = CaptureContext {
        source_name: "sample".into(),
        metadata: CaptureMetadata::default(),
        packets: vec![
            ParsedPacket {
                packet_number: 1,
                timestamp_epoch_micros: Some(1),
                link_type: Some(1),
                highest_protocol: Some(ProtocolKind::Dns),
                nodes: vec![ProtocolNode {
                    kind: ProtocolKind::Ipv4,
                    label: "IPv4".into(),
                    fields: vec![
                        ProtocolField { name: "src_ip".into(), value: "10.0.0.1".into() },
                        ProtocolField { name: "dst_ip".into(), value: "8.8.8.8".into() },
                    ],
                }],
                summary: "IPv4 -> UDP -> DNS".into(),
                warnings: vec![],
            },
            ParsedPacket {
                packet_number: 2,
                timestamp_epoch_micros: Some(2),
                link_type: Some(1),
                highest_protocol: Some(ProtocolKind::Http),
                nodes: vec![ProtocolNode {
                    kind: ProtocolKind::Ipv4,
                    label: "IPv4".into(),
                    fields: vec![
                        ProtocolField { name: "src_ip".into(), value: "10.0.0.1".into() },
                        ProtocolField { name: "dst_ip".into(), value: "93.184.216.34".into() },
                    ],
                }],
                summary: "IPv4 -> TCP -> HTTP".into(),
                warnings: vec![],
            },
        ],
        flows: vec![],
    };

    let result = query_packets(
        &ctx,
        &PacketQuery {
            protocol: Some("DNS".into()),
            host: Some("8.8.8.8".into()),
            text: None,
            packet_number: None,
        },
    )
        .unwrap();

    assert_eq!(result.total_items, 1);
    assert_eq!(result.items[0].packet_number, 1);
}