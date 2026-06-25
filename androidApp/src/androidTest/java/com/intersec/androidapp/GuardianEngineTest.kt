package com.intersec.androidapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.intersec.androidapp.core.bridge.RustBridgeClient
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Teste de "Coração Aberto" do Motor Rust.
 * Este teste valida a ponte JNI e o motor sem precisar abrir a UI.
 */
@RunWith(AndroidJUnit4::class)
class GuardianEngineTest {

    private val bridge = RustBridgeClient()

    @Test
    fun testRustEngineInitialization() {
        // Teste 1: Verifica se a biblioteca ".so" carrega e responde o Ping
        val response = bridge.ping()
        println("Motor Rust respondeu: $response")
        assertNotNull("O motor Rust deve retornar uma resposta", response)
    }

    @Test
    fun testNeuralCoreAnalysis() {
        // Teste 2: Valida se o Overview Neural está sendo gerado
        val overview = bridge.getCaptureOverview()
        println("Neural Intelligence Data: ${overview.length} bytes")
        assertTrue("O motor deve gerar metadados neurais", overview.length > 0)
    }

    @Test
    fun testVpnHandoverLogic() {
        // Teste 3: Simula um FD inválido (-1) para ver se o motor detecta o erro corretamente
        // (Isso valida nossa nova lógica de segurança de FD)
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
}
