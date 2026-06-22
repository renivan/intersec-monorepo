use std::fmt;

use ffi_contracts::FfiContractsError;

#[derive(Debug)]
pub enum AndroidAdapterError {
    Ffi(FfiContractsError),
}

impl fmt::Display for AndroidAdapterError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::Ffi(err) => write!(f, "{err}"),
        }
    }
}

impl std::error::Error for AndroidAdapterError {}

impl From<FfiContractsError> for AndroidAdapterError {
    fn from(value: FfiContractsError) -> Self {
        Self::Ffi(value)
    }
}