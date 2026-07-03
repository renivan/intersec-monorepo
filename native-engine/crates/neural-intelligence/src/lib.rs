use std::collections::HashMap;
use serde::{Serialize, Deserialize};

/**
 * NeuralCore (v3.0 - Rust).
 * Motor de alta performance para processamento de grafos de transporte e geo-localização.
 */

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct NeuralLink3D {
    pub id: String,
    pub src_ip: String,
    pub dst_ip: String,
    pub protocol: String,
    pub intensity: f32,
    pub lat: f64,
    pub lon: f64,
    pub country_code: String,
    pub x: f32,
    pub y: f32,
    pub z: f32,
}

pub struct NeuralIntelligenceEngine {
    links: HashMap<String, NeuralLink3D>,
    radius: f32,
}

impl NeuralIntelligenceEngine {
    pub fn new() -> Self {
        Self {
            links: HashMap::new(),
            radius: 100.0,
        }
    }

    /**
     * Injeta uma conexão e calcula a projeção esférica 3D instantaneamente.
     */
    pub fn push_transport_event(&mut self, ip: &str, proto: &str, lat: f64, lon: f64, volume: u64) {
        let id = format!("{}_{}", ip, proto);

        // Conversão Esférica -> Cartesiana (X, Y, Z) no Core Rust
        let phi = (90.0 - lat).to_radians() as f32;
        let theta = (lon + 180.0).to_radians() as f32;

        let x = -(self.radius * phi.sin() * theta.cos());
        let z = (self.radius * phi.sin() * theta.sin());
        let y = (self.radius * phi.cos());

        let intensity = (volume as f32 / 500000.0).clamp(0.2, 1.0);

        let link = NeuralLink3D {
            id: id.clone(),
            src_ip: "DEVICE".to_string(),
            dst_ip: ip.to_string(),
            protocol: proto.to_string(),
            intensity,
            lat,
            lon,
            country_code: "N/A".to_string(),
            x,
            y,
            z,
        };

        self.links.insert(id, link);

        // Mantém apenas os 100 links neurais mais ativos para performance
        if self.links.len() > 100 {
            // Lógica de expiração de cache neural aqui
        }
    }

    pub fn get_neural_snapshot(&self) -> Vec<NeuralLink3D> {
        self.links.values().cloned().collect()
    }
}
