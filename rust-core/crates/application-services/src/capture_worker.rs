use std::sync::{Arc, Mutex};
use std::thread;
use session_manager::SessionManager;
use protocol_engine::{decode_packet, DataLinkMode};
use core_types::PacketRecord;
use std::os::unix::io::RawFd;

pub struct CaptureWorker {
    sessions: Arc<Mutex<SessionManager>>,
}

impl CaptureWorker {
    pub fn new(sessions: Arc<Mutex<SessionManager>>) -> Self {
        Self { sessions }
    }

    pub fn start_pcap_capture(&self, _iface: String, _filter: String) -> Result<(), String> {
        // Desativado temporariamente para permitir build sem libpcap nativa
        Err("Captura direta (PCAP) requer libpcap. Use o Modo VPN Sentinel.".into())
    }

    /// Valida se o FD é um canal de rede ativo no Kernel do Android
    fn validate_fd(fd: RawFd) -> Result<(), String> {
        #[cfg(unix)]
        {
            use libc::{fcntl, F_GETFL};
            let flags = unsafe { fcntl(fd, F_GETFL) };
            if flags == -1 {
                return Err(format!("Falha Crítica de FD: {} não é um descritor válido.", fd));
            }
            log::info!("Master Check: File Descriptor {} validado com sucesso.", fd);
            Ok(())
        }
        #[cfg(not(unix))]
        { Ok(()) }
    }

    pub fn start_vpn_capture(&self, fd: i32) -> Result<(), String> {
        // Passo 1: Validação Master antes de iniciar a thread
        Self::validate_fd(fd as RawFd)?;

        let sessions = self.sessions.clone();

        thread::spawn(move || {
            #[cfg(unix)]
            {
                use std::os::unix::io::FromRawFd;
                use std::fs::File;
                use std::io::Read;

                // Segurança: VpnService FD é um canal de leitura de pacotes IP brutos
                let mut file = unsafe { File::from_raw_fd(fd) };

                // Buffer Otimizado (MTU 1500 + Margem)
                let mut buffer = [0u8; 2048];
                let mut packet_count = 0;

                log::info!("Motor Rust: Túnel Ativo. Iniciando processamento de fluxo...");

                loop {
                    match file.read(&mut buffer) {
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
                                link_type: Some(101), // Raw IP
                                raw_data,
                                read_warnings: Vec::new(),
                            };

                            // IMPORTANTE: Aqui usamos DataLinkMode::RawIP para VPN
                            if let Ok(parsed) = decode_packet(&record, &DataLinkMode::RawIP) {
                                let mut manager = sessions.lock().unwrap();
                                let _ = manager.push_packet(parsed);

                                // Atualiza fluxos de segurança em tempo real
                                if let Ok(active) = manager.active_mut() {
                                    let _ = flow_engine::update_flows(&mut active.capture.flows, &active.capture.packets);
                                }
                            }
                        }
                        Ok(_) => {
                            log::warn!("Motor Rust: Fim do fluxo no túnel.");
                            break;
                        }
                        Err(e) => {
                            log::error!("Erro Crítico de Leitura no FD {}: {}", fd, e);
                            break;
                        }
                    }
                }
            }
        });

        Ok(())
    }
}
