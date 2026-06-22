pub mod error;
pub mod format_detector;
pub mod pcap;
pub mod pcapng;
pub mod reader;
pub mod source_adapter;

pub use core_types::{
    CaptureFormat, CaptureMetadata, CaptureReadResult, CaptureReaderError, CaptureWarning,
    Endianness, PacketRecord, ReaderHandle, TimestampPrecision, ValidationStatus,
};

pub use reader::{open_file_capture, CaptureReader};
