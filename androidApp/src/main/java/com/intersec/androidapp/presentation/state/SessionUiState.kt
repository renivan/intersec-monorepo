package com.intersec.androidapp.presentation.state

import com.intersec.androidapp.core.bridge.NativeStoredSession

data class SessionUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val items: List<NativeStoredSession> = emptyList(),
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)

