use core_types::{ParsedPacket, PacketRecord};
use protocol_engine::{decode_packet, DataLinkMode};

pub struct SecurityDissector {
    mode: DataLinkMode,
}

impl SecurityDissector {
    pub fn new(mode: DataLinkMode) -> Self {
        Self { mode }
    }

    /// Disseca o pacote bruto em busca de metadados e assinaturas de segurança.
    pub fn dissect(&self, record: &PacketRecord) -> Result<ParsedPacket, String> {
        // Por enquanto delegamos ao protocol-engine, mas aqui entrará a lógica de DPI Master
        decode_packet(record, &self.mode)
    }

    /// Analisa se o conteúdo do pacote contém assinaturas de vazamento de dados (DPI).
    pub fn scan_for_data_leak(&self, packet: &ParsedPacket) -> bool {
        // Exemplo: Buscar por "cc_number" ou "password" em tráfego não seguro
        packet.summary.to_lowercase().contains("password")
    }
}
