use crate::CaptureFormat;

pub fn detect_capture_format(header: &[u8]) -> CaptureFormat {
    if header.len() < 4 {
        return CaptureFormat::Unknown;
    }

    match &header[0..4] {
        [0xD4, 0xC3, 0xB2, 0xA1] => CaptureFormat::Pcap,
        [0xA1, 0xB2, 0xC3, 0xD4] => CaptureFormat::Pcap,
        [0x4D, 0x3C, 0xB2, 0xA1] => CaptureFormat::Pcap,
        [0xA1, 0xB2, 0x3C, 0x4D] => CaptureFormat::Pcap,
        [0x0A, 0x0D, 0x0D, 0x0A] => CaptureFormat::PcapNg,
        _ => CaptureFormat::Unknown,
    }
}
