use crate::source_adapter::CaptureSource;
use crate::{
    CaptureFormat, CaptureMetadata, CaptureReaderError, CaptureWarning, Endianness, PacketRecord,
    TimestampPrecision,
};

const BLOCK_TYPE_SECTION_HEADER: u32 = 0x0A0D0D0A;
const BLOCK_TYPE_INTERFACE_DESCRIPTION: u32 = 0x00000001;
const BLOCK_TYPE_SIMPLE_PACKET: u32 = 0x00000003;
const BLOCK_TYPE_ENHANCED_PACKET: u32 = 0x00000006;

const OPTION_ENDOFOPT: u16 = 0;
const OPTION_IF_TSRESOL: u16 = 9;

#[derive(Debug, Clone)]
struct InterfaceDescription {
    link_type: u32,
    snaplen: u32,
    ts_resolution_raw: Option<u8>,
}

impl InterfaceDescription {
    fn precision(&self) -> TimestampPrecision {
        match self.ts_resolution_raw {
            Some(6) | None => TimestampPrecision::Microseconds,
            Some(9) => TimestampPrecision::Nanoseconds,
            _ => TimestampPrecision::Unknown,
        }
    }
}

pub struct PcapNgReader<S: CaptureSource> {
    source: S,
    endianness: Endianness,
    interfaces: Vec<InterfaceDescription>,
    next_packet_number: u64,
}

impl<S: CaptureSource> PcapNgReader<S> {
    pub fn open(mut source: S) -> Result<Self, CaptureReaderError> {
        source.seek_to(0)?;
        let shb_prefix = source.read_exact_current(12)?;
        let block_type = u32::from_le_bytes([shb_prefix[0], shb_prefix[1], shb_prefix[2], shb_prefix[3]]);
        if block_type != BLOCK_TYPE_SECTION_HEADER {
            return Err(CaptureReaderError::CorruptedHeader(
                "pcapng section header block not found at offset 0".into(),
            ));
        }
        let block_total_length_le = u32::from_le_bytes([shb_prefix[4], shb_prefix[5], shb_prefix[6], shb_prefix[7]]);
        if block_total_length_le < 28 {
            return Err(CaptureReaderError::InvalidBlockLength(
                "pcapng section header shorter than minimum size".into(),
            ));
        }
        let endianness = match &shb_prefix[8..12] {
            [0x4D, 0x3C, 0x2B, 0x1A] => Endianness::Little,
            [0x1A, 0x2B, 0x3C, 0x4D] => Endianness::Big,
            _ => return Err(CaptureReaderError::CorruptedHeader("invalid pcapng byte-order magic".into())),
        };
        let remaining_shb = source.read_exact_current((block_total_length_le as usize) - 12)?;
        let trailer = &remaining_shb[(remaining_shb.len() - 4)..];
        let trailer_value = read_u32(trailer, endianness);
        if trailer_value != block_total_length_le {
            return Err(CaptureReaderError::InvalidBlockLength(
                "pcapng section header trailer length mismatch".into(),
            ));
        }
        Ok(Self { source, endianness, interfaces: Vec::new(), next_packet_number: 1 })
    }

    pub fn metadata(&mut self, file_size: u64, source_name: String) -> Result<CaptureMetadata, CaptureReaderError> {
        let current_pos = self.source.current_position()?;
        let interfaces = scan_interfaces(&mut self.source, self.endianness)?;
        self.source.seek_to(current_pos)?;
        if self.interfaces.is_empty() {
            self.interfaces = interfaces.clone();
        }
        let first_interface = interfaces.first();
        Ok(CaptureMetadata {
            capture_id: source_name,
            format: CaptureFormat::PcapNg,
            file_size,
            interface_count: interfaces.len() as u32,
            default_link_type: first_interface.map(|i| i.link_type),
            snaplen: first_interface.map(|i| i.snaplen),
            timestamp_precision: first_interface.map(|i| i.precision()).unwrap_or(TimestampPrecision::Unknown),
            endianness: Some(self.endianness),
            version_major: None,
            version_minor: None,
            open_warnings: vec![],
            open_errors: vec![],
        })
    }

