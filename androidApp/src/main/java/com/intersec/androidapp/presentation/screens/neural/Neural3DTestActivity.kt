package com.intersec.androidapp.presentation.screens.neural

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateListOf
import com.intersec.androidapp.presentation.state.NeuralLink3D
import androidx.compose.ui.graphics.Color
import com.intersec.androidapp.presentation.state.AnalysisUiState

/**
 * CLI TEST RUNNER: Atividade isolada para testar o motor 3D via ADB.
 * Comando: adb shell am start -n com.intersec.androidapp/com.intersec.androidapp.presentation.screens.neural.Neural3DTestActivity
 */
class Neural3DTestActivity : ComponentActivity() {
    
    private val testLinks = mutableStateListOf<NeuralLink3D>()
    
    private val cliReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "SENTINEL_INJECT_NODE") {
                val ip = intent.getStringExtra("ip") ?: "0.0.0.0"
                val lat = intent.getFloatExtra("lat", 0f).toDouble()
                val lon = intent.getFloatExtra("lon", 0f).toDouble()
                
                testLinks.add(
                    NeuralLink3D(
                        id = System.currentTimeMillis().toString(),
                        sourceIp = "DEVICE",
                        destIp = ip,
                        protocol = "TCP",
                        intensity = 1.0f,
                        color = Color.Cyan,
                        latitude = lat,
                        longitude = lon,
                        countryCode = "TS",
                        city = "CLI_STATION"
                    )
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val filter = IntentFilter("SENTINEL_INJECT_NODE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(cliReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(cliReceiver, filter)
        }

        setContent {
            Neural3DContent(
                state = AnalysisUiState(neuralLinks = testLinks),
                onBack = { finish() }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(cliReceiver)
        } catch (_: Exception) {}
    }
}
