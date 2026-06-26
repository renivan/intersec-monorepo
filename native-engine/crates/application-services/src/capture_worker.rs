use std::sync::{Arc, Mutex};
use std::thread;
use session_manager::SessionManager;
use core_types::PacketRecord;

use engine_io::CaptureTunnel;
use security_dissector::SecurityDissector;
use neural_intelligence::NeuralIntelligence;
use traffic_valve::{TrafficValve, ValveAction};
use protocol_engine::DataLinkMode;

use crate::settings::SecuritySettings;

pub struct CaptureWorker {
    sessions: Arc<Mutex<SessionManager>>,
    security_settings: Arc<Mutex<SecuritySettings>>,
}

impl CaptureWorker {
    pub fn new(sessions: Arc<Mutex<SessionManager>>, security_settings: Arc<Mutex<SecuritySettings>>) -> Self {
        Self { sessions, security_settings }
    }

    pub fn start_vpn_capture(&self, fd: i32) -> Result<(), String> {
        let mut tunnel = CaptureTunnel::new(fd)?;
        let dissector = SecurityDissector::new(DataLinkMode::RawIP);
        let mut brain = NeuralIntelligence::new();

        let sessions = self.sessions.clone();
        let security_settings = self.security_settings.clone();

        thread::spawn(move || {
            let mut buffer = [0u8; 2048];
            let mut packet_count = 0;
            let mut valve = TrafficValve::new(1); // Default

            log::info!("GUARDIAN ATIVO: Sentinela operando no túnel.");

            loop {
                // 0. Sincroniza Configurações de Segurança Dinamicamente
                let (current_level, smart_shield, kill_switch) = {
                    let s = security_settings.lock().unwrap();
                    (s.level, s.smart_shield, s.kill_switch)
                };

                valve.set_security_level(current_level);

                match tunnel.read_packet(&mut buffer) {
                    Ok(n) if n > 0 => {
                        // 1. Verificação de Kill-Switch Global
                        if kill_switch {
                            log::warn!("KILL-SWITCH ATIVO: Tráfego bloqueado preventivamente.");
                            continue;
                        }

                        packet_count += 1;
                        let raw_data = buffer[..n].to_vec();
// ...

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

                        if let Ok(parsed) = dissector.dissect(&record) {
                            // 1. Verificação Neural
                            let _risk = brain.analyze_behavior(&parsed);
                            let threat_score = brain.get_threat_score();

                            // 2. Verificação de Violação (DPI)
                            let violation = if smart_shield {
                                dissector.check_violation(&parsed)
                            } else {
                                None
                            };
                            let has_violation = violation.is_some();

                            // 3. Veredito da Válvula de Tráfego
                            let decision = valve.decide(threat_score, has_violation);

                            match decision {
                                ValveAction::Release => {
                                    // Sincroniza com UI apenas pacotes legítimos
                                    let mut manager = sessions.lock().unwrap();
                                    let _ = manager.push_packet(parsed);
                                }
                                ValveAction::Drop => {
                                    if let Some(msg) = violation {
                                        log::error!("BLOQUEIO ATIVO: {}", msg);
                                    } else {
                                        log::error!("BLOQUEIO ATIVO: Risco Neural Detectado (Score: {}%)", threat_score);
                                    }

                                    // PERSISTÊNCIA FORENSE: Registra o bloqueio no disco para prova futura
                                    let mut manager = sessions.lock().unwrap();
                                    let _ = manager.persist_session("BLOCK_EVENT", Some("Ataque Neutralizado pela Sentinela.".into()));

                                    continue; // O pacote MORRE aqui. Não é enviado para a rede nem para a UI.
                                }
                                ValveAction::Hold => {
                                    log::info!("QUARENTENA: Pacote retido para análise.");
                                }
                            }
                        }
                    }
                    Ok(_) => break,
                    Err(e) if e.kind() == std::io::ErrorKind::WouldBlock => {
                        thread::sleep(std::time::Duration::from_millis(1));
                        continue;
                    }
                    Err(e) => {
                        log::error!("Erro Crítico no Túnel: {}", e);
                        break;
                    }
                }
            }
        });

        Ok(())
    }

    pub fn start_pcap_capture(&self, _iface: String, _filter: String) -> Result<(), String> {
        Err("Captura direta requer modo Root. Use o Modo VPN Sentinel.".into())
    }
}
