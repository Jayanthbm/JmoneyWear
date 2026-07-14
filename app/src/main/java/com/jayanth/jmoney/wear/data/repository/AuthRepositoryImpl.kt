package com.jayanth.jmoney.wear.data.repository

import com.jayanth.jmoney.wear.data.storage.SecureStorage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val secureStorage: SecureStorage
) : AuthRepository {

    private val _sessionFlow = MutableStateFlow<UserSession?>(null)
    override val sessionFlow: StateFlow<UserSession?> = _sessionFlow.asStateFlow()

    init {
        // Collect session status from Supabase to update our local session flow and storage
        CoroutineScope(Dispatchers.Main).launch {
            supabaseClient.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val session = status.session
                        _sessionFlow.value = session
                        try {
                            val json = Json.encodeToString(UserSession.serializer(), session)
                            secureStorage.saveSessionJson(json)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _sessionFlow.value = null
                        secureStorage.clearSession()
                    }
                    else -> {}
                }
            }
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return runCatching {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return runCatching {
            supabaseClient.auth.signOut()
            _sessionFlow.value = null
            secureStorage.clearSession()
        }
    }

    override suspend fun autoLogin(): Boolean {
        // 1. Check if we have a saved session in secure storage
        val savedSessionJson = secureStorage.getSessionJson()
        if (!savedSessionJson.isNullOrBlank()) {
            try {
                val session = Json.decodeFromString(UserSession.serializer(), savedSessionJson)
                supabaseClient.auth.importSession(session)
                _sessionFlow.value = session
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                secureStorage.clearSession()
            }
        }
        return false
    }

    override fun getCurrentUserEmail(): String? {
        return _sessionFlow.value?.user?.email
    }

    override fun getCurrentUserId(): String? {
        return _sessionFlow.value?.user?.id
    }
}
