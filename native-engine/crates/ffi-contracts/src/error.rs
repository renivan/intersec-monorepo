use std::fmt;

use mobile_bridge::MobileBridgeError;

#[derive(Debug)]
pub enum FfiContractsError {
    Bridge(MobileBridgeError),
    InvalidInput(String),
}

impl fmt::Display for FfiContractsError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::Bridge(err) => write!(f, "{err}"),
            Self::InvalidInput(msg) => write!(f, "invalid ffi input: {msg}"),
        }
    }
}

impl std::error::Error for FfiContractsError {}

impl From<MobileBridgeError> for FfiContractsError {
    fn from(value: MobileBridgeError) -> Self {
        Self::Bridge(value)
    }
}