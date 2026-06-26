use core_types::ParsedPacket;
use flow_engine::FlowTracker;

pub struct NeuralIntelligence {
    tracker: FlowTracker,
    global_entropy: f32,
}

impl NeuralIntelligence {
    pub fn new() -> Self {
        Self {
            tracker: FlowTracker::new(),
            global_entropy: 0.0,
        }
    }

    /// Analisa o comportamento do pacote dentro do contexto do organismo da rede.
    pub fn analyze_behavior(&mut self, packet: &ParsedPacket) -> f32 {
        let _ = self.tracker.ingest(packet);

        // Lógica de cálculo de risco neural
        if packet.summary.contains("RST") {
            self.global_entropy += 0.1;
        }

        self.global_entropy.min(1.0)
    }

    pub fn get_threat_score(&self) -> u8 {
        (self.global_entropy * 100.0) as u8
    }
}
