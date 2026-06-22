use crate::dto::{
    FfiFlowSearchResult,
    FfiPacketSearchResult,
    FfiSessionSnapshot,
    FfiStoredSession,
};

pub fn decode_csv_tags(input: &str) -> Vec<String> {
    input
        .split(',')
        .map(|value| value.trim())
        .filter(|value| !value.is_empty())
        .map(|value| value.to_string())
        .collect()
}

pub fn encode_session_snapshot(snapshot: &FfiSessionSnapshot) -> String {
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
        snapshot.applied_filters.join(","),
        snapshot.created_at_epoch_micros,
        snapshot.updated_at_epoch_micros,
    )
}

pub fn encode_packet_search_result(result: &FfiPacketSearchResult) -> String {
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

pub fn encode_flow_search_result(result: &FfiFlowSearchResult) -> String {
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

pub fn encode_session_list(items: &[FfiStoredSession]) -> String {
    let mut out = format!("total_items={}\n", items.len());

    for item in items {
        out.push_str(&format!(
            "session_id={} | source_name={} | total_packets={} | total_flows={} | tags={} | notes={}\n",
            item.session_id,
            item.source_name,
            item.total_packets,
            item.total_flows,
            item.tags.join(","),
            item.notes.clone().unwrap_or_else(|| "-".into()),
        ));
    }

    out
}