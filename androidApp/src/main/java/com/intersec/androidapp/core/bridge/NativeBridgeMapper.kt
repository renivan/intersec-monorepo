package com.intersec.androidapp.core.bridge

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intersec.androidapp.data.model.dto.*

/**
 * Converte as respostas brutas (JSON/Texto) do Native para DTOs do Kotlin.
 * Inclui mapeamento de erros amigÃ¡veis para falhas do motor nativo.
 */
class NativeBridgeMapper {

    private val gson = Gson()

    private fun mapFriendlyError(rawError: String): String {
        return when {
            rawError.contains("tcp header shorter than 20 bytes") -> 
                "Arquivo de captura inválido: cabeçalho TCP incompleto. O arquivo pode estar corrompido ou truncado."
            rawError.contains("MalformedPacket") -> 
                "Falha na anÃ¡lise: o motor Native encontrou pacotes malformados no arquivo."
            rawError.contains("no active capture loaded") ->
                "Nenhuma captura ativa. Por favor, importe um arquivo .pcap primeiro."
            else -> rawError
        }
    }

    fun mapSession(raw: String): NativeBridgeResult<SessionDto> {
        return try {
            if (raw.startsWith("ERROR:")) {
                val msg = raw.substringAfter("ERROR:").trim()
                return NativeBridgeResult(success = false, error = mapFriendlyError(msg))
            }
            
            val data = if (raw.contains("{")) {
                gson.fromJson(raw, SessionDto::class.java)
            } else {
                val map = parseKeyValueLines(raw)
                SessionDto(
                    sessionId = map["session_id"].orEmpty(),
                    sourceName = map["source_name"].orEmpty(),
                    packetCount = map["total_packets"]?.toLongOrNull() ?: 0L,
                    flowCount = map["total_flows"]?.toLongOrNull() ?: 0L,
                    duration = 0,
                )
            }
            NativeBridgeResult(success = true, data = data)
        } catch (e: Exception) {
            NativeBridgeResult(success = false, error = "Erro ao processar dados da sessão: ${e.message}")
        }
    }

    fun mapPacketResult(raw: String): NativeBridgeResult<PacketSearchResultDto> {
        return try {
            if (raw.startsWith("ERROR:")) {
                val msg = raw.substringAfter("ERROR:").trim()
                return NativeBridgeResult(success = false, error = mapFriendlyError(msg))
            }
            
            val data = if (raw.contains("{")) {
                gson.fromJson(raw, PacketSearchResultDto::class.java)
            } else {
                val lines = raw.lines()
                val totalItems = lines.firstOrNull { it.contains("total_items") }
                    ?.substringAfter("=")?.trim()?.toLongOrNull() ?: 0L
                
                val items = lines.asSequence().filter { it.contains("packet_number") }
                    .map { line ->
                        val parts = parsePipeLine(line)
                        PacketDto(
                            packetNumber = parts["packet_number"]?.toLongOrNull() ?: 0L,
                            timestamp = parts["timestamp"]?.toLongOrNull(),
                            protocol = parts["protocol"],
                            info = parts["summary"].orEmpty()
                        )
                    }.toList()
                PacketSearchResultDto(totalItems, items)
            }
            NativeBridgeResult(success = true, data = data)
        } catch (e: Exception) {
            NativeBridgeResult(success = false, error = "Erro ao processar busca de pacotes: ${e.message}")
        }
    }

    fun mapFlowResult(raw: String): NativeBridgeResult<FlowSearchResultDto> {
        return try {
            if (raw.startsWith("ERROR:")) {
                val msg = raw.substringAfter("ERROR:").trim()
                return NativeBridgeResult(success = false, error = mapFriendlyError(msg))
            }
            
            val data = if (raw.contains("{")) {
                gson.fromJson(raw, FlowSearchResultDto::class.java)
            } else {
                val lines = raw.lines()
                val totalItems = lines.firstOrNull { it.contains("total_items") }
                    ?.substringAfter("=")?.trim()?.toLongOrNull() ?: 0L
                
                val items = lines.filter { it.contains("label") }
                    .map { line ->
                        val parts = parsePipeLine(line)
                        FlowDto(
                            label = parts["label"].orEmpty(),
                            endpoints = parts["endpoints"].orEmpty(),
                            packetCount = parts["total_packets"]?.toLongOrNull() ?: 0L,
                            payloadBytes = parts["total_payload_bytes"]?.toLongOrNull() ?: 0L
                        )
                    }
                FlowSearchResultDto(totalItems, items)
            }
            NativeBridgeResult(success = true, data = data)
        } catch (e: Exception) {
            NativeBridgeResult(success = false, error = "Erro ao processar busca de fluxos: ${e.message}")
        }
    }

