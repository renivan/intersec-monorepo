package com.intersec.androidapp.presentation.state

import com.intersec.androidapp.data.model.dto.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Representa uma entrada individual no log de importação.
 * Usando SimpleDateFormat para compatibilidade com API 24+.
 */
data class ImportLogEntry(
    val timestamp: String = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date()),
    val level: LogLevel = LogLevel.INFO,
    val message: String
)

enum class LogLevel { INFO, WARNING, ERROR, SUCCESS }

/**
 * Estado unificado com suporte a rastreamento de logs.
 */
data class AnalysisUiState(
    val isLoading: Boolean = false,
    val session: SessionDto? = null,
    val overview: CaptureOverviewDto? = null,
    val packets: List<PacketDto> = emptyList(),
    val flows: List<FlowDto> = emptyList(),
    val error: String? = null,
    val importLogs: List<ImportLogEntry> = emptyList()
)
