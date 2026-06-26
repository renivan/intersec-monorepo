package com.intersec.androidapp.core.bridge

data class NativeSessionSnapshot(
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

data class NativePacketQuery(
    val protocol: String? = null,
    val host: String? = null,
    val text: String? = null,
    val packetNumber: Long? = null,
    val offset: Long = 0,
    val limit: Int = 50
)

data class NativePacketItem(
    val packetNumber: Long,
    val timestampEpochMicros: Long?,
    val highestProtocol: String?,
    val info: String,
    val riskLevel: String = "Low",
    val securityAlert: String? = null
)

data class NativePacketSearchResult(
    val totalItems: Long,
    val items: List<NativePacketItem>,
)

data class NativeFlowQuery(
    val protocol: String? = null,
    val host: String? = null,
    val port: Int? = null,
    val text: String? = null,
    val offset: Long = 0,
    val limit: Int = 50
)

data class NativeFlowItem(
    val label: String,
    val endpoints: String,
    val totalPackets: Long,
    val totalPayloadBytes: Long,
    val isInsecure: Boolean = false
)

data class NativeFlowSearchResult(
    val totalItems: Long,
    val items: List<NativeFlowItem>,
)

data class NativeStoredSession(
    val sessionId: String,
    val sourceName: String,
    val totalPackets: Long,
    val totalFlows: Long,
    val tagsCsv: String,
    val notes: String?,
)

