use std::fmt;

use application_services::ApplicationServicesError;
use storage::StorageError;

#[derive(Debug)]
pub enum FacadeApiError {
    ApplicationServices(ApplicationServicesError),
    Storage(StorageError),
}

impl fmt::Display for FacadeApiError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::ApplicationServices(err) => write!(f, "{err}"),
            Self::Storage(err) => write!(f, "{err}"),
        }
    }
}

impl std::error::Error for FacadeApiError {}

impl From<ApplicationServicesError> for FacadeApiError {
    fn from(value: ApplicationServicesError) -> Self {
        Self::ApplicationServices(value)
    }
}

impl From<StorageError> for FacadeApiError {
    fn from(value: StorageError) -> Self {
        Self::Storage(value)
    }
}