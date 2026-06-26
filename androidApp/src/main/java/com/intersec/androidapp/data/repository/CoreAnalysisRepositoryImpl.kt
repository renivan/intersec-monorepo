package com.intersec.androidapp.data.repository

import com.intersec.androidapp.core.bridge.*
import com.intersec.androidapp.data.model.dto.*
import com.intersec.androidapp.domain.repository.CoreAnalysisRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ImplementaÃ§Ã£o do repositÃ³rio de anÃ¡lise baseada no motor Native.
 * FASE 2: Delega a paginaÃ§Ã£o (offset/limit) totalmente para o motor nativo.
 */
class CoreAnalysisRepositoryImpl(
    private val bridge: NativeBridgeFacade,
) : CoreAnalysisRepository {

    override suspend fun openCapture(path: String, nowEpochMicros: Long): Result<NativeSessionSnapshot> = withContext(Dispatchers.IO) {
        val res = bridge.openCapture(path)
        if (res.success && res.data != null) {
            Result.success(mapDtoToSnapshot(res.data))
        } else {
            Result.failure(Exception(res.error ?: "Erro ao abrir captura"))
        }
    }

    override suspend fun snapshotActive(): Result<NativeSessionSnapshot> = withContext(Dispatchers.IO) {
        val res = bridge.snapshotActive()
        if (res.success && res.data != null) {
            Result.success(mapDtoToSnapshot(res.data))
        } else {
            Result.failure(Exception(res.error ?: "Erro ao obter snapshot ativo"))
        }
    }

    override suspend fun getPackets(): List<PacketDto> {
        val result = bridge.getPackets()
        if (!result.success || result.data == null) {
            throw Exception(result.error ?: "Erro ao carregar pacotes")
        }
        return result.data
    }

    override suspend fun getFlows(): List<FlowDto> {
        val result = bridge.getFlows()
        if (!result.success || result.data == null) {
            throw Exception(result.error ?: "Erro ao carregar fluxos")
        }
        return result.data
    }

    override suspend fun queryPackets(query: NativePacketQuery): Result<NativePacketSearchResult> = withContext(Dispatchers.IO) {
        val res = bridge.queryPackets(query)
        if (res.success && res.data != null) {
            val items = res.data.items.map { 
                NativePacketItem(it.packetNumber, it.timestamp, it.protocol, it.info)
            }
            Result.success(NativePacketSearchResult(res.data.totalItems, items))
        } else {
            Result.failure(Exception(res.error ?: "Erro na consulta de pacotes"))
        }
    }

    override suspend fun queryFlows(query: NativeFlowQuery): Result<NativeFlowSearchResult> = withContext(Dispatchers.IO) {
        val res = bridge.queryFlows(query)
        if (res.success && res.data != null) {
            val items = res.data.items.map { 
                NativeFlowItem(it.label, it.endpoints, it.packetCount, it.payloadBytes)
            }
            Result.success(NativeFlowSearchResult(res.data.totalItems, items))
        } else {
            Result.failure(Exception(res.error ?: "Erro na consulta de fluxos"))
        }
    }

    override suspend fun persistActive(tagsCsv: String, notes: String?): Result<Unit> = withContext(Dispatchers.IO) {
        Result.success(Unit)
    }

    override suspend fun listStoredSessions(): Result<List<NativeStoredSession>> = withContext(Dispatchers.IO) {
        Result.success(emptyList())
    }

    override suspend fun startCapture(networkInterface: String, filter: String): Result<String> = withContext(Dispatchers.IO) {
        val res = bridge.startCapture(networkInterface, filter)
        if (res.success && res.data != null) {
            Result.success(res.data)
        } else {
            Result.failure(Exception(res.error ?: "Erro ao iniciar captura"))
        }
    }

    override suspend fun stopCapture(sessionId: String): Result<NativeSessionSnapshot> = withContext(Dispatchers.IO) {
        val res = bridge.stopCapture(sessionId)
        if (res.success && res.data != null) {
            Result.success(mapDtoToSnapshot(res.data))
        } else {
            Result.failure(Exception(res.error ?: "Erro ao parar captura"))
        }
    }

    override suspend fun capturePackets(sessionId: String, limit: Int): Result<List<NativePacketItem>> = withContext(Dispatchers.IO) {
        val res = bridge.capturePackets(sessionId, limit)
        if (res.success && res.data != null) {
            val items = res.data.map { dto ->
                NativePacketItem(
                    packetNumber = dto.packetNumber,
                    timestampEpochMicros = dto.timestamp,
                    highestProtocol = dto.protocol,
                    info = dto.info,
                    riskLevel = dto.riskLevel,
                    securityAlert = null
                )
            }
            Result.success(items)
        } else {
            Result.failure(Exception(res.error ?: "Erro ao capturar pacotes"))
        }
    }

    override suspend fun captureFlows(sessionId: String, limit: Int): Result<List<NativeFlowItem>> = withContext(Dispatchers.IO) {
        val res = bridge.getFlows() // Usando o endpoint de fluxos ativos
        if ((res.success && res.data != null)) {
            val items = res.data.asSequence().take(limit).map { dto ->
                NativeFlowItem(
                    label = dto.label,
                    endpoints = dto.endpoints,
                    totalPackets = dto.packetCount,
                    totalPayloadBytes = dto.payloadBytes
                )
            }.toList()
            Result.success(items)
        } else {
            Result.failure(Exception(res.error ?: "Erro ao capturar fluxos"))
        }
    }

    override suspend fun getOverview(): Result<CaptureOverviewDto> = withContext(Dispatchers.IO) {
        val res = bridge.getCaptureOverview()
        if (res.success && res.data != null) {
            Result.success(res.data)
        } else {
            Result.failure(Exception(res.error ?: "Erro ao obter overview"))
        }
    }

    override suspend fun updateSecuritySettings(level: Int, smartShield: Boolean, killSwitch: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        val res = bridge.updateSecuritySettings(level, smartShield, killSwitch)
        if (res.success && res.data != null) {
            Result.success(res.data)
        } else {
            Result.failure(Exception(res.error ?: "Erro ao atualizar configurações de segurança"))
        }
    }

    private fun mapDtoToSnapshot(dto: SessionDto) = NativeSessionSnapshot(
        sessionId = dto.sessionId,
        sourceName = dto.sourceName,
        totalPackets = dto.packetCount,
        totalFlows = dto.flowCount,
        activePacketNumber = null,
        activeFlowLabel = null,
        searchText = null,
        appliedFiltersCsv = "",
        createdAtEpochMicros = 0L,
        updatedAtEpochMicros = 0L
    )
}

