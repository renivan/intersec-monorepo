# Estrutura JNI para o Core Rust

Esta pasta contém as bibliotecas nativas compiladas do projeto Rust (`.so`).

### Onde colocar os arquivos:

Para que a integração funcione, você deve compilar o projeto Rust para Android e copiar o arquivo `libwireshark_mobile_core.so` para as pastas correspondentes abaixo:

- **arm64-v8a/**: Dispositivos físicos modernos (64-bit).
- **armeabi-v7a/**: Dispositivos físicos antigos (32-bit).
- **x86_64/**: Emuladores Android em computadores modernos.
- **x86/**: Emuladores Android antigos.

### Exemplo de caminho final:
`androidApp/src/main/jniLibs/arm64-v8a/libwireshark_mobile_core.so`

---
*Nota: Certifique-se de que o nome do arquivo seja exatamente `libwireshark_mobile_core.so`, conforme configurado no `RustRuntimeLoader.kt`.*
