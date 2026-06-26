use std::fs::File;
use std::io::{Read, Write};
use std::os::unix::io::{FromRawFd, RawFd};

pub struct CaptureTunnel {
    file: File,
    fd: RawFd,
}

impl CaptureTunnel {
    /// Cria uma interface de túnel a partir de um File Descriptor validado.
    pub fn new(fd: i32) -> Result<Self, String> {
        #[cfg(unix)]
        {
            use libc::{fcntl, F_GETFL, F_SETFL, O_NONBLOCK};

            // Validação de Integridade do FD
            let flags = unsafe { fcntl(fd, F_GETFL) };
            if flags == -1 {
                return Err(format!("Engine-IO: FD {} inválido ou inacessível.", fd));
            }

            // Força modo Non-Blocking para garantir a leveza do sistema
            if unsafe { fcntl(fd, F_SETFL, flags | O_NONBLOCK) } == -1 {
                return Err("Engine-IO: Falha ao configurar I/O assíncrono.".into());
            }

            Ok(Self {
                file: unsafe { File::from_raw_fd(fd) },
                fd,
            })
        }
        #[cfg(not(unix))]
        {
            let _ = fd;
            Err("Engine-IO: Suporte apenas para sistemas baseados em Unix (Android/iOS).".into())
        }
    }

    /// Lê um pacote bruto do túnel. (Fast Path)
    pub fn read_packet(&mut self, buffer: &mut [u8]) -> Result<usize, std::io::Error> {
        self.file.read(buffer)
    }

    /// Escreve um pacote de volta para a rede. (Válvula de Saída)
    pub fn write_packet(&mut self, data: &[u8]) -> Result<(), std::io::Error> {
        self.file.write_all(data)
    }

    pub fn fd(&self) -> i32 {
        self.fd
    }
}
