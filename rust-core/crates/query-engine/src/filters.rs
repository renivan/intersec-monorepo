#[derive(Debug, Clone, PartialEq, Eq)]
pub struct PacketQuery {
    pub protocol: Option<String>,
    pub host: Option<String>,
    pub text: Option<String>,
    pub packet_number: Option<u64>,
    pub offset: usize,
    pub limit: usize,
}

impl Default for PacketQuery {
    fn default() -> Self {
        Self {
            protocol: None,
            host: None,
            text: None,
            packet_number: None,
            offset: 0,
            limit: 100,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FlowQuery {
    pub protocol: Option<String>,
    pub host: Option<String>,
    pub port: Option<u16>,
    pub text: Option<String>,
    pub offset: usize,
    pub limit: usize,
}

impl Default for FlowQuery {
    fn default() -> Self {
        Self {
            protocol: None,
            host: None,
            port: None,
            text: None,
            offset: 0,
            limit: 100,
        }
    }
}
