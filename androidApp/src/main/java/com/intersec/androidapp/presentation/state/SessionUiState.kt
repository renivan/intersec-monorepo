package com.intersec.androidapp.presentation.state

import com.intersec.androidapp.core.bridge.RustStoredSession

data class SessionUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val items: List<RustStoredSession> = emptyList(),
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)
