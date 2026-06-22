use std::collections::{BTreeMap, HashMap};

use core_types::{ParsedPacket, ProtocolKind};

use crate::flow_identifier::{identify_flow, FlowKey};
use crate::summary::{build_flow_summary, FlowSummary};
use crate::FlowEngineError;

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum FlowSecurityLevel {
    Safe,
    Unusual,
    Suspicious,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct BehaviorProfile {
    pub flow_count: usize,
    pub total_bytes: u64,
    pub last_seen_micros: u64,
    pub protocols: HashMap<String, usize>,
    pub risk_score: u8,
}

#[derive(Default)]
pub struct BehaviorTracker {
    pub profiles: HashMap<String, BehaviorProfile>, // Key: IP Address
}

impl BehaviorTracker {
    pub fn new() -> Self {
        Self::default()
    }

    pub fn update(&mut self, packet: &ParsedPacket) {
        let ts = packet.timestamp_epoch_micros.unwrap_or(0);

        // Extrai IPs de origem e destino para perfilamento
        let mut ips = Vec::new();
        for node in &packet.nodes {
            for field in &node.fields {
                if field.name.contains("src_ip") || field.name.contains("dst_ip") {
                    ips.push(field.value.clone());
                }
            }
        }

        for ip in ips {
            let profile = self.profiles.entry(ip).or_insert(BehaviorProfile {
                flow_count: 0,
                total_bytes: 0,
                last_seen_micros: ts,
                protocols: HashMap::new(),
                risk_score: 0,
            });

            profile.flow_count += 1;
            profile.last_seen_micros = profile.last_seen_micros.max(ts);

            if let Some(highest) = &packet.highest_protocol {
                *profile.protocols.entry(highest.label()).or_insert(0) += 1;
            }

            // Heurística de Risco por IP: Se acessa muitos protocolos diferentes ou volume súbito
            if profile.protocols.len() > 10 {
                profile.risk_score = profile.risk_score.max(40); // Possível escaneamento
            }
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FlowAggregate {
    pub key: FlowKey,
    pub packet_numbers: Vec<u64>,
    pub highest_protocols: Vec<String>,
    pub total_payload_bytes: usize,
    pub has_syn: bool,
    pub has_ack: bool,
    pub has_rst: bool,
}

impl FlowAggregate {
    pub fn security_level(&self) -> FlowSecurityLevel {
        // Lógica Master de Heurística
        let is_encrypted = self.highest_protocols.iter().any(|p|
            p.contains("TLS") || p.contains("HTTPS") || p.contains("QUIC") || p.contains("SSH")
        );

        if self.has_rst {
            return FlowSecurityLevel::Suspicious;
        }

        if self.has_syn && !self.has_ack {
            return FlowSecurityLevel::Unusual; // Conexão pendente/Escaneamento
        }

        if is_encrypted {
            FlowSecurityLevel::Safe
        } else {
            FlowSecurityLevel::Unusual // Tráfego em texto claro
        }
    }

    pub fn summary(&self) -> FlowSummary {
        build_flow_summary(self)
    }
}

#[derive(Default)]
pub struct FlowTracker {
    flows: BTreeMap<FlowKey, FlowAggregate>,
}

impl FlowTracker {
    pub fn new() -> Self {
        Self {
            flows: BTreeMap::new(),
        }
    }

    pub fn ingest(&mut self, packet: &ParsedPacket) -> Result<(), FlowEngineError> {
        // Ignoramos erros de camada de endereço (ex: ARP, NDP) para não travar a análise.
        // Apenas fluxos com endereços IP (v4/v6) são rastreados.
        let key = match identify_flow(packet) {
            Ok(k) => k,
            Err(FlowEngineError::MissingAddressLayer) => return Ok(()), // Pula silenciosamente
            Err(e) => return Err(e),
        };

        let aggregate = self.flows.entry(key.clone()).or_insert_with(|| FlowAggregate {
            key,
            packet_numbers: Vec::new(),
            highest_protocols: Vec::new(),
            total_payload_bytes: 0,
            has_syn: false,
            has_ack: false,
            has_rst: false,
        });

        aggregate.packet_numbers.push(packet.packet_number);

        // Extração Master de Flags TCP para Inteligência
        for node in &packet.nodes {
            for field in &node.fields {
                let val_upper = field.value.to_uppercase();
                if field.name.contains("flags") || node.label.contains("TCP") {
                    if val_upper.contains("SYN") { aggregate.has_syn = true; }
                    if val_upper.contains("ACK") { aggregate.has_ack = true; }
                    if val_upper.contains("RST") { aggregate.has_rst = true; }
                }
            }
        }

        if let Some(highest) = &packet.highest_protocol {
            aggregate.highest_protocols.push(highest.label());
        }

        aggregate.total_payload_bytes += estimate_payload_len(packet);
        Ok(())
    }

    pub fn into_flows(self) -> Vec<FlowAggregate> {
        self.flows.into_values().collect()
    }
}

pub fn track_flows(packets: &[ParsedPacket]) -> Result<Vec<FlowAggregate>, FlowEngineError> {
    let mut tracker = FlowTracker::new();

    for packet in packets {
        tracker.ingest(packet)?;
    }

    Ok(tracker.into_flows())
}

fn estimate_payload_len(packet: &ParsedPacket) -> usize {
    if packet
        .nodes
        .iter()
        .any(|node| matches!(node.kind, ProtocolKind::Dns | ProtocolKind::Http | ProtocolKind::Tls))
    {
        return 1;
    }

    0
}
