use core_types::{ProtocolField, ProtocolKind, ProtocolNode};

use crate::ProtocolEngineError;

pub struct EthernetFrame<'a> {
    pub ether_type: u16,
    pub payload: &'a [u8],
    pub node: ProtocolNode,
}

pub fn parse_ethernet_frame(data: &[u8]) -> Result<EthernetFrame<'_>, ProtocolEngineError> {
    if data.len() < 14 {
        return Err(ProtocolEngineError::MalformedPacket(
            "ethernet frame shorter than 14 bytes".into(),
        ));
    }

    let dst = format_mac(&data[0..6]);
    let src = format_mac(&data[6..12]);
    let ether_type = u16::from_be_bytes([data[12], data[13]]);

    let node = ProtocolNode {
        kind: ProtocolKind::Ethernet,
        label: format!("Ethernet {src} -> {dst}"),
        fields: vec![
            ProtocolField { name: "src_mac".into(), value: src },
            ProtocolField { name: "dst_mac".into(), value: dst },
            ProtocolField { name: "ether_type".into(), value: format!("0x{ether_type:04X}") },
        ],
    };

    Ok(EthernetFrame { ether_type, payload: &data[14..], node })
}

pub struct NullLinkFrame<'a> {
    pub protocol_family: u32,
    pub payload: &'a [u8],
    pub node: ProtocolNode,
}

pub fn parse_null_link(data: &[u8]) -> Result<NullLinkFrame<'_>, ProtocolEngineError> {
    if data.len() < 4 {
        return Err(ProtocolEngineError::MalformedPacket(
            "null link frame shorter than 4 bytes".into(),
        ));
    }

    // Protocol family is in host byte order (NE).
    // On Android (Linux), it's usually Little Endian.
    let family = u32::from_ne_bytes([data[0], data[1], data[2], data[3]]);

    let node = ProtocolNode {
        kind: ProtocolKind::Loopback,
        label: format!("Loopback (Family {family})"),
        fields: vec![
            ProtocolField { name: "family".into(), value: family.to_string() },
        ],
    };

    Ok(NullLinkFrame { protocol_family: family, payload: &data[4..], node })
}

fn format_mac(bytes: &[u8]) -> String {
    bytes.iter().map(|b| format!("{b:02X}")).collect::<Vec<_>>().join(":")
}
