# 🛡️ ESTRUTURA ESTRUTURAL: interSec Elite Tactical

Este documento detalha a organização do projeto interSec, focado em segurança tática e análise de rede de alta performance.

---

## 🏗️ Arquitetura Global

### 📂 Raiz do Projeto
- `androidApp/`: Módulo principal da interface Android (Jetpack Compose).
- `native-engine/`: Motor de análise e captura desenvolvido em Rust/Native.
- `shared/`: Código compartilhado Kotlin Multiplatform (KMP).
- `gradle/`: Configurações de build e dependências (Version Catalog).

---

## 📱 Módulo Android (`androidApp`)

### `src/main/java/com/intersec/androidapp/`

- **`app/`**: Inicialização global (`MainApplication.kt`).
- **`core/`**: Lógica fundamental do sistema.
    - `bridge/`: Comunicação JNI com o motor nativo.
    - `network/`: Inspeção de hardware e feeds de inteligência.
    - `storage/`: Persistência via DataStore.
    - `vpn/`: Implementação do túnel Sentinel.
- **`data/`**: Repositórios e modelos de dados.
- **`di/`**: Injeção de dependências manual (`AppModule`, `AppBootstrap`).
- **`presentation/`**: Camada de interface (MVVM).
    - `viewmodel/`: Gestão de estado e lógica de UI.
    - `screens/`: Telas modulares (Auth, Capture, Security, Overview).
    - `navigation/`: Grafo de navegação seguro.
- **`ui/`**: Design System e Temas PRO.

---

## ⚙️ Motor Nativo (`native-engine`)

- **`crates/`**: Módulos internos do motor.
    - `protocol-engine/`: Decodificação profunda de pacotes.
    - `flow-engine/`: Análise de fluxos e inteligência neural.
    - `session-manager/`: Gestão de estado de captura nativa.

---

## 🛠️ Manutenção e Limpeza
O projeto segue uma política de "Mocks Zero". Todos os dados visualizados são processados em tempo real pelo motor nativo ou persistidos via Firebase.

**Estado Atual**: 100% Produção - Tactical Elite Ready.
