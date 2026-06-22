package com.intersec.androidapp.presentation.state

data class DiagnosticUiState(
    val isLibraryLoaded: Boolean = false,
    val pingResult: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
