package com.intersec.androidapp.core.bridge

/**
 * Wrapper genérico para os resultados vindos do motor Rust.
 */
data class RustBridgeResult<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)
