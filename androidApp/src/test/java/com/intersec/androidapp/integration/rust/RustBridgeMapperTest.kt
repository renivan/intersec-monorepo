package com.intersec.androidapp.integration.rust

import com.intersec.androidapp.core.bridge.NativeBridgeMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeBridgeMapperTest {

    private val mapper = NativeBridgeMapper()

    @Test
    fun `toSessionSnapshot should parse key-value lines correctly`() {
        val raw = """
            session_id = 12345
            source_name = capture.pcap
            total_packets = 100
            total_flows = 10
            created_at_epoch_micros = 1700000000000000
        """.trimIndent()

        val result = mapper.mapSession(raw)

        assertTrue("Mapping should be successful", result.success)
        val data = result.data!!
        assertEquals("12345", data.sessionId)
        assertEquals("capture.pcap", data.sourceName)
        assertEquals(100L, data.packetCount)
        assertEquals(10L, data.flowCount)
        // O motor Native retorna created_at_epoch_micros para sessÃµes abertas
        // O Mapper atual ainda nÃ£o popula o campo duration para texto puro key-value
        assertEquals(0L, data.duration)
    }

    @Test
    fun `toPacketSearchResult should parse search result with multiple items`() {
        val raw = """
            total_items=2
            packet_number=1 | timestamp=1700000100 | protocol=TCP | summary=Syn
            packet_number=2 | timestamp=1700000200 | protocol=HTTP | summary=GET /
        """.trimIndent()

        val result = mapper.mapPacketResult(raw)

        assertTrue("Mapping should be successful", result.success)
        val data = result.data!!
        assertEquals(2L, data.totalItems)
        assertEquals(2, data.items.size)
        
        assertEquals(1L, data.items[0].packetNumber)
        assertEquals("TCP", data.items[0].protocol)
        assertEquals("Syn", data.items[0].info)
        
        assertEquals(2L, data.items[1].packetNumber)
        assertEquals("HTTP", data.items[1].protocol)
    }

    @Test
    fun `toFlowSearchResult should parse flow items correctly`() {
        val raw = """
            total_items=1
            label=TCP Flow | endpoints=192.168.1.1 -> 8.8.8.8 | total_packets=5 | total_payload_bytes=1024
        """.trimIndent()

        val result = mapper.mapFlowResult(raw)

        assertTrue("Mapping should be successful", result.success)
        val data = result.data!!
        assertEquals(1L, data.totalItems)
        assertEquals(1, data.items.size)
        assertEquals("TCP Flow", data.items[0].label)
        assertEquals("192.168.1.1 -> 8.8.8.8", data.items[0].endpoints)
        assertEquals(5L, data.items[0].packetCount)
        assertEquals(1024L, data.items[0].payloadBytes)
    }
}

