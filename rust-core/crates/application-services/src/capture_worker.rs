use std::sync::{Arc, Mutex};
use std::thread;
use session_manager::SessionManager;
#[allow(unused_imports)]
use protocol_engine::decode_packet;
#[allow(unused_imports)]
use core_types::PacketRecord;

pub struct CaptureWorker {
    sessions: Arc<Mutex<SessionManager>>,
}

impl CaptureWorker {
    pub fn new(sessions: Arc<Mutex<SessionManager>>) -> Self {
        Self { sessions }
    }

    pub fn start_pcap_capture(&self, iface: String, filter: String) -> Result<(), String> {
        let sessions = self.sessions.clone();

        thread::spawn(move || {
            #[cfg(not(target_os = "windows"))]
            {
                use pcap::{Capture, Device};

                let device = if iface == "any" || iface.is_empty() {
                    Device::lookup().unwrap().unwrap()
                } else {
                    Device::from(&*iface)
                };

                let mut cap = Capture::from_device(device)
                    .unwrap()
                    .promisc(true)
                    .snaplen(65535)
                    .timeout(100)
                    .open()
                    .unwrap();

                if !filter.is_empty() {
                    cap.filter(&filter, true).unwrap();
                }

                let mut packet_count = 0;

                while let Ok(packet) = cap.next_packet() {
                    packet_count += 1;

                    let record = PacketRecord {
                        packet_number: packet_count,
                        file_offset: 0,
                        interface_id: Some(0),
                        timestamp_epoch_micros: Some(
                            std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_micros() as u64
                        ),
                        captured_length: packet.header.caplen,
                        original_length: packet.header.len,
                        link_type: Some(1), // Ethernet
                        raw_data: packet.data.to_vec(),
                        read_warnings: Vec::new(),
                    };

                    if let Ok(parsed) = decode_packet(&record) {
                        let mut manager = sessions.lock().unwrap();
                        let _ = manager.push_packet(parsed);

                        // Atualiza fluxos em tempo real
                        if let Ok(active) = manager.active_mut() {
                            let _ = flow_engine::update_flows(&mut active.capture.flows, &active.capture.packets);
                        }
                    }
                }
            }

            #[cfg(target_os = "windows")]
            {
                let _ = (iface, filter, sessions);
                log::warn!("PCAP capture not supported on Windows in this worker implementation");
            }
        });

        Ok(())
    }

    pub fn start_vpn_capture(&self, fd: i32) -> Result<(), String> {
        let sessions = self.sessions.clone();

        thread::spawn(move || {
            #[cfg(unix)]
            {
                use std::os::unix::io::FromRawFd;
                use std::fs::File;
                use std::io::Read;

                let mut file = unsafe { File::from_raw_fd(fd) };
                let mut buffer = [0u8; 65535];
                let mut packet_count = 0;

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

                            if let Ok(parsed) = decode_packet(&record) {
                                let mut manager = sessions.lock().unwrap();
                                let _ = manager.push_packet(parsed);

                                // Atualiza fluxos em tempo real
                                if let Ok(active) = manager.active_mut() {
                                    let _ = flow_engine::update_flows(&mut active.capture.flows, &active.capture.packets);
                                }
                            }
                        }
                        Ok(_) => break,
                        Err(_) => break,
                    }
                }
            }

            #[cfg(not(unix))]
            {
                let _ = (fd, sessions);
                log::warn!("VPN capture (FD) only supported on Unix systems (Android/iOS)");
            }
        });

        Ok(())
    }
}
