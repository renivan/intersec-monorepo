# --- InterSec Master Build System ---
# Script de compilação unificado e organizado.
# Localização: Root do Projeto

$SCRIPT_DIR = $PSScriptRoot
$RUST_PROJECT_PATH = Join-Path $SCRIPT_DIR "native-engine"
$ANDROID_JNI_PATH = Join-Path $SCRIPT_DIR "androidApp\src\main\jniLibs"
$LIB_NAME = "libwireshark_mobile_core.so"

# Detecta NDK automaticamente ou usa o padrão
if (-not $env:ANDROID_NDK_HOME) {
    $env:ANDROID_NDK_HOME = "C:\Users\ClienteAdm\AppData\Local\Android\Sdk\ndk\30.0.14904198"
}

$TARGETS = @(
    @{ Target = "aarch64-linux-android";   JniDir = "arm64-v8a" },
    @{ Target = "x86_64-linux-android";    JniDir = "x86_64" },
    @{ Target = "armv7-linux-androideabi"; JniDir = "armeabi-v7a" },
    @{ Target = "i686-linux-android";      JniDir = "x86" }
)

Clear-Host
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   INTERSEC MASTER UNIFIED BUILD SYSTEM   " -ForegroundColor White -BackgroundColor Blue
Write-Host "==========================================" -ForegroundColor Cyan

if ($args.Contains("--clean")) {
    Write-Host "`n[CLEAN] Removendo artefatos antigos..." -ForegroundColor Yellow
    Push-Location $RUST_PROJECT_PATH
    cargo clean
    Pop-Location
}

Write-Host "`n[PATHS]" -ForegroundColor Gray
Write-Host "Core Rust: $RUST_PROJECT_PATH" -ForegroundColor Gray
Write-Host "Jni Libs:  $ANDROID_JNI_PATH" -ForegroundColor Gray

Write-Host "`n[BUILD] Iniciando compilação multi-arch..." -ForegroundColor Cyan

Push-Location $RUST_PROJECT_PATH

$total = $TARGETS.Count
$current = 0

foreach ($item in $TARGETS) {
    $current++
    $target = $item.Target
    $jniDir = $item.JniDir

    Write-Host "`n[#$current/$total] Compilando $target ($jniDir)..." -ForegroundColor Yellow -NoNewline

    # Executa a compilação via cargo-ndk
    $output = cargo ndk --target $target build --release --package android-jni-bridge 2>&1

    if ($LASTEXITCODE -eq 0) {
        $sourceFile = Join-Path $RUST_PROJECT_PATH "target\$target\release\$LIB_NAME"
        $destFolder = Join-Path $ANDROID_JNI_PATH $jniDir

        if (-not (Test-Path $destFolder)) { New-Item -ItemType Directory -Path $destFolder | Out-Null }

        if (Test-Path $sourceFile) {
            Copy-Item -Path $sourceFile -Destination (Join-Path $destFolder $LIB_NAME) -Force
            Write-Host " [OK]" -ForegroundColor Green
        } else {
            Write-Host " [ERRO: Binário não encontrado]" -ForegroundColor Red
        }
    } else {
        Write-Host " [FALHA]" -ForegroundColor Red
        Write-Host $output -ForegroundColor Red
    }
}

Pop-Location
Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "   ESTADO: PRONTO PARA DEPLOY (ANDROID)   " -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
