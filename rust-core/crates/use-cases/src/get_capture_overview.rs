use std::collections::BTreeMap;
use crate::error::UseCasesError;
use crate::models::{CaptureContext, CaptureOverview, SecurityCounts};

/**
 * Calcula o Overview da captura com Inteligência de Rede Master.
 */
pub fn get_capture_overview(ctx: &CaptureContext) -> Result<CaptureOverview, UseCasesError> {
    let mut protocol_count = BTreeMap::<String, usize>::new();
    let mut host_count = BTreeMap::<String, usize>::new();

    let mut first_ts: Option<u64> = None;
    let mut last_ts: Option<u64> = None;
    let mut total_volume_bytes: u64 = 0;
    let mut safe_count = 0;
    let mut unusual_count = 0;
    let mut suspicious_count = 0;
    let mut events = Vec::new();

    for packet in &ctx.packets {
        if let Some(highest) = &packet.highest_protocol {
            *protocol_count.entry(highest.label()).or_insert(0) += 1;
        }

        for node in &packet.nodes {
            for field in &node.fields {
                if field.name == "src_ip" || field.name == "dst_ip" {
                    *host_count.entry(field.value.clone()).or_insert(0) += 1;
                }
            }
        }

        if let Some(ts) = packet.timestamp_epoch_micros {
            first_ts = Some(match first_ts {
                Some(current) => current.min(ts),
                None => ts,
            });

            last_ts = Some(match last_ts {
                Some(current) => current.max(ts),
                None => ts,
            });
        }
    }

    for flow in &ctx.flows {
        total_volume_bytes += flow.total_payload_bytes as u64;

        match flow.security_level() {
            flow_engine::FlowSecurityLevel::Safe => safe_count += 1,
            flow_engine::FlowSecurityLevel::Unusual => unusual_count += 1,
            flow_engine::FlowSecurityLevel::Suspicious => suspicious_count += 1,
        }

        if flow.has_syn && !flow.has_ack {
            let msg = format!("Conexão TCP incompleta detectada: {}", flow.summary().endpoints);
            if !events.contains(&msg) { events.push(msg); }
        }
    }

    if suspicious_count > 0 {
        events.push(format!("Detectados {} fluxos com comportamento de RST (possível bloqueio ou ataque)", suspicious_count));
    }

    if events.is_empty() {
        events.push("Nenhum comportamento malicioso detectado".into());
    }

    let mut top_protocols: Vec<(String, usize)> = protocol_count.into_iter().collect();
    top_protocols.sort_by(|a, b| b.1.cmp(&a.1).then_with(|| a.0.cmp(&b.0)));

    let mut top_hosts: Vec<(String, usize)> = host_count.into_iter().collect();
    top_hosts.sort_by(|a, b| b.1.cmp(&a.1).then_with(|| a.0.cmp(&b.0)));

    // Lógica Master de GeoIP (Simulada para MVP)
    let mut geo_points = Vec::new();
    for (host, count) in top_hosts.iter().take(5) {
        if !host.starts_with("192.168.") && !host.starts_with("10.") {
            // Se for IP externo, mapeia para um ponto geográfico
            geo_points.push(crate::models::GeoPoint {
                latitude: -23.5505, // São Paulo (Exemplo)
                longitude: -46.6333,
                country_name: "Brazil".into(),
                country_code: "BR".into(),
                city: Some("São Paulo".into()),
                flow_count: *count,
            });
        }
    }

    let total_flows = ctx.flows.len();
    let risk_score = if total_flows > 0 {
        ((suspicious_count * 100 + unusual_count * 20) / total_flows).min(100) as u8
    } else {
        0
    };

    Ok(CaptureOverview {
        total_packets: ctx.packets.len(),
        total_flows,
        total_volume_bytes,
        average_risk_score: risk_score,
        first_timestamp_epoch_micros: first_ts,
        last_timestamp_epoch_micros: last_ts,
        top_protocols,
        top_hosts,
        security_counts: SecurityCounts {
            safe: safe_count,
            unusual: unusual_count,
            suspicious: suspicious_count,
            active_alerts: (suspicious_count > 0) as usize,
        },
        events,
        geo_points,
    })
}
