package com.example.splashandregist.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splashandregist.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class UserProfile(
    @SerialName("email") val email: String,
    @SerialName("role") val role: String
)
class LoginViewModel : ViewModel() {

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun login(
        context: Context,
        emailInput: String,
        passwordInput: String,
        onLoginSuccess: (String) -> Unit // String ini nanti isinya 'admin' atau 'customer'
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Login ke Supabase Auth (Cek Password)
                SupabaseClient.client.auth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }

                // 2. Jika Password benar, Cari Role dia di tabel 'users'
                // Kita cari berdasarkan email yang sedang login
                val userProfile = SupabaseClient.client
                    .from("users")
                    .select {
                        filter {
                            eq("email", emailInput)
                        }
                    }
                    .decodeSingle<UserProfile>() // Ambil 1 data saja

                // 3. Kabari UI bahwa login sukses & kirim Role-nya
                onLoginSuccess(userProfile.role)

            } catch (e: Exception) {
                e.printStackTrace()
                // Cek error khusus (Password salah vs User tidak ada)
                val message = if (e.message?.contains("Invalid login") == true) {
                    "Email atau Password Salah!"
                } else {
                    "Login Gagal: ${e.message}"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } finally {
                _isLoading.value = false
            }
        }
    }
}