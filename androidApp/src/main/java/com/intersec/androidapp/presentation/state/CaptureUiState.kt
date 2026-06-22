package com.intersec.androidapp.presentation.state

import com.intersec.androidapp.core.bridge.RustSessionSnapshot

data class CaptureUiState(
    val isLoading: Boolean = false,
    val session: RustSessionSnapshot? = null,
    val errorMessage: String? = null,
)
