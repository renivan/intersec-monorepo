package com.intersec.androidapp.core.service

import android.app.*
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.intersec.androidapp.core.bridge.RustBridgeClient
import kotlinx.coroutines.*

/**
 * Guardian Service: Mantém o motor Rust vivo em background para análise contínua.
 */
class AnalysisForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val bridgeClient = RustBridgeClient()
    private var isRunning = false

    companion object {
        const val CHANNEL_ID = "intersec_guardian_channel"
        const val NOTIFICATION_ID = 888
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            startForeground(NOTIFICATION_ID, createNotification("Monitoramento Ativo", "InterSec está vigiando sua rede..."))
            startAnalysisLoop()
        }
        return START_STICKY
    }

    private fun startAnalysisLoop() {
        serviceScope.launch {
            while (isRunning) {
                try {
                    // Polling do motor Rust para detecção de anomalias em background
                    val overviewJson = bridgeClient.getCaptureOverview()
                    // Aqui implementaríamos a lógica de notificação se o risk_score subir
                    
                    updateNotification(overviewJson)
                    
                    delay(5000) // Verifica a cada 5 segundos em background
                } catch (e: Exception) {
                    delay(10000)
                }
            }
        }
    }

    private fun updateNotification(json: String) {
        val parts = json.split("|")
        val riskScore = if (parts.size >= 4) {
            parts[3].toIntOrNull() ?: 0
        } else 0

        updateIconBadge(riskScore)

        val notification = createNotification(
            "InterSec Sentinel", 
            if (riskScore > 50) "⚠️ ALERTA: Ameaça detectada na rede ($riskScore%)" else "Análise ativa. Rede Protegida."
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun updateIconBadge(score: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)
            if (score > 50) {
                val shortcut = ShortcutInfo.Builder(this, "alert_id")
                    .setShortLabel("⚠️ AMEAÇA!")
                    .setLongLabel("Ameaça detectada - Toque para agir")
                    .setIcon(Icon.createWithResource(this, android.R.drawable.ic_dialog_alert))
                    .setIntent(Intent(this, Class.forName("com.intersec.androidapp.MainActivity")).apply { action = "OPEN_ALERTS" })
                    .build()
                shortcutManager.dynamicShortcuts = listOf(shortcut)
            } else {
                shortcutManager.removeAllDynamicShortcuts()
            }
        }
    }

    private fun createNotification(title: String, content: String): Notification {
        val intent = Intent(this, Class.forName("com.intersec.androidapp.MainActivity"))
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock) // Temporário até termos o ícone master
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "InterSec Guardian Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificações do monitor de segurança persistente InterSec"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isRunning = false
        serviceScope.cancel()
        super.onDestroy()
    }
}
