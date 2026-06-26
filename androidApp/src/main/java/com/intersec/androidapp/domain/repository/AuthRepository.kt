package com.intersec.androidapp.domain.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

/**
 * Interface para operações de autenticação.
 */
interface AuthRepository {
    val currentUser: Flow<FirebaseUser?>
    
    suspend fun signIn(email: String, password: String): Result<FirebaseUser>
    suspend fun signUp(email: String, password: String): Result<FirebaseUser>
    suspend fun signOut()
    suspend fun isUserLoggedIn(): Boolean
}
