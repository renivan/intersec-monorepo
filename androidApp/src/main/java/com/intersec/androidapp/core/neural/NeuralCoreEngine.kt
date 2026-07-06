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
     * Estrutura a visualização como pontos de interceptação.
     */
    fun processConnection(ip: String, protocol: String, volume: Long) {
        scope.launch {
            // Verifica se já temos este nó para evitar sobrecarga neural
            val existing = _neuralStream.value.find { it.destIp == ip }
            if (existing != null) {
                // Se já existe, apenas atualizamos a intensidade (Pulso de Interceptação)
                updateNodeIntensity(ip, volume)
                return@launch
            }

            // 1. Busca Localização REAL (Sem Mocks)
            val geoResult = geoIpRepository.resolveIp(ip)
            
            geoResult.onSuccess { geo ->
                val lat = geo.lat ?: 0.0
                val lon = geo.lon ?: 0.0

                // 2. Transforma Esférico -> Cartesiano 3D (X, Y, Z)
                // Usamos um raio um pouco maior que a Terra para flutuar os nós
                val radius = 105f 
                val phi = Math.toRadians(90 - lat).toFloat()
                val theta = Math.toRadians(lon + 180).toFloat()

                val x = -(radius * sin(phi) * cos(theta))
                val z = (radius * sin(phi) * sin(theta))
                val y = (radius * cos(phi))

                // 3. Calcula Intensidade Neural baseado no volume de transporte
                val intensity = (volume / 500000f).coerceIn(0.4f, 1.0f)

                // 4. Injeta no fluxo global do sistema como PONTO DE INTERCEPTAÇÃO
                val newLink = NeuralLink3D(
                    id = java.util.UUID.randomUUID().toString(),
                    sourceIp = "DEVICE",
                    destIp = ip,
                    protocol = protocol,
                    intensity = intensity,
                    color = getProtocolColor(protocol),
                    latitude = lat,
                    longitude = lon,
                    countryCode = geo.countryCode ?: "??",
                    city = geo.city ?: "Unknown",
                    x = x,
                    y = y,
                    z = z,
                    isInterception = true
                )

                _neuralStream.value = (_neuralStream.value + newLink).takeLast(100)
            }
        }
    }

    private fun updateNodeIntensity(ip: String, volume: Long) {
        _neuralStream.value = _neuralStream.value.map { node ->
            if (node.destIp == ip) {
                val newIntensity = (node.intensity + 0.2f).coerceAtMost(1.0f)
                node.copy(intensity = newIntensity, lastPulseTime = System.currentTimeMillis())
            } else {
                node
            }
        }
    }

    private fun getProtocolColor(protocol: String): androidx.compose.ui.graphics.Color {
        return when {
            protocol.contains("TCP") -> androidx.compose.ui.graphics.Color(0xFF00FBFF) // Cyan
            protocol.contains("UDP") -> androidx.compose.ui.graphics.Color(0xFFFF00D4) // Magenta
            protocol.contains("TLS") || protocol.contains("SSL") -> androidx.compose.ui.graphics.Color(0xFF7000FF) // Purple
            else -> androidx.compose.ui.graphics.Color.Green
        }
    }

    fun clearNeuralMap() {
        _neuralStream.value = emptyList()
    }
}
