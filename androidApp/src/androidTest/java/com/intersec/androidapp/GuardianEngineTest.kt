package com.intersec.androidapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.intersec.androidapp.core.bridge.NativeBridgeClient
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Teste de "CoraÃ§Ã£o Aberto" do Motor Native.
 * Este teste valida a ponte JNI e o motor sem precisar abrir a UI.
 */
@RunWith(AndroidJUnit4::class)
class GuardianEngineTest {

    private val bridge = NativeBridgeClient()

    @Test
    fun testNativeEngineInitialization() {
        // Teste 1: Verifica se a biblioteca ".so" carrega e responde o Ping
        val response = bridge.ping()
        println("Motor Native respondeu: $response")
        assertNotNull("O motor Native deve retornar uma resposta", response)
    }

    @Test
    fun testNeuralCoreAnalysis() {
        // Teste 2: Valida se o Overview Neural está sendo gerado
        val overview = bridge.getCaptureOverview()
        println("Neural Intelligence Data: ${overview.length} bytes")
        assertTrue("O motor deve gerar metadados neurais", overview.isNotEmpty())
    }

    @Test
    fun testVpnHandoverLogic() {
        // Teste 3: Simula um FD invÃ¡lido (-1) para ver se o motor detecta o erro corretamente
        // (Isso valida nossa nova lÃ³gica de seguranÃ§a de FD)
        val success = bridge.attachVpnTunnel(-1)
        println("Resultado do teste de FD inválido: $success (Esperado: false)")
        assertTrue("O motor deve recusar um FD inválido", !success)
    }

    @Test
    fun testThreatDatabaseInjection() {
        // Teste 4: Simula a injeção de uma base de ameaças (estilo Cisco/Fortinet)
        val dummyIntel = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        val success = bridge.updateThreatDatabase(dummyIntel)
        println("Injeção de Inteligência Global: $success")
        assertTrue("O motor deve aceitar a atualização da base de ameaças", success)
    }

    @Test
    fun testActiveAttackBlocking() {
        // Teste 5: Simulador de Ataque Real
        // Simula um pacote HTTP que contêm a palavra "password"
        val maliciousPayload = "GET /login?user=admin&password=123 HTTP/1.1".toByteArray()
        val verdict = bridge.simulateAttack(maliciousPayload)
        
        println("Simulação de Ataque: '$verdict'")
        
        assertTrue(
            "O motor deve detectar e dropar o ataque de senha",
            verdict.startsWith("DROP|VIOLAÇÃO")
        )
    }
}

