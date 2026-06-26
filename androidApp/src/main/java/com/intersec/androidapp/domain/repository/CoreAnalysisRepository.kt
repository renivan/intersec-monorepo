package com.intersec.androidapp.domain.repository

import com.intersec.androidapp.core.bridge.*
import com.intersec.androidapp.data.model.dto.*

/**
 * Interface do repositÃ³rio de análise, definindo as operações permitidas no motor Native.
 */
interface CoreAnalysisRepository {
    suspend fun openCapture(path: String, nowEpochMicros: Long): Result<NativeSessionSnapshot>
    suspend fun snapshotActive(): Result<NativeSessionSnapshot>
    suspend fun queryPackets(query: NativePacketQuery): Result<NativePacketSearchResult>
    suspend fun queryFlows(query: NativeFlowQuery): Result<NativeFlowSearchResult>
    suspend fun persistActive(tagsCsv: String, notes: String?): Result<Unit>
    suspend fun listStoredSessions(): Result<List<NativeStoredSession>>
    
    // MÃ©todos solicitados para suporte a DTOs JSON
    suspend fun getPackets(): List<PacketDto>
    suspend fun getFlows(): List<FlowDto>

    // MÃ©todos para captura em tempo real
    suspend fun startCapture(networkInterface: String, filter: String = ""): Result<String>
    suspend fun stopCapture(sessionId: String): Result<NativeSessionSnapshot>
    suspend fun capturePackets(sessionId: String, limit: Int = 100): Result<List<NativePacketItem>>
    suspend fun captureFlows(sessionId: String, limit: Int = 100): Result<List<NativeFlowItem>>
    suspend fun getOverview(): Result<CaptureOverviewDto>
    suspend fun updateSecuritySettings(level: Int, smartShield: Boolean, killSwitch: Boolean): Result<Boolean>
}

