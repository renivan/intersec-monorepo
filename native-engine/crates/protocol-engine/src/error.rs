use std::fmt;

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum ProtocolEngineError {
    UnsupportedLinkType(u32),
    MalformedPacket(String),
}

impl fmt::Display for ProtocolEngineError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::UnsupportedLinkType(v) => write!(f, "unsupported link type: {v}"),
            Self::MalformedPacket(msg) => write!(f, "malformed packet: {msg}"),
        }
    }
}

impl std::error::Error for ProtocolEngineError {}
