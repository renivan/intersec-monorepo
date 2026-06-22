package com.intersec.androidapp.core.bridge

import com.intersec.androidapp.data.model.dto.*

/**
 * Fachada para interagir com o motor Rust.
 * Fornece métodos de alto nível que lidam com a ponte nativa e o mapeamento de dados.
 */
class RustBridgeFacade(
    private val client: RustBridgeClient = RustBridgeClient(),
    private val mapper: RustBridgeMapper = RustBridgeMapper()
) {

    fun ping(): RustBridgeResult<String> {
        return try {
            val res = client.ping()
            RustBridgeResult(success = true, data = res)
        } catch (e: Exception) {
            RustBridgeResult(success = false, error = e.message)
        }
    }

    fun openCapture(path: String): RustBridgeResult<SessionDto> {
        val raw = client.openCapture(path)
        return mapper.mapSession(raw)
    }

    fun snapshotActive(): RustBridgeResult<SessionDto> {
        val raw = client.snapshotActive()
        return mapper.mapSession(raw)
    }

    fun queryPackets(query: RustPacketQuery): RustBridgeResult<PacketSearchResultDto> {
        val raw = client.queryPackets(query)
        return mapper.mapPacketResult(raw)
    }

    fun queryFlows(query: RustFlowQuery): RustBridgeResult<FlowSearchResultDto> {
        val raw = client.queryFlows(query)
        return mapper.mapFlowResult(raw)
    }

    fun getPackets(): RustBridgeResult<List<PacketDto>> {
        val raw = client.getPackets()
        return mapper.mapPackets(raw)
    }

    fun getFlows(): RustBridgeResult<List<FlowDto>> {
        val raw = client.getFlows()
        return mapper.mapFlows(raw)
    }

    fun startCapture(networkInterface: String, filter: String = ""): RustBridgeResult<String> {
        return try {
            val sessionId = client.startCapture(networkInterface, filter)
            RustBridgeResult(success = true, data = sessionId)
        } catch (e: Exception) {
            RustBridgeResult(success = false, error = e.message)
        }
    }

    fun stopCapture(sessionId: String): RustBridgeResult<SessionDto> {
        val raw = client.stopCapture(sessionId)
        return mapper.mapSession(raw)
    }

    fun capturePackets(sessionId: String, limit: Int = 100): RustBridgeResult<List<PacketDto>> {
        val raw = client.capturePackets(sessionId, limit)
        return mapper.mapPackets(raw)
    }

    fun getCaptureOverview(): RustBridgeResult<CaptureOverviewDto> {
        val raw = client.getCaptureOverview()
        return mapper.mapCaptureOverview(raw)
    }
}
