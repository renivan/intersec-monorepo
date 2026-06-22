use std::fmt;

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum QueryEngineError {
    InvalidQuery(String),
}

impl fmt::Display for QueryEngineError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::InvalidQuery(msg) => write!(f, "invalid query: {msg}"),
        }
    }
}

impl std::error::Error for QueryEngineError {
    fn source(&self) -> Option<&(dyn std::error::Error + 'static)> {
        None
    }
}