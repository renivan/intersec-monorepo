package com.intersec.androidapp.core.vpn

import android.content.Intent
import android.net.ConnectivityManager
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
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
                .addAddress("10.8.0.2", 32)
                .addRoute("0.0.0.0", 0) 
                .addDnsServer("8.8.8.8")
                .setMtu(1500)
                .addDisallowedApplication(packageName) // Evita loop infinito
                .setBlocking(false)

            // Trava a conexão na rede atual para evitar bypass e garantir estabilidade (MVP)
            activeNetwork?.let {
                builder.setUnderlyingNetworks(arrayOf(it))
                Log.d("InterSecVPN", "Conexão travada no túnel Sentinel para a rede: $it")
            }

            vpnInterface = builder.establish()
            
            vpnInterface?.let { pfd ->
                val fd = pfd.detachFd() // Entrega a posse do FD para o motor Native (Evita conflito fdsan)
                Log.d("InterSecVPN", "Túnel estabelecido (FD $fd). Conectando Guardian Engine...")
                
                val success = bridgeClient.attachVpnTunnel(fd)
                if (success) {
                    Log.d("InterSecVPN", "Análise em Tempo Real ATIVADA via VpnService.")
                    updateNotification()
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

    private fun updateNotification() {
        // Futura implementação de notificação de serviço em primeiro plano
    }

    private fun stopVpn() {
        vpnInterface?.close()
        vpnInterface = null
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}

