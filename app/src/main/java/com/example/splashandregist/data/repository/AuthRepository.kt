package com.example.splashandregist.data.repository

import android.util.Log
import com.example.splashandregist.data.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

class AuthRepository {

    private val auth: Auth get() = SupabaseClient.client.auth

    suspend fun register(email: String, password: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun login(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun logout() {
        auth.signOut()
    }

    val sessionStatus: Flow<SessionStatus>
        get() = auth.sessionStatus.onEach { status ->
            Log.d("AuthRepo", "SessionStatus: $status")
        }

    fun currentSession(): UserSession? = SupabaseClient.session()
}
