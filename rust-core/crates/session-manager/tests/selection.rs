use core_types::{CaptureMetadata, ParsedPacket, ProtocolField, ProtocolKind, ProtocolNode};
use flow_engine::{track_flows, FlowKey};
use session_manager::SessionManager;
use use_cases::CaptureContext;

fn make_context() -> CaptureContext {
    let packets = vec![
        ParsedPacket {
            packet_number: 1,
            timestamp_epoch_micros: Some(1),
            link_type: Some(1),
            highest_protocol: Some(ProtocolKind::Http),
            nodes: vec![
                ProtocolNode {
                    kind: ProtocolKind::Ipv4,
                    label: "IPv4".into(),
                    fields: vec![
                        ProtocolField { name: "src_ip".into(), value: "10.0.0.1".into() },
                        ProtocolField { name: "dst_ip".into(), value: "93.184.216.34".into() },
                    ],
                },
                ProtocolNode {
                    kind: ProtocolKind::Tcp,
                    label: "TCP".into(),
                    fields: vec![
                        ProtocolField { name: "src_port".into(), value: "34567".into() },
                        ProtocolField { name: "dst_port".into(), value: "80".into() },
                    ],
                },
            ],
            summary: "IPv4 -> TCP -> HTTP".into(),
            warnings: vec![],
        },
        ParsedPacket {
            packet_number: 2,
            timestamp_epoch_micros: Some(2),
            link_type: Some(1),
            highest_protocol: Some(ProtocolKind::Http),
            nodes: vec![
                ProtocolNode {
                    kind: ProtocolKind::Ipv4,
                    label: "IPv4".into(),
                    fields: vec![
                        ProtocolField { name: "src_ip".into(), value: "93.184.216.34".into() },
                        ProtocolField { name: "dst_ip".into(), value: "10.0.0.1".into() },
                    ],
                },
                ProtocolNode {
                    kind: ProtocolKind::Tcp,
                    label: "TCP".into(),
                    fields: vec![
                        ProtocolField { name: "src_port".into(), value: "80".into() },
                        ProtocolField { name: "dst_port".into(), value: "34567".into() },
                    ],
                },
            ],
            summary: "IPv4 -> TCP -> HTTP".into(),
            warnings: vec![],
        },
    ];

    let flows = track_flows(&packets).unwrap();

    CaptureContext {
        source_name: "manual".into(),
        metadata: CaptureMetadata::default(),
        packets,
        flows,
    }
}

#[test]
fn selects_packet_and_flow() {
    let mut manager = SessionManager::new();
    let ctx = make_context();
    let flow_key: FlowKey = ctx.flows[0].key.clone();

    manager.load_context(ctx, 100).unwrap();
    manager.select_packet(1, 200).unwrap();
    manager.select_flow(&flow_key, 300).unwrap();

    let snapshot = manager.snapshot().unwrap();
    assert_eq!(snapshot.active_packet_number, Some(1));
    assert!(snapshot.active_flow_label.is_some());
}