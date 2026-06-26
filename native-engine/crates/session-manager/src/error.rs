use std::fmt;

use use_cases::UseCasesError;

#[derive(Debug)]
pub enum SessionManagerError {
    UseCases(UseCasesError),
    NoActiveSession,
    PacketNotFound(u64),
    FlowNotFound(String),
}

impl fmt::Display for SessionManagerError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::UseCases(err) => write!(f, "{err}"),
            Self::NoActiveSession => write!(f, "no active session"),
            Self::PacketNotFound(n) => write!(f, "packet not found in active session: {n}"),
            Self::FlowNotFound(v) => write!(f, "flow not found in active session: {v}"),
        }
    }
}

impl std::error::Error for SessionManagerError {}

impl From<UseCasesError> for SessionManagerError {
    fn from(value: UseCasesError) -> Self {
        Self::UseCases(value)
    }
}