    pub fn next_packet(&mut self) -> Result<Option<PacketRecord>, CaptureReaderError> {
        loop {
            let offset = self.source.current_position()?;
            let header = match self.source.read_exact_current(8) {
                Ok(bytes) => bytes,
                Err(CaptureReaderError::UnexpectedEof) => return Ok(None),
                Err(err) => return Err(err),
            };
            let block_type = read_u32(&header[0..4], self.endianness);
            let block_total_length = read_u32(&header[4..8], self.endianness);
            if block_total_length < 12 {
                return Err(CaptureReaderError::InvalidBlockLength(format!(
                    "pcapng block at offset {offset} shorter than minimum block size"
                )));
            }
            let remaining = self.source.read_exact_current((block_total_length as usize) - 8)?;
            let trailer = &remaining[(remaining.len() - 4)..];
            let trailer_value = read_u32(trailer, self.endianness);
            if trailer_value != block_total_length {
                return Err(CaptureReaderError::InvalidBlockLength(format!(
                    "pcapng block at offset {offset} has mismatched trailer length"
                )));
            }
            let body = &remaining[..(remaining.len() - 4)];
            match block_type {
                BLOCK_TYPE_SECTION_HEADER => continue,
                BLOCK_TYPE_INTERFACE_DESCRIPTION => {
                    self.interfaces.push(parse_interface_description_block(body, self.endianness)?);
                    continue;
                }
                BLOCK_TYPE_ENHANCED_PACKET => return Ok(Some(self.parse_enhanced_packet_block(offset, body)?)),
                BLOCK_TYPE_SIMPLE_PACKET => return Ok(Some(self.parse_simple_packet_block(offset, body)?)),
                _ => continue,
            }
        }
    }

    fn parse_enhanced_packet_block(&mut self, offset: u64, body: &[u8]) -> Result<PacketRecord, CaptureReaderError> {
        if body.len() < 20 {
            return Err(CaptureReaderError::CorruptedHeader(
                "enhanced packet block is shorter than minimum body".into(),
            ));
        }
        let interface_id = read_u32(&body[0..4], self.endianness);
        let ts_high = read_u32(&body[4..8], self.endianness) as u64;
        let ts_low = read_u32(&body[8..12], self.endianness) as u64;
        let captured_length = read_u32(&body[12..16], self.endianness);
        let original_length = read_u32(&body[16..20], self.endianness);
        let iface = self.interfaces.get(interface_id as usize);
        let link_type = iface.map(|i| i.link_type);
        let timestamp_epoch_micros = normalize_pcapng_timestamp_to_micros((ts_high << 32) | ts_low, iface.and_then(|i| i.ts_resolution_raw));
        let payload_start = 20;
        let padded_payload_length = pad_to_32(captured_length as usize);
        let payload_end = payload_start + padded_payload_length;
        if body.len() < payload_end {
            return Err(CaptureReaderError::UnexpectedEof);
        }
        let raw_data = body[payload_start..payload_start + captured_length as usize].to_vec();
        let packet = PacketRecord {
            packet_number: self.next_packet_number,
            file_offset: offset,
            interface_id: Some(interface_id),
            timestamp_epoch_micros,
            captured_length,
            original_length,
            link_type,
            raw_data,
            read_warnings: Vec::<CaptureWarning>::new(),
        };
        self.next_packet_number += 1;
        Ok(packet)
    }

