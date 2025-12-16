package com.example.splashandregist.viewmodel // Sesuaikan package

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splashandregist.data.model.Booking
import com.example.splashandregist.data.repository.BookingRepository
import com.example.splashandregist.data.repository.HotelOption
import kotlinx.coroutines.launch

class BookingViewModel : ViewModel() {

    private val repository = BookingRepository()

    // Data List Booking
    private val _bookings = mutableStateListOf<Booking>()
    val bookings: List<Booking> get() = _bookings

    // Data List Hotel (Buat Dropdown)
    private val _hotelOptions = mutableStateListOf<HotelOption>()
    val hotelOptions: List<HotelOption> get() = _hotelOptions

    // Status Loading (biar tombol gak diklik 2x)
    var isLoading by mutableStateOf(false)
    var isUploading by mutableStateOf(false) // Alias untuk isLoading saat upload

    // 1. Ambil Data Booking
    fun fetchBookings() {
        viewModelScope.launch {
            try {
                val data = repository.getBookings()
                _bookings.clear()
                _bookings.addAll(data)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // 2. Ambil Data Dropdown Hotel
    fun fetchHotelOptions() {
        viewModelScope.launch {
            try {
                val data = repository.getHotelOptions()
                _hotelOptions.clear()
                _hotelOptions.addAll(data)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // 3. Hapus Data
    fun deleteBooking(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteBooking(id)
                fetchBookings() // Refresh
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // 4. LOGIKA SIMPAN BOOKING (Bisa dengan Gambar atau Tanpa Gambar)
    fun addBookingWithImage(context: Context, booking: Booking, imageUri: Uri?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isUploading = true // Mulai Loading
            try {
                var finalBooking = booking

                // Jika user memilih gambar di awal
                if (imageUri != null) {
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                    val imageBytes = inputStream?.readBytes()

                    if (imageBytes != null) {
                        val url = repository.uploadProofImage(imageBytes)
                        // Kalau ada gambar, status langsung Confirmed
                        finalBooking = booking.copy(proofImageUrl = url, status = "Confirmed")
                    }
                }

                // Simpan ke Database
                repository.createBooking(finalBooking)

                fetchBookings() // Refresh List
                onSuccess()     // Balik ke halaman list

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Gagal Simpan: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isUploading = false // Stop Loading
            }
        }
    }

    // 5. LOGIKA KONFIRMASI LUNAS (Dari Halaman Detail)
    fun confirmPayment(context: Context, bookingId: Long, imageUri: Uri, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isUploading = true
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val imageBytes = inputStream?.readBytes()

                if (imageBytes != null) {
                    val url = repository.uploadProofImage(imageBytes)
                    // Update Status jadi Lunas & Simpan URL
                    repository.updateStatusWithProof(bookingId, url)

                    fetchBookings()
                    onSuccess()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal Upload: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isUploading = false
            }
        }
    }
}