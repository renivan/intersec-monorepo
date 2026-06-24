package com.intersec.androidapp.core.log

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * GuardianLogger: Centralizador de Diagnóstico Silencioso.
 * Envia logs para o Logcat (Android Studio) e para o Firebase Storage.
 */
object GuardianLogger {
    private const val TAG = "interSec_CORE"
    private val storage = FirebaseStorage.getInstance()
    private val logFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    // Buffer local para evitar uploads excessivos
    private val sessionLogs = StringBuilder()

    fun log(module: String, message: String, isCritical: Boolean = false) {
        val timestamp = logFormat.format(Date())
        val formattedLine = "[$timestamp] [$module] $message"
        
        // 1. Android Studio (Logcat)
        if (isCritical) {
            Log.e(TAG, formattedLine)
        } else {
            Log.d(TAG, formattedLine)
        }

        // 2. Acumula para Firebase
        sessionLogs.append(formattedLine).append("\n")
        
        // Se for crítico ou o buffer estiver grande, faz upload
        if (isCritical || sessionLogs.length > 5000) {
            syncToCloud()
        }
    }

    fun syncToCloud() {
        if (sessionLogs.isEmpty()) return
        
        val content = sessionLogs.toString()
        sessionLogs.setLength(0) // Limpa o buffer

        val fileName = "logs/diagnostic_${System.currentTimeMillis()}.log"
        val storageRef = storage.reference.child(fileName)
        
        storageRef.putBytes(content.toByteArray())
            .addOnSuccessListener {
                Log.i(TAG, "Nuvem: Log de diagnóstico sincronizado com sucesso.")
            }
            .addOnFailureListener {
                Log.w(TAG, "Nuvem: Falha na sincronização de log: ${it.message}")
            }
    }
}
