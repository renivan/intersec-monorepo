use std::collections::HashMap;
use core_types::ParsedPacket;

#[derive(Debug, Clone, Default)]
pub struct NeuralNode {
    pub id: String,
    pub activity_score: f32, // 0.0 a 1.0 (brilho no mapa)
    pub connections: Vec<String>,
    pub protocol_weights: HashMap<String, f32>,
}

pub struct NetworkNeuralIntelligence {
    pub nodes: HashMap<String, NeuralNode>,
    pub global_threat_level: f32,
}

impl NetworkNeuralIntelligence {
    pub fn new() -> Self {
        Self {
            nodes: HashMap::new(),
            global_threat_level: 0.0,
        }
    }

    pub fn process_neural_event(&mut self, packet: &ParsedPacket) {
        let src = extract_ip(packet, true);
        let dst = extract_ip(packet, false);

        // Atualiza ou cria o nó neural para a origem
        let src_node = self.nodes.entry(src.clone()).or_insert(NeuralNode {
            id: src.clone(),
            ..Default::default()
        });

        src_node.activity_score = (src_node.activity_score + 0.1).min(1.0);
        if !src_node.connections.contains(&dst) {
            src_node.connections.push(dst.clone());
        }

        // Lógica de Inteligência: Se houver RST frequente, aumenta o threat level
        if packet.summary.contains("RST") {
            self.global_threat_level = (self.global_threat_level + 0.05).min(1.0);
        }
    }
}

fn extract_ip(packet: &ParsedPacket, is_src: Boolean) -> String {
    // Busca nos campos decodificados pelo motor Rust
    for node in &packet.nodes {
        for field in &node.fields {
            if (is_src && field.name.contains("src_ip")) || (!is_src && field.name.contains("dst_ip")) {
                return field.value.clone();
            }
        }
    }
    "unknown".to_string()
}
