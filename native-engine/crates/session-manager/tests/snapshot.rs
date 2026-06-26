use core_types::CaptureMetadata;
use session_manager::SessionManager;
use use_cases::CaptureContext;

#[test]
fn creates_snapshot_for_empty_context() {
    let mut manager = SessionManager::new();

    let ctx = CaptureContext {
        source_name: "empty".into(),
        metadata: CaptureMetadata::default(),
        packets: vec![],
        flows: vec![],
    };

    manager.load_context(ctx, 500).unwrap();
    manager.set_search_text(Some("dns".into()), 600).unwrap();
    manager.set_filters(vec!["udp.port == 53".into()], 700).unwrap();

    let snapshot = manager.snapshot().unwrap();

    assert_eq!(snapshot.source_name, "empty");
    assert_eq!(snapshot.total_packets, 0);
    assert_eq!(snapshot.total_flows, 0);
    assert_eq!(snapshot.search_text, Some("dns".into()));
    assert_eq!(snapshot.applied_filters.len(), 1);
}