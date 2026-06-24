#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum ValveAction {
    Release, // Libera o pacote
    Drop,    // Bloqueia o pacote
    Hold,    // Segura para decisão do usuário (Quarentena)
}

pub struct TrafficValve {
    security_level: u8, // 0 = Baixo, 1 = Normal, 2 = Alto
}

impl TrafficValve {
    pub fn new(level: u8) -> Self {
        Self { security_level: level }
    }

    /// Toma a decisão final sobre o destino do pacote no cano.
    pub fn decide(&self, risk_score: u8) -> ValveAction {
        match self.security_level {
            0 => ValveAction::Release, // Modo Baixo: Apenas observa
            1 => {
                if risk_score > 70 { ValveAction::Hold }
                else { ValveAction::Release }
            },
            2 => {
                if risk_score > 50 { ValveAction::Drop }
                else { ValveAction::Release }
            },
            _ => ValveAction::Release,
        }
    }
}