    fun mapCaptureOverview(raw: String): NativeBridgeResult<CaptureOverviewDto> {
        return try {
            if (raw.startsWith("ERROR:")) {
                val msg = raw.substringAfter("ERROR:").trim()
                return NativeBridgeResult(success = false, error = mapFriendlyError(msg))
            }
            
            val lines = raw.lines()
            if (lines.isEmpty()) return NativeBridgeResult(success = false, error = "Resposta vazia do motor")
            
            val mainParts = parsePipeLine(lines[0])
            val secParts = if (lines.size > 1) parsePipeLine(lines[1]) else emptyMap()
            
            val securityCounts = SecurityCounts(
                safe = secParts["safe"]?.toIntOrNull() ?: 0,
                unusual = secParts["unusual"]?.toIntOrNull() ?: 0,
                suspicious = secParts["suspicious"]?.toIntOrNull() ?: 0,
                activeAlerts = secParts["active_alerts"]?.toIntOrNull() ?: 0
            )
            
            val topComms = mutableListOf<CommunicationDto>()
            val protoStats = mutableListOf<ProtocolStatDto>()
            val trafficOrigin = mutableListOf<CountryActivityDto>()
            val events = mutableListOf<String>()
            val geoPoints = mutableListOf<GeoPointDto>()
            
            lines.drop(2).forEach { line ->
                when {
                    line.startsWith("proto=") -> {
                        val p = line.substringAfter("proto=").substringBefore(" ")
                        val c = line.substringAfter("count=").toIntOrNull() ?: 0
                        protoStats.add(ProtocolStatDto(p, "", c))
                    }
                    line.startsWith("host=") -> {
                        val h = line.substringAfter("host=").substringBefore(" ")
                        val c = line.substringAfter("count=").toLongOrNull() ?: 0L
                        topComms.add(CommunicationDto(h, "...", 0, c)) // Simplificado
                    }
                    line.startsWith("event=") -> {
                        events.add(line.substringAfter("event=").trim())
                    }
                    line.startsWith("geo=") -> {
                        // geo=LAT|LON|COUNTRY|CODE|COUNT
                        val p = line.substringAfter("geo=").split("|")
                        if (p.size >= 5) {
                            geoPoints.add(GeoPointDto(
                                latitude = p[0].toDoubleOrNull() ?: 0.0,
                                longitude = p[1].toDoubleOrNull() ?: 0.0,
                                countryName = p[2],
                                countryCode = p[3],
                                city = null,
                                flowCount = p[4].toIntOrNull() ?: 0
                            ))
                        }
                    }
                }
            }
            
            val data = CaptureOverviewDto(
                totalVolumeBytes = mainParts["total_volume_bytes"]?.toLongOrNull() ?: 0L,
                averageRiskScore = mainParts["average_risk_score"]?.toIntOrNull() ?: 0,
                securityCounts = securityCounts,
                topCommunications = topComms,
                protocolStats = protoStats,
                trafficOrigin = trafficOrigin,
                events = events,
                geoPoints = geoPoints
            )
            
            NativeBridgeResult(success = true, data = data)
        } catch (e: Exception) {
            NativeBridgeResult(success = false, error = "Erro ao processar overview: ${e.message}")
        }
    }

    fun mapPackets(raw: String): NativeBridgeResult<List<PacketDto>> {
        return try {
            if (raw.startsWith("ERROR:")) {
                val msg = raw.substringAfter("ERROR:").trim()
                return NativeBridgeResult(success = false, error = mapFriendlyError(msg))
            }
            val data: List<PacketDto> = if (raw.contains("[")) {
                val type = object : TypeToken<List<PacketDto>>() {}.type
                gson.fromJson(raw, type)
            } else {
                raw.lines()
                    .filter { it.contains("packet_number") }
                    .map { line ->
                        val parts = parsePipeLine(line)
                        PacketDto(
                            packetNumber = parts["packet_number"]?.toLongOrNull() ?: 0L,
                            timestamp = parts["timestamp"]?.toLongOrNull(),
                            protocol = parts["protocol"],
                            info = parts["summary"].orEmpty()
                        )
                    }
            }
            NativeBridgeResult(success = true, data = data)
        } catch (e: Exception) {
            NativeBridgeResult(success = false, error = "Erro ao processar lista de pacotes: ${e.message}")
        }
    }

    fun mapFlows(raw: String): NativeBridgeResult<List<FlowDto>> {
        return try {
            if (raw.startsWith("ERROR:")) {
                val msg = raw.substringAfter("ERROR:").trim()
                return NativeBridgeResult(success = false, error = mapFriendlyError(msg))
            }
            val data: List<FlowDto> = if (raw.contains("[")) {
                val type = object : TypeToken<List<FlowDto>>() {}.type
                gson.fromJson(raw, type)
            } else {
                raw.lines()
                    .filter { it.contains("label") }
                    .map { line ->
                        val parts = parsePipeLine(line)
                        FlowDto(
                            label = parts["label"].orEmpty(),
                            endpoints = parts["endpoints"].orEmpty(),
                            packetCount = parts["total_packets"]?.toLongOrNull() ?: 0L,
                            payloadBytes = parts["total_payload_bytes"]?.toLongOrNull() ?: 0L
                        )
                    }
            }
            NativeBridgeResult(success = true, data = data)
        } catch (e: Exception) {
            NativeBridgeResult(success = false, error = "Erro ao processar lista de fluxos: ${e.message}")
        }
    }

    private fun parseKeyValueLines(raw: String): Map<String, String> {
        return raw.lines()
            .filter { it.contains("=") }
            .associate { line ->
                val key = line.substringBefore("=").trim()
                val value = line.substringAfter("=").trim()
                key to value
            }
    }

    private fun parsePipeLine(raw: String): Map<String, String> {
        return raw.split("|")
            .map { it.trim() }
            .filter { it.contains("=") }
            .associate { segment ->
                val key = segment.substringBefore("=").trim()
                val value = segment.substringAfter("=").trim()
                key to value
            }
    }
}

