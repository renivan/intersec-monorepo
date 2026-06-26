use std::fmt;

use facade_api::FacadeApiError;

#[derive(Debug)]
pub enum MobileBridgeError {
    Facade(FacadeApiError),
}

impl fmt::Display for MobileBridgeError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::Facade(err) => write!(f, "{err}"),
        }
    }
}

impl std::error::Error for MobileBridgeError {}

impl From<FacadeApiError> for MobileBridgeError {
    fn from(value: FacadeApiError) -> Self {
        Self::Facade(value)
    }
}