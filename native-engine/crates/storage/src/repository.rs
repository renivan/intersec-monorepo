use crate::error::StorageError;
use crate::models::{StorageStats, StoredSessionRecord};

pub trait StorageRepository {
    fn save_session(&mut self, record: StoredSessionRecord) -> Result<(), StorageError>;

    fn upsert_session(&mut self, record: StoredSessionRecord) -> Result<(), StorageError>;

    fn get_session(&self, session_id: &str) -> Result<StoredSessionRecord, StorageError>;

    fn list_sessions(&self) -> Result<Vec<StoredSessionRecord>, StorageError>;

    fn delete_session(&mut self, session_id: &str) -> Result<(), StorageError>;

    fn stats(&self) -> Result<StorageStats, StorageError>;
}