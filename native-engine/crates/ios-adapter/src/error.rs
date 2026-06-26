use std::fmt;

use ffi_contracts::FfiContractsError;

#[derive(Debug)]
pub enum IosAdapterError {
    Ffi(FfiContractsError),
}

impl fmt::Display for IosAdapterError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::Ffi(err) => write!(f, "{err}"),
        }
    }
}

impl std::error::Error for IosAdapterError {}

impl From<FfiContractsError> for IosAdapterError {
    fn from(value: FfiContractsError) -> Self {
        Self::Ffi(value)
    }
}