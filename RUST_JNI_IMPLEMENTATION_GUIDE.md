═══════════════════════════════════════════════════════════════════════════════
          GUIA: IMPLEMENTAR FUNÇÕES RUST JNI PARA CAPTURA EM TEMPO REAL
═══════════════════════════════════════════════════════════════════════════════

Este guia mostra como implementar as funções Rust necessárias para suportar
captura em tempo real no lado Kotlin/Android.

═══════════════════════════════════════════════════════════════════════════════
📍 LOCALIZAÇÃO DOS ARQUIVOS
═══════════════════════════════════════════════════════════════════════════════

Arquivo Kotlin (Android):
C:\Users\ClienteAdm\AndroidStudioProjects\intersec\
  └─ androidApp/src/main/java/com/intersec/androidapp/core/bridge/
     └─ RustBridgeClient.kt (Onde estão as declarações JNI)

Arquivos Rust (a serem atualizados):
C:\Users\ClienteAdm\Documents\InterSec\rust\wireshark_mobile_core_rust\
  └─ wireshark_mobile_core_rust/src/lib.rs
     └─ Adicionar funções para captura em tempo real

═══════════════════════════════════════════════════════════════════════════════
🔧 FUNÇÕES JNI A IMPLEMENTAR
═══════════════════════════════════════════════════════════════════════════════

1. startCaptureNative
   ─────────────────────────────────────────────────────────────────────

   Assinatura Kotlin (já definida):
   ```
   external fun startCaptureNative(networkInterface: String, filter: String): String
   ```

   Assinatura Rust a implementar:
   ```rust
   #[no_mangle]
   pub extern "C" fn startCaptureNative(
       network_interface: *const c_char,
       filter: *const c_char,
   ) -> *const c_char {
       // 1. Converter strings C para Rust
       let iface = unsafe { CStr::from_ptr(network_interface).to_string_lossy() };
       let filter_str = unsafe { CStr::from_ptr(filter).to_string_lossy() };

       // 2. Iniciar captura (usar pcap/npcap)
       match start_live_capture(&iface, &filter_str) {
           Ok(session_id) => {
               // 3. Retornar JSON com session ID
               let response = json!({
                   "success": true,
                   "sessionId": session_id,
                   "error": null
               });
               // 4. Converter para C string
               let c_str = CString::new(response.to_string()).unwrap();
               Box::into_raw(c_str.into_boxed_c_str()) as *const c_char
           }
           Err(e) => {
               let response = json!({
                   "success": false,
                   "sessionId": null,
                   "error": e.to_string()
               });
               let c_str = CString::new(response.to_string()).unwrap();
               Box::into_raw(c_str.into_boxed_c_str()) as *const c_char
           }
       }
   }
   ```

   Implementação auxiliar Rust:
   ```rust
   fn start_live_capture(interface: &str, filter: &str) -> Result<String, Box<dyn Error>> {
       use pcap::Capture;
       
       // 1. Abrir interface
       let mut cap = Capture::from_device(interface)?
           .promisc(true)
           .snaplen(65535)
           .timeout(1000)
           .open()?;
       
       // 2. Aplicar filtro BPF
       if !filter.is_empty() {
           cap.filter(filter)?;
       }
       
       // 3. Gerar session ID (UUID)
       let session_id = uuid::Uuid::new_v4().to_string();
       
       // 4. Armazenar em HashMap global ou thread local
       ACTIVE_CAPTURES.lock().insert(session_id.clone(), cap);
       
       Ok(session_id)
   }
   ```

