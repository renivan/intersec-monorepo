package com.intersec.androidapp.core.file

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Utilitário para importar capturas do Android para o cache interno.
 */
object AndroidCaptureImporter {

    fun importToCache(
        context: Context,
        uri: Uri,
    ): String {
        val resolvedName = resolveDisplayName(context.contentResolver, uri)
        val extension = if (resolvedName?.endsWith(".pcapng", ignoreCase = true) == true) ".pcapng" else ".pcap"
        val fileName = resolvedName ?: "capture_${System.currentTimeMillis()}$extension"

        val target = File(context.cacheDir, fileName)

        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Não foi possível abrir o arquivo selecionado." }

            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return target.absolutePath
    }

    private fun resolveDisplayName(
        contentResolver: ContentResolver,
        uri: Uri,
    ): String? {
        val cursor = contentResolver.query(uri, null, null, null, null) ?: return null

        cursor.use {
            if (!it.moveToFirst()) return null
            val index = it.getColumnIndex("_display_name")
            if (index < 0) return null
            return it.getString(index)
        }
    }
}
