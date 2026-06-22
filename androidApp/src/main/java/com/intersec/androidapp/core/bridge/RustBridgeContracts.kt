package com.intersec.androidapp.core.bridge

data class RustSessionSnapshot(
    val sessionId: String,
    val sourceName: String,
    val totalPackets: Long,
    val totalFlows: Long,
    val activePacketNumber: Long?,
    val activeFlowLabel: String?,
    val searchText: String?,
    val appliedFiltersCsv: String,
    val createdAtEpochMicros: Long,
    val updatedAtEpochMicros: Long,
)

data class RustPacketQuery(
    val protocol: String? = null,
    val host: String? = null,
    val text: String? = null,
    val packetNumber: Long? = null,
    val offset: Long = 0,
    val limit: Int = 50
)

data class RustPacketItem(
    val packetNumber: Long,
    val timestampEpochMicros: Long?,
    val highestProtocol: String?,
    val info: String,
    val riskLevel: String = "Low",
    val securityAlert: String? = null
)

data class RustPacketSearchResult(
    val totalItems: Long,
    val items: List<RustPacketItem>,
)

data class RustFlowQuery(
    val protocol: String? = null,
    val host: String? = null,
    val port: Int? = null,
    val text: String? = null,
    val offset: Long = 0,
    val limit: Int = 50
)

data class RustFlowItem(
    val label: String,
    val endpoints: String,
    val totalPackets: Long,
    val totalPayloadBytes: Long,
    val isInsecure: Boolean = false
)

data class RustFlowSearchResult(
    val totalItems: Long,
    val items: List<RustFlowItem>,
)

data class RustStoredSession(
    val sessionId: String,
    val sourceName: String,
    val totalPackets: Long,
    val totalFlows: Long,
    val tagsCsv: String,
    val notes: String?,
)
