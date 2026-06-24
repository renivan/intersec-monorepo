use std::fmt;

use capture_reader::CaptureReaderError;
use flow_engine::FlowEngineError;

#[derive(Debug)]
pub enum UseCasesError {
    CaptureReader(CaptureReaderError),
    ProtocolEngine(String),
    FlowEngine(FlowEngineError),
    PacketNotFound(u64),
    FlowNotFound(String),
    InvalidPagination(String),
}

impl fmt::Display for UseCasesError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::CaptureReader(err) => write!(f, "{err}"),
            Self::ProtocolEngine(err) => write!(f, "{err}"),
            Self::FlowEngine(err) => write!(f, "{err}"),
            Self::PacketNotFound(n) => write!(f, "packet not found: {n}"),
            Self::FlowNotFound(key) => write!(f, "flow not found: {key}"),
            Self::InvalidPagination(msg) => write!(f, "invalid pagination: {msg}"),
        }
    }
}

impl std::error::Error for UseCasesError {}

impl From<CaptureReaderError> for UseCasesError {
    fn from(value: CaptureReaderError) -> Self {
        Self::CaptureReader(value)
    }
}

impl From<FlowEngineError> for UseCasesError {
    fn from(value: FlowEngineError) -> Self {
        Self::FlowEngine(value)
    }
}
