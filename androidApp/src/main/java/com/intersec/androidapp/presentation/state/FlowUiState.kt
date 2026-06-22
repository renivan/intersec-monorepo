package com.intersec.androidapp.presentation.state

import com.intersec.androidapp.core.bridge.RustFlowItem

data class FlowUiState(
    val isLoading: Boolean = false,
    val items: List<RustFlowItem> = emptyList(),
    val totalItems: Long = 0L,
    val errorMessage: String? = null,
)
