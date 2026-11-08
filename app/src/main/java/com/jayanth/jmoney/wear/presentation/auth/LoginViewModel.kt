package com.jayanth.jmoney.wear.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayanth.jmoney.wear.BuildConfig
import com.jayanth.jmoney.wear.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    object Authenticated : AuthState
    data class Error(val message: String) : AuthState
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    val sessionFlow = authRepository.sessionFlow

    init {
        checkSession()
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.sessionFlow.collect { session ->
                if (session == null && _authState.value is AuthState.Authenticated) {
                    _authState.value = AuthState.Idle
                }
            }
        }
    }

    private fun checkSession() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val restored = authRepository.autoLogin()
            if (restored) {
                _authState.value = AuthState.Authenticated
            } else {
                // If not restored, try auto-signing in with development credentials
                val devEmail = BuildConfig.DEV_EMAIL
                val devPassword = BuildConfig.DEV_PASSWORD
                if (devEmail.isNotEmpty() && devPassword.isNotEmpty()) {
                    signIn(devEmail, devPassword)
                } else {
                    _authState.value = AuthState.Idle
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signInWithEmail(email, password)
                .onSuccess {
                    _authState.value = AuthState.Authenticated
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.localizedMessage ?: "Login failed")
                }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
