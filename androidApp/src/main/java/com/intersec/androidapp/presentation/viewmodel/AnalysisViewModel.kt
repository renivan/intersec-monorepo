package com.intersec.androidapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intersec.androidapp.app.MainApplication
import com.intersec.androidapp.core.storage.SecuritySettingsManager
import com.intersec.androidapp.di.AppBootstrap
import com.intersec.androidapp.domain.repository.CoreAnalysisRepository
import com.intersec.androidapp.presentation.state.AnalysisUiState
import com.intersec.androidapp.presentation.state.ImportLogEntry
import com.intersec.androidapp.presentation.state.LogLevel
import com.intersec.androidapp.core.network.ThreatIntelManager
import com.intersec.androidapp.core.network.NetworkInspector
import com.intersec.androidapp.core.ads.AdManager
import com.intersec.androidapp.ui.theme.AppThemeType
import com.intersec.androidapp.presentation.state.NetworkState
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
 * ViewModel principal unificado com suporte a log detalhado de importação e configurações de segurança.
 */
class AnalysisViewModel(
    private val repository: CoreAnalysisRepository = AppBootstrap.coreAnalysisRepository,
    private val securitySettings: SecuritySettingsManager = MainApplication.appModule.securitySettingsManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()
    private var pollJob: Job? = null

    init {
        startPolling()
        observeSecuritySettings()
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

        securitySettings.rewardedMinutesMonth.onEach { mins ->
            _uiState.update { it.copy(rewardedMinutesMonth = mins) }
        }.launchIn(viewModelScope)

        securitySettings.isDarkMode.onEach { isDark ->
            _uiState.update { it.copy(isDarkMode = isDark) }
        }.launchIn(viewModelScope)
    }

    private fun syncSecurityWithNative() {
        val state = _uiState.value
        viewModelScope.launch {
            repository.updateSecuritySettings(
                level = state.securityLevel,
                smartShield = state.isShieldActive,
                killSwitch = state.isKillSwitchOn
            ).onFailure { e ->
                addLog("Aviso: Falha ao sincronizar configurações com o motor: ${e.message}", LogLevel.WARNING)
            }
        }
    }

    fun toggleSmartShield(active: Boolean) {
        viewModelScope.launch {
            securitySettings.setSmartShield(active)
        }
    }

    fun toggleKillSwitch(active: Boolean) {
        viewModelScope.launch {
            securitySettings.setKillSwitch(active)
        }
    }

    fun updateSecurityLevel(level: Int) {
        viewModelScope.launch {
            securitySettings.setSecurityLevel(level)
        }
    }

    fun upgradeToPro() {
        viewModelScope.launch {
            securitySettings.setUserTier(1)
        }
    }

    fun updateTheme(theme: AppThemeType) {
        viewModelScope.launch {
            securitySettings.setThemeType(theme.id)
        }
    }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            securitySettings.setDarkMode(isDark)
        }
    }

    fun addRewardTime() {
        viewModelScope.launch {
            val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
            securitySettings.addRewardedMinute(currentMonth)
        }
    }

    fun setShowAdRewardDialog(show: Boolean) {
        _uiState.update { it.copy(showAdRewardDialog = show) }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (true) {
                updateNetworkStatus()
                delay(1000.milliseconds)
                val state = _uiState.value
                // Só atualiza se houver uma sessão ativa e não estiver em processo de carga inicial
                if ((state.session != null && !state.isLoading)) {
                    refreshActiveSession()
                }
            }
        }
    }

    private fun updateNetworkStatus() {
        val info = NetworkInspector.getActiveNetworkInfo(MainApplication.instance)
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
    }

    fun addLog(message: String, level: LogLevel = LogLevel.INFO) {
        _uiState.update { it.copy(importLogs = it.importLogs + ImportLogEntry(message = message, level = level)) }
    }

    fun openCapture(path: String) {
        addLog("Iniciando importação do arquivo: $path")
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val now = System.currentTimeMillis() * 1000L

            addLog("Chamando motor nativo (JNI)...")
            repository.openCapture(path, now).fold(
                onSuccess = { snapshot ->
                    addLog("sucesso nativo! Sessão criada: ${snapshot.sessionId}", LogLevel.SUCCESS)
                    addLog("Métricas iniciais: ${snapshot.totalPackets} pacotes, ${snapshot.totalFlows} fluxos.")
                    
                    val sessionDto = com.intersec.androidapp.data.model.dto.SessionDto(
                        sessionId = snapshot.sessionId,
                        sourceName = snapshot.sourceName,
                        packetCount = snapshot.totalPackets,
                        flowCount = snapshot.totalFlows
                    )
                    
                    // Carregar Overview Real
                    repository.getOverview().fold(
                        onSuccess = { overview ->
                            _uiState.update { it.copy(isLoading = false, session = sessionDto, overview = overview) }
                        },
                        onFailure = {
                            _uiState.update { it.copy(isLoading = false, session = sessionDto) }
                        }
                    )
                },
                onFailure = { e ->
                    val errorMsg = e.message ?: "Erro desconhecido"
                    addLog("FALHA NATIVA: $errorMsg", LogLevel.ERROR)
                    _uiState.update { it.copy(isLoading = false, session = null, error = errorMsg) }
                }
            )
        }
    }

    fun clearLogs() {
        _uiState.update { it.copy(importLogs = emptyList()) }
    }

    fun refreshActiveSession() {
        addLog("Atualizando estado da sessão...")
        viewModelScope.launch {
            val startTime = System.nanoTime()
            repository.snapshotActive().fold(
                onSuccess = { snapshot ->
                    val snapshotTime = (System.nanoTime() - startTime) / 1_000_000.0
                    addLog("Snapshot obtido em ${"%.2f".format(snapshotTime)}ms")
                    
                    val sessionDto = com.intersec.androidapp.data.model.dto.SessionDto(
                        sessionId = snapshot.sessionId,
                        sourceName = snapshot.sourceName,
                        packetCount = snapshot.totalPackets,
                        flowCount = snapshot.totalFlows
                    )
                    
                    // Carregar Overview Real
                    val ovStart = System.nanoTime()
                    repository.getOverview().fold(
                        onSuccess = { overview ->
                            val ovTime = (System.nanoTime() - ovStart) / 1_000_000.0
                            addLog("Overview real carregado em ${"%.2f".format(ovTime)}ms", LogLevel.SUCCESS)
                            _uiState.update { it.copy(session = sessionDto, overview = overview) }
                        },
                        onFailure = { e ->
                            addLog("Aviso: Falha ao carregar overview real: ${e.message}", LogLevel.WARNING)
                            _uiState.update { it.copy(session = sessionDto) }
                        }
                    )
                },
                onFailure = { e ->
                    addLog("Erro ao obter snapshot: ${e.message}", LogLevel.WARNING)
                }
            )
        }
    }

    fun clearSession() {
        addLog("Sessão limpa pelo usuário.")
        _uiState.value = AnalysisUiState()
    }

    fun blockIp(ip: String, reason: String = "MANUAL BLOCK") {
        val newRule = com.intersec.androidapp.presentation.state.FirewallRule(target = ip, reason = reason)
        _uiState.update { it.copy(firewallRules = it.firewallRules + newRule) }
        addLog("Firewall: IP $ip bloqueado. Motivo: $reason", LogLevel.WARNING)
        // TODO: Persistir e sincronizar com o motor nativo
    }

    fun removeFirewallRule(rule: com.intersec.androidapp.presentation.state.FirewallRule) {
        _uiState.update { it.copy(firewallRules = it.firewallRules - rule) }
        addLog("Firewall: Regra para ${rule.target} removida.", LogLevel.INFO)
    }

}

