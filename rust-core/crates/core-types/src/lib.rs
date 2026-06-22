use std::fmt;

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum CaptureFormat {
    Pcap,
    PcapNg,
    Unknown,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Endianness {
    Little,
    Big,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum TimestampPrecision {
    Microseconds,
    Nanoseconds,
    Unknown,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum ValidationStatus {
    Valid,
    ValidWithWarnings,
    Invalid,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum CaptureWarning {
    TruncatedPacket,
    PartialReadRecovery,
    MalformedNonCriticalBlock,
    TimestampPrecisionFallback,
    UnsupportedButRecoverable(String),
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct CaptureMetadata {
    pub capture_id: String,
    pub format: CaptureFormat,
    pub file_size: u64,
    pub interface_count: u32,
    pub default_link_type: Option<u32>,
    pub snaplen: Option<u32>,
    pub timestamp_precision: TimestampPrecision,
    pub endianness: Option<Endianness>,
    pub version_major: Option<u16>,
    pub version_minor: Option<u16>,
    pub open_warnings: Vec<CaptureWarning>,
    pub open_errors: Vec<String>,
}

impl Default for CaptureMetadata {
    fn default() -> Self {
        Self {
            capture_id: "capture-unknown".into(),
            format: CaptureFormat::Unknown,
            file_size: 0,
            interface_count: 0,
            default_link_type: None,
            snaplen: None,
            timestamp_precision: TimestampPrecision::Unknown,
            endianness: None,
            version_major: None,
            version_minor: None,
            open_warnings: Vec::new(),
            open_errors: Vec::new(),
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct PacketRecord {
    pub packet_number: u64,
    pub file_offset: u64,
    pub interface_id: Option<u32>,
    pub timestamp_epoch_micros: Option<u64>,
    pub captured_length: u32,
    pub original_length: u32,
    pub link_type: Option<u32>,
    pub raw_data: Vec<u8>,
    pub read_warnings: Vec<CaptureWarning>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct CaptureReadResult {
    pub metadata: CaptureMetadata,
    pub validation_status: ValidationStatus,
    pub warnings: Vec<CaptureWarning>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ReaderHandle {
    pub source_name: String,
    pub format: CaptureFormat,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum CaptureReaderError {
    UnsupportedFormat,
    CorruptedHeader(String),
    UnexpectedEof,
    InvalidBlockLength(String),
    UnknownByteOrder,
    ReadPermissionError(String),
    SourceUnavailable(String),
    UnsupportedFeature(String),
}

impl fmt::Display for CaptureReaderError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::UnsupportedFormat => write!(f, "unsupported capture format"),
            Self::CorruptedHeader(msg) => write!(f, "corrupted header: {msg}"),
            Self::UnexpectedEof => write!(f, "unexpected end of file"),
            Self::InvalidBlockLength(msg) => write!(f, "invalid block length: {msg}"),
            Self::UnknownByteOrder => write!(f, "unknown byte order"),
            Self::ReadPermissionError(msg) => write!(f, "read permission error: {msg}"),
            Self::SourceUnavailable(msg) => write!(f, "source unavailable: {msg}"),
            Self::UnsupportedFeature(msg) => write!(f, "unsupported feature: {msg}"),
        }
    }
}

impl std::error::Error for CaptureReaderError {}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum ProtocolKind {
    Ethernet,
    Loopback,
    Arp,
    Ipv4,
    Ipv6,
    Tcp,
    Udp,
    Icmp,
    Icmpv6,
    Dns,
    Tls,
    Http,
    Dhcp,
    Ntp,
    Quic,
    Ssh,
    Unknown(String),
}

impl ProtocolKind {
    pub fn label(&self) -> String {
        match self {
            Self::Ethernet => "Ethernet".into(),
            Self::Loopback => "Loopback".into(),
            Self::Arp => "ARP".into(),
            Self::Ipv4 => "IPv4".into(),
            Self::Ipv6 => "IPv6".into(),
            Self::Tcp => "TCP".into(),
            Self::Udp => "UDP".into(),
            Self::Icmp => "ICMP".into(),
            Self::Icmpv6 => "ICMPv6".into(),
            Self::Dns => "DNS".into(),
            Self::Tls => "TLS".into(),
            Self::Http => "HTTP".into(),
            Self::Dhcp => "DHCP".into(),
            Self::Ntp => "NTP".into(),
            Self::Quic => "QUIC".into(),
            Self::Ssh => "SSH".into(),
            Self::Unknown(value) => format!("Unknown({value})"),
        }
    }
}
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ProtocolField {
    pub name: String,
    pub value: String,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ProtocolNode {
    pub kind: ProtocolKind,
    pub label: String,
    pub fields: Vec<ProtocolField>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ParsedPacket {
    pub packet_number: u64,
    pub timestamp_epoch_micros: Option<u64>,
    pub link_type: Option<u32>,
    pub highest_protocol: Option<ProtocolKind>,
    pub nodes: Vec<ProtocolNode>,
    pub summary: String,
    pub warnings: Vec<String>,
}
