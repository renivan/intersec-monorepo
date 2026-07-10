package com.intersec.androidapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intersec.androidapp.di.AppBootstrap
import com.intersec.androidapp.presentation.state.ActionStatus
import com.intersec.androidapp.presentation.state.ActionType
import com.intersec.androidapp.presentation.state.CaptureActionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciar ações de captura em tempo real
 */
class CaptureActionsViewModel : ViewModel() {

    private val repository = AppBootstrap.coreAnalysisRepository

    private val _uiState = MutableStateFlow(CaptureActionUiState())
    val uiState: StateFlow<CaptureActionUiState> = _uiState.asStateFlow()

    fun startCapture(networkInterface: String = "wlan0", filter: String = "TCP.port == 443") {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    currentAction = ActionType.START_CAPTURE,
                    actionStatus = ActionStatus.IN_PROGRESS
                )
            }

            repository.startCapture(networkInterface, filter).fold(
                onSuccess = { sessionId ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentSessionId = sessionId,
                            isCapturing = true,
                            actionStatus = ActionStatus.SUCCESS,
                            statusMessage = "Captura iniciada: $sessionId",
                            captureInterface = networkInterface,
                            captureFilter = filter
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            actionStatus = ActionStatus.ERROR,
                            statusMessage = error.message ?: "Erro ao iniciar captura"
                        )
                    }
                }
            )
        }
    }

    fun stopCapture() {
        if (_uiState.value.currentSessionId == null) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    currentAction = ActionType.STOP_CAPTURE,
                    actionStatus = ActionStatus.IN_PROGRESS
                )
            }

            repository.stopCapture(_uiState.value.currentSessionId!!).fold(
                onSuccess = { snapshot ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isCapturing = false,
                            actionStatus = ActionStatus.SUCCESS,
                            statusMessage = "Captura parada. Total: ${snapshot.totalPackets} pacotes, ${snapshot.totalFlows} fluxos",
                            lastSnapshot = snapshot
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            actionStatus = ActionStatus.ERROR,
                            statusMessage = error.message ?: "Erro ao parar captura"
                        )
                    }
                }
            )
        }
    }

    fun refreshPackets() {
        if (_uiState.value.currentSessionId == null) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    currentAction = ActionType.REFRESH_PACKETS,
                    actionStatus = ActionStatus.IN_PROGRESS
                )
            }

            repository.capturePackets(_uiState.value.currentSessionId!!, 100).fold(
                onSuccess = { packets ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            actionStatus = ActionStatus.SUCCESS,
                            statusMessage = "Pacotes carregados: ${packets.size}",
                            currentPackets = packets
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            actionStatus = ActionStatus.ERROR,
                            statusMessage = error.message ?: "Erro ao carregar pacotes"
                        )
                    }
                }
            )
        }
    }

    fun exportCapture() {
        val sessionId = _uiState.value.currentSessionId ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    currentAction = ActionType.EXPORT_CAPTURE,
                    actionStatus = ActionStatus.IN_PROGRESS
                )
            }

            // TODO: Implementar exportação PCAP/JSON
            _uiState.update {
                it.copy(
                    isLoading = false,
                    actionStatus = ActionStatus.SUCCESS,
                    statusMessage = "Captura pronta para exportar"
                )
            }
        }
    }

    fun updateFilter(newFilter: String) {
        _uiState.update {
            it.copy(captureFilter = newFilter)
        }
    }
}



