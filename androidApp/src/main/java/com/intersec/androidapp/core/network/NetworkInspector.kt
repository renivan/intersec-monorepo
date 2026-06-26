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
        return when {
            name.contains("wlan", ignoreCase = true) -> "Wi-Fi"
            name.contains("eth", ignoreCase = true) -> "Ethernet"
            name.contains("rmnet", ignoreCase = true) || name.contains("pdp", ignoreCase = true) -> "Dados Móveis"
            else -> "Hardware"
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
            if (cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                typeName = "Wi-Fi"
                details = "Wireless Link"
                interfaceName = findInterfaceByPattern(listOf("wlan", "ap", "tiwlan"))
            } else if (cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                typeName = getCellularGeneration(context)
                details = "Rede de Dados Móvel"
                interfaceName = findInterfaceByPattern(listOf("rmnet", "pdp", "ppp"))
            } else if (cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                typeName = "Ethernet"
                details = "Cabo de Rede (Emulador)"
                interfaceName = findInterfaceByPattern(listOf("eth", "enp"))
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

    private fun getCellularGeneration(context: Context): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return try {
            @SuppressLint("MissingPermission")
            @Suppress("DEPRECATION")
            val networkType = try {
                telephonyManager.networkType
            } catch (_: SecurityException) {
                TelephonyManager.NETWORK_TYPE_UNKNOWN
            }

            when (networkType) {
                TelephonyManager.NETWORK_TYPE_NR -> "5G/6G"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G/LTE"
                TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_HSPA -> "3G/HSPA"
                else -> "4G"
            }
        } catch (e: Exception) {
            "4G"
        }
    }
}
