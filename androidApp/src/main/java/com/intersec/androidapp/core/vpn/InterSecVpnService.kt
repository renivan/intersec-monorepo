package com.intersec.androidapp.core.vpn

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.intersec.androidapp.core.bridge.RustBridgeClient

/**
 * InterSec Sentinel Tunnel: O "Porteiro" do tráfego.
 * Estabelece o túnel VPN e entrega o File Descriptor para o motor Rust.
 */
class InterSecVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val bridgeClient = RustBridgeClient()

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
            val builder = Builder()
                .setSession("InterSec Sentinel")
                .addAddress("10.8.0.2", 32)
                .addRoute("0.0.0.0", 0) 
                .addDnsServer("8.8.8.8")
                .setMtu(1500)
                .addDisallowedApplication(packageName) // REGRA DE OURO: Evita loop infinito
                .setBlocking(false)

            vpnInterface = builder.establish()
            
            vpnInterface?.let { pfd ->
                val fd = pfd.fd
                Log.d("InterSecVPN", "Túnel estabelecido (FD $fd). Conectando Guardian Engine...")
                
                val success = bridgeClient.attachVpnTunnel(fd)
                if (success) {
                    Log.d("InterSecVPN", "Análise em Tempo Real ATIVADA via VpnService.")
                    updateNotification("InterSec: Monitoramento Ativo")
                } else {
                    Log.e("InterSecVPN", "Erro: O motor Rust recusou a conexão do túnel.")
                    stopVpn()
                }
            }
        } catch (e: Exception) {
            Log.e("InterSecVPN", "Falha crítica ao subir VpnService: ${e.message}")
            stopVpn()
        }
    }

    private fun updateNotification(text: String) {
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
