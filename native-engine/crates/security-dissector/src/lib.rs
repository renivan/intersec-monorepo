use core_types::{ParsedPacket, PacketRecord};
use protocol_engine::{decode_packet, DataLinkMode};

pub struct SecurityDissector {
    mode: DataLinkMode,
}

impl SecurityDissector {
    pub fn new(mode: DataLinkMode) -> Self {
        Self { mode }
    }

    pub fn dissect(&self, record: &PacketRecord) -> Result<ParsedPacket, String> {
        decode_packet(record, &self.mode)
    }

    /// Executa a Inspeção Profunda (DPI) em busca de vazamento de dados ou links maliciosos.
    pub fn check_violation(&self, packet: &ParsedPacket) -> Option<String> {
        let summary = packet.summary.to_lowercase();

        // Também verificamos os dados brutos se disponíveis (DPI Real)
        let raw_str = String::from_utf8_lossy(&packet.raw_data).to_lowercase();

        // 1. Detecção de Vazamento de Credenciais (Egress Protection)
        if summary.contains("password") || raw_str.contains("password") ||
           summary.contains("passwd")   || raw_str.contains("passwd") ||
           summary.contains("login")    || raw_str.contains("login") {
            return Some("VIOLAÇÃO: Tentativa de envio de credenciais em texto claro detectada.".into());
        }

        // 2. Detecção de Infiltração de Executáveis (Ingress Protection)
        if summary.contains(".exe") || raw_str.contains(".exe") ||
           summary.contains(".sh")  || raw_str.contains(".sh") ||
           summary.contains(".scr") || raw_str.contains(".scr") {
            return Some("VIOLAÇÃO: Tentativa de download de executável suspeito bloqueada.".into());
        }

        // 3. Detecção de Dados Sensíveis (DLP)
        if summary.contains("card_number") || raw_str.contains("card_number") ||
           summary.contains("cvv")         || raw_str.contains("cvv") {
            return Some("CRÍTICO: Bloqueio de exfiltração de dados de pagamento.".into());
        }

        None
    }
}
