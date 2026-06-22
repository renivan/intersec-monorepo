use crate::source_adapter::CaptureSource;
use crate::{
    CaptureFormat, CaptureMetadata, CaptureReaderError, CaptureWarning, Endianness, PacketRecord,
    TimestampPrecision,
};

#[derive(Debug, Clone)]
pub struct PcapGlobalHeader {
    pub endianness: Endianness,
    pub timestamp_precision: TimestampPrecision,
    pub version_major: u16,
    pub version_minor: u16,
    pub snaplen: u32,
    pub network: u32,
}

pub struct PcapReader<S: CaptureSource> {
    source: S,
    global_header: PcapGlobalHeader,
    next_packet_number: u64,
}

impl<S: CaptureSource> PcapReader<S> {
    pub fn open(mut source: S) -> Result<Self, CaptureReaderError> {
        source.seek_to(0)?;
        let header = source.read_exact_current(24)?;
        let global_header = parse_global_header(&header)?;
        Ok(Self {
            source,
            global_header,
            next_packet_number: 1,
        })
    }

    pub fn metadata(&self, file_size: u64, source_name: String) -> CaptureMetadata {
        CaptureMetadata {
            capture_id: source_name,
            format: CaptureFormat::Pcap,
            file_size,
            interface_count: 1,
            default_link_type: Some(self.global_header.network),
            snaplen: Some(self.global_header.snaplen),
            timestamp_precision: self.global_header.timestamp_precision,
            endianness: Some(self.global_header.endianness),
            version_major: Some(self.global_header.version_major),
            version_minor: Some(self.global_header.version_minor),
            open_warnings: Vec::new(),
            open_errors: Vec::new(),
        }
    }

    pub fn next_packet(&mut self) -> Result<Option<PacketRecord>, CaptureReaderError> {
        let offset = self.source.current_position()?;

        let header_bytes = match self.source.read_exact_current(16) {
            Ok(bytes) => bytes,
            Err(CaptureReaderError::UnexpectedEof) => return Ok(None),
            Err(err) => return Err(err),
        };

        let (ts_sec, ts_frac, incl_len, orig_len) =
            parse_packet_header(&header_bytes, self.global_header.endianness);

        let data = self.source.read_exact_current(incl_len as usize)?;

        let timestamp_epoch_micros = Some(match self.global_header.timestamp_precision {
            TimestampPrecision::Microseconds => ts_sec as u64 * 1_000_000 + ts_frac as u64,
            TimestampPrecision::Nanoseconds => ts_sec as u64 * 1_000_000 + (ts_frac as u64 / 1_000),
            TimestampPrecision::Unknown => ts_sec as u64 * 1_000_000 + ts_frac as u64,
        });

        let packet = PacketRecord {
            packet_number: self.next_packet_number,
            file_offset: offset,
            interface_id: Some(0),
            timestamp_epoch_micros,
            captured_length: incl_len,
            original_length: orig_len,
            link_type: Some(self.global_header.network),
            raw_data: data,
            read_warnings: Vec::<CaptureWarning>::new(),
        };

        self.next_packet_number += 1;
        Ok(Some(packet))
    }
}

pub fn parse_global_header(bytes: &[u8]) -> Result<PcapGlobalHeader, CaptureReaderError> {
    if bytes.len() != 24 {
        return Err(CaptureReaderError::CorruptedHeader(
            "invalid pcap global header length".into(),
        ));
    }

    let magic = [bytes[0], bytes[1], bytes[2], bytes[3]];

    let (endianness, precision) = match magic {
        [0xD4, 0xC3, 0xB2, 0xA1] => (Endianness::Little, TimestampPrecision::Microseconds),
        [0xA1, 0xB2, 0xC3, 0xD4] => (Endianness::Big, TimestampPrecision::Microseconds),
        [0x4D, 0x3C, 0xB2, 0xA1] => (Endianness::Little, TimestampPrecision::Nanoseconds),
        [0xA1, 0xB2, 0x3C, 0x4D] => (Endianness::Big, TimestampPrecision::Nanoseconds),
        _ => return Err(CaptureReaderError::UnsupportedFormat),
    };

    let version_major = read_u16(&bytes[4..6], endianness);
    let version_minor = read_u16(&bytes[6..8], endianness);
    let snaplen = read_u32(&bytes[16..20], endianness);
    let network = read_u32(&bytes[20..24], endianness);

    Ok(PcapGlobalHeader {
        endianness,
        timestamp_precision: precision,
        version_major,
        version_minor,
        snaplen,
        network,
    })
}

fn parse_packet_header(bytes: &[u8], endianness: Endianness) -> (u32, u32, u32, u32) {
    let ts_sec = read_u32(&bytes[0..4], endianness);
    let ts_frac = read_u32(&bytes[4..8], endianness);
    let incl_len = read_u32(&bytes[8..12], endianness);
    let orig_len = read_u32(&bytes[12..16], endianness);
    (ts_sec, ts_frac, incl_len, orig_len)
}

fn read_u16(bytes: &[u8], endianness: Endianness) -> u16 {
    let arr = [bytes[0], bytes[1]];
    match endianness {
        Endianness::Little => u16::from_le_bytes(arr),
        Endianness::Big => u16::from_be_bytes(arr),
    }
}

fn read_u32(bytes: &[u8], endianness: Endianness) -> u32 {
    let arr = [bytes[0], bytes[1], bytes[2], bytes[3]];
    match endianness {
        Endianness::Little => u32::from_le_bytes(arr),
        Endianness::Big => u32::from_be_bytes(arr),
    }
}
