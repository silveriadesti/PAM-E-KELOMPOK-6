package com.example.splashandregist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splashandregist.data.repository.AuthRepository
import com.example.splashandregist.ui.common.AuthUiState
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    val isAuthenticated: StateFlow<Boolean> =
        repo.sessionStatus
            .map { it is SessionStatus.Authenticated }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                repo.currentSession() != null
            )

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                repo.login(email, password)
                _uiState.value = AuthUiState.Idle // 🔥 JANGAN NUNGGU SESSION
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Login gagal")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                repo.register(email, password)
                _uiState.value = AuthUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Register gagal")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
        }
    }
}
