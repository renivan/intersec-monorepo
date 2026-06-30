package com.intersec.androidapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.intersec.androidapp.presentation.screens.capture.CaptureRealtimeScreen
import com.intersec.androidapp.ui.theme.InterSecTheme
import org.junit.Rule
import org.junit.Test

/**
 * Testes de UI para a tela de Captura Real-time.
 * Valida os estados críticos de interação do usuário.
 */
class CaptureRealtimeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testInitialState_ShowsStartButton() {
        composeTestRule.setContent {
            InterSecTheme {
                CaptureRealtimeScreen()
            }
        }

        // Verifica se o título e o botão inicial estão presentes
        composeTestRule.onNodeWithText("Captura Real-time").assertIsDisplayed()
        
        // Como o usuário padrão é FREE, deve mostrar o botão com menção a AD
        composeTestRule.onNodeWithText("INICIAR (AD REQ)").assertIsDisplayed()
    }

    @Test
    fun testFilterInput_IsAvailable() {
        composeTestRule.setContent {
            InterSecTheme {
                CaptureRealtimeScreen()
            }
        }

        // Verifica o campo de filtro BPF
        composeTestRule.onNodeWithText("🎯 Filtro BPF (Opcional)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ex: tcp port 443").assertIsDisplayed()
    }
}
