package com.example.splashandregist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.launch
import com.example.splashandregist.data.repositories.BookingRepository
import com.example.splashandregist.data.model.Booking

class BookingViewModel : ViewModel() {

    // 1. Panggil si Pelayan (Repository)
    private val repository = BookingRepository()

    // 2. Siapkan Wadah Data (List Booking)
    // Awalnya kosong, nanti diisi setelah data datang dari internet
    private val _bookings = mutableStateListOf<Booking>()
    val bookings: List<Booking> get() = _bookings

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
// ... kode lama (fetchBookings) biarkan di atas ...

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

    // 3. FUNGSI TAMBAH (CREATE)
    fun addBooking(booking: Booking) {
        viewModelScope.launch {
            try {
                repository.createBooking(booking)
                fetchBookings() // Refresh otomatis
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}