package com.intersec.androidapp.core.bridge

import com.intersec.androidapp.data.model.dto.*

/**
 * Fachada para interagir com o motor Native.
 * Fornece mÃ©todos de alto nÃ­vel que lidam com a ponte nativa e o mapeamento de dados.
 */
class NativeBridgeFacade(
    private val client: NativeBridgeClient = NativeBridgeClient(),
    private val mapper: NativeBridgeMapper = NativeBridgeMapper()
) {

    fun openCapture(path: String): NativeBridgeResult<SessionDto> {
        val raw = client.openCapture(path)
        return mapper.mapSession(raw)
    }

    fun snapshotActive(): NativeBridgeResult<SessionDto> {
        val raw = client.snapshotActive()
        return mapper.mapSession(raw)
    }

    fun queryPackets(query: NativePacketQuery): NativeBridgeResult<PacketSearchResultDto> {
        val raw = client.queryPackets(query)
        return mapper.mapPacketResult(raw)
    }

    fun queryFlows(query: NativeFlowQuery): NativeBridgeResult<FlowSearchResultDto> {
        val raw = client.queryFlows(query)
        return mapper.mapFlowResult(raw)
    }

    fun getPackets(): NativeBridgeResult<List<PacketDto>> {
        val raw = client.getPackets()
        return mapper.mapPackets(raw)
    }

    fun getFlows(): NativeBridgeResult<List<FlowDto>> {
        val raw = client.getFlows()
        return mapper.mapFlows(raw)
    }

    fun startCapture(networkInterface: String, filter: String = ""): NativeBridgeResult<String> {
        return try {
            val sessionId = client.startCapture(networkInterface, filter)
            NativeBridgeResult(success = true, data = sessionId)
        } catch (e: Exception) {
            NativeBridgeResult(success = false, error = e.message)
        }
    }

    fun stopCapture(sessionId: String): NativeBridgeResult<SessionDto> {
        val raw = client.stopCapture(sessionId)
        return mapper.mapSession(raw)
    }

    fun capturePackets(sessionId: String, limit: Int = 100): NativeBridgeResult<List<PacketDto>> {
        val raw = client.capturePackets(sessionId, limit)
        return mapper.mapPackets(raw)
    }

    fun getCaptureOverview(): NativeBridgeResult<CaptureOverviewDto> {
        val raw = client.getCaptureOverview()
        return mapper.mapCaptureOverview(raw)
    }

    fun updateSecuritySettings(level: Int, smartShield: Boolean, killSwitch: Boolean): NativeBridgeResult<Boolean> {
        return try {
            val result = client.updateSecuritySettings(level, smartShield, killSwitch)
            NativeBridgeResult(success = true, data = result)
        } catch (e: Exception) {
            NativeBridgeResult(success = false, error = e.message)
        }
    }
}

