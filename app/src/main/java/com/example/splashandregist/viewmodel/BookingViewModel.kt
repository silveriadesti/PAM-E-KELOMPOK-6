package com.example.splashandregist.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.launch
import com.example.splashandregist.data.model.Booking
import com.example.splashandregist.data.repository.BookingRepository
import com.example.splashandregist.data.repository.SimpleHotel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.splashandregist.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage

class BookingViewModel : ViewModel() {

    // 1. Panggil si Pelayan (Repository)
    private val repository = BookingRepository()

    // 2. Siapkan Wadah Data (List Booking)
    // Awalnya kosong, nanti diisi setelah data datang dari internet
    private val _bookings = mutableStateListOf<Booking>()
    val bookings: List<Booking> get() = _bookings

    //DAFTAR HOTEL UNTUK BOOKING
    private val _hotelOptions = mutableStateListOf<SimpleHotel>()
    val hotelOptions: List<SimpleHotel> get() = _hotelOptions

//    BOOKING
    val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
    ?: throw Exception("User belum login")

    //UPLOAD GAMBAR
    var isUploading by mutableStateOf(false)
        private set

    // 3. Perintah: "Ambil Data Sekarang!"
    fun fetchBookings() {
        viewModelScope.launch {
            try {
                // Suruh pelayan ambil data
                val data = repository.getBookings()

                // Bersihkan wadah lama, isi dengan yang baru
                _bookings.clear()
                _bookings.addAll(data)

            } catch (e: Exception) {
                // Kalau error (misal internet mati), catat di Logcat
                println("🔥🔥 ERROR SUPABASE: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    //AMBIL DATA HOTEL
    fun fetchHotelOptions() {
        viewModelScope.launch {
            try {
                val data = repository.getHotelOptions()
                _hotelOptions.clear()
                _hotelOptions.addAll(data)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // 1. FUNGSI HAPUS (DELETE)
    fun deleteBooking(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteBooking(id)
                fetchBookings() // Refresh otomatis setelah dihapus
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 2. FUNGSI UPDATE STATUS (UPDATE)
    // Ubah status dari "Pending" jadi "Confirmed" (Lunas)
    fun confirmBooking(id: Long) {
        viewModelScope.launch {
            try {
                repository.updateStatus(id, "Confirmed")
                fetchBookings() // Refresh otomatis
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 3. FUNGSI TAMBAH (CREATE) DENGAN LOGGING LENGKAP
    fun addBookingWithImage(
        context: Context,
        booking: Booking,
        imageUri: Uri?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                var imageUrl: String? = null

                // ===== UPLOAD IMAGE =====
                if (imageUri != null) {
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                        ?: throw Exception("Gagal membuka gambar")

                    val bytes = inputStream.readBytes()

                    val fileName = "booking_${System.currentTimeMillis()}.jpg"

                    SupabaseClient.client.storage
                        .from("booking-proofs")
                        .upload(fileName, bytes)

                    imageUrl = SupabaseClient.client.storage
                        .from("booking-proofs")
                        .publicUrl(fileName)
                }

                // ===== INSERT DATA =====
                val data = booking.copy(
                    proofImageUrl = imageUrl
                )

                SupabaseClient.client.postgrest["bookings"]
                    .insert(data)

                fetchBookings()
                onSuccess()

            } catch (e: Exception) {
                Log.e("BOOKING_ERROR", e.message ?: "Unknown error")

                Toast.makeText(
                    context,
                    "Gagal menyimpan booking: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


}