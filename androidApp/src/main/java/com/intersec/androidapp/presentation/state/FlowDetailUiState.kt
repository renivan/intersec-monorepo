package com.intersec.androidapp.presentation.state

import com.intersec.androidapp.core.bridge.RustFlowItem

data class FlowDetailUiState(
    val isLoading: Boolean = false,
    val item: RustFlowItem? = null,
    val errorMessage: String? = null
)
