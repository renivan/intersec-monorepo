use std::collections::BTreeMap;

use crate::error::StorageError;
use crate::models::{StorageStats, StoredSessionRecord};
use crate::repository::StorageRepository;

#[derive(Default)]
pub struct InMemoryStorage {
    sessions: BTreeMap<String, StoredSessionRecord>,
}

impl InMemoryStorage {
    pub fn new() -> Self {
        Self {
            sessions: BTreeMap::new(),
        }
    }

    fn validate_record(record: &StoredSessionRecord) -> Result<(), StorageError> {
        if record.snapshot.session_id.trim().is_empty() {
            return Err(StorageError::InvalidInput(
                "session_id must not be empty".into(),
            ));
        }

        if record.snapshot.source_name.trim().is_empty() {
            return Err(StorageError::InvalidInput(
                "source_name must not be empty".into(),
            ));
        }

        Ok(())
    }
}

impl StorageRepository for InMemoryStorage {
    fn save_session(&mut self, record: StoredSessionRecord) -> Result<(), StorageError> {
        Self::validate_record(&record)?;

        let key = record.snapshot.session_id.clone();
        if self.sessions.contains_key(&key) {
            return Err(StorageError::DuplicateSession(key));
        }

        self.sessions.insert(key, record);
        Ok(())
    }

    fn upsert_session(&mut self, record: StoredSessionRecord) -> Result<(), StorageError> {
        Self::validate_record(&record)?;

        let key = record.snapshot.session_id.clone();
        self.sessions.insert(key, record);
        Ok(())
    }

    fn get_session(&self, session_id: &str) -> Result<StoredSessionRecord, StorageError> {
        self.sessions
            .get(session_id)
            .cloned()
            .ok_or_else(|| StorageError::SessionNotFound(session_id.into()))
    }

    fn list_sessions(&self) -> Result<Vec<StoredSessionRecord>, StorageError> {
        Ok(self.sessions.values().cloned().collect())
    }

    fn delete_session(&mut self, session_id: &str) -> Result<(), StorageError> {
        if self.sessions.remove(session_id).is_none() {
            return Err(StorageError::SessionNotFound(session_id.into()));
        }

        Ok(())
    }

    fn stats(&self) -> Result<StorageStats, StorageError> {
        let total_sessions = self.sessions.len();
        let total_packets_indexed = self
            .sessions
            .values()
            .map(|record| record.snapshot.total_packets)
            .sum();

        let total_flows_indexed = self
            .sessions
            .values()
            .map(|record| record.snapshot.total_flows)
            .sum();

        Ok(StorageStats {
            total_sessions,
            total_packets_indexed,
            total_flows_indexed,
        })
    }
}