use core_types::{ParsedPacket, PacketRecord, ProtocolNode, ProtocolField, ProtocolKind};

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum DataLinkMode {
    Ethernet, // Para Root/PCAP
    RawIP,    // Para VPN Sentinel
}

pub fn decode_packet(record: &PacketRecord, mode: &DataLinkMode) -> Result<ParsedPacket, String> {
    let data = &record.raw_data;
    if data.is_empty() { return Err("Pacote vazio".into()); }

    let mut nodes = Vec::new();
    let mut offset = 0;

    match mode {
        DataLinkMode::Ethernet => {
            if data.len() < 14 { return Err("Pacote Ethernet curto".into()); }
            nodes.push(ProtocolNode {
                label: "Ethernet".into(),
                kind: ProtocolKind::Ethernet,
                fields: vec![ProtocolField { name: "type".into(), value: "IPv4/v6".into() }],
            });
            offset = 14;
        }
        DataLinkMode::RawIP => {
            offset = 0;
        }
    }

    if data.len() > offset {
        let version = data[offset] >> 4;
        if version == 4 {
            decode_ipv4(data, offset, &mut nodes);
        } else if version == 6 {
            decode_ipv6(data, offset, &mut nodes);
        }
    }

    let highest = nodes.last().map(|n| n.kind.clone());
    let summary = generate_summary(&nodes.last());

    Ok(ParsedPacket {
        packet_number: record.packet_number,
        timestamp_epoch_micros: record.timestamp_epoch_micros,
        link_type: record.link_type,
        highest_protocol: highest,
        nodes,
        summary,
        warnings: Vec::new(),
        raw_data: record.raw_data.clone(),
    })
}

fn decode_ipv4(data: &[u8], offset: usize, nodes: &mut Vec<ProtocolNode>) {
    if data.len() < offset + 20 { return; }
    let src_ip = format!("{}.{}.{}.{}", data[offset+12], data[offset+13], data[offset+14], data[offset+15]);
    let dst_ip = format!("{}.{}.{}.{}", data[offset+16], data[offset+17], data[offset+18], data[offset+19]);

    nodes.push(ProtocolNode {
        label: "IPv4".into(),
        kind: ProtocolKind::Ipv4,
        fields: vec![
            ProtocolField { name: "src_ip".into(), value: src_ip },
            ProtocolField { name: "dst_ip".into(), value: dst_ip },
        ],
    });
}

fn decode_ipv6(_data: &[u8], _offset: usize, nodes: &mut Vec<ProtocolNode>) {
    nodes.push(ProtocolNode {
        label: "IPv6".into(),
        kind: ProtocolKind::Ipv6,
        fields: vec![],
    });
}

fn generate_summary(node: &Option<&ProtocolNode>) -> String {
    match node {
        Some(n) => format!("Traffic: {}", n.label),
        None => "Unknown Protocol".into(),
    }
}
