package com.example.splashandregist.data.repository

import io.github.jan.supabase.postgrest.from
import com.example.splashandregist.data.model.Booking
import com.example.splashandregist.data.SupabaseClient

// Class ini adalah "Pelayan" khusus urusan Booking
class BookingRepository {

    // 1. FUNGSI AMBIL DATA (READ)
    // "Tolong ambilkan semua daftar booking dari dapur (Supabase)"
    suspend fun getBookings(): List<Booking> {
        return SupabaseClient.client
            .from("bookings") // Nama tabel di Supabase
            .select()         // Perintah: Pilih semua
            .decodeList<Booking>() // Terjemahkan jadi daftar Booking
    }

    // 2. FUNGSI TAMBAH DATA (CREATE)
    // "Tolong antarkan pesanan baru ini ke dapur"
    suspend fun createBooking(booking: Booking) {
        SupabaseClient.client
            .from("bookings")
            .insert(booking) // Perintah: Masukkan data
    }

    // 3. FUNGSI UPDATE STATUS (UPDATE)
    // "Tolong ubah status pesanan ini jadi Confirmed/Lunas"
    suspend fun updateStatus(bookingId: Long, newStatus: String) {
        SupabaseClient.client
            .from("bookings")
            .update(
                {
                    set("status", newStatus) // Ubah kolom 'status' jadi nilai baru
                }
            ) {
                filter {
                    eq("id", bookingId) // Cari yang ID-nya cocok
                }
            }
    }

    // 4. FUNGSI HAPUS DATA (DELETE)
    // "Tolong buang pesanan ini ke tempat sampah"
    suspend fun deleteBooking(bookingId: Long) {
        SupabaseClient.client
            .from("bookings")
            .delete {
                filter {
                    eq("id", bookingId) // Cari yang ID-nya cocok
                }
            }
    }

    //UPLOAD GAMBAR
    suspend fun uploadProofImage(imageBytes: ByteArray): String {
        val fileName = "proof_${System.currentTimeMillis()}_${Random.nextInt(1000)}.jpg"
        val bucket = SupabaseClient.client.storage.from("booking-proofs")
        bucket.upload(fileName, imageBytes) { upsert = false }
        return bucket.publicUrl(fileName)
    }
    // Konfirmasi Lunas TANPA upload gambar (kalau gambar sudah diupload pas create)
    suspend fun confirmBookingOnly(bookingId: Long) {
        SupabaseClient.client.from("bookings").update({ set("status", "Confirmed") }) { filter { eq("id", bookingId) } }
    }

}