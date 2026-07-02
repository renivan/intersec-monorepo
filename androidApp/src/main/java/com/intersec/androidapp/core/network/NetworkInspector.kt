package com.intersec.androidapp.core.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import java.net.NetworkInterface

data class NetworkInfo(
    val interfaceName: String,
    val typeName: String, // WiFi, 4G, 5G, Ethernet
    val isConnected: Boolean,
    val details: String, // SSID or Carrier name
)

object NetworkInspector {
    fun getAvailableInterfaces(): List<NetworkInfo> {
        return try {
            NetworkInterface.getNetworkInterfaces().asSequence()
                .filter { !it.isLoopback }
                .map { ni ->
                    NetworkInfo(
                        interfaceName = ni.name,
                        typeName = inferTypeFromName(ni.name),
                        isConnected = ni.isUp,
                        details = if (ni.displayName != ni.name) ni.displayName else "Interface Interna"
                    )
                }.toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun inferTypeFromName(name: String): String {
        val lowerName = name.lowercase()
        return when {
            lowerName.contains("wlan") || lowerName.contains("ap") || lowerName.contains("tiwlan") -> "Wi-Fi"
            lowerName.contains("eth") || lowerName.contains("enp") || lowerName.contains("eno") -> "Ethernet"
            lowerName.contains("rmnet") || lowerName.contains("pdp") || lowerName.contains("ccmni") || 
            lowerName.contains("ppp") || lowerName.contains("uwb") -> "Dados Móveis"
            lowerName.contains("tun") || lowerName.contains("ppp") || lowerName.contains("tap") -> "VPN/Tunnel"
            lowerName.contains("rndis") || lowerName.contains("usb") -> "USB Tethering"
            else -> "Hardware/System"
        }
    }

    fun getActiveNetworkInfo(context: Context): NetworkInfo {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        var interfaceName = "Nenhuma"
        var typeName = "Desconectado"
        var details = "Aguardando Rede"
        val isConnected = capabilities != null

        capabilities?.let { cap ->
            when {
                cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    typeName = "Wi-Fi"
                    details = "Conexão Wireless"
                    interfaceName = findInterfaceByPattern(listOf("wlan", "ap", "tiwlan"))
                }
                cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    typeName = getCellularGeneration(context)
                    details = "Rede de Operadora"
                    interfaceName = findInterfaceByPattern(listOf("rmnet", "pdp", "ppp", "ccmni"))
                }
                cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    typeName = "Ethernet"
                    details = "Cabo/Emulador"
                    interfaceName = findInterfaceByPattern(listOf("eth", "enp", "eno"))
                }
                cap.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                    typeName = "VPN"
                    details = "Túnel Seguro Ativo"
                    interfaceName = findInterfaceByPattern(listOf("tun", "tap", "ppp"))
                }
                cap.hasTransport(NetworkCapabilities.TRANSPORT_USB) -> {
                    typeName = "USB"
                    details = "Tethering via USB"
                    interfaceName = findInterfaceByPattern(listOf("rndis", "usb"))
                }
                else -> {
                    typeName = "Outro"
                    details = "Interface Desconhecida"
                    interfaceName = "net0"
                }
            }
        }

        return NetworkInfo(interfaceName, typeName, isConnected, details)
    }

    private fun findInterfaceByPattern(patterns: List<String>): String {
        return try {
            (NetworkInterface.getNetworkInterfaces().asSequence().find { ni ->
                patterns.any { ni.name.contains(it, ignoreCase = true) }
            }?.name) ?: (patterns.first() + "0")
        } catch (_: Exception) {
            patterns.first() + "0"
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCellularGeneration(context: Context): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return try {
            @Suppress("DEPRECATION")
            val networkType = try {
                telephonyManager.networkType
            } catch (_: SecurityException) {
                TelephonyManager.NETWORK_TYPE_UNKNOWN
            }

            @Suppress("DEPRECATION")
            when (networkType) {
                TelephonyManager.NETWORK_TYPE_NR -> "5G/6G"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G/LTE"
                TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA -> "3G/HSPA+"
                TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_EVDO_B -> "3G"
                TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_IDEN -> "2G/GPRS/EDGE"
                else -> "Dados Móveis"
            }
        } catch (e: Exception) {
            "4G"
        }
    }
}
