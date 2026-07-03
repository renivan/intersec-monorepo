package com.intersec.androidapp.core.network

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.intersec.androidapp.core.bridge.NativeBridgeClient

/**
 * ThreatIntelManager: Gerencia a sincronização de bases de dados de ameaças via Firestore.
 * Conecta-se ao repositório global e blinda o motor nativo em tempo real.
 */
object ThreatIntelManager {
    private const val TAG = "interSec_INTEL"
    var isTestEnvironment: Boolean = false

    /**
     * Sincroniza a "Blacklist" do Firestore para o motor Native.
     * @param isPro Se true, baixa inteligência avançada.
     */
    fun syncThreatFeeds(isPro: Boolean = false) {
        try {
            val db = FirebaseFirestore.getInstance()
            val bridge = NativeBridgeClient()
            
            Log.d(TAG, "sincronizando feeds de ameaças (Premium=$isPro)...")

            db.collection("threat_intel")
                .get()
                .addOnSuccessListener { documents ->
                    val ipList = documents.mapNotNull { it.getString("ip") }
                    if (ipList.isNotEmpty()) {
                        // Injeção direta no motor nativo interSec
                        val blob = ipList.joinToString(",").toByteArray()
                        val success = bridge.updateThreatDatabase(blob)
                        if (success) {
                            Log.i(TAG, "Motor nativo blindado com ${ipList.size} assinaturas globais.")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Falha na sincronização de inteligência: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Erro crítico de inicialização de inteligência: ${e.message}")
        }
    }
}
