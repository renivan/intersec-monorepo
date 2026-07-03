package com.intersec.androidapp.core.neural

import com.intersec.androidapp.core.network.geoip.GeoIpRepository
import com.intersec.androidapp.presentation.state.NeuralLink3D
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Motor Neural Independente (v3.0).
 * Responsável por fundir inteligência de transporte com geo-localização para UI 3D.
 */
class NeuralCoreEngine(
    private val geoIpRepository: GeoIpRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val _neuralStream = MutableStateFlow<List<NeuralLink3D>>(emptyList())
    val neuralStream: StateFlow<List<NeuralLink3D>> = _neuralStream.asStateFlow()

    /**
     * Processa dados crus de rede e converte em entidades 3D Geo-Referenciadas.
     */
    fun processConnection(ip: String, protocol: String, volume: Long) {
        scope.launch {
            // 1. Busca Localização REAL (Sem Mocks)
            val geoResult = geoIpRepository.resolveIp(ip)
            
            geoResult.onSuccess { geo ->
                val lat = geo.lat ?: 0.0
                val lon = geo.lon ?: 0.0

                // 2. Transforma Esférico -> Cartesiano 3D (X, Y, Z)
                val radius = 100f
                val phi = Math.toRadians(90 - lat).toFloat()
                val theta = Math.toRadians(lon + 180).toFloat()

                val x = -(radius * sin(phi) * cos(theta))
                val z = (radius * sin(phi) * sin(theta))
                val y = (radius * cos(phi))

                // 3. Calcula Intensidade Neural baseado no volume de transporte
                val intensity = (volume / 500000f).coerceIn(0.2f, 1.0f)

                // 4. Injeta no fluxo global do sistema
                val newLink = NeuralLink3D(
                    id = java.util.UUID.randomUUID().toString(),
                    sourceIp = "DEVICE",
                    destIp = ip,
                    protocol = protocol,
                    intensity = intensity,
                    color = if (protocol.contains("TCP")) androidx.compose.ui.graphics.Color.Cyan else androidx.compose.ui.graphics.Color.Magenta,
                    latitude = lat,
                    longitude = lon,
                    countryCode = geo.countryCode ?: "??",
                    city = geo.city ?: "Unknown",
                    x = x,
                    y = y,
                    z = z
                )

                _neuralStream.value = (_neuralStream.value + newLink).takeLast(60)
            }
        }
    }

    fun clearNeuralMap() {
        _neuralStream.value = emptyList()
    }
}
