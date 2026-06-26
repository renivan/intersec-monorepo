package com.intersec.androidapp.core.network

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.intersec.androidapp.core.bridge.NativeBridgeClient
import java.io.File

/**
 * ThreatIntelManager: Gerencia a sincronização de bases de dados de ameaças mundiais.
 * As mesmas bases usadas por Cisco Talos e Emerging Threats.
 */
object ThreatIntelManager {
    private const val TAG = "interSec_INTEL"
    private val storage = FirebaseStorage.getInstance()
    private val bridge = NativeBridgeClient()

    /**
     * Sincroniza a "Blacklist de Elite" do Firebase para o motor Native.
     * @param isPro Se true, baixa a base completa em tempo real. Se false, base padrão.
     */
    fun syncThreatFeeds(isPro: Boolean = false) {
        val feedPath = if (isPro) "intel/global_threat_pro.bin" else "intel/global_threat_free.bin"
        val localFile = File.createTempFile("threat_intel", ".bin")

        Log.d(TAG, "Iniciando download de Inteligência Global: $feedPath")

        storage.reference.child(feedPath).getFile(localFile)
            .addOnSuccessListener {
                val bytes = localFile.readBytes()
                Log.i(TAG, "Sucesso: ${bytes.size} bytes de inteligência baixados.")
                
                // Entrega a blindagem diretamente para o motor nativo Native
                val success = bridge.updateThreatDatabase(bytes)
                if (success) {
                    Log.i(TAG, "Motor Native: Blindagem Industrial ATUALIZADA.")
                }
            }
            .addOnFailureListener {
                Log.w(TAG, "Falha ao baixar inteligência global: ${it.message}. Usando base local offline.")
            }
    }
}

