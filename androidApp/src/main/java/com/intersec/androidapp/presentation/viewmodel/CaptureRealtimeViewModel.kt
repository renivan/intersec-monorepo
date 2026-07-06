package com.intersec.androidapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intersec.androidapp.di.AppBootstrap
import com.intersec.androidapp.domain.repository.CoreAnalysisRepository
import com.intersec.androidapp.core.network.NetworkInspector
import com.intersec.androidapp.app.MainApplication
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
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * ViewModel REAL para gerenciar captura de pacotes via Motor Native.
 * Agora com suporte a VPN e Análise de Fluxo.
 */
class CaptureRealtimeViewModel(
    private val repository: CoreAnalysisRepository = AppBootstrap.coreAnalysisRepository,
    private val neuralEngine: com.intersec.androidapp.core.neural.NeuralCoreEngine = com.intersec.androidapp.app.MainApplication.appModule.neuralCoreEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureRealtimeUiState())
    val uiState: StateFlow<CaptureRealtimeUiState> = _uiState.asStateFlow()

    private var captureJob: Job? = null
    private var flowJob: Job? = null
    private var timerJob: Job? = null
    private var overviewJob: Job? = null
    private var purgeJob: Job? = null
    private var sessionId: String? = null

    init {
        detectNetwork()
        syncWithNative()
    }

    private fun syncWithNative() {
        viewModelScope.launch {
            repository.snapshotActive().onSuccess { snapshot ->
                if (snapshot.sessionId.isNotEmpty() && !snapshot.sessionId.contains("null")) {
                    onCaptureStarted(snapshot.sessionId, snapshot.sourceName, "Restored Session")
                }
            }
        }
    }

    private fun detectNetwork() {
        viewModelScope.launch {
            while (isActive) {
                val info = NetworkInspector.getActiveNetworkInfo(MainApplication.instance)
                val allInterfaces = NetworkInspector.getAvailableInterfaces()
                _uiState.update { 
                    it.copy(
                        networkInterface = info.interfaceName,
                        networkName = "${info.typeName} - ${info.details}",
                        availableInterfaces = allInterfaces
                    )
                }
                delay(2.seconds)
            }
        }
    }

    fun requestVpnAuthorization() {
        _uiState.update { it.copy(showVpnTerms = true) }
    }

    fun acceptVpnTerms() {
        _uiState.update { it.copy(showVpnTerms = false) }
        // O acionamento real da intent de VPN será feito na View via callback
    }

    fun startCapture(networkInterface: String = _uiState.value.networkInterface) {
        if (_uiState.value.isCapturing) return
        val filter = _uiState.value.filterInput

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // Tenta wlan0 (Wi-Fi real) e fallback para eth0 (emulador/ethernet)
            repository.startCapture(networkInterface, filter).fold(
                onSuccess = { id ->
                    onCaptureStarted(id, networkInterface, filter)
                },
                onFailure = { 
                    repository.startCapture("eth0", filter).fold(
                        onSuccess = { id -> onCaptureStarted(id, "eth0", filter) },
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
        pollOverview()
        setupMaintenanceJobs()
    }

    private fun setupMaintenanceJobs() {
        val tierFlow = MainApplication.appModule.securitySettingsManager.userTier
        viewModelScope.launch {
            tierFlow.collect { tier ->
                _uiState.update { it.copy(userTier = tier) }
                if (tier == 0) { // FREE Tier
                    startAutoPurgeTask()
                } else {
                    purgeJob?.cancel()
                }
            }
        }
    }

    private fun startAutoPurgeTask() {
        purgeJob?.cancel()
        purgeJob = viewModelScope.launch {
            while (isActive) {
                delay(10.minutes)
                if (_uiState.value.isCapturing) {
                    // Limpeza de cache no motor a cada 10 min para usuários FREE
                    sessionId?.let { id ->
                        repository.stopCapture(id)
                        repository.startCapture(_uiState.value.networkInterface, _uiState.value.filterInput)
                    }
                }
            }
        }
    }

    fun updateFilterInput(newFilter: String) {
        _uiState.update { it.copy(filterInput = newFilter) }
    }

    fun onVpnAuthorized() {
        onCaptureStarted("vpn-session", "vpn0", _uiState.value.filterInput)
    }

    fun toggleTier() {
        val nextTier = if (_uiState.value.userTier == 0) 1 else 0
        viewModelScope.launch {
            MainApplication.appModule.securitySettingsManager.setUserTier(nextTier)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, errorDetail = null) }
    }

    private fun pollPackets() {
        captureJob?.cancel()
        captureJob = viewModelScope.launch {
            while (isActive && _uiState.value.isCapturing && !_uiState.value.isPaused) {
                try {
                    val currentSessionId = sessionId ?: break
                    
                    repository.capturePackets(currentSessionId, limit = 50).fold(
                        onSuccess = { newPackets ->
                            if (newPackets.isNotEmpty()) {
                                processPackets(newPackets)
                            }
                        },
                        onFailure = { e ->
                            android.util.Log.e("CaptureVM", "Erro ao coletar pacotes: ${e.message}")
                        }
                    )
                } catch (e: Exception) {
                    android.util.Log.e("CaptureVM", "Falha crítica no polling de pacotes: ${e.message}")
                }
                delay(200.milliseconds)
            }
        }
    }

    private fun processPackets(newPackets: List<com.intersec.androidapp.core.bridge.NativePacketItem>) {
        val mappedPackets = newPackets.map { nativeItem ->
            // Alimenta o motor neural 3D com dados reais de fluxo
            nativeItem.highestProtocol?.let { proto ->
                neuralEngine.processConnection("EXT_NODE", proto, 64L)
            }

            RealtimePacketModel(
                number = nativeItem.packetNumber.toInt(),
                timestampSeconds = (nativeItem.timestampEpochMicros ?: 0L).toDouble() / 1_000_000.0,
                sourceAddress = "...", 
                destinationAddress = "...",
                protocol = nativeItem.highestProtocol ?: "UNK",
                flags = null,
                size = 0,
                info = nativeItem.info,
                colorType = mapProtocolToColor(nativeItem.highestProtocol, nativeItem.info)
            )
        }
        
        _uiState.update { state ->
            val updatedList = (state.packets + mappedPackets).takeLast(100)
            
            // Regra FREE: Expira cache local após 5 minutos
            val currentTime = System.currentTimeMillis()
            val finalPackets = if (state.userTier == 0 && (currentTime - state.captureStartTime > 5 * 60 * 1000)) {
                updatedList.takeLast(10) // Mantém apenas o rastro mínimo
            } else {
                updatedList
            }

            state.copy(
                packets = finalPackets,
                totalPackets = state.totalPackets + mappedPackets.size
            )
        }
    }

    private fun pollFlows() {
        flowJob?.cancel()
        flowJob = viewModelScope.launch {
            while (isActive && _uiState.value.isCapturing && !_uiState.value.isPaused) {
                try {
                    val currentSessionId = sessionId ?: break
                    
                    repository.captureFlows(currentSessionId, limit = 20).fold(
                        onSuccess = { newFlows ->
                            _uiState.update { state ->
                                val updatedFlows = newFlows.map { 
                                    RealtimeFlowModel(it.label, it.endpoints, it.totalPackets, it.totalPayloadBytes, it.isInsecure)
                                }
                                state.copy(
                                    flows = updatedFlows,
                                    neuralNodes = generateNeuralNodes(updatedFlows)
                                )
                            }
                        },
                        onFailure = { e ->
                            android.util.Log.e("CaptureVM", "Erro ao coletar fluxos: ${e.message}")
                        }
                    )
                } catch (e: Exception) {
                    android.util.Log.e("CaptureVM", "Falha crítica no polling de fluxos: ${e.message}")
                }
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

    private fun pollOverview() {
        overviewJob?.cancel()
        overviewJob = viewModelScope.launch {
            while (isActive && _uiState.value.isCapturing) {
                repository.getOverview().onSuccess { overview ->
                    _uiState.update { it.copy(totalBytes = overview.totalVolumeBytes) }
                }
                delay(2000.milliseconds)
            }
        }
    }

    private fun generateNeuralNodes(flows: List<RealtimeFlowModel>): List<NeuralNodeModel> {
        return flows.take(8).mapIndexed { index, flow ->
            val angle = (index * (360f / 8f)) * (Math.PI / 180f).toFloat()
            val radius = 0.35f
            NeuralNodeModel(
                id = flow.label,
                x = 0.5f + radius * kotlin.math.cos(angle),
                y = 0.5f + radius * kotlin.math.sin(angle),
                intensity = (flow.packetCount.toFloat() / 100f).coerceIn(0.2f, 1.0f),
                connections = listOf("core")
            )
        }
    }

    fun onInterfaceSelected(name: String, type: String) {
        _uiState.update { it.copy(networkInterface = name, networkName = type) }
    }

    fun pauseCapture() {
        _uiState.update { it.copy(isPaused = true, statusIndicator = StatusIndicator.PAUSED, showSummaryModal = true) }
    }

    fun resumeCapture() {
        _uiState.update { it.copy(isPaused = false, statusIndicator = StatusIndicator.ACTIVE, showSummaryModal = false) }
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
                    statusIndicator = StatusIndicator.IDLE,
                    showSummaryModal = true
                )
            }
            captureJob?.cancel()
            flowJob?.cancel()
            timerJob?.cancel()
            overviewJob?.cancel()
        }
    }

    fun discardCapture() {
        viewModelScope.launch {
            sessionId?.let { repository.stopCapture(it) }
            sessionId = null
            _uiState.value = CaptureRealtimeUiState()
            captureJob?.cancel()
            flowJob?.cancel()
            timerJob?.cancel()
            overviewJob?.cancel()
        }
    }

    fun hideSummary() {
        _uiState.update { it.copy(showSummaryModal = false) }
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
}

