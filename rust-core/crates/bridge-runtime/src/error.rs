use std::fmt;

use android_adapter::AndroidAdapterError;
use ios_adapter::IosAdapterError;

#[derive(Debug)]
pub enum BridgeRuntimeError {
    Android(AndroidAdapterError),
    Ios(IosAdapterError),
    InvalidRuntimeState(String),
}

impl fmt::Display for BridgeRuntimeError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::Android(err) => write!(f, "{err}"),
            Self::Ios(err) => write!(f, "{err}"),
            Self::InvalidRuntimeState(msg) => write!(f, "invalid runtime state: {msg}"),
        }
    }
}

impl std::error::Error for BridgeRuntimeError {}

impl From<AndroidAdapterError> for BridgeRuntimeError {
    fn from(value: AndroidAdapterError) -> Self {
        Self::Android(value)
    }
}

impl From<IosAdapterError> for BridgeRuntimeError {
    fn from(value: IosAdapterError) -> Self {
        Self::Ios(value)
    }
}