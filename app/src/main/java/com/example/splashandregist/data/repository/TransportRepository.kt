package com.example.splashandregist.data.repository


import android.content.Context
import android.net.Uri
import com.example.splashandregist.data.SupabaseClient
import com.example.splashandregist.data.model.Transport
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import java.util.UUID


// Repository bertugas mengatur semua interaksi dengan database & storage
class TransportRepository {


    /* ================= UPLOAD GAMBAR ================= */
    // Suspend digunakan agar fungsi yang mengakses internet dijalankan di background tanpa mengganggu UI dan tidak freeze
    suspend fun uploadImage(
        context: Context,
        imageUri: Uri // URI gambar yang dipilih user (alamat file lokal)
    ): String {
        // Membuat nama file unik agar tidak bentrok di storage
        val fileName = "transport_${UUID.randomUUID()}.jpg"


        // Membaca file gambar dari URI menjadi byte array
        val bytes = context.contentResolver
            .openInputStream(imageUri) // Membuka file gambar
            ?.readBytes() // Membaca isi file
            ?: throw Exception("File tidak ditemukan") // Jika gagal


        // Mengambil bucket storage Supabase bernama "transport-images"
        val bucket = SupabaseClient.client.storage.from("transport-images")

        // Upload file ke Supabase Storage
        bucket.upload(fileName, bytes)


        // Mengembalikan URL publik gambar
        return bucket.publicUrl(fileName)
    }


    /* ================= GET DATA ================= */
    // Mengambil seluruh data transport dari tabel "transports"
    suspend fun getTransports(): List<Transport> {
        return SupabaseClient.client
            .from("transports") // Nama tabel di database
            .select()
            .decodeList<Transport>() // Convert JSON ke List<Transport>
    }


    /* ================= INSERT DATA ================= */
    // Menyimpan data transport baru ke database
    suspend fun insertTransport(t: Transport) {
        SupabaseClient.client.from("transports").insert(t)
    }


    /* ================= UPDATE DATA ================= */
    // Menyimpan data transport baru ke database
    suspend fun updateTransport(t: Transport) {
        SupabaseClient.client
            .from("transports")
            .update({
                // Set nilai kolom sesuai data terbaru
                set("name", t.name)
                set("type", t.type)
                set("route", t.route)
                set("capacity", t.capacity)
                set("price", t.price)
                set("image_url", t.imageUrl)
                set("description", t.description)
            }) {
                // Update hanya data dengan ID yang sesuai
                filter { eq("id", t.id!!) }
            }
    }


    /* ================= DELETE DATA ================= */
// Menghapus data transport berdasarkan ID
    suspend fun deleteTransport(id: String) {
        SupabaseClient.client
            .from("transports") // Tabel target
            .delete {
                filter { eq("id", id) }
                // Hapus data dengan ID tertentu
            }
    }
}

