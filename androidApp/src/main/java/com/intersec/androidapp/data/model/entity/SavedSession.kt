package com.intersec.androidapp.data.model.entity

/**
 * Representa uma sessão de captura que foi salva permanentemente no dispositivo.
 */
data class SavedSession(
    val id: String,
    val filePath: String,
    val name: String,
    val createdAt: Long,
    val packetCount: Long = 0,
    val flowCount: Long = 0,
)
