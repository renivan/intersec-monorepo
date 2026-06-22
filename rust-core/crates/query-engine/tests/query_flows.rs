use core_types::CaptureMetadata;
use flow_engine::{FlowAggregate, FlowEndpoint, FlowKey, TransportProtocol};
use query_engine::{query_flows, FlowQuery};
use use_cases::CaptureContext;

#[test]
fn filters_flows_by_protocol_host_and_port() {
    let flow = FlowAggregate {
        key: FlowKey {
            transport: TransportProtocol::Tcp,
            a: FlowEndpoint {
                address: "10.0.0.1".into(),
                port: Some(34567),
            },
            b: FlowEndpoint {
                address: "93.184.216.34".into(),
                port: Some(80),
            },
        },
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

    let result = query_flows(
        &ctx,
        &FlowQuery {
            protocol: Some("HTTP".into()),
            host: Some("93.184.216.34".into()),
            port: Some(80),
            text: None,
        },
    )
        .unwrap();

    assert_eq!(result.total_items, 1);
    assert!(result.items[0].label.contains("TCP"));
}