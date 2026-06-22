package com.intersec.androidapp.core.file

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Gerencia o upload de capturas e logs para o Firebase Cloud Storage.
 */
object FirebaseStorageManager {
    private val storage = Firebase.storage
    private val storageRef = storage.reference

    /**
     * Faz upload de um arquivo local (.pcap ou log) para o bucket gratuito.
     */
    suspend fun uploadFile(localFile: File, remotePath: String): Result<String> {
        return try {
            val fileRef = storageRef.child(remotePath)
            val uploadTask = fileRef.putFile(android.net.Uri.fromFile(localFile))
            
            val snapshot = uploadTask.await()
            val downloadUrl = snapshot.metadata?.reference?.downloadUrl?.await().toString()
            
            Log.d("FirebaseStorage", "Upload concluído: $downloadUrl")
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Erro no upload: ${e.message}")
            Result.failure(e)
        }
    }
}
