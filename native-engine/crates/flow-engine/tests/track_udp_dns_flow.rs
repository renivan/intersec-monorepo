use core_types::{ParsedPacket, ProtocolField, ProtocolKind, ProtocolNode};
use flow_engine::track_flows;

#[test]
fn tracks_udp_dns_flow_and_builds_summary() {
    let packet = ParsedPacket {
        packet_number: 10,
        timestamp_epoch_micros: Some(10),
        link_type: Some(1),
        highest_protocol: Some(ProtocolKind::Dns),
        nodes: vec![
            ProtocolNode {
                kind: ProtocolKind::Ipv6,
                label: "IPv6 2001:DB8::1 -> 2001:DB8::2".into(),
                fields: vec![
                    ProtocolField {
                        name: "src_ip".into(),
                        value: "2001:DB8::1".into(),
                    },
                    ProtocolField {
                        name: "dst_ip".into(),
                        value: "2001:DB8::2".into(),
                    },
                ],
            },
            ProtocolNode {
                kind: ProtocolKind::Udp,
                label: "UDP 53000 -> 53".into(),
                fields: vec![
                    ProtocolField {
                        name: "src_port".into(),
                        value: "53000".into(),
                    },
                    ProtocolField {
                        name: "dst_port".into(),
                        value: "53".into(),
                    },
                ],
            },
            ProtocolNode {
                kind: ProtocolKind::Dns,
                label: "DNS".into(),
                fields: vec![],
            },
        ],
        summary: "IPv6 -> UDP -> DNS".into(),
        warnings: vec![],
    };

    let flows = track_flows(&[packet]).unwrap();
    assert_eq!(flows.len(), 1);

    let summary = flows[0].summary();
    assert!(summary.label.contains("UDP"));
    assert!(summary.label.contains("53"));
}