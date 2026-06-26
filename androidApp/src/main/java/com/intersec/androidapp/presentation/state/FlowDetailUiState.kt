package com.intersec.androidapp.presentation.state

import com.intersec.androidapp.core.bridge.NativeFlowItem

data class FlowDetailUiState(
    val isLoading: Boolean = false,
    val item: NativeFlowItem? = null,
    val errorMessage: String? = null
)

