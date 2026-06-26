package com.intersec.androidapp.presentation.state

import com.intersec.androidapp.core.bridge.NativePacketItem

data class PacketDetailUiState(
    val isLoading: Boolean = false,
    val item: NativePacketItem? = null,
    val errorMessage: String? = null
)

