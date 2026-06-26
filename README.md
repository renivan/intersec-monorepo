# 🛡️ interSec: Professional Cybersecurity & Network Intelligence

O **interSec** é uma plataforma avançada de cibersegurança para Android, projetada para fornecer análise de tráfego em tempo real, detecção de vulnerabilidades e inteligência de rede. O objetivo do projeto é democratizar o acesso à análise de pacotes (DPI), oferecendo uma interface intuitiva para usuários iniciantes e ferramentas profundas para analistas experientes.

---

## 🚀 Versão Atual: **v2.0.0 - Cybersecurity Intelligence**

Esta versão marca a transição para um ecossistema de produção robusto, com foco em usabilidade e segurança de dados.

---

## ✨ Principais Funcionalidades

- **🔍 Monitoramento em Tempo Real**: Captura e análise de pacotes com motor nativo de alta performance.
- **🧠 Mapa de Conectividade Neural**: Visualização gráfica de fluxos de dados e identificação de IPs.
- **🛡️ Central de Segurança**: Gerenciamento de proteção ativa (IPS) e bloqueio de ameaças.
- **🔐 Autenticação Segura**: Gerenciamento de perfis de usuário via Firebase.
- **🎨 Temas Personalizáveis**: Opções visuais ajustadas para diferentes ambientes de análise.

---

## 🏗️ Arquitetura do Sistema

O projeto é estruturado de forma modular para garantir escalabilidade:

1. **Native Core (Rust)**: O coração técnico, responsável pela decodificação de protocolos (L2-L7) e performance bruta.
2. **Android Guardian (Compose)**: Interface moderna desenvolvida em Jetpack Compose, focada em clareza e resposta rápida.
3. **Sentinel Tunnel (VPN)**: Tecnologia que permite a inspeção de tráfego local sem a necessidade de acesso ROOT (Superusuário).
4. **Cloud Intel (Firebase)**: Sincronização de regras de segurança e banco de dados de ameaças globais.

---

## 🛠️ Instruções de Desenvolvimento

### Pré-requisitos
- Android Studio 2026.1.1+
- Android NDK
- Conexão com Firebase ativa

### Compilação
- **Aplicativo**: `./gradlew :androidApp:assembleDebug`
- **Motor Nativo**: `./build_rust_android.ps1`

---

## 📜 Histórico de Evolução
- **v2.0.0**: Implementação de Perfis, Monetização e Refatoração de Interface Profissional.
- **v1.1.0**: Lançamento do Túnel VPN e Análise de Fluxos.
- **v1.0.0**: MVP com Captura Básica e Integração JNI.

---

**Status**: 100% Produção - Código Seguro e Validado.
