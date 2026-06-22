use capture_reader::format_detector::detect_capture_format;
use capture_reader::CaptureFormat;

#[test]
fn detects_pcap_little_endian() {
    let header = [0xD4, 0xC3, 0xB2, 0xA1];
    assert_eq!(detect_capture_format(&header), CaptureFormat::Pcap);
}

#[test]
fn detects_pcapng() {
    let header = [0x0A, 0x0D, 0x0D, 0x0A];
    assert_eq!(detect_capture_format(&header), CaptureFormat::PcapNg);
}
