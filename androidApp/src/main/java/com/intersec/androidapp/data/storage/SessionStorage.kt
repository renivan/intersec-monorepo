package com.intersec.androidapp.data.storage

import com.intersec.androidapp.data.model.entity.SavedSession

/**
 * Interface para armazenamento persistente de metadados das sessões capturadas.
 */
interface SessionStorage {
    suspend fun save(session: SavedSession)
    suspend fun list(): List<SavedSession>
    suspend fun delete(sessionId: String)
}
