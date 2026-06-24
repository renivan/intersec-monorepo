# Caminho do projeto - altere se necessário
$proj = 'C:\Users\ClienteAdm\Documents\InterSec\rust\wireshark_mobile_core_rust\wireshark_mobile_core_rust'

if (-not (Test-Path $proj)) {
  Write-Error "Pasta não encontrada: $proj"
  exit 1
}

Set-Location -Path $proj

# 1) .gitignore para Rust/Cargo
@"
# Rust/Cargo
/target
**/target
**/target/*
**/*.rs.bk
Cargo.lock

# IDEs
.vscode/
.idea/
*.iml

# macOS
.DS_Store

# Visual Studio
.vs/

# Build outputs
/dist/
/build/
"@ | Out-File -FilePath .gitignore -Encoding utf8

# 2) README.md básico

# wireshark_mobile_core_rust

Projeto Rust - núcleo mobile (wireshark_mobile_core_rust).

Como usar:
- Build: `cargo build --release`
- Test: `cargo test`

Licença: GPL-3.0
"@ | Out-File -FilePath README.md -Encoding utf8

# 3) Baixar a GPLv3 oficial para LICENSE
try {
  Invoke-WebRequest -Uri 'https://www.gnu.org/licenses/gpl-3.0.txt' -OutFile 'LICENSE' -UseBasicParsing -ErrorAction Stop
  Write-Host "LICENSE baixada de https://www.gnu.org/licenses/gpl-3.0.txt"
} catch {
  Write-Warning "Falha ao baixar a GPLv3 automaticamente; gerando cabeçalho mínimo."
  @"
GNU GENERAL PUBLIC LICENSE
Version 3, 29 June 2007

Copyright (C) $(Get-Date -Format yyyy) <Renivan>

(Use o texto oficial em https://www.gnu.org/licenses/gpl-3.0.txt)
"@ | Out-File -FilePath LICENSE -Encoding utf8
}

# 4) Criar workflow GitHub Actions para Rust
$workflowDir = '.github\workflows'
if (-not (Test-Path $workflowDir)) { New-Item -ItemType Directory -Path $workflowDir -Force | Out-Null }

@"
name: Rust CI

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]

jobs:
  build:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        toolchain: [stable]
    steps:
      - uses: actions/checkout@v4

      - name: Install Rust toolchain
        uses: actions-rs/toolchain@v1
        with:
          toolchain: \${{ matrix.toolchain }}
          override: true
          components: rustfmt, clippy
          profile: minimal

      - name: Cache cargo registry and git
        uses: actions/cache@v4
        with:
          path: |
            ~/.cargo/registry
            ~/.cargo/git
          key: \${{ runner.os }}-cargo-\${{ hashFiles('**/Cargo.lock') }}
          restore-keys: \${{ runner.os }}-cargo-

      - name: Build
        run: cargo build --workspace --all-features --verbose

      - name: Format check
        run: cargo fmt --all -- --check

      - name: Clippy
        run: cargo clippy --workspace --all-targets --all-features -- -D warnings

      - name: Test
        run: cargo test --workspace --all-features --no-fail-fast
"@ | Out-File -FilePath "$workflowDir\ci.yml" -Encoding utf8

# 5) Initialize git and commit
if (-not (Test-Path .git)) {
  git init
  Write-Host "Git repo inicializado."
} else {
  Write-Host "Repositório Git já inicializado."
}

# Ensure git user configured
$gitName = git config user.name
$gitEmail = git config user.email
if (-not $gitName -or -not $gitEmail) {
  Write-Warning "Git user.name or user.email não configurado localmente. Configure com:"
  Write-Host "  git config --global user.name 'Seu Nome'"
  Write-Host "  git config --global user.email 'seu@exemplo'"
}

git add -A
git commit -m "chore: project initial setup (README, .gitignore, LICENSE, CI)" -q 2>$null || Write-Host "Nenhuma alteração a commitar (ou commit falhou)."

# Force branch main
git branch -M main 2>$null

# 6) Criar repo no GitHub via gh (irá usar a conta autenticada)
$repoName = Split-Path -Leaf (Get-Location)
Write-Host "Tentando criar o repositório GitHub: $repoName (público)."

# Check for gh
$ghExists = (Get-Command gh -ErrorAction SilentlyContinue) -ne $null
if (-not $ghExists) {
  Write-Warning "GitHub CLI 'gh' não encontrada no PATH. Instale e autentique com 'gh auth login' antes de criar o repositório remoto."
  Write-Host "Comandos locais prontos — você pode criar o remoto manualmente com:"
  Write-Host "  gh repo create $repoName --public --source=. --remote=origin --push --confirm"
  exit 0
}

# Create repo and push
try {
  # If origin exists, skip gh create and just push
  $origin = git remote get-url origin 2>$null
  if ($origin) {
    Write-Host "Remote 'origin' já existe: $origin. Tentando dar push."
    git push -u origin main
  } else {
    gh repo create --public --source=. --remote=origin --push --confirm $repoName
    Write-Host "Repositório criado e push executado."
  }
} catch {
  Write-Warning "Falha ao criar/push do repositório via gh: $($_.Exception.Message)"
  Write-Host "Você pode executar manualmente:"
  Write-Host "  gh repo create $repoName --public --source=. --remote=origin --push --confirm"
}

# 7) Mostrar remotes
git remote -v