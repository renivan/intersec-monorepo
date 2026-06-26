use std::fmt;

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum FlowEngineError {
    MissingAddressLayer,
    MissingTransportLayer,
    InvalidPort(String),
}

impl fmt::Display for FlowEngineError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::MissingAddressLayer => write!(f, "missing address layer"),
            Self::MissingTransportLayer => write!(f, "missing transport layer"),
            Self::InvalidPort(v) => write!(f, "invalid port: {v}"),
        }
    }
}

impl std::error::Error for FlowEngineError {}