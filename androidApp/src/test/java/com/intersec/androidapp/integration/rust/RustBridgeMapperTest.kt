package com.intersec.androidapp.integration.rust

import org.junit.Assert.assertEquals
import org.junit.Test

class RustBridgeMapperTest {

    @Test
    fun `toSessionSnapshot should parse key-value lines correctly`() {
        val raw = """
            session_id = 12345
            source_name = capture.pcap
            total_packets = 100
            total_flows = 10
            created_at_epoch_micros = 1700000000000000
        """.trimIndent()

        val result = RustBridgeMapper.toSessionSnapshot(raw)

        assertEquals("12345", result.sessionId)
        assertEquals("capture.pcap", result.sourceName)
        assertEquals(100L, result.totalPackets)
        assertEquals(10L, result.totalFlows)
        assertEquals(1700000000000000L, result.createdAtEpochMicros)
    }

    @Test
    fun `toPacketSearchResult should parse search result with multiple items`() {
        val raw = """
            total_items=2
            packet_number=1 | timestamp=1700000100 | protocol=TCP | summary=Syn
            packet_number=2 | timestamp=1700000200 | protocol=HTTP | summary=GET /
        """.trimIndent()

        val result = RustBridgeMapper.toPacketSearchResult(raw)

        assertEquals(2L, result.totalItems)
        assertEquals(2, result.items.size)
        
        assertEquals(1L, result.items[0].packetNumber)
        assertEquals("TCP", result.items[0].highestProtocol)
        assertEquals("Syn", result.items[0].summary)
        
        assertEquals(2L, result.items[1].packetNumber)
        assertEquals("HTTP", result.items[1].highestProtocol)
    }

    @Test
    fun `toFlowSearchResult should parse flow items correctly`() {
        val raw = """
            total_items=1
            label=TCP Flow | endpoints=192.168.1.1 -> 8.8.8.8 | total_packets=5 | total_payload_bytes=1024
        """.trimIndent()

        val result = RustBridgeMapper.toFlowSearchResult(raw)

        assertEquals(1L, result.totalItems)
        assertEquals(1, result.items.size)
        assertEquals("TCP Flow", result.items[0].label)
        assertEquals("192.168.1.1 -> 8.8.8.8", result.items[0].endpoints)
        assertEquals(5L, result.items[0].totalPackets)
        assertEquals(1024L, result.items[0].totalPayloadBytes)
    }
}
