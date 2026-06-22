use std::sync::{Arc, Mutex};
use std::thread;
use session_manager::SessionManager;
use protocol_engine::decode_packet;
use core_types::PacketRecord;

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

    pub fn start_vpn_capture(&self, fd: i32) -> Result<(), String> {
        let sessions = self.sessions.clone();

        thread::spawn(move || {
            #[cfg(unix)]
            {
                use std::os::unix::io::FromRawFd;
                use std::fs::File;
                use std::io::Read;

                // Segurança: VpnService FD é um canal de leitura de pacotes IP brutos
                let mut file = unsafe { File::from_raw_fd(fd) };
                let mut buffer = [0u8; 65535];
                let mut packet_count = 0;

                log::info!("Motor Rust: Escutando tráfego no túnel VPN (FD {})...", fd);

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
                                link_type: Some(101), // Raw IP (Padrão Android VPN)
                                raw_data,
                                read_warnings: Vec::new(),
                            };

                            if let Ok(parsed) = decode_packet(&record) {
                                let mut manager = sessions.lock().unwrap();
                                let _ = manager.push_packet(parsed);

                                // Atualiza fluxos de segurança em tempo real
                                if let Ok(active) = manager.active_mut() {
                                    let _ = flow_engine::update_flows(&mut active.capture.flows, &active.capture.packets);
                                }
                            }
                        }
                        Ok(_) => break, // EOF
                        Err(e) => {
                            log::error!("Erro na leitura do túnel: {}", e);
                            break;
                        }
                    }
                }
            }
        });

        Ok(())
    }
}
