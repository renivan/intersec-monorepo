package com.intersec.androidapp.core.bridge

/**
 * Wrapper genÃ©rico para os resultados vindos do motor Native.
 */
data class NativeBridgeResult<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

