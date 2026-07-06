package com.intersec.androidapp.core.runtime

object NativeRuntimeLoader {
    @Volatile
    private var loaded: Boolean = false

    @Synchronized
    fun ensureLoaded(): Boolean {
        if (loaded) return true

        return try {
            System.loadLibrary("wireshark_mobile_core")
            loaded = true
            true
        } catch (e: UnsatisfiedLinkError) {
            android.util.Log.e("interSec_CORE", "ERRO CRÍTICO: Biblioteca nativa não encontrada!")
            false
        }
    }
}

