@file:Suppress("unused", "unused", "unused")

package com.intersec.androidapp.core.network

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.intersec.androidapp.core.bridge.NativeBridgeClient

/**
 * ThreatIntelManager: Gerencia a sincronização de bases de dados de ameaças via Firestore.
 * Conecta-se ao repositório global e blinda o motor nativo em tempo real.
 */
@Suppress("unused", "unused", "unused", "unused", "unused", "unused")
object ThreatIntelManager {
    private const val TAG = "interSec_INTEL"
    var isTestEnvironment: Boolean = false

    /**
     * Sincroniza a "Blacklist" do Firestore para o motor Native.
     * @param isPro Se true, baixa inteligência avançada.
     */
    fun syncThreatFeeds(isPro: Boolean = false, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        try {
            val db = FirebaseFirestore.getInstance()
            val bridge = NativeBridgeClient()
            
            Log.d(TAG, "sincronizando feeds de ameaças (Premium=$isPro)...")

            db.collection("threat_intel")
                .get()
                .addOnSuccessListener { documents ->
                    val ipList = documents.mapNotNull { it.getString("ip") }
                    if (ipList.isNotEmpty()) {
                        val blob = ipList.joinToString(",").toByteArray()
                        val success = bridge.updateThreatDatabase(blob)
                        if (success) {
                            Log.i(TAG, "Motor nativo blindado com ${ipList.size} assinaturas.")
                            onComplete(true, "${ipList.size} assinaturas injetadas.")
                        } else {
                            onComplete(false, "Falha de JNI ao injetar base.")
                        }
                    } else {
                        // Se vazio, tenta injetar um marcador de ativação
                        bridge.updateThreatDatabase("ACTIVATED".toByteArray())
                        onComplete(true, "Base ativada (Vazia).")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Falha Firestore: ${e.message}")
                    onComplete(false, "Erro Cloud Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Erro crítico: ${e.message}")
            onComplete(false, "Exceção: ${e.message}")
        }
    }
}