2. stopCaptureNative
   ─────────────────────────────────────────────────────────────────────

   Assinatura Kotlin (já definida):
   ```
   external fun stopCaptureNative(sessionId: String): String
   ```

   Assinatura Rust a implementar:
   ```rust
   #[no_mangle]
   pub extern "C" fn stopCaptureNative(session_id: *const c_char) -> *const c_char {
       let session = unsafe { CStr::from_ptr(session_id).to_string_lossy() };

       match ACTIVE_CAPTURES.lock().remove(session.as_ref()) {
           Some(_capture) => {
               // Captura finalizada, retornar stats
               let stats = json!({
                   "success": true,
                   "sessionId": session,
                   "totalPackets": 100,  // TODO: contar pacotes reais
                   "totalFlows": 10,
                   "sourceName": "eth0",
                   "error": null
               });
               let c_str = CString::new(stats.to_string()).unwrap();
               Box::into_raw(c_str.into_boxed_c_str()) as *const c_char
           }
           None => {
               let error = json!({
                   "success": false,
                   "error": "Session not found"
               });
               let c_str = CString::new(error.to_string()).unwrap();
               Box::into_raw(c_str.into_boxed_c_str()) as *const c_char
           }
       }
   }
   ```

3. capturePacketsNative
   ─────────────────────────────────────────────────────────────────────

   Assinatura Kotlin (já definida):
   ```
   external fun capturePacketsNative(sessionId: String, limit: Int): String
   ```

   Assinatura Rust a implementar:
   ```rust
   #[no_mangle]
   pub extern "C" fn capturePacketsNative(
       session_id: *const c_char,
       limit: i32,
   ) -> *const c_char {
       let session = unsafe { CStr::from_ptr(session_id).to_string_lossy() };

       match get_captured_packets(session.as_ref(), limit as usize) {
           Ok(packets) => {
               let response = json!({
                   "success": true,
                   "items": packets,
                   "totalItems": packets.len(),
                   "error": null
               });
               let c_str = CString::new(response.to_string()).unwrap();
               Box::into_raw(c_str.into_boxed_c_str()) as *const c_char
           }
           Err(e) => {
               let error = json!({
                   "success": false,
                   "items": [],
                   "totalItems": 0,
                   "error": e.to_string()
               });
               let c_str = CString::new(error.to_string()).unwrap();
               Box::into_raw(c_str.into_boxed_c_str()) as *const c_char
           }
       }
   }

   fn get_captured_packets(session_id: &str, limit: usize) -> Result<Vec<PacketDto>, Box<dyn Error>> {
       let captures = ACTIVE_CAPTURES.lock();
       let _capture = captures.get(session_id).ok_or("Session not found")?;
       
       // TODO: Implementar captura de pacotes
       // For now, retorna exemplo
       let mut packets = Vec::new();
       
       // Iterar sobre pacotes capturados
       // for packet in capture.iter() { ... }
       
       Ok(packets.into_iter().take(limit).collect())
   }
   ```

═══════════════════════════════════════════════════════════════════════════════
🗄️ ESTRUTURA DE DADOS RUST
═══════════════════════════════════════════════════════════════════════════════

```rust
use serde::{Deserialize, Serialize};
use std::sync::Mutex;
use std::collections::HashMap;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct PacketDto {
    pub packet_number: u64,
    pub timestamp: u64,
    pub protocol: String,
    pub summary: String,
    pub risk_level: Option<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SessionDto {
    pub session_id: String,
    pub source_name: String,
    pub packet_count: u64,
    pub flow_count: u64,
}

// HashMap global para manter captures ativas
lazy_static::lazy_static! {
    static ref ACTIVE_CAPTURES: Mutex<HashMap<String, pcap::Capture<pcap::Active>>> = 
        Mutex::new(HashMap::new());
}
```

═══════════════════════════════════════════════════════════════════════════════
📦 DEPENDÊNCIAS NECESSÁRIAS NO Cargo.toml
═══════════════════════════════════════════════════════════════════════════════

```toml
[dependencies]
pcap = "0.1"
serde = { version = "1.0", features = ["derive"] }
serde_json = "1.0"
uuid = { version = "1.0", features = ["v4", "serde"] }
lazy_static = "1.4"
```

═══════════════════════════════════════════════════════════════════════════════
🔗 FLUXO DE EXECUÇÃO
═══════════════════════════════════════════════════════════════════════════════

