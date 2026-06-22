use crate::dto::{
    KotlinFlowSearchResult,
    KotlinPacketSearchResult,
    KotlinRuntimeSnapshot,
    KotlinSessionSnapshot,
    KotlinStoredSession,
};

pub fn decode_csv_filters(input: &str) -> Vec<String> {
    input
        .split(',')
        .map(|value| value.trim())
        .filter(|value| !value.is_empty())
        .map(|value| value.to_string())
        .collect()
}

pub fn encode_session_snapshot_text(snapshot: &KotlinSessionSnapshot) -> String {
    format!(
        concat!(
            "session_id={}\n",
            "source_name={}\n",
            "total_packets={}\n",
            "total_flows={}\n",
            "active_packet_number={}\n",
            "active_flow_label={}\n",
            "search_text={}\n",
            "applied_filters_csv={}\n",
            "created_at_epoch_micros={}\n",
            "updated_at_epoch_micros={}\n"
        ),
        snapshot.session_id,
        snapshot.source_name,
        snapshot.total_packets,
        snapshot.total_flows,
        snapshot
            .active_packet_number
            .map(|v| v.to_string())
            .unwrap_or_else(|| "-".into()),
        snapshot
            .active_flow_label
            .clone()
            .unwrap_or_else(|| "-".into()),
        snapshot.search_text.clone().unwrap_or_else(|| "-".into()),
        snapshot.applied_filters_csv,
        snapshot.created_at_epoch_micros,
        snapshot.updated_at_epoch_micros,
    )
}

pub fn encode_packet_result_text(result: &KotlinPacketSearchResult) -> String {
    let mut out = format!("total_items={}\n", result.total_items);

    for item in &result.items {
        out.push_str(&format!(
            "packet_number={} | timestamp={} | protocol={} | summary={}\n",
            item.packet_number,
            item.timestamp_epoch_micros
                .map(|v| v.to_string())
                .unwrap_or_else(|| "-".into()),
            item.highest_protocol.clone().unwrap_or_else(|| "-".into()),
            item.summary
        ));
    }

    out
}

pub fn encode_flow_result_text(result: &KotlinFlowSearchResult) -> String {
    let mut out = format!("total_items={}\n", result.total_items);

    for item in &result.items {
        out.push_str(&format!(
            "label={} | endpoints={} | total_packets={} | total_payload_bytes={}\n",
            item.label,
            item.endpoints,
            item.total_packets,
            item.total_payload_bytes,
        ));
    }

    out
}

pub fn encode_session_list_text(items: &[KotlinStoredSession]) -> String {
    let mut out = format!("total_items={}\n", items.len());

    for item in items {
        out.push_str(&format!(
            "session_id={} | source_name={} | total_packets={} | total_flows={} | tags_csv={} | notes={}\n",
            item.session_id,
            item.source_name,
            item.total_packets,
            item.total_flows,
            item.tags_csv,
            item.notes.clone().unwrap_or_else(|| "-".into()),
        ));
    }

    out
}

pub fn encode_capture_overview_text(ov: &crate::dto::KotlinCaptureOverview) -> String {
    let mut out = format!(
        "total_packets={} | total_flows={} | total_volume_bytes={} | average_risk_score={}\n",
        ov.total_packets, ov.total_flows, ov.total_volume_bytes, ov.average_risk_score
    );
    out.push_str(&format!(
        "safe={} | unusual={} | suspicious={} | active_alerts={}\n",
        ov.security_counts.safe, ov.security_counts.unusual, ov.security_counts.suspicious, ov.security_counts.active_alerts
    ));

    for (proto, count) in &ov.top_protocols {
        out.push_str(&format!("proto={} count={}\n", proto, count));
    }

    for (host, count) in &ov.top_hosts {
        out.push_str(&format!("host={} count={}\n", host, count));
    }

    for event in &ov.events {
        out.push_str(&format!("event={}\n", event));
    }

    out
}

pub fn encode_runtime_snapshot_text(snapshot: &KotlinRuntimeSnapshot) -> String {
    format!(
        concat!(
            "platform_label={}\n",
            "initialized={}\n",
            "active_capture_loaded={}\n",
            "stored_sessions_count={}\n",
            "last_opened_source={}\n"
        ),
        snapshot.platform_label,
        snapshot.initialized,
        snapshot.active_capture_loaded,
        snapshot.stored_sessions_count,
        snapshot
            .last_opened_source
            .clone()
            .unwrap_or_else(|| "-".into()),
    )
}