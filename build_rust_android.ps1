# --- InterSec Master Build System ---
# Este script realiza a compilação otimizada do Core Rust para Android.
# Foco: Performance, Limpeza de Logs e Saneamento de Alertas.

$RUST_PROJECT_PATH = "C:\Users\ClienteAdm\Documents\InterSec\rust\wireshark_mobile_core_rust\wireshark_mobile_core_rust"
$ANDROID_JNI_PATH = "C:\Users\ClienteAdm\AndroidStudioProjects\intersec\androidApp\src\main\jniLibs"
$LIB_NAME = "libwireshark_mobile_core.so"
$env:ANDROID_NDK_HOME = "C:\Users\ClienteAdm\AppData\Local\Android\Sdk\ndk\30.0.14904198"

$TARGETS = @(
    @{ Target = "aarch64-linux-android";   JniDir = "arm64-v8a" },
    @{ Target = "x86_64-linux-android";    JniDir = "x86_64" },
    @{ Target = "armv7-linux-androideabi"; JniDir = "armeabi-v7a" },
    @{ Target = "i686-linux-android";      JniDir = "x86" }
)

Clear-Host
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   INTERSEC CORE - MASTER BUILD SYSTEM    " -ForegroundColor White -BackgroundColor Blue
Write-Host "==========================================" -ForegroundColor Cyan

$choice = "N" # Default para builds rápidas
if ($args.Contains("--clean")) { $choice = "S" }

if ($choice -eq 'S') {
    Write-Host "`n[CLEAN] Removendo artefatos antigos..." -ForegroundColor Yellow
    Push-Location $RUST_PROJECT_PATH
    cargo clean
    Pop-Location
}

Write-Host "`n[NDK] $env:ANDROID_NDK_HOME" -ForegroundColor Gray
Write-Host "[BUILD] Iniciando compilação multi-arch..." -ForegroundColor Cyan

Push-Location $RUST_PROJECT_PATH

$total = $TARGETS.Count
$current = 0

foreach ($item in $TARGETS) {
    $current++
    $target = $item.Target
    $jniDir = $item.JniDir

    Write-Host "`n[#$current/$total] Compilando $target ($jniDir)..." -ForegroundColor Yellow -NoNewline

    # Execução silenciosa (sem --verbose) para evitar ruído no PowerShell
    $output = cargo ndk --target $target build --release --package android-jni-bridge 2>&1

    if ($LASTEXITCODE -eq 0) {
        $sourceFile = "$RUST_PROJECT_PATH\target\$target\release\$LIB_NAME"
        $destFolder = "$ANDROID_JNI_PATH\$jniDir"

        if (-not (Test-Path $destFolder)) { New-Item -ItemType Directory -Path $destFolder | Out-Null }

        if (Test-Path $sourceFile) {
            Copy-Item -Path $sourceFile -Destination "$destFolder\$LIB_NAME" -Force
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
Write-Host "   MOTOR RUST OTIMIZADO E SINCRONIZADO    " -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
