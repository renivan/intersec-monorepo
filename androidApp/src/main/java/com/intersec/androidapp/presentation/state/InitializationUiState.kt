package com.intersec.androidapp.presentation.state

/**
 * Estado da tela de inicialização profissional (Health Check)
 */
data class InitializationUiState(
    val status: String = "Iniciando sistemas...",
    val progress: Float = 0.0f,
    val isNativeLoaded: Boolean = false,
    val isPersistenceReady: Boolean = false,
    val isAdsReady: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null
)
