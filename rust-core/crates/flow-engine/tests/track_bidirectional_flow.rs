use core_types::{ParsedPacket, ProtocolField, ProtocolKind, ProtocolNode};
use flow_engine::track_flows;

fn make_packet(
    number: u64,
    src_ip: &str,
    dst_ip: &str,
    src_port: &str,
    dst_port: &str,
) -> ParsedPacket {
    ParsedPacket {
        packet_number: number,
        timestamp_epoch_micros: Some(number),
        link_type: Some(1),
        highest_protocol: Some(ProtocolKind::Tcp),
        nodes: vec![
            ProtocolNode {
                kind: ProtocolKind::Ipv4,
                label: format!("IPv4 {src_ip} -> {dst_ip}"),
                fields: vec![
                    ProtocolField {
                        name: "src_ip".into(),
                        value: src_ip.into(),
                    },
                    ProtocolField {
                        name: "dst_ip".into(),
                        value: dst_ip.into(),
                    },
                ],
            },
            ProtocolNode {
                kind: ProtocolKind::Tcp,
                label: format!("TCP {src_port} -> {dst_port}"),
                fields: vec![
                    ProtocolField {
                        name: "src_port".into(),
                        value: src_port.into(),
                    },
                    ProtocolField {
                        name: "dst_port".into(),
                        value: dst_port.into(),
                    },
                ],
            },
        ],
        summary: "IPv4 -> TCP".into(),
        warnings: vec![],
    }
}

#[test]
fn groups_bidirectional_packets_into_same_flow() {
    let packets = vec![
        make_packet(1, "10.0.0.1", "93.184.216.34", "34567", "80"),
        make_packet(2, "93.184.216.34", "10.0.0.1", "80", "34567"),
    ];

    let flows = track_flows(&packets).unwrap();
    assert_eq!(flows.len(), 1);
    assert_eq!(flows[0].packet_numbers, vec![1, 2]);
}