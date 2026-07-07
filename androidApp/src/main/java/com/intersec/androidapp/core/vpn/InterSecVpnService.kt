package com.intersec.androidapp.core.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.ConnectivityManager
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.intersec.androidapp.MainActivity
import com.intersec.androidapp.R
import com.intersec.androidapp.core.bridge.NativeBridgeClient

/**
 * InterSec Sentinel Tunnel: O "Porteiro" do tráfego.
 * Estabelece o túnel VPN e entrega o File Descriptor para o motor Native.
 */
class InterSecVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val bridgeClient = NativeBridgeClient()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> startVpn()
            "STOP" -> stopVpn()
        }
        return START_STICKY
    }

    private fun startVpn() {
        if (vpnInterface != null) return

        try {
            val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork

            val builder = Builder()
                .setSession("InterSec Sentinel")
                .addAddress("10.8.0.2", 24) // Máscara mais larga para flexibilidade interna
                .addRoute("0.0.0.0", 0)     // Rota padrão IPv4
                .addRoute("::", 0)          // Rota padrão IPv6 (Crucial para celulares modernos)
                .addDnsServer("8.8.8.8")
                .addDnsServer("1.1.1.1")    // DNS de backup
                .setMtu(1350)               // MTU reduzido para evitar fragmentação em redes móveis
                .addDisallowedApplication(packageName) // Evita loop infinito do próprio app
                .allowBypass()              // Permite que o sistema use a rede real se o túnel falhar (Evita "morte" da net)
                .setBlocking(false)

            // Configurações de rede subjacente
            activeNetwork?.let {
                builder.setUnderlyingNetworks(arrayOf(it))
            }

            vpnInterface = builder.establish()
            
            vpnInterface?.let { pfd ->
                val fd = pfd.detachFd() // Entrega a posse do FD para o motor Native (Evita conflito fdsan)
                Log.d("InterSecVPN", "Túnel estabelecido (FD $fd). Conectando Guardian Engine...")
                
                val success = bridgeClient.attachVpnTunnel(fd)
                if (success) {
                    Log.d("InterSecVPN", "Análise em Tempo Real ATIVADA via VpnService.")
                    startForeground(SENTINEL_NOTIFICATION_ID, createNotification())
                    bridgeClient.startCapture("tun0", "") // tun0 é o padrão para VpnService
                } else {
                    Log.e("InterSecVPN", "Erro: O motor Native recusou a conexão do túnel.")
                    // Se falhar, fechamos o FD manualmente para não vazar
                    try { ParcelFileDescriptor.adoptFd(fd).close() } catch (_: Exception) {}
                    stopVpn()
                }
            }
        } catch (e: Exception) {
            Log.e("InterSecVPN", "Falha crítica ao subir VpnService: ${e.message}")
            stopVpn()
        }
    }

    private fun createNotification(): Notification {
        val channelId = "sentinel_vpn_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Sentinel Security Tunnel", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_neural_core)
            .setContentTitle("Escudo Sentinel Ativo")
            .setContentText("Monitorando tráfego e protegendo conexões...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun stopVpn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        vpnInterface?.close()
        vpnInterface = null
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    companion object {
        private const val SENTINEL_NOTIFICATION_ID = 1001
    }
}

