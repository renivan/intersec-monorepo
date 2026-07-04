package com.intersec.androidapp.presentation.screens.neural

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Teste de Integração para Neural3DTestActivity.
 * Verifica se a injeção de nós via Broadcast (simulando ADB) funciona corretamente.
 */
@RunWith(AndroidJUnit4::class)
class Neural3DTestActivityTest {

    @get:Rule
    val composeTestRule = androidx.compose.ui.test.junit4.v2.createAndroidComposeRule<Neural3DTestActivity>()

    @Test
    fun testInjectNodeViaBroadcastUpdatesHud() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // 1. Abre o HUD Técnico para visualizar o contador de nodes
        composeTestRule.onNodeWithContentDescription("Menu HUD").performClick()
        
        // 2. Verifica se o contador inicial é 0
        composeTestRule.onNodeWithText("0").assertIsDisplayed()

        // 3. Dispara o Broadcast que o CLI TEST RUNNER escuta
        val intent = Intent("SENTINEL_INJECT_NODE").apply {
            putExtra("ip", "10.0.0.1")
            putExtra("lat", -23.5f)
            putExtra("lon", -46.6f)
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)

        // 4. Aguarda a recomposição do Compose e verifica se o contador mudou para 1
        composeTestRule.waitUntil(3000) {
            try {
                composeTestRule.onNodeWithText("1").assertIsDisplayed()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
    }

    @Test
    fun testInjectNodeDisplaysCorrectGeoData() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val testIp = "8.8.8.8"
        
        // Dispara Broadcast com IP específico
        val intent = Intent("SENTINEL_INJECT_NODE").apply {
            putExtra("ip", testIp)
            putExtra("lat", 37.422f)
            putExtra("lon", -122.084f)
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)

        // Abre o HUD
        composeTestRule.onNodeWithContentDescription("Menu HUD").performClick()

        // Verifica se o IP injetado aparece no HUD (se implementado na UI)
        // No momento, o Neural3DScreen mostra apenas lat/lon do último link no HUD técnico
        composeTestRule.onNodeWithText("37.4220").assertIsDisplayed()
        composeTestRule.onNodeWithText("-122.0840").assertIsDisplayed()
    }
}
