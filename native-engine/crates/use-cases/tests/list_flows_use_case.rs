use core_types::CaptureMetadata;
use flow_engine::{FlowAggregate, FlowEndpoint, FlowKey, TransportProtocol};
use use_cases::{get_flow_detail, list_flows, CaptureContext};

#[test]
fn lists_flows_and_returns_detail() {
    let key = FlowKey {
        transport: TransportProtocol::Tcp,
        a: FlowEndpoint {
            address: "10.0.0.1".into(),
            port: Some(34567),
        },
        b: FlowEndpoint {
            address: "93.184.216.34".into(),
            port: Some(80),
        },
    };

    let flow = FlowAggregate {
        key: key.clone(),
        packet_numbers: vec![1, 2],
        highest_protocols: vec!["HTTP".into()],
        total_payload_bytes: 4,
    };

    let ctx = CaptureContext {
        source_name: "sample".into(),
        metadata: CaptureMetadata::default(),
        packets: vec![],
        flows: vec![flow],
    };

    let list = list_flows(&ctx).unwrap();
    assert_eq!(list.total_items, 1);
    assert_eq!(list.items[0].total_packets, 2);

    let detail = get_flow_detail(&ctx, &key).unwrap();
    assert_eq!(detail.flow.packet_numbers, vec![1, 2]);
}