    fn parse_simple_packet_block(&mut self, offset: u64, body: &[u8]) -> Result<PacketRecord, CaptureReaderError> {
        if body.len() < 4 {
            return Err(CaptureReaderError::CorruptedHeader(
                "simple packet block is shorter than minimum body".into(),
            ));
        }
        let original_length = read_u32(&body[0..4], self.endianness);
        let iface = self.interfaces.first();
        let link_type = iface.map(|i| i.link_type);
        let available_payload = body.len().saturating_sub(4);
        let captured_length = std::cmp::min(original_length as usize, available_payload) as u32;
        let raw_data = body[4..4 + captured_length as usize].to_vec();
        let packet = PacketRecord {
            packet_number: self.next_packet_number,
            file_offset: offset,
            interface_id: Some(0),
            timestamp_epoch_micros: None,
            captured_length,
            original_length,
            link_type,
            raw_data,
            read_warnings: Vec::<CaptureWarning>::new(),
        };
        self.next_packet_number += 1;
        Ok(packet)
    }
}

fn scan_interfaces<S: CaptureSource>(source: &mut S, endianness: Endianness) -> Result<Vec<InterfaceDescription>, CaptureReaderError> {
    let start = source.current_position()?;
    let mut interfaces = Vec::<InterfaceDescription>::new();
    loop {
        let header = match source.read_exact_current(8) {
            Ok(bytes) => bytes,
            Err(CaptureReaderError::UnexpectedEof) => break,
            Err(err) => return Err(err),
        };
        let block_type = read_u32(&header[0..4], endianness);
        let block_total_length = read_u32(&header[4..8], endianness);
        if block_total_length < 12 {
            return Err(CaptureReaderError::InvalidBlockLength(
                "pcapng block shorter than minimum size during interface scan".into(),
            ));
        }
        let remaining = source.read_exact_current((block_total_length as usize) - 8)?;
        let trailer = &remaining[(remaining.len() - 4)..];
        let trailer_value = read_u32(trailer, endianness);
        if trailer_value != block_total_length {
            return Err(CaptureReaderError::InvalidBlockLength(
                "pcapng block has mismatched trailer length during interface scan".into(),
            ));
        }
        let body = &remaining[..(remaining.len() - 4)];
        if block_type == BLOCK_TYPE_INTERFACE_DESCRIPTION {
            interfaces.push(parse_interface_description_block(body, endianness)?);
        }
    }
    source.seek_to(start)?;
    Ok(interfaces)
}

fn parse_interface_description_block(body: &[u8], endianness: Endianness) -> Result<InterfaceDescription, CaptureReaderError> {
    if body.len() < 8 {
        return Err(CaptureReaderError::CorruptedHeader(
            "interface description block is shorter than minimum body".into(),
        ));
    }
    let link_type = read_u16(&body[0..2], endianness) as u32;
    let snaplen = read_u32(&body[4..8], endianness);
    let ts_resolution_raw = parse_ts_resolution_option(&body[8..], endianness)?;
    Ok(InterfaceDescription { link_type, snaplen, ts_resolution_raw })
}

fn parse_ts_resolution_option(options: &[u8], endianness: Endianness) -> Result<Option<u8>, CaptureReaderError> {
    let mut offset = 0_usize;
    while offset + 4 <= options.len() {
        let code = read_u16(&options[offset..offset + 2], endianness);
        let len = read_u16(&options[offset + 2..offset + 4], endianness) as usize;
        offset += 4;
        if code == OPTION_ENDOFOPT {
            break;
        }
        if offset + len > options.len() {
            return Err(CaptureReaderError::UnexpectedEof);
        }
        let value = &options[offset..offset + len];
        let padded_len = pad_to_32(len);
        offset += padded_len;
        if code == OPTION_IF_TSRESOL && !value.is_empty() {
            return Ok(Some(value[0]));
        }
    }
    Ok(None)
}

fn normalize_pcapng_timestamp_to_micros(raw: u64, resolution_raw: Option<u8>) -> Option<u64> {
    match resolution_raw.unwrap_or(6) {
        6 => Some(raw),
        9 => Some(raw / 1_000),
        _ => None,
    }
}

fn pad_to_32(len: usize) -> usize {
    (len + 3) & !3
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
