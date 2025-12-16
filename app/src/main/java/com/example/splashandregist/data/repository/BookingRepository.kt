package com.example.splashandregist.data.repository // Sesuaikan package

import com.example.splashandregist.data.SupabaseClient
import com.example.splashandregist.data.model.Booking
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.Serializable
import kotlin.random.Random

// Class kecil khusus untuk data Dropdown Hotel
@Serializable
data class HotelOption(val id: Long, val name: String)

class BookingRepository {

    // 1. Ambil Semua Booking (Diurutkan dari yang terbaru/ID terbesar)
    suspend fun getBookings(): List<Booking> {
        return SupabaseClient.client
            .from("bookings")
            .select()
            .decodeList<Booking>()
            .sortedByDescending { it.id }
    }

    // 2. Simpan Booking Baru
    suspend fun createBooking(booking: Booking) {
        SupabaseClient.client.from("bookings").insert(booking)
    }

    // 3. Hapus Booking
    suspend fun deleteBooking(bookingId: Long) {
        SupabaseClient.client.from("bookings").delete {
            filter { eq("id", bookingId) }
        }
    }

    // --- FITUR BARU (WAJIB ADA) ---

    // 4. Update Status + Simpan URL Gambar (Untuk Konfirmasi Lunas)
    suspend fun updateStatusWithProof(bookingId: Long, imageUrl: String) {
        SupabaseClient.client.from("bookings").update(
            {
                set("status", "Confirmed")
                set("proof_image_url", imageUrl)
            }
        ) {
            filter { eq("id", bookingId) }
        }
    }

    // 5. Ambil Daftar Nama Hotel (Untuk Dropdown di Form Tambah)
    suspend fun getHotelOptions(): List<HotelOption> {
        return SupabaseClient.client
            .from("hotels")
            // Cuma ambil kolom ID dan NAME biar ringan
            .select(columns = Columns.list("id", "name"))
            .decodeList<HotelOption>()
    }

    // 6. Upload Gambar ke Supabase Storage
    suspend fun uploadProofImage(imageBytes: ByteArray): String {
        // Nama file acak biar gak bentrok
        val fileName = "proof_${System.currentTimeMillis()}_${Random.nextInt(1000)}.jpg"
        val bucket = SupabaseClient.client.storage.from("booking-proofs") // Pastikan bucket ini ada di Supabase!
        bucket.upload(fileName, imageBytes) { upsert = false }
        return bucket.publicUrl(fileName)
    }
}