use std::fmt;

use query_engine::QueryEngineError;
use session_manager::SessionManagerError;
use storage::StorageError;
use use_cases::UseCasesError;

#[derive(Debug)]
pub enum ApplicationServicesError {
    SessionManager(SessionManagerError),
    Storage(StorageError),
    QueryEngine(QueryEngineError),
    UseCases(UseCasesError),
    CaptureError(String),
}

impl fmt::Display for ApplicationServicesError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::SessionManager(err) => write!(f, "{err}"),
            Self::Storage(err) => write!(f, "{err}"),
            Self::QueryEngine(err) => write!(f, "{err}"),
            Self::UseCases(err) => write!(f, "{err}"),
            Self::CaptureError(err) => write!(f, "Capture error: {err}"),
        }
    }
}

impl std::error::Error for ApplicationServicesError {}

impl From<SessionManagerError> for ApplicationServicesError {
    fn from(value: SessionManagerError) -> Self {
        Self::SessionManager(value)
    }
}

impl From<StorageError> for ApplicationServicesError {
    fn from(value: StorageError) -> Self {
        Self::Storage(value)
    }
}

impl From<QueryEngineError> for ApplicationServicesError {
    fn from(value: QueryEngineError) -> Self {
        Self::QueryEngine(value)
    }
}

impl From<UseCasesError> for ApplicationServicesError {
    fn from(value: UseCasesError) -> Self {
        Self::UseCases(value)
    }
}