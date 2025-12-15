package com.example.splashandregist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splashandregist.data.repositories.AuthRepository
import com.example.splashandregist.ui.common.UiResult
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<UiResult<Boolean>>(UiResult.Idle)
    val authState: StateFlow<UiResult<Boolean>> = _authState

    val isAuthenticated: StateFlow<Boolean> = repo.sessionStatus
        .map { it is SessionStatus.Authenticated }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = repo.currentSession() != null
        )

    fun register(email: String, password: String) {
        _authState.value = UiResult.Loading
        viewModelScope.launch {
            try {
                repo.register(email, password)
                _authState.value = UiResult.Success(true)
            } catch (e: Exception) {
                _authState.value = UiResult.Error(e.message ?: "Register gagal")
            }
        }
    }

    fun login(email: String, password: String) {
        _authState.value = UiResult.Loading
        viewModelScope.launch {
            try {
                repo.login(email, password)
                _authState.value = UiResult.Success(true)
            } catch (e: Exception) {
                _authState.value = UiResult.Error(e.message ?: "Login gagal")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
            _authState.value = UiResult.Success(false)
        }
    }
}
