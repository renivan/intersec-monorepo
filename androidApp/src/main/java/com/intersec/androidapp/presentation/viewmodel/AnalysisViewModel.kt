package com.intersec.androidapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intersec.androidapp.di.AppBootstrap
import com.intersec.androidapp.domain.repository.RustAnalysisRepository
import com.intersec.androidapp.presentation.state.AnalysisUiState
import com.intersec.androidapp.presentation.state.ImportLogEntry
import com.intersec.androidapp.presentation.state.LogLevel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel principal unificado com suporte a log detalhado de importação.
 */
class AnalysisViewModel(
    private val repository: RustAnalysisRepository = AppBootstrap.rustAnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()
    private var pollJob: Job? = null

    init {
        startPolling()
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (true) {
                if (_uiState.value.session != null) {
                    refreshActiveSession()
                }
                delay(3000) // Poll a cada 3 segundos para o Dashboard
            }
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

            addLog("Chamando motor nativo Rust (JNI)...")
            repository.openCapture(path, now).fold(
                onSuccess = { snapshot ->
                    addLog("Sucesso nativo! Sessão criada: ${snapshot.sessionId}", LogLevel.SUCCESS)
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

}
