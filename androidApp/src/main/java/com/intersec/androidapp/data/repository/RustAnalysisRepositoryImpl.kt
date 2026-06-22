package com.intersec.androidapp.data.repository

import com.intersec.androidapp.core.bridge.*
import com.intersec.androidapp.data.model.dto.*
import com.intersec.androidapp.domain.repository.RustAnalysisRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementação do repositório de análise baseada no motor Rust.
 * FASE 2: Delega a paginação (offset/limit) totalmente para o motor nativo.
 */
class RustAnalysisRepositoryImpl(
    private val bridge: RustBridgeFacade
) : RustAnalysisRepository {

    override suspend fun openCapture(path: String, nowEpochMicros: Long): Result<RustSessionSnapshot> = withContext(Dispatchers.IO) {
        val res = bridge.openCapture(path)
        if (res.success && res.data != null) {
            Result.success(mapDtoToSnapshot(res.data))
        } else {
            Result.failure(Exception(res.error ?: "Erro ao abrir captura"))
        }
    }

    override suspend fun snapshotActive(): Result<RustSessionSnapshot> = withContext(Dispatchers.IO) {
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

    override suspend fun queryPackets(query: RustPacketQuery): Result<RustPacketSearchResult> = withContext(Dispatchers.IO) {
        val res = bridge.queryPackets(query)
        if (res.success && res.data != null) {
            val items = res.data.items.map { 
                RustPacketItem(it.packetNumber, it.timestamp, it.protocol, it.info)
            }
            Result.success(RustPacketSearchResult(res.data.totalItems, items))
        } else {
            Result.failure(Exception(res.error ?: "Erro na consulta de pacotes"))
        }
    }

    override suspend fun queryFlows(query: RustFlowQuery): Result<RustFlowSearchResult> = withContext(Dispatchers.IO) {
        val res = bridge.queryFlows(query)
        if (res.success && res.data != null) {
            val items = res.data.items.map { 
                RustFlowItem(it.label, it.endpoints, it.packetCount, it.payloadBytes)
            }
            Result.success(RustFlowSearchResult(res.data.totalItems, items))
        } else {
            Result.failure(Exception(res.error ?: "Erro na consulta de fluxos"))
        }
    }

    override suspend fun persistActive(tagsCsv: String, notes: String?): Result<Unit> = withContext(Dispatchers.IO) {
        Result.success(Unit)
    }

    override suspend fun listStoredSessions(): Result<List<RustStoredSession>> = withContext(Dispatchers.IO) {
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

    override suspend fun stopCapture(sessionId: String): Result<RustSessionSnapshot> = withContext(Dispatchers.IO) {
        val res = bridge.stopCapture(sessionId)
        if (res.success && res.data != null) {
            Result.success(mapDtoToSnapshot(res.data))
        } else {
            Result.failure(Exception(res.error ?: "Erro ao parar captura"))
        }
    }

    override suspend fun capturePackets(sessionId: String, limit: Int): Result<List<RustPacketItem>> = withContext(Dispatchers.IO) {
        val res = bridge.capturePackets(sessionId, limit)
        if (res.success && res.data != null) {
            val items = res.data.map { dto ->
                RustPacketItem(
                    packetNumber = dto.packetNumber,
                    timestampEpochMicros = dto.timestamp,
                    highestProtocol = dto.protocol,
                    info = dto.info,
                    riskLevel = dto.riskLevel ?: "Low",
                    securityAlert = null
                )
            }
            Result.success(items)
        } else {
            Result.failure(Exception(res.error ?: "Erro ao capturar pacotes"))
        }
    }

    override suspend fun captureFlows(sessionId: String, limit: Int): Result<List<RustFlowItem>> = withContext(Dispatchers.IO) {
        val res = bridge.getFlows() // Usando o endpoint de fluxos ativos
        if (res.success && res.data != null) {
            val items = res.data.take(limit).map { dto ->
                RustFlowItem(
                    label = dto.label,
                    endpoints = dto.endpoints,
                    totalPackets = dto.packetCount,
                    totalPayloadBytes = dto.payloadBytes
                )
            }
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

    private fun mapDtoToSnapshot(dto: SessionDto) = RustSessionSnapshot(
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
