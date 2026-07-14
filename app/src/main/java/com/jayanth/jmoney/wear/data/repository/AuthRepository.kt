package com.jayanth.jmoney.wear.data.repository

import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val sessionFlow: StateFlow<UserSession?>
    
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
    suspend fun autoLogin(): Boolean
    fun getCurrentUserEmail(): String?
    fun getCurrentUserId(): String?
}
