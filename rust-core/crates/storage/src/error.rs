use std::fmt;

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum StorageError {
    SessionNotFound(String),
    DuplicateSession(String),
    InvalidInput(String),
}

impl fmt::Display for StorageError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::SessionNotFound(id) => write!(f, "session not found: {id}"),
            Self::DuplicateSession(id) => write!(f, "duplicate session: {id}"),
            Self::InvalidInput(msg) => write!(f, "invalid input: {msg}"),
        }
    }
}

impl std::error::Error for StorageError {}