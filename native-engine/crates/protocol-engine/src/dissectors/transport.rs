use core_types::{ProtocolField, ProtocolKind, ProtocolNode};

use crate::ProtocolEngineError;

pub struct TcpSegment<'a> {
    pub src_port: u16,
    pub dst_port: u16,
    pub payload: &'a [u8],
    pub node: ProtocolNode,
}

pub struct UdpDatagram<'a> {
    pub src_port: u16,
    pub dst_port: u16,
    pub payload: &'a [u8],
    pub node: ProtocolNode,
}

pub fn parse_tcp_segment(data: &[u8]) -> Result<TcpSegment<'_>, ProtocolEngineError> {
    if data.len() < 20 {
        return Err(ProtocolEngineError::MalformedPacket("tcp header shorter than 20 bytes".into()));
    }
    let src_port = u16::from_be_bytes([data[0], data[1]]);
    let dst_port = u16::from_be_bytes([data[2], data[3]]);
    let data_offset_words = data[12] >> 4;
    if data_offset_words < 5 {
        return Err(ProtocolEngineError::MalformedPacket("invalid tcp data offset".into()));
    }
    let header_len = (data_offset_words as usize) * 4;
    if data.len() < header_len {
        return Err(ProtocolEngineError::MalformedPacket("tcp shorter than declared data offset".into()));
    }
    let flags = data[13];
    let payload = &data[header_len..];
    let node = ProtocolNode {
        kind: ProtocolKind::Tcp,
        label: format!("TCP {src_port} -> {dst_port}"),
        fields: vec![
            ProtocolField { name: "src_port".into(), value: src_port.to_string() },
            ProtocolField { name: "dst_port".into(), value: dst_port.to_string() },
            ProtocolField { name: "flags".into(), value: format!("0x{flags:02X}") },
        ],
    };
    Ok(TcpSegment { src_port, dst_port, payload, node })
}

pub fn parse_udp_datagram(data: &[u8]) -> Result<UdpDatagram<'_>, ProtocolEngineError> {
    if data.len() < 8 {
        return Err(ProtocolEngineError::MalformedPacket("udp header shorter than 8 bytes".into()));
    }
    let src_port = u16::from_be_bytes([data[0], data[1]]);
    let dst_port = u16::from_be_bytes([data[2], data[3]]);
    let length = u16::from_be_bytes([data[4], data[5]]) as usize;
    let payload_end = std::cmp::min(length, data.len());
    let payload = if payload_end >= 8 { &data[8..payload_end] } else { &[] };
    let node = ProtocolNode {
        kind: ProtocolKind::Udp,
        label: format!("UDP {src_port} -> {dst_port}"),
        fields: vec![
            ProtocolField { name: "src_port".into(), value: src_port.to_string() },
            ProtocolField { name: "dst_port".into(), value: dst_port.to_string() },
            ProtocolField { name: "length".into(), value: length.to_string() },
        ],
    };
    Ok(UdpDatagram { src_port, dst_port, payload, node })
}
