package com.intersec.androidapp.data.storage

import com.intersec.androidapp.data.model.entity.SavedSession

/**
 * Implementação em memória do armazenamento de sessões (FASE 1).
 * Pode ser evoluído para Room Database no futuro.
 */
class SessionStorageImpl : SessionStorage {

    private val sessions = mutableListOf<SavedSession>()

    override suspend fun save(session: SavedSession) {
        sessions.add(session)
    }

    override suspend fun list(): List<SavedSession> {
        return sessions.toList()
    }

    override suspend fun delete(sessionId: String) {
        sessions.removeIf { it.id == sessionId }
    }
}
