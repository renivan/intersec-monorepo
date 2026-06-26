package com.intersec.androidapp.presentation.state

import com.intersec.androidapp.core.bridge.NativeSessionSnapshot
import com.intersec.androidapp.core.bridge.NativePacketItem

/**
 * Estado da UI para ações de captura em tempo real
 */
data class CaptureActionUiState(
    val isLoading: Boolean = false,
    val isCapturing: Boolean = false,
    val currentSessionId: String? = null,
    val captureInterface: String = "wlan0",
    val captureFilter: String = "TCP.port == 443",
    val actionStatus: ActionStatus = ActionStatus.IDLE,
    val currentAction: ActionType? = null,
    val statusMessage: String? = null,
    val lastSnapshot: NativeSessionSnapshot? = null,
    val currentPackets: List<NativePacketItem> = emptyList(),
    val errorMessage: String? = null
)

enum class ActionStatus {
    IDLE,
    IN_PROGRESS,
    SUCCESS,
    ERROR
}

enum class ActionType {
    START_CAPTURE,
    STOP_CAPTURE,
    REFRESH_PACKETS,
    EXPORT_CAPTURE,
}


