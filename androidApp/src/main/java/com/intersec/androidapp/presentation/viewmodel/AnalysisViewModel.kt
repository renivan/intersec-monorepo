package com.intersec.androidapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intersec.androidapp.app.MainApplication
import com.intersec.androidapp.core.network.NetworkInspector
import com.intersec.androidapp.core.network.ThreatIntelManager
import com.intersec.androidapp.core.storage.SecuritySettingsManager
import com.intersec.androidapp.di.AppBootstrap
import com.intersec.androidapp.domain.repository.CoreAnalysisRepository
import com.intersec.androidapp.presentation.state.AnalysisUiState
import com.intersec.androidapp.presentation.state.ImportLogEntry
import com.intersec.androidapp.presentation.state.LogLevel
import com.intersec.androidapp.presentation.state.NetworkState
import com.intersec.androidapp.ui.theme.AppThemeType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel principal unificado (Hardened v3.0).
 * Integra Motor Neural Rust, Geo-Localização e Segurança Premium.
 */
class AnalysisViewModel(
    private val repository: CoreAnalysisRepository = AppBootstrap.coreAnalysisRepository,
    private val securitySettings: SecuritySettingsManager = MainApplication.appModule.securitySettingsManager,
    private val locationTracker: com.intersec.androidapp.core.location.LocationTracker = MainApplication.appModule.locationTracker,
    private val tierManager: com.intersec.androidapp.core.auth.TierManager = MainApplication.appModule.tierManager,
    private val neuralEngine: com.intersec.androidapp.core.neural.NeuralCoreEngine = MainApplication.appModule.neuralCoreEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()
    private var pollJob: Job? = null

    init {
        startPolling()
        observeSecuritySettings()
        observeLocation()
        observeNeuralStream()
        tierManager.syncAndValidate()
    }

    private fun observeLocation() {
        locationTracker.currentLocation.onEach { location ->
            location?.let {
                _uiState.update { it.copy(lastLatitude = location.latitude, lastLongitude = location.longitude) }
            }
        }.launchIn(viewModelScope)
    }

    private fun observeNeuralStream() {
        neuralEngine.neuralStream.onEach { links ->
            _uiState.update { it.copy(neuralLinks = links) }
        }.launchIn(viewModelScope)
    }

    private fun observeSecuritySettings() {
        securitySettings.smartShieldActive.onEach { active ->
            _uiState.update { it.copy(isShieldActive = active) }
            syncSecurityWithNative()
        }.launchIn(viewModelScope)

        securitySettings.killSwitchActive.onEach { active ->
            _uiState.update { it.copy(isKillSwitchOn = active) }
            syncSecurityWithNative()
        }.launchIn(viewModelScope)

        securitySettings.securityLevel.onEach { level ->
            _uiState.update { it.copy(securityLevel = level) }
            syncSecurityWithNative()
        }.launchIn(viewModelScope)

        securitySettings.userTier.onEach { tier ->
            _uiState.update { it.copy(userTier = tier) }
            ThreatIntelManager.syncThreatFeeds(tier == 1)
        }.launchIn(viewModelScope)

        securitySettings.themeType.onEach { typeId ->
            val theme = AppThemeType.entries.find { it.id == typeId } ?: AppThemeType.CYBER_INTERSECURITY
            _uiState.update { it.copy(themeType = theme) }
        }.launchIn(viewModelScope)

        securitySettings.isDarkMode.onEach { isDark ->
            _uiState.update { it.copy(isDarkMode = isDark) }
        }.launchIn(viewModelScope)

        securitySettings.firewallRules.onEach { rulesSet ->
            val rules = rulesSet.map {
                val parts = it.split("|")
                com.intersec.androidapp.presentation.state.FirewallRule(
                    target = parts.getOrNull(0) ?: "",
                    reason = parts.getOrNull(1) ?: "MANUAL BLOCK"
                )
            }
            _uiState.update { it.copy(firewallRules = rules) }
        }.launchIn(viewModelScope)
    }

    private fun syncSecurityWithNative() {
        val state = _uiState.value
        viewModelScope.launch {
            repository.updateSecuritySettings(
                level = state.securityLevel,
                smartShield = state.isShieldActive,
                killSwitch = state.isKillSwitchOn
            )
        }
    }

    fun toggleSmartShield(active: Boolean) {
        viewModelScope.launch { securitySettings.setSmartShield(active) }
    }

    fun toggleKillSwitch(active: Boolean) {
        viewModelScope.launch { securitySettings.setKillSwitch(active) }
    }

    fun updateSecurityLevel(level: Int) {
        viewModelScope.launch { securitySettings.setSecurityLevel(level) }
    }

    fun upgradeToPro() {
        if (_uiState.value.isLoading) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            tierManager.performUpgrade()
            addLog("Upgrade: Acesso PREMIUM ativado e motor validado.", LogLevel.SUCCESS)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun startRealtimeLocation() {
        locationTracker.startTracking()
    }

    fun stopRealtimeLocation() {
        locationTracker.stopTracking()
    }

    fun updateTheme(theme: AppThemeType) {
        viewModelScope.launch { securitySettings.setThemeType(theme.id) }
    }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch { securitySettings.setDarkMode(isDark) }
    }

    fun setShowAdRewardDialog(show: Boolean) {
        _uiState.update { it.copy(showAdRewardDialog = show) }
    }

    fun addRewardTime() {
        viewModelScope.launch {
            val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
            securitySettings.addRewardedMinute(currentMonth)
            addLog("Recompensa: Tempo adicional de monitoramento PREMIUM desbloqueado.", LogLevel.SUCCESS)
        }
    }

    fun openCapture(path: String) {
        addLog("Iniciando processamento neural de arquivo: $path")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.openCapture(path, System.currentTimeMillis() * 1000L).fold(
                onSuccess = { snapshot ->
                    addLog("Sessão carregada: ${snapshot.sessionId}", LogLevel.SUCCESS)
                    refreshActiveSession()
                },
                onFailure = { e ->
                    addLog("Falha ao abrir captura: ${e.message}", LogLevel.ERROR)
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun clearSession() {
        addLog("Sessão terminada e memória limpa.")
        _uiState.value = AnalysisUiState(userTier = _uiState.value.userTier)
    }

    fun clearLogs() {
        _uiState.update { it.copy(importLogs = emptyList()) }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (true) {
                updateNetworkStatus()
                delay(1000.milliseconds)
                val state = _uiState.value
                if ((state.session != null && !state.isLoading)) {
                    refreshActiveSession()
                }
            }
        }
    }

    private fun updateNetworkStatus() {
        try {
            val context = MainApplication.instance
            val info = NetworkInspector.getActiveNetworkInfo(context)
            _uiState.update { 
                it.copy(
                    networkState = NetworkState(
                        interfaceName = info.interfaceName,
                        typeName = info.typeName,
                        details = info.details,
                        isConnected = info.isConnected
                    )
                )
            }
        } catch (_: Exception) {}
    }

    fun addLog(message: String, level: LogLevel = LogLevel.INFO) {
        _uiState.update { it.copy(importLogs = it.importLogs + ImportLogEntry(message = message, level = level)) }
    }

    fun refreshActiveSession() {
        viewModelScope.launch {
            repository.snapshotActive().fold(
                onSuccess = { snapshot ->
                    val sessionDto = com.intersec.androidapp.data.model.dto.SessionDto(
                        sessionId = snapshot.sessionId,
                        sourceName = snapshot.sourceName,
                        packetCount = snapshot.totalPackets,
                        flowCount = snapshot.totalFlows
                    )
                    
                    repository.getOverview().fold(
                        onSuccess = { overview ->
                            // Alimenta o Motor Neural Rust com dados reais de transporte
                            overview.topCommunications.forEach { comm ->
                                val lat = _uiState.value.lastLatitude ?: 0.0
                                val lon = _uiState.value.lastLongitude ?: 0.0
                                neuralEngine.processConnection(comm.destination, "TCP", comm.volumeBytes)
                            }
                            _uiState.update { it.copy(session = sessionDto, overview = overview) }
                        },
                        onFailure = { _uiState.update { it.copy(session = sessionDto) } }
                    )
                },
                onFailure = {}
            )
        }
    }

    fun blockIp(ip: String, reason: String = "MANUAL BLOCK") {
        if (_uiState.value.userTier != 1) return
        viewModelScope.launch {
            securitySettings.addFirewallRule(ip, reason)
            addLog("Firewall: IP $ip bloqueado.", LogLevel.WARNING)
        }
    }

    /**
     * Inicia a inspeção profunda de um nó no mapa 3D.
     * Recupera o payload e detalhes técnicos do transporte.
     */
    fun inspectNeuralLink(link: com.intersec.androidapp.presentation.state.NeuralLink3D?) {
        _uiState.update { it.copy(selectedNeuralLink = link, inspectedPacketPayload = null) }
        
        link?.let {
            addLog("Inspecionando conexão neural: ${link.destIp} [${link.protocol}]")
            
            // Simulação de decodificação profunda para a UI 3D
            // Na FASE FINAL, isso buscará o payload real via repository.getPacketDetail()
            viewModelScope.launch {
                delay(500.milliseconds)
                val mockPayload = "HEX: 45 00 00 3c 1c 46 40 00 40 06 ...\n" +
                                 "DATA: GET /api/v1/intelligence HTTP/1.1\n" +
                                 "USER-AGENT: interSec-Guardian/3.0\n" +
                                 "STATUS: ENCRYPTED_TLS_V1.3"
                _uiState.update { it.copy(inspectedPacketPayload = mockPayload) }
            }
        }
    }

    fun removeFirewallRule(rule: com.intersec.androidapp.presentation.state.FirewallRule) {
        viewModelScope.launch {
            securitySettings.removeFirewallRule("${rule.target}|${rule.reason}")
        }
    }
}
