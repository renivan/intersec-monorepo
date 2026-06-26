package com.intersec.androidapp.presentation.state

data class AuthUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
