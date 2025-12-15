package com.example.splashandregist.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splashandregist.data.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

// --- DATA CLASS (MODEL) ---
@Serializable
data class Hotel(
    val id: Long? = null,
    @SerialName("name") val name: String,
    @SerialName("location") val location: String,
    @SerialName("price") val price: String,
    @SerialName("description") val description: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("user_id") val userId: String? = null
)

// --- VIEW MODEL (LOGIC) ---
class HotelViewModel : ViewModel() {

    // List hotel
    private val _hotels = mutableStateListOf<Hotel>()
    val hotels: List<Hotel> get() = _hotels

    // State Loading
    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading

    // 1. Ambil Data Hotel (READ)
    fun getHotels() {
        viewModelScope.launch {
            try {
                val data = SupabaseClient.client
                    .from("hotels")
                    .select()
                    .decodeList<Hotel>()
                _hotels.clear()
                _hotels.addAll(data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    // 2. Ambil 1 Hotel berdasarkan ID (DETAIL)
    fun getHotelById(id: String): Hotel? {
        val idLong = id.toLongOrNull()
        return _hotels.find { it.id == idLong }
    }

    // 3. Upload Gambar & Simpan Data (CREATE)
    fun uploadImageAndSaveHotel(
        context: Context,
        imageUri: Uri,
        name: String,
        location: String,
        price: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                // A. Baca file gambar
                val imageBytes = context.contentResolver.openInputStream(imageUri)?.use {
                    it.readBytes()
                } ?: throw Exception("Gagal membaca file gambar")

                // B. Upload ke Storage
                val fileName = "${UUID.randomUUID()}.jpg"
                val bucket = SupabaseClient.client.storage.from("hotel-images")
                bucket.upload(fileName, imageBytes)
                val publicUrl = bucket.publicUrl(fileName)

                // C. Simpan ke Database
                val newHotel = Hotel(
                    name = name,
                    location = location,
                    price = "Rp $price",
                    description = description,
                    imageUrl = publicUrl
                )
                SupabaseClient.client.from("hotels").insert(newHotel)

                // D. Refresh & Selesai
                getHotels()
                onSuccess()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun updateHotel(
        context: Context,
        hotelId: Long,        // ID hotel yang mau diedit
        imageUri: Uri?,       // Gambar baru (Bisa NULL kalau tidak ganti gambar)
        currentImageUrl: String, // URL gambar lama (Jaga-jaga kalau gak ganti gambar)
        name: String,
        location: String,
        price: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                var finalImageUrl = currentImageUrl

                // 1. CEK: Apakah user memilih gambar baru?
                if (imageUri != null) {
                    // Kalau ada gambar baru, upload dulu
                    val imageBytes = context.contentResolver.openInputStream(imageUri)?.use {
                        it.readBytes()
                    } ?: throw Exception("Gagal membaca gambar")

                    val fileName = "${UUID.randomUUID()}.jpg"
                    val bucket = SupabaseClient.client.storage.from("hotel-images")
                    bucket.upload(fileName, imageBytes)
                    finalImageUrl = bucket.publicUrl(fileName)
                }

                // 2. UPDATE data ke Database
                // Kita buat object Hotel baru, tapi ID-nya pakai ID lama
                val updatedHotel = Hotel(
                    id = hotelId,
                    name = name,
                    location = location,
                    price = price, // Pastikan formatnya "Rp ..."
                    description = description,
                    imageUrl = finalImageUrl
                )

                // Perintah Update ke Supabase
                SupabaseClient.client.from("hotels").update(
                    {
                        set("name", updatedHotel.name)
                        set("location", updatedHotel.location)
                        set("price", updatedHotel.price)
                        set("description", updatedHotel.description)
                        set("image_url", updatedHotel.imageUrl)
                    }
                ) {
                    filter {
                        eq("id", hotelId) // Cari baris yang ID-nya cocok
                    }
                }

                // 3. Refresh List & Selesai
                getHotels()
                onSuccess()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Gagal Update: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _isUploading.value = false
            }
        }
    }
}