USER UI KOTLIN                    JNI BRIDGE                 RUST CODE
   │                                 │                          │
   │──── "Iniciar Captura" ─────────>│                          │
   │                              (RustBridgeClient)            │
   │                                 │─ startCaptureNative() ──>│
   │                                 │                          │
   │                                 │      (abre pcap,         │
   │                                 │       aplica filter,     │
   │                                 │       gera ID)           │
   │                                 │                          │
   │<────── sessionId ────────────────│<─ retorna JSON ─────────│
   │                                 │                          │
   │                                 │                          │
   │──── "Atualizar Pacotes" ───────>│                          │
   │                                 │─ capturePacketsNative()─>│
   │                                 │                          │
   │                                 │   (lê pacotes da         │
   │                                 │    captura ativa)        │
   │                                 │                          │
   │<────── pacotes ────────────────>│<─ retorna JSON ─────────│
   │                                 │                          │
   │──── "Parar Captura" ───────────>│                          │
   │                                 │─ stopCaptureNative() ──>│
   │                                 │                          │
   │                                 │   (fecha pcap,           │
   │                                 │    retorna stats)        │
   │                                 │                          │
   │<────── snapshot ────────────────│<─ retorna JSON ─────────│
   │                                 │                          │

═══════════════════════════════════════════════════════════════════════════════
⚙️ CONFIGURAÇÃO DO BUILD SCRIPT
═══════════════════════════════════════════════════════════════════════════════

No build_rust_android.ps1, a compilação já está configurada para gerar
a biblioteca dinâmica libwireshark_mobile_core.so

Certifique-se de que os targets estão configurados:

```powershell
$TARGETS = @(
    @{ Target = "aarch64-linux-android";   JniDir = "arm64-v8a" },
    @{ Target = "x86_64-linux-android";    JniDir = "x86_64" },
    @{ Target = "armv7-linux-androideabi"; JniDir = "armeabi-v7a" },
    @{ Target = "i686-linux-android";      JniDir = "x86" }
)
```

Após implementar as funções Rust, execute:
./build_rust_android.ps1

═══════════════════════════════════════════════════════════════════════════════
✅ CHECKLIST DE IMPLEMENTAÇÃO
═══════════════════════════════════════════════════════════════════════════════

[ ] 1. Adicionar dependências ao Cargo.toml (pcap, serde_json, uuid)
[ ] 2. Criar estruturas de dados (PacketDto, SessionDto)
[ ] 3. Implementar ACTIVE_CAPTURES global
[ ] 4. Implementar start_live_capture()
[ ] 5. Implementar get_captured_packets()
[ ] 6. Implementar startCaptureNative() JNI
[ ] 7. Implementar stopCaptureNative() JNI
[ ] 8. Implementar capturePacketsNative() JNI
[ ] 9. Adicionar tratamento de erros
[ ] 10. Testar com a UI Kotlin
[ ] 11. Otimizar performance
[ ] 12. Adicionar logging para debug

═══════════════════════════════════════════════════════════════════════════════
🐛 TROUBLESHOOTING
═══════════════════════════════════════════════════════════════════════════════

Problema: UnsatisfiedLinkError: native method not found
Solução:
- Compilar Rust com cargo ndk
- Verificar nome exato da função (snake_case em Rust)
- Conferir se .so está em jniLibs/

Problema: Captura não funciona em dispositivo real
Solução:
- Usar npcap em vez de libpcap
- Adicionar permissão CHANGE_NETWORK_STATE no AndroidManifest
- Testar com adb logcat para ver erros JNI

Problema: Memory leak
Solução:
- Implementar liberação de memória com Box::into_raw()
- Usar String::from_raw_parts() para recuperar dados
- Limpar ACTIVE_CAPTURES periodicamente

═══════════════════════════════════════════════════════════════════════════════
📚 REFERÊNCIAS
═══════════════════════════════════════════════════════════════════════════════

- Android JNI: https://developer.android.com/training/articles/on-device-debugging
- Rust FFI: https://doc.rust-lang.org/nomicon/ffi.html
- libpcap: https://www.tcpdump.org/papers/sniffing-faq.html
- cargo-ndk: https://github.com/bbqsrc/cargo-ndk

═══════════════════════════════════════════════════════════════════════════════

