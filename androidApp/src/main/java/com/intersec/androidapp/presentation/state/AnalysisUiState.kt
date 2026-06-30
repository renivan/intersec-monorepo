package com.intersec.androidapp.presentation.state

import com.intersec.androidapp.data.model.dto.CaptureOverviewDto
import com.intersec.androidapp.data.model.dto.FlowDto
import com.intersec.androidapp.data.model.dto.PacketDto
import com.intersec.androidapp.data.model.dto.SessionDto
import com.intersec.androidapp.ui.theme.AppThemeType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

data class FirewallRule(
    val target: String,
    val reason: String,
    val action: String = "DROP",
    val timestamp: Long = System.currentTimeMillis()
)

data class NetworkState(
    val interfaceName: String = "Nenhuma",
    val typeName: String = "Desconectado",
    val details: String = "Aguardando Rede",
    val isConnected: Boolean = false
)

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
    val themeType: AppThemeType = AppThemeType.CYBER_INTERSECURITY,
    val isDarkMode: Boolean = true,
    val rewardedMinutesMonth: Int = 0,
    val showAdRewardDialog: Boolean = false,
    val firewallRules: List<FirewallRule> = emptyList(),
    val networkState: NetworkState = NetworkState()
)
