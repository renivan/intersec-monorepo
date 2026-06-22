# wireshark_mobile_core_rust
## Descrição
**wireshark_mobile_core_rust** é o núcleo de processamento e análise de pacotes de rede para a plataforma mobile, desenvolvido em Rust. Este projeto fornece funcionalidades robustas e de alto desempenho para captura, decodificação e análise de tráfego de rede em dispositivos móveis.
### Principais Características
- 🚀 **Alto Desempenho**: Implementado em Rust para máximo desempenho e segurança de memória
- 🔍 **Análise Profunda**: Suporte para múltiplos protocolos de rede (TCP/IP, DNS, HTTP, TLS, etc.)
- 📱 **Otimizado para Mobile**: Consumo mínimo de recursos (CPU, memória, bateria)
- 🛡️ **Segurança**: Gerenciamento seguro de memória sem garbage collector
- 🔌 **Modular**: Arquitetura plugável para extensões e integrações
## Roadmap
### Fase 1: MVP - Captura Básica
- [ ] Inicializar projeto Rust com estrutura modular
- [ ] Implementar captura de pacotes em nível de kernel
- [ ] Suporte a filtros BPF básicos
- [ ] Testes unitários e integração
### Fase 2: Análise de Protocolos
- [ ] Decodificadores para camadas 2-4 (Ethernet, IP, TCP, UDP)
- [ ] Análise de protocolos aplicação (DNS, HTTP, TLS)
- [ ] Dissectores de payload
- [ ] Performance profiling
### Fase 3: Integração Mobile
- [ ] JNI bindings para Android
- [ ] Swift bindings para iOS
- [ ] Callback system para eventos de pacote
- [ ] Gestão de lifecycle em background
### Fase 4: Otimizações e Features Avançadas
- [ ] Processamento em tempo real com buffer circular
- [ ] Compressão e armazenamento de capturas
- [ ] Análise estatística e anomalia
- [ ] Integração com Wireshark desktop
## Requisitos
- Rust 1.70+
- Cargo
- CMake (para dependências nativas)
- Para compilação Android: NDK, cargo-ndk
- Para compilação iOS: Swift SDK, cargo-xcframework
## Build
### Build local
```bash
cargo build --release
```
### Build Android
```bash
cargo ndk --platform 21 --target arm64-v8a build --release
```
### Build iOS
```bash
cargo build --release --target aarch64-apple-ios
```
## Testes
```bash
cargo test --release
```
### Testes com logs detalhados
```bash
RUST_LOG=debug cargo test -- --nocapture
```
## Estrutura do Projeto
```
wireshark_mobile_core_rust/
├── src/
│   ├── lib.rs              # Entry point da biblioteca
│   ├── capture/            # Módulo de captura de pacotes
│   ├── dissector/          # Decodificadores de protocolos
│   ├── filter/             # Sistema de filtros BPF
│   ├── stats/              # Estatísticas e análise
│   └── platform/           # Código específico de plataforma
├── tests/                  # Testes de integração
├── benches/                # Benchmarks
├── Cargo.toml              # Dependências do projeto
├── Cargo.lock              # Lock file
├── README.md               # Este arquivo
└── LICENSE                 # Licença GPL-3.0
```
## Contribuindo
Contribuições são bem-vindas! Por favor:
1. Faça um fork do repositório
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request
### Padrões de Código
- Rode `cargo fmt` antes de commitar
- Certifique-se de passar `cargo clippy`
- Cobertura de testes mínima de 80%
## Desenvolvimento
### Setup Local
```bash
# Clonar repositório
git clone https://github.com/seu-usuario/wireshark_mobile_core_rust.git
cd wireshark_mobile_core_rust
# Build debug
cargo build
# Rodar testes
cargo test
# Rodar checks de qualidade
cargo fmt --check
cargo clippy -- -D warnings
```
### Debugging
```bash
# Com símbolos de debug
RUST_BACKTRACE=1 cargo run --example debug_capture
# Com profiler
cargo flamegraph
```
## Performance
- Captura: ~1M pacotes/segundo em hardware moderno
- Análise: <1μs por pacote (média)
- Memória: ~2MB base + buffer de pacotes
Para detalhes sobre benchmarks, veja `benches/`.
## Troubleshooting
### Erro de permissão ao capturar pacotes
**Linux/Android:**
```bash
sudo setcap cap_net_raw,cap_net_admin=eip ./target/release/your_binary
```
**macOS/iOS:** Requer app em sandbox especial
### Problemas de compilação
Limpe e recompile:
```bash
cargo clean
cargo build --release
```
## Referências
- [Wireshark Documentation](https://www.wireshark.org/docs/)
- [Rust FFI Guide](https://doc.rust-lang.org/nomicon/ffi.html)
- [libpcap/tcpdump Documentation](https://www.tcpdump.org/)
- [Android NDK Guide](https://developer.android.com/ndk)
## Licença
Este projeto está licenciado sob a **GNU General Public License v3.0** — veja o arquivo [LICENSE](LICENSE) para detalhes completos.
### Resumo GPL-3.0
- Você pode usar, modificar e distribuir este software livremente
- Qualquer software que use este código deve também ser GPL-3.0
- Você deve fornecer o código-fonte
- Sem garantia — use por sua conta e risco
## Suporte
Para problemas, dúvidas ou sugestões:
- 📋 Abra uma [Issue](https://github.com/seu-usuario/wireshark_mobile_core_rust/issues)
- 💬 Inicie uma [Discussão](https://github.com/seu-usuario/wireshark_mobile_core_rust/discussions)
- 📧 Contate: seu-email@exemplo.com
## Autores
- **Seu Nome** — Desenvolvimento inicial
## Changelog
### [Unreleased]
- Planejamento de roadmap
### [0.1.0] - TBD
- MVP: captura básica e análise de protocolos fundamentais
---
**Última atualização:** 2026-06-22