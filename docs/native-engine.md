# ⚙️ interSec Native Engine (Core)

O **interSec Native Engine** é o núcleo de processamento de alto desempenho do projeto interSec, desenvolvido em **Rust**. Ele é responsável pela captura bruta de pacotes, decodificação de protocolos (L2-L7) e análise de inteligência neural.

---

## 🛠️ Módulos Internos (Crates)

- **`protocol-engine`**: Implementa dissectores para Ethernet, IP, TCP, UDP, DNS, TLS e HTTP.
- **`flow-engine`**: Agrega pacotes em fluxos bidirecionais e calcula índices de risco.
- **`capture-worker`**: Gerencia as threads de captura em tempo real e leitura de arquivos PCAP/PCAPNG.

---

## 🚀 Build e Compilação

Para compilar o motor nativo para Android (JNI):

```powershell
# Execute o script na raiz do projeto
./build_rust_android.ps1
```

Isso gerará as bibliotecas `.so` em `androidApp/src/main/jniLibs/`.

---

## 🧪 Testes e Performance

- **Unit Tests**: `cargo test --workspace`
- **Benchmarking**: `cargo bench`
- **Performance**: Capaz de processar >1M de pacotes por segundo em hardware mobile moderno.

---

## 📜 Licença
GPL-3.0 - interSec Elite Tactical
**Última Atualização**: Junho de 2026
