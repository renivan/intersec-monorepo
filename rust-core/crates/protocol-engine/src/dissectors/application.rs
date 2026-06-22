use core_types::{ProtocolKind, ProtocolNode};

pub fn detect_application_tcp(
    src_port: u16,
    dst_port: u16,
    payload: &[u8],
) -> Option<ProtocolNode> {
    if src_port == 53 || dst_port == 53 {
        return Some(ProtocolNode {
            kind: ProtocolKind::Dns,
            label: "DNS over TCP".into(),
            fields: vec![],
        });
    }

    if src_port == 22 || dst_port == 22 || looks_like_ssh(payload) {
        return Some(ProtocolNode {
            kind: ProtocolKind::Ssh,
            label: "SSH".into(),
            fields: vec![],
        });
    }

    if src_port == 443 || dst_port == 443 || src_port == 8443 || dst_port == 8443 || looks_like_tls(payload) {
        return Some(ProtocolNode {
            kind: ProtocolKind::Tls,
            label: "TLS".into(),
            fields: vec![],
        });
    }

    if src_port == 80 || dst_port == 80 || src_port == 8080 || dst_port == 8080 || looks_like_http(payload) {
        return Some(ProtocolNode {
            kind: ProtocolKind::Http,
            label: "HTTP".into(),
            fields: vec![],
        });
    }

    None
}

pub fn detect_application_udp(
    src_port: u16,
    dst_port: u16,
    _payload: &[u8],
) -> Option<ProtocolNode> {
    if src_port == 53 || dst_port == 53 {
        return Some(ProtocolNode {
            kind: ProtocolKind::Dns,
            label: "DNS".into(),
            fields: vec![],
        });
    }

    if src_port == 67 || dst_port == 67 || src_port == 68 || dst_port == 68 {
        return Some(ProtocolNode {
            kind: ProtocolKind::Dhcp,
            label: "DHCP".into(),
            fields: vec![],
        });
    }

    if src_port == 123 || dst_port == 123 {
        return Some(ProtocolNode {
            kind: ProtocolKind::Ntp,
            label: "NTP".into(),
            fields: vec![],
        });
    }

    if src_port == 443 || dst_port == 443 || src_port == 8443 || dst_port == 8443 {
        return Some(ProtocolNode {
            kind: ProtocolKind::Quic,
            label: "QUIC".into(),
            fields: vec![],
        });
    }

    None
}

fn looks_like_http(payload: &[u8]) -> bool {
    let prefixes: &[&[u8]] = &[
        b"GET ",
        b"POST ",
        b"PUT ",
        b"HEAD ",
        b"DELETE ",
        b"OPTIONS ",
        b"HTTP/",
    ];

    prefixes.iter().any(|prefix| payload.starts_with(prefix))
}

fn looks_like_tls(payload: &[u8]) -> bool {
    // TLS record: content-type(0x14/0x15/0x16/0x17) + version 0x03 xx
    payload.len() >= 3
        && matches!(payload[0], 0x14 | 0x15 | 0x16 | 0x17)
        && payload[1] == 0x03
}

fn looks_like_ssh(payload: &[u8]) -> bool {
    payload.starts_with(b"SSH-")
}