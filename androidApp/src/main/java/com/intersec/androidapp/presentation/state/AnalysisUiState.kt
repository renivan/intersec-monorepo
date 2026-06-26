package com.intersec.androidapp.presentation.state

import com.intersec.androidapp.data.model.dto.*
import com.intersec.androidapp.ui.theme.AppThemeType
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
    val importLogs: List<ImportLogEntry> = emptyList(),
    
    val isShieldActive: Boolean = true,
    val isKillSwitchOn: Boolean = false,
    val securityLevel: Int = 1,
    val userTier: Int = 0, // 0=FREE, 1=PRO
    val themeType: AppThemeType = AppThemeType.TACTICAL_MILITARY
)
