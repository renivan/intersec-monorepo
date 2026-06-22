use std::path::Path;

use crate::format_detector::detect_capture_format;
use crate::pcap::PcapReader;
use crate::pcapng::PcapNgReader;
use crate::source_adapter::{CaptureSource, FileCaptureSource};
use crate::{
    CaptureFormat, CaptureMetadata, CaptureReadResult, CaptureReaderError, PacketRecord,
    ReaderHandle, ValidationStatus,
};

pub enum CaptureReader {
    Pcap(PcapReader<FileCaptureSource>, ReaderHandle, CaptureMetadata),
    PcapNg(PcapNgReader<FileCaptureSource>, ReaderHandle, CaptureMetadata),
}

pub fn open_file_capture(path: impl AsRef<Path>) -> Result<(CaptureReader, CaptureReadResult), CaptureReaderError> {
    let mut source = FileCaptureSource::open(path.as_ref())?;
    let size = source.size()?;
    let header = source.read_exact_at(0, 12)?;
    let format = detect_capture_format(&header);
    let source_name = source.display_name();

    match format {
        CaptureFormat::Pcap => {
            let reader = PcapReader::open(source)?;
            let metadata = reader.metadata(size, source_name.clone());
            let handle = ReaderHandle { source_name, format };
            let result = CaptureReadResult {
                metadata: metadata.clone(),
                validation_status: ValidationStatus::Valid,
                warnings: vec![],
            };
            Ok((CaptureReader::Pcap(reader, handle, metadata), result))
        }
        CaptureFormat::PcapNg => {
            let mut reader = PcapNgReader::open(source)?;
            let metadata = reader.metadata(size, source_name.clone())?;
            let handle = ReaderHandle { source_name, format };
            let result = CaptureReadResult {
                metadata: metadata.clone(),
                validation_status: ValidationStatus::Valid,
                warnings: vec![],
            };
            Ok((CaptureReader::PcapNg(reader, handle, metadata), result))
        }
        CaptureFormat::Unknown => Err(CaptureReaderError::UnsupportedFormat),
    }
}

impl CaptureReader {
    pub fn metadata(&self) -> &CaptureMetadata {
        match self {
            Self::Pcap(_, _, metadata) => metadata,
            Self::PcapNg(_, _, metadata) => metadata,
        }
    }

    pub fn handle(&self) -> &ReaderHandle {
        match self {
            Self::Pcap(_, handle, _) => handle,
            Self::PcapNg(_, handle, _) => handle,
        }
    }

    pub fn next_packet(&mut self) -> Result<Option<PacketRecord>, CaptureReaderError> {
        match self {
            Self::Pcap(reader, _, _) => reader.next_packet(),
            Self::PcapNg(reader, _, _) => reader.next_packet(),
        }
    }
}
