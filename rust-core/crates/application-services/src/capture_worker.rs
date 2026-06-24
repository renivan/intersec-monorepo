use std::sync::{Arc, Mutex};
use std::thread;
use session_manager::SessionManager;
use core_types::PacketRecord;

// Novos Módulos do Pipeline Master
use engine_io::CaptureTunnel;
use security_dissector::SecurityDissector;
use neural_intelligence::NeuralIntelligence;
use traffic_valve::{TrafficValve, ValveAction};
use protocol_engine::DataLinkMode;

pub struct CaptureWorker {
    sessions: Arc<Mutex<SessionManager>>,
}

impl CaptureWorker {
    pub fn new(sessions: Arc<Mutex<SessionManager>>) -> Self {
        Self { sessions }
    }

    pub fn start_vpn_capture(&self, fd: i32) -> Result<(), String> {
        let mut tunnel = CaptureTunnel::new(fd)?;
        let dissector = SecurityDissector::new(DataLinkMode::RawIP);
        let mut brain = NeuralIntelligence::new();
        let valve = TrafficValve::new(1); // Iniciamos no Modo Normal

        let sessions = self.sessions.clone();

        thread::spawn(move || {
            let mut buffer = [0u8; 2048];
            let mut packet_count = 0;

            log::info!("Pipeline Master Ativo: Escutando tráfego no FD {}...", fd);

            loop {
                // CAMADA 1: IO (Leitura)
                match tunnel.read_packet(&mut buffer) {
                    Ok(n) if n > 0 => {
                        packet_count += 1;
                        let raw_data = buffer[..n].to_vec();

                        let record = PacketRecord {
                            packet_number: packet_count,
                            file_offset: 0,
                            interface_id: Some(0),
                            timestamp_epoch_micros: Some(
                                std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_micros() as u64
                            ),
                            captured_length: n as u32,
                            original_length: n as u32,
                            link_type: Some(101),
                            raw_data,
                            read_warnings: Vec::new(),
                        };

                        // CAMADA 2: Security Dissector (Análise de Protocolo e DPI)
                        if let Ok(parsed) = dissector.dissect(&record) {

                            // CAMADA 3: Neural Intelligence (Heurísticas de Comportamento)
                            let risk_score = brain.analyze_behavior(&parsed);
                            let threat_score = brain.get_threat_score();

                            // CAMADA 4: Traffic Valve (Veredito de Segurança)
                            let decision = valve.decide(threat_score);

                            match decision {
                                ValveAction::Release => {
                                    // Libera o pacote: No modo passivo, apenas ignoramos ou re-escrevemos se necessário
                                }
                                ValveAction::Drop => {
                                    log::warn!("Guardian: Bloqueando pacote suspeito do IP.");
                                    continue; // Pula para o próximo loop sem processar
                                }
                                ValveAction::Hold => {
                                    log::info!("Guardian: Pacote em quarentena aguardando decisão.");
                                    // TODO: Implementar buffer de quarentena
                                }
                            }

                            // Sincronização com o Gerenciador de Sessão (UI)
                            let mut manager = sessions.lock().unwrap();
                            let _ = manager.push_packet(parsed);
                        }
                    }
                    Ok(_) => break, // EOF
                    Err(e) if e.kind() == std::io::ErrorKind::WouldBlock => {
                        thread::sleep(std::time::Duration::from_millis(1));
                        continue;
                    }
                    Err(e) => {
                        log::error!("Erro Crítico no Pipeline: {}", e);
                        break;
                    }
                }
            }
        });

        Ok(())
    }

    pub fn start_pcap_capture(&self, _iface: String, _filter: String) -> Result<(), String> {
        Err("Captura direta (PCAP) requer libpcap. Use o Modo VPN Sentinel.".into())
    }
}
