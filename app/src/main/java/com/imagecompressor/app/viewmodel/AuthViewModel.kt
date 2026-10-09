package com.imagecompressor.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.imagecompressor.app.auth.FirebaseAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object SignedOut : AuthUiState
    data object Loading : AuthUiState
    data class SignedIn(val user: FirebaseUser) : AuthUiState
    data class Error(val message: String) : AuthUiState
    data object ResetEmailSent : AuthUiState
}

/**
 * Backs the Login / Register / Forgot-password screens. Firebase Authentication is
 * OPTIONAL — the app is fully usable in guest mode without ever touching this ViewModel.
 */
class AuthViewModel(
    private val authManager: FirebaseAuthManager = FirebaseAuthManager()
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(
        authManager.currentUser?.let { AuthUiState.SignedIn(it) } ?: AuthUiState.SignedOut
    )
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    val currentUser get() = authManager.currentUser

    fun register(email: String, password: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            authManager.register(email, password)
                .onSuccess { _state.value = AuthUiState.SignedIn(it) }
                .onFailure { _state.value = AuthUiState.Error(it.message ?: "Registration failed") }
        }
    }

    fun login(email: String, password: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            authManager.login(email, password)
                .onSuccess { _state.value = AuthUiState.SignedIn(it) }
                .onFailure { _state.value = AuthUiState.Error(it.message ?: "Login failed") }
        }
    }

    fun sendPasswordReset(email: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            authManager.sendPasswordReset(email)
                .onSuccess { _state.value = AuthUiState.ResetEmailSent }
                .onFailure { _state.value = AuthUiState.Error(it.message ?: "Could not send reset email") }
        }
    }

    fun continueAsGuest() {
        _state.value = AuthUiState.SignedOut
    }

    fun logout() {
        authManager.logout()
        _state.value = AuthUiState.SignedOut
    }
}
