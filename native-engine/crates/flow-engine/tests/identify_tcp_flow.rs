use core_types::{ParsedPacket, ProtocolField, ProtocolKind, ProtocolNode};
use flow_engine::{identify_flow, TransportProtocol};

#[test]
fn identifies_canonical_tcp_flow() {
    let packet = ParsedPacket {
        packet_number: 1,
        timestamp_epoch_micros: Some(1),
        link_type: Some(1),
        highest_protocol: Some(ProtocolKind::Http),
        nodes: vec![
            ProtocolNode {
                kind: ProtocolKind::Ipv4,
                label: "IPv4 10.0.0.1 -> 93.184.216.34".into(),
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
            },
            ProtocolNode {
                kind: ProtocolKind::Tcp,
                label: "TCP 34567 -> 80".into(),
                fields: vec![
                    ProtocolField {
                        name: "src_port".into(),
                        value: "34567".into(),
                    },
                    ProtocolField {
                        name: "dst_port".into(),
                        value: "80".into(),
                    },
                ],
            },
        ],
        summary: "IPv4 -> TCP -> HTTP".into(),
        warnings: vec![],
    };

    let key = identify_flow(&packet).unwrap();
    assert_eq!(key.transport, TransportProtocol::Tcp);
    assert_eq!(key.a.address, "10.0.0.1");
    assert_eq!(key.a.port, Some(34567));
    assert_eq!(key.b.address, "93.184.216.34");
    assert_eq!(key.b.port, Some(80));
}