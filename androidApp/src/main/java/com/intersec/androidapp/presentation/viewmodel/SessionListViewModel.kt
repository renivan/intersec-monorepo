package com.intersec.androidapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intersec.androidapp.di.AppBootstrap
import com.intersec.androidapp.presentation.state.SessionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionListViewModel : ViewModel() {

    private val repository = AppBootstrap.coreAnalysisRepository

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                infoMessage = null,
            )

            val result = repository.listStoredSessions()
            result.fold(
                onSuccess = { sessions ->
                    _uiState.value = SessionUiState(
                        isLoading = false,
                        isSaving = false,
                        items = sessions,
                        errorMessage = null,
                        infoMessage = null,
                    )
                },
                onFailure = { error ->
                    _uiState.value = SessionUiState(
                        isLoading = false,
                        isSaving = false,
                        items = emptyList(),
                        errorMessage = error.message ?: "Falha ao carregar sessões.",
                        infoMessage = null,
                    )
                },
            )
        }
    }

    fun persistActiveSession(
        tagsCsv: String,
        notes: String?,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null,
                infoMessage = null,
            )

            val result = repository.persistActive(tagsCsv, notes)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = null,
                        infoMessage = "Sessão ativa salva com sucesso.",
                    )
                    loadSessions()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Falha ao salvar sessão ativa.",
                        infoMessage = null,
                    )
                },
            )
        }
    }

}

