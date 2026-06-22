use crate::dto::{
    BridgeFlowSearchResult,
    BridgePacketSearchResult,
    BridgeSessionSnapshot,
    BridgeStoredSession,
};

pub fn serialize_session_snapshot_text(snapshot: &BridgeSessionSnapshot) -> String {
    let active_packet = snapshot
        .active_packet_number
        .map(|v| v.to_string())
        .unwrap_or_else(|| "-".into());

    let active_flow = snapshot
        .active_flow_label
        .clone()
        .unwrap_or_else(|| "-".into());

    let search_text = snapshot.search_text.clone().unwrap_or_else(|| "-".into());

    format!(
        concat!(
            "session_id={}\n",
            "source_name={}\n",
            "total_packets={}\n",
            "total_flows={}\n",
            "active_packet_number={}\n",
            "active_flow_label={}\n",
            "search_text={}\n",
            "applied_filters={}\n",
            "created_at_epoch_micros={}\n",
            "updated_at_epoch_micros={}"
        ),
        snapshot.session_id,
        snapshot.source_name,
        snapshot.total_packets,
        snapshot.total_flows,
        active_packet,
        active_flow,
        search_text,
        snapshot.applied_filters.join(","),
        snapshot.created_at_epoch_micros,
        snapshot.updated_at_epoch_micros,
    )
}

pub fn serialize_packet_result_text(result: &BridgePacketSearchResult) -> String {
    let mut out = format!("total_items={}\n", result.total_items);

    for item in &result.items {
        let ts = item
            .timestamp_epoch_micros
            .map(|v| v.to_string())
            .unwrap_or_else(|| "-".into());

        let protocol = item
            .highest_protocol
            .clone()
            .unwrap_or_else(|| "-".into());

        out.push_str(&format!(
            "packet_number={} | timestamp={} | protocol={} | summary={}\n",
            item.packet_number,
            ts,
            protocol,
            item.summary
        ));
    }

    out
}

pub fn serialize_flow_result_text(result: &BridgeFlowSearchResult) -> String {
    let mut out = format!("total_items={}\n", result.total_items);

    for item in &result.items {
        out.push_str(&format!(
            "label={} | endpoints={} | total_packets={} | total_payload_bytes={}\n",
            item.label,
            item.endpoints,
            item.total_packets,
            item.total_payload_bytes
        ));
    }

    out
}

pub fn serialize_stored_sessions_text(items: &[BridgeStoredSession]) -> String {
    let mut out = format!("total_items={}\n", items.len());

    for item in items {
        out.push_str(&format!(
            "session_id={} | source_name={} | total_packets={} | total_flows={} | tags={} | notes={}\n",
            item.session_id,
            item.source_name,
            item.total_packets,
            item.total_flows,
            item.tags.join(","),
            item.notes.clone().unwrap_or_else(|| "-".into())
        ));
    }

    out
}

pub fn serialize_capture_overview_text(ov: &crate::dto::BridgeCaptureOverview) -> String {
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

    out
}