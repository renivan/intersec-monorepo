package com.intersec.androidapp.core.network.geoip

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

data class GeoIpResponse(
    val status: String,
    val country: String?,
    val countryCode: String?,
    val city: String?,
    val lat: Double?,
    val lon: Double?,
    val query: String
)

/**
 * Repositório Real de Geo-Localização (Sem Mocks).
 * Utiliza o serviço ip-api.com para resolver coordenadas de IPs globais.
 */
class GeoIpRepository(private val gson: Gson = Gson()) {

    private val cache = mutableMapOf<String, GeoIpResponse>()

    suspend fun resolveIp(ip: String): Result<GeoIpResponse> = withContext(Dispatchers.IO) {
        // Verifica cache local para evitar requisições redundantes
        cache[ip]?.let { return@withContext Result.success(it) }

        try {
            // Ignora IPs privados ou locais
            if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip == "127.0.0.1") {
                return@withContext Result.failure(Exception("IP Privado/Local"))
            }

            val url = "http://ip-api.com/json/$ip?fields=status,country,countryCode,city,lat,lon,query"
            val responseText = URL(url).readText()
            val response = gson.fromJson(responseText, GeoIpResponse::class.java)

            if (response.status == "success") {
                cache[ip] = response
                Result.success(response)
            } else {
                Result.failure(Exception("Falha ao localizar IP: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
