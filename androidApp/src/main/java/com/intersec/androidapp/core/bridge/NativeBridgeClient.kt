package com.intersec.androidapp.core.bridge

import com.intersec.androidapp.core.runtime.NativeRuntimeLoader
import dalvik.annotation.optimization.FastNative

/**
 * Camada de baixo nÃ­vel que realiza as chamadas nativas (JNI) para o Core em Native.
 * Otimizada com @FastNative para transiÃ§Ãµes de baixa latÃªncia.
 */
class NativeBridgeClient(
    private val loader: NativeRuntimeLoader = NativeRuntimeLoader,
) {

    fun ping(): String {
        loader.ensureLoaded()
        return Native.pingNative()
    }

    fun openCapture(path: String): String {
        loader.ensureLoaded()
        return Native.openCaptureNative(path, System.currentTimeMillis() * 1000L)
    }

    fun snapshotActive(): String {
        loader.ensureLoaded()
        return Native.snapshotActiveNative()
    }

    fun queryPackets(query: NativePacketQuery): String {
        loader.ensureLoaded()
        return Native.queryPacketsNative(
            protocol = query.protocol,
            host = query.host,
            text = query.text,
            packetNumber = query.packetNumber ?: -1L,
            offset = query.offset,
            limit = query.limit,
        )
    }

    fun queryFlows(query: NativeFlowQuery): String {
        loader.ensureLoaded()
        return Native.queryFlowsNative(
            protocol = query.protocol,
            host = query.host,
            port = query.port ?: -1,
            text = query.text,
            offset = query.offset,
            limit = query.limit,
        )
    }

    fun getPackets(): String {
        loader.ensureLoaded()
        return Native.queryPacketsNative(null, null, null, -1L, 0, Int.MAX_VALUE)
    }

    fun getFlows(): String {
        loader.ensureLoaded()
        return Native.queryFlowsNative(null, null, -1, null, 0, Int.MAX_VALUE)
    }

    fun startCapture(networkInterface: String, filter: String = ""): String {
        loader.ensureLoaded()
        return Native.startCaptureNative(networkInterface, filter)
    }

    fun stopCapture(sessionId: String): String {
        loader.ensureLoaded()
        return Native.stopCaptureNative(sessionId)
    }

    fun capturePackets(sessionId: String, limit: Int = 100): String {
        loader.ensureLoaded()
        return Native.capturePacketsNative(sessionId, limit)
    }

    fun getCaptureOverview(): String {
        loader.ensureLoaded()
        return Native.getCaptureOverviewNative()
    }

    fun runFullSystemTest(): String {
        loader.ensureLoaded()
        return Native.runFullSystemTestNative()
    }

    /**
     * Entrega o controle do tÃºnel VPN para o motor Native.
     */
    fun attachVpnTunnel(fd: Int): Boolean {
        loader.ensureLoaded()
        return Native.attachVpnTunnelNative(fd)
    }

    /**
     * Atualiza a base de dados de ameaÃ§as globais (DPI Master).
     */
    fun updateThreatDatabase(data: ByteArray): Boolean {
        loader.ensureLoaded()
        return Native.updateThreatDatabaseNative(data)
    }

    /**
     * Simula um pacote e retorna o veredito de seguranÃ§a do motor.
     */
    fun simulateAttack(data: ByteArray): String {
        loader.ensureLoaded()
        return Native.simulateAttackNative(data)
    }

    fun updateSecuritySettings(level: Int, smartShield: Boolean, killSwitch: Boolean): Boolean {
        loader.ensureLoaded()
        return Native.updateSecuritySettingsNative(level, smartShield, killSwitch)
    }

    fun activatePremiumFeatures(token: String): Boolean {
        loader.ensureLoaded()
        return Native.activatePremiumFeaturesNative(token)
    }

    private object Native {
        @JvmStatic
        @FastNative
        external fun pingNative(): String

        @JvmStatic
        external fun openCaptureNative(path: String, nowEpochMicros: Long): String

        @JvmStatic
        @FastNative
        external fun snapshotActiveNative(): String

        @JvmStatic
        @FastNative
        external fun queryPacketsNative(
            protocol: String?,
            host: String?,
            text: String?,
            packetNumber: Long,
            offset: Long,
            limit: Int
        ): String

        @JvmStatic
        @FastNative
        external fun queryFlowsNative(
            protocol: String?,
            host: String?,
            port: Int,
            text: String?,
            offset: Long,
            limit: Int
        ): String

        @JvmStatic
        external fun startCaptureNative(networkInterface: String, filter: String): String

        @JvmStatic
        external fun stopCaptureNative(sessionId: String): String

        @JvmStatic
        @FastNative
        external fun capturePacketsNative(sessionId: String, limit: Int): String

        @JvmStatic
        @FastNative
        external fun getCaptureOverviewNative(): String

        @JvmStatic
        @FastNative
        external fun runFullSystemTestNative(): String

        @JvmStatic
        @FastNative
        external fun attachVpnTunnelNative(fd: Int): Boolean

        @JvmStatic
        external fun updateThreatDatabaseNative(data: ByteArray): Boolean

        @JvmStatic
        external fun simulateAttackNative(data: ByteArray): String

        @JvmStatic
        external fun updateSecuritySettingsNative(level: Int, smartShield: Boolean, killSwitch: Boolean): Boolean

        @JvmStatic
        external fun activatePremiumFeaturesNative(token: String): Boolean
    }
}

