use query_engine::{
    query_flows, query_packets, FlowQuery, FlowSearchResult, PacketQuery, PacketSearchResult,
};
use session_manager::{SessionManager, SessionSnapshot};
use storage::{StorageRepository, StoredSessionRecord};
use use_cases::get_capture_overview;

use crate::error::ApplicationServicesError;
use crate::settings::SecuritySettings;

use std::sync::{Arc, Mutex};
use crate::capture_worker::CaptureWorker;

pub struct ApplicationServices<R: StorageRepository> {
    pub sessions: Arc<Mutex<SessionManager>>,
    pub storage: R,
    pub security_settings: Arc<Mutex<SecuritySettings>>,
    pub neural_engine: Arc<Mutex<neural_intelligence::NeuralIntelligenceEngine>>,
}

impl<R: StorageRepository> ApplicationServices<R> {
    pub fn new(storage: R) -> Self {
        Self {
            sessions: Arc::new(Mutex::new(SessionManager::new())),
            storage,
            security_settings: Arc::new(Mutex::new(SecuritySettings::default())),
            neural_engine: Arc::new(Mutex::new(neural_intelligence::NeuralIntelligenceEngine::new())),
        }
    }

    pub fn open_capture(
        &mut self,
        path: &str,
        now_epoch_micros: u64,
    ) -> Result<SessionSnapshot, ApplicationServicesError> {
        let mut sessions = self.sessions.lock().unwrap();
        sessions.open_capture_session(path, now_epoch_micros)?;
        let snapshot = sessions.snapshot()?;
        Ok(snapshot)
    }

    pub fn snapshot_active(&self) -> Result<SessionSnapshot, ApplicationServicesError> {
        Ok(self.sessions.lock().unwrap().snapshot()?)
    }

    pub fn persist_active(
        &mut self,
        tags: Vec<String>,
        notes: Option<String>,
    ) -> Result<(), ApplicationServicesError> {
        let sessions = self.sessions.lock().unwrap();
        let session = sessions.active()?;
        let overview = get_capture_overview(&session.capture)?;
        let snapshot = sessions.snapshot()?;
// ...

        let record = StoredSessionRecord {
            snapshot,
            overview: Some(overview),
            tags,
            notes,
        };

        self.storage.upsert_session(record)?;
        Ok(())
    }

    pub fn query_active_packets(
        &self,
        query: &PacketQuery,
    ) -> Result<PacketSearchResult, ApplicationServicesError> {
        let sessions = self.sessions.lock().unwrap();
        let session = sessions.active()?;
        Ok(query_packets(&session.capture, query)?)
    }

    pub fn query_active_flows(
        &self,
        query: &FlowQuery,
    ) -> Result<FlowSearchResult, ApplicationServicesError> {
        let sessions = self.sessions.lock().unwrap();
        let session = sessions.active()?;
        Ok(query_flows(&session.capture, query)?)
    }

    pub fn get_capture_overview(&self) -> Result<use_cases::models::CaptureOverview, ApplicationServicesError> {
        let sessions = self.sessions.lock().unwrap();
        let session = sessions.active()?;
        Ok(get_capture_overview(&session.capture)?)
    }

    pub fn start_capture(&mut self, iface: &str, filter: &str) -> Result<String, ApplicationServicesError> {
        let id = {
            let mut sessions = self.sessions.lock().unwrap();
            sessions.start_live_session(iface, filter)?
        };

        let worker = CaptureWorker::new(self.sessions.clone(), self.security_settings.clone());
        worker.start_pcap_capture(iface.to_string(), filter.to_string()).map_err(|e| ApplicationServicesError::CaptureError(e))?;

        Ok(id)
    }

    pub fn attach_vpn_tunnel(&mut self, fd: i32) -> Result<(), ApplicationServicesError> {
        let _id = {
            let mut sessions = self.sessions.lock().unwrap();
            sessions.start_live_session("vpn", "")?
        };

        let worker = CaptureWorker::new(self.sessions.clone(), self.security_settings.clone());
        worker.start_vpn_capture(fd).map_err(|e| ApplicationServicesError::CaptureError(e))?;

        Ok(())
    }

