use std::fs::File;
use std::io::{Read, Seek, SeekFrom};
use std::path::{Path, PathBuf};

use crate::CaptureReaderError;

pub trait CaptureSource {
    fn display_name(&self) -> String;
    fn size(&self) -> Result<u64, CaptureReaderError>;
    fn read_exact_at(&mut self, offset: u64, size: usize) -> Result<Vec<u8>, CaptureReaderError>;
    fn seek_to(&mut self, offset: u64) -> Result<(), CaptureReaderError>;
    fn read_exact_current(&mut self, size: usize) -> Result<Vec<u8>, CaptureReaderError>;
    fn current_position(&mut self) -> Result<u64, CaptureReaderError>;
}

pub struct FileCaptureSource {
    path: PathBuf,
    file: File,
}

impl FileCaptureSource {
    pub fn open(path: impl AsRef<Path>) -> Result<Self, CaptureReaderError> {
        let path_buf = path.as_ref().to_path_buf();
        let file = File::open(&path_buf)
            .map_err(|e| CaptureReaderError::SourceUnavailable(e.to_string()))?;
        Ok(Self { path: path_buf, file })
    }
}

impl CaptureSource for FileCaptureSource {
    fn display_name(&self) -> String {
        self.path
            .file_name()
            .map(|s| s.to_string_lossy().to_string())
            .unwrap_or_else(|| self.path.display().to_string())
    }

    fn size(&self) -> Result<u64, CaptureReaderError> {
        std::fs::metadata(&self.path)
            .map(|m| m.len())
            .map_err(|e| CaptureReaderError::SourceUnavailable(e.to_string()))
    }

    fn read_exact_at(&mut self, offset: u64, size: usize) -> Result<Vec<u8>, CaptureReaderError> {
        self.file
            .seek(SeekFrom::Start(offset))
            .map_err(|e| CaptureReaderError::SourceUnavailable(e.to_string()))?;
        self.read_exact_current(size)
    }

    fn seek_to(&mut self, offset: u64) -> Result<(), CaptureReaderError> {
        self.file
            .seek(SeekFrom::Start(offset))
            .map(|_| ())
            .map_err(|e| CaptureReaderError::SourceUnavailable(e.to_string()))
    }

    fn read_exact_current(&mut self, size: usize) -> Result<Vec<u8>, CaptureReaderError> {
        let mut buf = vec![0_u8; size];
        self.file
            .read_exact(&mut buf)
            .map_err(|_| CaptureReaderError::UnexpectedEof)?;
        Ok(buf)
    }

    fn current_position(&mut self) -> Result<u64, CaptureReaderError> {
        self.file
            .stream_position()
            .map_err(|e| CaptureReaderError::SourceUnavailable(e.to_string()))
    }
}
