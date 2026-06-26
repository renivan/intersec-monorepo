package com.intersec.androidapp.presentation.state

import com.intersec.androidapp.core.bridge.NativePacketItem

data class PacketUiState(
    val isLoading: Boolean = false,
    val items: List<NativePacketItem> = emptyList(),
    val totalItems: Long = 0L,
    val errorMessage: String? = null,
)

