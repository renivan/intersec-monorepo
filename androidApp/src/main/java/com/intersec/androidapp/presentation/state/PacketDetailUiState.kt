package com.intersec.androidapp.presentation.state

import com.intersec.androidapp.core.bridge.RustPacketItem

data class PacketDetailUiState(
    val isLoading: Boolean = false,
    val item: RustPacketItem? = null,
    val errorMessage: String? = null
)
