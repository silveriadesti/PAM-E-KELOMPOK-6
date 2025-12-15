package com.example.splashandregist.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

// Gunakan 'object' agar dia jadi Singleton (Cuma ada 1 koneksi untuk seluruh aplikasi)
object SupabaseClient {

    private const val SUPABASE_URL = "https://yhuflxzqvednwqluokhz.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlodWZseHpxdmVkbndxbHVva2h6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU1ODMwNTEsImV4cCI6MjA4MTE1OTA1MX0.ePSA65T7DvGe4OvLT55AARtx4H-fsWlC-ISjYit3A7M"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        // Pasang fitur-fitur yang mau dipakai
        install(Auth.Companion)      // Buat Login
        install(Postgrest.Companion) // Buat Database (CRUD)
        install(Storage.Companion)   // Buat Upload Gambar
    }
}