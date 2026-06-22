package com.intersec.androidapp.core.runtime

object RustRuntimeLoader {
    @Volatile
    private var loaded: Boolean = false

    @Synchronized
    fun ensureLoaded() {
        if (loaded) return

        // Ajuste este nome para o nome final da biblioteca nativa gerada/empacotada.
        System.loadLibrary("wireshark_mobile_core")
        loaded = true
    }

    fun isLoaded(): Boolean = loaded
}
