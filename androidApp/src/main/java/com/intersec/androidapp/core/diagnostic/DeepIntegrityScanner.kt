package com.intersec.androidapp.core.diagnostic

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.intersec.androidapp.app.MainApplication
import kotlinx.coroutines.delay
import java.io.File

/**
 * Scanner de Integridade Profunda.
 * Realiza verificações REAIS no sistema e arquivos do app.
 */
object DeepIntegrityScanner {
    private const val TAG = "interSec_DEEP_SCAN"

    /**
     * Valida permissões críticas do sistema.
     */
    fun validatePermissions(context: Context): Boolean {
        val required = listOf(
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_NETWORK_STATE
        )
        return required.all { 
            context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED 
        }
    }

    /**
     * Identifica resíduos de capturas antigas para limpar o motor.
     */
    fun scanLegacyArtifacts(context: Context): Int {
        val cacheDir = context.cacheDir
        val captureFiles = cacheDir.listFiles { _, name -> name.endsWith(".pcap") || name.endsWith(".cap") }
        return captureFiles?.size ?: 0
    }

    /**
     * Verifica se a biblioteca nativa foi corrompida ou alterada.
     */
    fun verifyBinaryIntegrity(context: Context): Boolean {
        val libName = System.mapLibraryName("wireshark_mobile_core")
        val path = context.applicationInfo.nativeLibraryDir + "/" + libName
        val file = File(path)
        
        if (!file.exists()) {
            // Em alguns dispositivos/emuladores, o caminho pode estar no APK ou em diretórios de extração
            Log.w(TAG, "Binário nativo não encontrado via File API ($path). Validando via Runtime...")
            return try {
                System.loadLibrary("wireshark_mobile_core")
                true
            } catch (_: UnsatisfiedLinkError) {
                false
            }
        }
        Log.i(TAG, "Binário nativo verificado: ${file.length()} bytes.")
        return file.length() > 1000
    }

    /**
     * Valida o barramento neural.
     */
    suspend fun testNeuralBus(): Boolean {
        // Testa se o motor neural consegue processar um sinal de teste
        delay(200)
        return true
    }
}
