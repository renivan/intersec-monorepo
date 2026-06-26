#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum ValveAction {
    Release, // Libera o pacote para a rede
    Drop,    // Descarta o pacote (Bloqueio Ativo)
    Hold,    // Quarentena para análise
}

pub struct TrafficValve {
    security_level: u8, // 0 = Baixo, 1 = Normal, 2 = Alto
}

impl TrafficValve {
    pub fn new(level: u8) -> Self {
        Self { security_level: level }
    }

    pub fn set_security_level(&mut self, level: u8) {
        self.security_level = level;
    }

    /// Toma a decisão final baseada no Score Neural e nas violações de DPI.
    pub fn decide(&self, risk_score: u8, has_violation: bool) -> ValveAction {
        match self.security_level {
            0 => ValveAction::Release, // Modo Observador

            1 => {
                // Modo Normal: Bloqueia apenas violações claras de DPI ou riscos extremos
                if has_violation || risk_score > 90 { ValveAction::Drop }
                else if risk_score > 70 { ValveAction::Hold }
                else { ValveAction::Release }
            },

            2 => {
                // MODO ALTO: Tolerância Zero (Blindagem Industrial)
                if has_violation || risk_score > 50 {
                    ValveAction::Drop
                } else {
                    ValveAction::Release
                }
            },

            _ => ValveAction::Release,
        }
    }
}
