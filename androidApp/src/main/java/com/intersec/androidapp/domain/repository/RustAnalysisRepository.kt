package com.intersec.androidapp.domain.repository

import com.intersec.androidapp.core.bridge.*
import com.intersec.androidapp.data.model.dto.*

/**
 * Interface do repositório de análise, definindo as operações permitidas no motor Rust.
 */
interface RustAnalysisRepository {
    suspend fun openCapture(path: String, nowEpochMicros: Long): Result<RustSessionSnapshot>
    suspend fun snapshotActive(): Result<RustSessionSnapshot>
    suspend fun queryPackets(query: RustPacketQuery): Result<RustPacketSearchResult>
    suspend fun queryFlows(query: RustFlowQuery): Result<RustFlowSearchResult>
    suspend fun persistActive(tagsCsv: String, notes: String?): Result<Unit>
    suspend fun listStoredSessions(): Result<List<RustStoredSession>>
    
    // Métodos solicitados para suporte a DTOs JSON
    suspend fun getPackets(): List<PacketDto>
    suspend fun getFlows(): List<FlowDto>

    // Métodos para captura em tempo real
    suspend fun startCapture(networkInterface: String, filter: String = ""): Result<String>
    suspend fun stopCapture(sessionId: String): Result<RustSessionSnapshot>
    suspend fun capturePackets(sessionId: String, limit: Int = 100): Result<List<RustPacketItem>>
    suspend fun captureFlows(sessionId: String, limit: Int = 100): Result<List<RustFlowItem>>
    suspend fun getOverview(): Result<CaptureOverviewDto>
}
