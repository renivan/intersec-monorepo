package com.intersec.androidapp.presentation.state

import com.intersec.androidapp.core.bridge.NativeFlowItem

data class FlowUiState(
    val isLoading: Boolean = false,
    val items: List<NativeFlowItem> = emptyList(),
    val totalItems: Long = 0L,
    val errorMessage: String? = null,
)

