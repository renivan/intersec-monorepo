use flow_engine::FlowKey;
use use_cases::{open_capture, CaptureContext};

use crate::error::SessionManagerError;
use crate::models::{SessionId, SessionSnapshot, SessionState};

#[derive(Default)]
pub struct SessionManager {
    active: Option<SessionState>,
    sequence: u64,
}

impl SessionManager {
    pub fn new() -> Self {
        Self {
            active: None,
            sequence: 0,
        }
    }

    pub fn open_capture_session(
        &mut self,
        path: &str,
        now_epoch_micros: u64,
    ) -> Result<&SessionState, SessionManagerError> {
        let ctx = open_capture(path)?;
        self.sequence += 1;

        let session = SessionState {
            session_id: SessionId(format!("session-{}", self.sequence)),
            capture: ctx,
            active_packet_number: None,
            active_flow_key: None,
            search_text: None,
            applied_filters: Vec::new(),
            created_at_epoch_micros: now_epoch_micros,
            updated_at_epoch_micros: now_epoch_micros,
        };

        self.active = Some(session);
        self.active()
    }

    pub fn load_context(
        &mut self,
        context: CaptureContext,
        now_epoch_micros: u64,
    ) -> Result<&SessionState, SessionManagerError> {
        self.sequence += 1;

        let session = SessionState {
            session_id: SessionId(format!("session-{}", self.sequence)),
            capture: context,
            active_packet_number: None,
            active_flow_key: None,
            search_text: None,
            applied_filters: Vec::new(),
            created_at_epoch_micros: now_epoch_micros,
            updated_at_epoch_micros: now_epoch_micros,
        };

        self.active = Some(session);
        self.active()
    }

    pub fn close_active(&mut self) {
        self.active = None;
    }

    pub fn active(&self) -> Result<&SessionState, SessionManagerError> {
        self.active.as_ref().ok_or(SessionManagerError::NoActiveSession)
    }

    pub fn active_mut(&mut self) -> Result<&mut SessionState, SessionManagerError> {
        self.active
            .as_mut()
            .ok_or(SessionManagerError::NoActiveSession)
    }

    pub fn select_packet(
        &mut self,
        packet_number: u64,
        now_epoch_micros: u64,
    ) -> Result<(), SessionManagerError> {
        let session = self.active_mut()?;

        let exists = session
            .capture
            .packets
            .iter()
            .any(|packet| packet.packet_number == packet_number);

        if !exists {
            return Err(SessionManagerError::PacketNotFound(packet_number));
        }

        session.active_packet_number = Some(packet_number);
        session.updated_at_epoch_micros = now_epoch_micros;
        Ok(())
    }

    pub fn select_flow(
        &mut self,
        key: &FlowKey,
        now_epoch_micros: u64,
    ) -> Result<(), SessionManagerError> {
        let session = self.active_mut()?;

        let exists = session.capture.flows.iter().any(|flow| &flow.key == key);
        if !exists {
            return Err(SessionManagerError::FlowNotFound(format!("{key:?}")));
        }

        session.active_flow_key = Some(key.clone());
        session.updated_at_epoch_micros = now_epoch_micros;
        Ok(())
    }

    pub fn clear_selection(&mut self, now_epoch_micros: u64) -> Result<(), SessionManagerError> {
        let session = self.active_mut()?;
        session.active_packet_number = None;
        session.active_flow_key = None;
        session.updated_at_epoch_micros = now_epoch_micros;
        Ok(())
    }

    pub fn set_search_text(
        &mut self,
        value: Option<String>,
        now_epoch_micros: u64,
    ) -> Result<(), SessionManagerError> {
        let session = self.active_mut()?;
        session.search_text = value;
        session.updated_at_epoch_micros = now_epoch_micros;
        Ok(())
    }

    pub fn set_filters(
        &mut self,
        values: Vec<String>,
        now_epoch_micros: u64,
    ) -> Result<(), SessionManagerError> {
        let session = self.active_mut()?;
        session.applied_filters = values;
        session.updated_at_epoch_micros = now_epoch_micros;
        Ok(())
    }

    pub fn snapshot(&self) -> Result<SessionSnapshot, SessionManagerError> {
        let session = self.active()?;

        let active_flow_label = session
            .active_flow_key
            .as_ref()
            .and_then(|key| {
                session
                    .capture
                    .flows
                    .iter()
                    .find(|flow| &flow.key == key)
                    .map(|flow| flow.summary().label)
            });

        Ok(SessionSnapshot {
            session_id: session.session_id.0.clone(),
            source_name: session.capture.source_name.clone(),
            total_packets: session.capture.packets.len(),
            total_flows: session.capture.flows.len(),
            active_packet_number: session.active_packet_number,
            active_flow_label,
            search_text: session.search_text.clone(),
            applied_filters: session.applied_filters.clone(),
            created_at_epoch_micros: session.created_at_epoch_micros,
            updated_at_epoch_micros: session.updated_at_epoch_micros,
        })
    }

    pub fn start_live_session(&mut self, iface: &str, _filter: &str) -> Result<String, SessionManagerError> {
        self.sequence += 1;
        let now = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_micros() as u64;

        let session = SessionState {
            session_id: SessionId(format!("live-{}", self.sequence)),
            capture: CaptureContext {
                source_name: format!("Live: {}", iface),
                metadata: core_types::CaptureMetadata::default(),
                packets: Vec::new(),
                flows: Vec::new(),
            },
            active_packet_number: None,
            active_flow_key: None,
            search_text: None,
            applied_filters: Vec::new(),
            created_at_epoch_micros: now,
            updated_at_epoch_micros: now,
        };

        let id = session.session_id.0.clone();
        self.active = Some(session);
        Ok(id)
    }

    pub fn push_packet(&mut self, packet: core_types::ParsedPacket) -> Result<(), SessionManagerError> {
        let session = self.active_mut()?;
        session.capture.packets.push(packet);
        // Limita buffer em memória para não estourar RAM do celular
        if session.capture.packets.len() > 1000 {
            session.capture.packets.remove(0);
        }
        Ok(())
    }

    pub fn persist_session(&mut self, _tags: &str, _notes: Option<String>) -> Result<(), SessionManagerError> {
        // Mock de persistência para teste de fluxo.
        // No futuro, aqui chamaremos o crate `storage` para gravar em SQLite ou Binary.
        log::info!("SessionManager: Sincronizando dados para o disco...");
        Ok(())
    }

    pub fn stop_session(&mut self, session_id: &str) -> Result<SessionSnapshot, SessionManagerError> {
        let snap = self.snapshot()?;
        if snap.session_id == session_id {
            // No futuro, aqui fecharíamos o handle do pcap
            return Ok(snap);
        }
        Err(SessionManagerError::NoActiveSession)
    }
}