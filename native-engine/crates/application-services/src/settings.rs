#[derive(Debug, Clone, Copy)]
pub struct SecuritySettings {
    pub level: u8,
    pub smart_shield: bool,
    pub kill_switch: bool,
}

impl Default for SecuritySettings {
    fn default() -> Self {
        Self {
            level: 1, // Normal
            smart_shield: true,
            kill_switch: false,
        }
    }
}
