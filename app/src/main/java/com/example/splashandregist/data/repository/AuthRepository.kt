package com.example.splashandregist.data.repositories




import android.util.Log
import com.example.splashandregist.data.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
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
        get() {
            // Gunakan operator onEach untuk melakukan logging
            return auth.sessionStatus.onEach { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        // Log sumber sesi di sini, di dalam Repository
                        Log.d("AuthRepo", "Authenticated from source: ${status.source}")
                    }
                    is SessionStatus.NotAuthenticated -> {
                        Log.d("AuthRepo", "Status: Not authenticated. Signed out: ${status.isSignOut}")
                    }
                    else -> {
                        Log.d("AuthRepo", "Unknown status: $status")
                    }
                }
            }
        }



    fun currentSession(): UserSession? = SupabaseClient.session()
}