    pub fn update_threat_database(&mut self, _data: Vec<u8>) -> Result<(), ApplicationServicesError> {
        // TODO: Injetar no módulo security-dissector
        Ok(())
    }

    pub fn update_security_settings(&self, level: u8, smart_shield: bool, kill_switch: bool) -> Result<bool, ApplicationServicesError> {
        let mut settings = self.security_settings.lock().unwrap();
        settings.level = level;
        settings.smart_shield = smart_shield;
        settings.kill_switch = kill_switch;

        log::info!("MOTOR: Configurações de Segurança Atualizadas - Nível: {}, Shield: {}, KillSwitch: {}",
            level, smart_shield, kill_switch);

        Ok(true)
    }

    pub fn simulate_attack_verdict(&self, data: Vec<u8>) -> String {
        use security_dissector::SecurityDissector;
        use neural_intelligence::NeuralIntelligence;
        use traffic_valve::{TrafficValve, ValveAction};
        use protocol_engine::DataLinkMode;
        use core_types::PacketRecord;

        let dissector = SecurityDissector::new(DataLinkMode::RawIP);
        let mut brain = NeuralIntelligence::new();
        let valve = TrafficValve::new(2); // MODO ALTO

        let record = PacketRecord {
            packet_number: 1,
            file_offset: 0,
            interface_id: Some(0),
            timestamp_epoch_micros: Some(0),
            captured_length: data.len() as u32,
            original_length: data.len() as u32,
            link_type: Some(101),
            raw_data: data,
            read_warnings: Vec::new(),
        };

        if let Ok(parsed) = dissector.dissect(&record) {
            let _ = brain.analyze_behavior(&parsed);
            let threat_score = brain.get_threat_score();
            let violation = dissector.check_violation(&parsed);

            match valve.decide(threat_score, violation.is_some()) {
                ValveAction::Release => "RELEASE".into(),
                ValveAction::Drop => format!("DROP|{}", violation.unwrap_or("RISCO_NEURAL".into())),
                ValveAction::Hold => "HOLD".into(),
            }
        } else {
            "ERROR|DISSECTOR_FAIL".into()
        }
    }

    pub fn stop_capture(&mut self, session_id: &str) -> Result<SessionSnapshot, ApplicationServicesError> {
        Ok(self.sessions.lock().unwrap().stop_session(session_id)?)
    }

    pub fn get_latest_packets(&self, limit: usize) -> Result<Vec<use_cases::models::PacketListItem>, ApplicationServicesError> {
        let sessions = self.sessions.lock().unwrap();
        let session = sessions.active()?;

        let total = session.capture.packets.len();
        let start = if total > limit { total - limit } else { 0 };

        let items = session.capture.packets[start..]
            .iter()
            .map(|packet| use_cases::models::PacketListItem {
                packet_number: packet.packet_number,
                timestamp_epoch_micros: packet.timestamp_epoch_micros,
                highest_protocol: packet.highest_protocol.as_ref().map(|p| p.label()),
                summary: packet.summary.clone(),
            })
            .collect();

        Ok(items)
    }

    pub fn push_live_packet(&mut self, packet: core_types::ParsedPacket) -> Result<(), ApplicationServicesError> {
        Ok(self.sessions.lock().unwrap().push_packet(packet)?)
    }

    pub fn get_neural_snapshot(&self) -> Vec<neural_intelligence::NeuralLink3D> {
        self.neural_engine.lock().unwrap().get_neural_snapshot()
    }

    pub fn push_neural_event(&self, ip: &str, proto: &str, lat: f64, lon: f64, volume: u64) {
        self.neural_engine.lock().unwrap().push_transport_event(ip, proto, lat, lon, volume);
    }
}
