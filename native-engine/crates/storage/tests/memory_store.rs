use session_manager::SessionSnapshot;
use storage::{InMemoryStorage, StoredSessionRecord, StorageRepository};
use use_cases::CaptureOverview;

fn sample_record(session_id: &str) -> StoredSessionRecord {
    StoredSessionRecord {
        snapshot: SessionSnapshot {
            session_id: session_id.into(),
            source_name: "sample.pcap".into(),
            total_packets: 10,
            total_flows: 3,
            active_packet_number: Some(1),
            active_flow_label: Some("TCP 10.0.0.1:12345 <-> 93.184.216.34:80".into()),
            search_text: Some("http".into()),
            applied_filters: vec!["tcp.port == 80".into()],
            created_at_epoch_micros: 100,
            updated_at_epoch_micros: 200,
        },
        overview: Some(CaptureOverview {
            total_packets: 10,
            total_flows: 3,
            first_timestamp_epoch_micros: Some(100),
            last_timestamp_epoch_micros: Some(500),
            top_protocols: vec![("HTTP".into(), 4), ("DNS".into(), 2)],
            top_hosts: vec![("10.0.0.1".into(), 8), ("93.184.216.34".into(), 4)],
        }),
        tags: vec!["lab".into(), "baseline".into()],
        notes: Some("session persisted in memory".into()),
    }
}

#[test]
fn saves_lists_gets_and_deletes_sessions() {
    let mut repo = InMemoryStorage::new();

    repo.save_session(sample_record("session-1")).unwrap();
    repo.save_session(sample_record("session-2")).unwrap();

    let all = repo.list_sessions().unwrap();
    assert_eq!(all.len(), 2);

    let one = repo.get_session("session-1").unwrap();
    assert_eq!(one.snapshot.session_id, "session-1");
    assert_eq!(one.snapshot.total_packets, 10);

    let stats = repo.stats().unwrap();
    assert_eq!(stats.total_sessions, 2);
    assert_eq!(stats.total_packets_indexed, 20);
    assert_eq!(stats.total_flows_indexed, 6);

    repo.delete_session("session-2").unwrap();

    let stats_after_delete = repo.stats().unwrap();
    assert_eq!(stats_after_delete.total_sessions, 1);
}