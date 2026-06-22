package com.intersec.androidapp.data.model.dto

import com.google.gson.annotations.SerializedName

data class SessionDto(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("source_name") val sourceName: String,
    @SerializedName("total_packets") val packetCount: Long,
    @SerializedName("total_flows") val flowCount: Long,
    @SerializedName("duration_micros") val duration: Long = 0
)

data class PacketDto(
    @SerializedName("packet_number") val packetNumber: Long,
    @SerializedName("timestamp") val timestamp: Long?,
    @SerializedName("protocol") val protocol: String?,
    @SerializedName("summary") val info: String,
    @SerializedName("risk_level") val riskLevel: String = "Low"
)

data class FlowDto(
    @SerializedName("label") val label: String,
    @SerializedName("endpoints") val endpoints: String,
    @SerializedName("total_packets") val packetCount: Long,
    @SerializedName("total_payload_bytes") val payloadBytes: Long,
    @SerializedName("is_insecure") val isInsecure: Boolean = false
)

data class PacketSearchResultDto(
    @SerializedName("total_items") val totalItems: Long,
    @SerializedName("items") val items: List<PacketDto>
)

data class FlowSearchResultDto(
    @SerializedName("total_items") val totalItems: Long,
    @SerializedName("items") val items: List<FlowDto>
)

data class CaptureOverviewDto(
    val totalVolumeBytes: Long = 0,
    val averageRiskScore: Int = 0,
    val securityCounts: SecurityCounts = SecurityCounts(),
    val topCommunications: List<CommunicationDto> = emptyList(),
    val trafficOrigin: List<CountryActivityDto> = emptyList(),
    val protocolStats: List<ProtocolStatDto> = emptyList(),
    val events: List<String> = emptyList(),
    val geoPoints: List<GeoPointDto> = emptyList()
)

data class GeoPointDto(
    val latitude: Double,
    val longitude: Double,
    val countryName: String,
    val countryCode: String,
    val city: String?,
    val flowCount: Int
)

data class SecurityCounts(
    val safe: Int = 0,
    val unusual: Int = 0,
    val suspicious: Int = 0,
    val activeAlerts: Int = 0
)

data class CommunicationDto(
    val source: String,
    val destination: String,
    val packetCount: Long,
    val volumeBytes: Long
)

data class CountryActivityDto(
    val country: String,
    val flag: String,
    val percentage: Float
)

data class ProtocolStatDto(
    val name: String,
    val port: String,
    val flowCount: Int,
    val isSecure: Boolean = false,
    val isPredominant: Boolean = false
)
