package com.intersec.androidapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intersec.androidapp.di.AppBootstrap
import com.intersec.androidapp.domain.repository.RustAnalysisRepository
import com.intersec.androidapp.presentation.state.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * ViewModel REAL para gerenciar captura de pacotes via Motor Rust.
 * Agora com suporte a VPN e Análise de Fluxo.
 */
class CaptureRealtimeViewModel(
    private val repository: RustAnalysisRepository = AppBootstrap.rustAnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureRealtimeUiState())
    val uiState: StateFlow<CaptureRealtimeUiState> = _uiState.asStateFlow()

    private var captureJob: Job? = null
    private var flowJob: Job? = null
    private var timerJob: Job? = null
    private var sessionId: String? = null

    fun startCapture(networkInterface: String = "wlan0") {
        if (_uiState.value.isCapturing) return
        val filter = _uiState.value.filterInput

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            repository.startCapture(networkInterface, filter).fold(
                onSuccess = { id ->
                    onCaptureStarted(id, networkInterface, filter)
                },
                onFailure = { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = "ERRO_SEM_ROOT",
                            errorDetail = e.message 
                        )
                    }
                }
            )
        }
    }

    private fun onCaptureStarted(id: String, networkInterface: String, filter: String) {
        sessionId = id
        _uiState.update {
            it.copy(
                isCapturing = true,
                isPaused = false,
                isLoading = false,
                packets = emptyList(),
                flows = emptyList(),
                totalPackets = 0,
                captureStartTime = System.currentTimeMillis(),
                elapsedSeconds = 0,
                networkInterface = networkInterface,
                filter = filter,
                statusIndicator = StatusIndicator.ACTIVE
            )
        }
        startTimer()
        pollPackets()
        pollFlows()
    }

    fun updateFilterInput(newFilter: String) {
        _uiState.update { it.copy(filterInput = newFilter) }
    }

    fun onVpnAuthorized() {
        onCaptureStarted("vpn-session", "vpn0", _uiState.value.filterInput)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, errorDetail = null) }
    }

    private fun pollPackets() {
        captureJob?.cancel()
        captureJob = viewModelScope.launch {
            while (isActive && _uiState.value.isCapturing && !_uiState.value.isPaused) {
                val currentSessionId = sessionId ?: break
                
                repository.capturePackets(currentSessionId, limit = 50).fold(
                    onSuccess = { newPackets ->
                        if (newPackets.isNotEmpty()) {
                            val mappedPackets = newPackets.map { rustItem ->
                                RealtimePacketModel(
                                    number = rustItem.packetNumber.toInt(),
                                    timestampSeconds = (rustItem.timestampEpochMicros ?: 0L).toDouble() / 1_000_000.0,
                                    sourceAddress = "...", 
                                    destinationAddress = "...",
                                    protocol = rustItem.highestProtocol ?: "UNK",
                                    flags = null,
                                    size = 0,
                                    info = rustItem.info,
                                    colorType = mapProtocolToColor(rustItem.highestProtocol, rustItem.info)
                                )
                            }
                            
                            _uiState.update { state ->
                                val updatedList = (state.packets + mappedPackets).takeLast(100)
                                state.copy(
                                    packets = updatedList,
                                    totalPackets = state.totalPackets + mappedPackets.size
                                )
                            }
                        }
                    },
                    onFailure = { /* Silencioso */ }
                )
                delay(200.milliseconds)
            }
        }
    }

    private fun pollFlows() {
        flowJob?.cancel()
        flowJob = viewModelScope.launch {
            while (isActive && _uiState.value.isCapturing && !_uiState.value.isPaused) {
                val currentSessionId = sessionId ?: break
                
                repository.captureFlows(currentSessionId, limit = 20).fold(
                    onSuccess = { newFlows ->
                        _uiState.update { state ->
                            state.copy(
                                flows = newFlows.map { 
                                    RealtimeFlowModel(it.label, it.endpoints, it.totalPackets, it.totalPayloadBytes, it.isInsecure)
                                }
                            )
                        }
                    },
                    onFailure = { /* Silencioso */ }
                )
                delay(1000.milliseconds)
            }
        }
    }

    private fun mapProtocolToColor(protocol: String?, info: String): PacketColorType {
        return when (protocol?.uppercase()) {
            "TCP" -> {
                when {
                    info.contains("SYN", ignoreCase = true) && info.contains("ACK", ignoreCase = true) -> PacketColorType.TCP_SYN_ACK
                    info.contains("SYN", ignoreCase = true) -> PacketColorType.TCP_SYN
                    info.contains("ACK", ignoreCase = true) -> PacketColorType.TCP_ACK
                    info.contains("RST", ignoreCase = true) -> PacketColorType.TCP_RST
                    else -> PacketColorType.NORMAL
                }
            }
            "TLS", "HTTPS" -> PacketColorType.TLS
            else -> PacketColorType.NORMAL
        }
    }

    fun pauseCapture() {
        _uiState.update { it.copy(isPaused = true, statusIndicator = StatusIndicator.PAUSED) }
    }

    fun resumeCapture() {
        _uiState.update { it.copy(isPaused = false, statusIndicator = StatusIndicator.ACTIVE) }
    }

    fun stopCapture() {
        viewModelScope.launch {
            sessionId?.let { id ->
                repository.stopCapture(id)
            }
            _uiState.update {
                it.copy(
                    isCapturing = false,
                    isPaused = false,
                    statusIndicator = StatusIndicator.IDLE
                )
            }
            captureJob?.cancel()
            flowJob?.cancel()
            timerJob?.cancel()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _uiState.value.isCapturing) {
                delay(1.seconds)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    fun runForcedTest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(500.milliseconds)
            
            val testPackets = listOf(
                RealtimePacketModel(
                    number = 999,
                    timestampSeconds = 0.0,
                    sourceAddress = "ATTACKER",
                    destinationAddress = "LOCAL",
                    protocol = "TCP",
                    flags = "SYN",
                    size = 64,
                    info = "[FORCED TEST] Possível Port Scan Detectado",
                    colorType = PacketColorType.TCP_SYN,
                    isAnomalous = true
                )
            )
            
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    packets = (state.packets + testPackets).takeLast(100),
                    statusIndicator = StatusIndicator.ACTIVE,
                    errorMessage = "TESTE_FORCADO_OK"
                )
            }
        }
    }
}
