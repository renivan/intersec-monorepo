package com.intersec.androidapp.presentation.state

import androidx.compose.ui.graphics.Color

/**
 * Modelo de dados para a interface 3D Sentinel.
 * Representa uma conexão neural geo-localizada no espaço 3D.
 */
data class NeuralLink3D(
    val id: String,
    val sourceIp: String,
    val destIp: String,
    val protocol: String,
    val intensity: Float, // 0.0 a 1.0 (brilho da conexão)
    val color: Color,
    
    // Coordenadas para projeção no Globo 3D
    val latitude: Double,
    val longitude: Double,
    val countryCode: String,
    val city: String,
    
    // Vetores 3D (X, Y, Z) calculados
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,

    val isInterception: Boolean = false,
    val lastPulseTime: Long = System.currentTimeMillis()
)

data class Neural3DUiState(
    val isActive: Boolean = false,
    val links: List<NeuralLink3D> = emptyList(),
    val totalNodesDetected: Int = 0,
    val globeRotation: Float = 0f,
    val isScanning: Boolean = false
)
