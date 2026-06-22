package com.intersec.androidapp.presentation.state

import androidx.compose.ui.graphics.Color

/**
 * Estado da tela de captura em tempo real
 */
data class CaptureRealtimeUiState(
    val isCapturing: Boolean = false,
    val isPaused: Boolean = false,
    val packets: List<RealtimePacketModel> = emptyList(),
    val flows: List<RealtimeFlowModel> = emptyList(),
    val totalPackets: Int = 0,
    val captureStartTime: Long = 0L,
    val elapsedSeconds: Int = 0,
    val packetsPerSecond: Int = 0,
    val networkInterface: String = "wlan0",
    val networkName: String = "InterSec_NET_5G",
    val filter: String = "",
    val filterInput: String = "",
    val isLiveUpdate: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val errorDetail: String? = null,
    val statusIndicator: StatusIndicator = StatusIndicator.IDLE,
    val neuralNodes: List<NeuralNodeModel> = emptyList()
)

data class NeuralNodeModel(
    val id: String,
    val x: Float, // 0.0 - 1.0
    val y: Float, // 0.0 - 1.0
    val intensity: Float, // 0.0 - 1.0
    val connections: List<String>
)

data class RealtimePacketModel(
    val number: Int,
    val timestampSeconds: Double, // 0.000s format
    val sourceAddress: String, // 2C06...6B44
    val destinationAddress: String, // 2804...418F
    val protocol: String, // TCP, TLS, UDP
    val flags: String?, // SYN, ACK, SYN,ACK, CLIENT HELLO, SERVER HELLO, RST
    val size: Int, // 74, 60, etc
    val info: String, // Custom info
    val colorType: PacketColorType = PacketColorType.NORMAL,
    val isAnomalous: Boolean = false
)

data class RealtimeFlowModel(
    val label: String,
    val endpoints: String,
    val packetCount: Long,
    val payloadBytes: Long,
    val isInsecure: Boolean = false
)

enum class PacketColorType {
    TCP_SYN,      // #EAB308 (amarelo)
    TCP_SYN_ACK,  // #22C55E (verde)
    TCP_ACK,      // #3B82F6 (azul)
    TLS,          // #8B5CF6 (roxo)
    TCP_RST,      // #EF4444 (vermelho)
    ANOMALY,      // #F97316 (laranja)
    NORMAL        // Cinza padrão
}

enum class StatusIndicator {
    IDLE,
    ACTIVE,
    PAUSED,
}

object PacketColorPalette {
    val TCP_SYN = Color(0xFFEAB308)      // #EAB308 (amarelo)
    val TCP_SYN_ACK = Color(0xFF22C55E)  // #22C55E (verde)
    val TCP_ACK = Color(0xFF3B82F6)      // #3B82F6 (azul)
    val TLS = Color(0xFF8B5CF6)          // #8B5CF6 (roxo)
    val TCP_RST = Color(0xFFEF4444)      // #EF4444 (vermelho)
    val ANOMALY = Color(0xFFF97316)      // #F97316 (laranja)
    val NORMAL = Color(0xFF64748B)       // Cinza padrão

    val BACKGROUND_DARK = Color(0xFF0F172A)  // #0F172A (dark)
    val CARD_BACKGROUND = Color(0xFF1E293B)  // #1E293B (cards)

    fun getColorForType(type: PacketColorType): Color = when (type) {
        PacketColorType.TCP_SYN -> TCP_SYN
        PacketColorType.TCP_SYN_ACK -> TCP_SYN_ACK
        PacketColorType.TCP_ACK -> TCP_ACK
        PacketColorType.TLS -> TLS
        PacketColorType.TCP_RST -> TCP_RST
        PacketColorType.ANOMALY -> ANOMALY
        PacketColorType.NORMAL -> NORMAL
    }
}


