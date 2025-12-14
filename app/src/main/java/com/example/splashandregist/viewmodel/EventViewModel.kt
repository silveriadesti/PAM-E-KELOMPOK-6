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


// ===============================================
// 1. DATA MODEL (Disesuaikan dengan Skema Events)
// ===============================================


@Serializable
data class Event(
    val id: Long? = null,
    @SerialName("user_id")
    val userId: String? = null, // Asumsi bisa null/diisi belakangan
    val title: String,
    @SerialName("event_date")
    val eventDate: String,
    val location: String,
    @SerialName("display_price")
    val displayPrice: String,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val description: String? = null,
    @SerialName("price_details")
    val priceDetails: String? = null
)


// ===============================================
// 2. VIEW MODEL IMPLEMENTASI
// ===============================================


class EventViewModel : ViewModel() {


    // List Event
    private val _events = mutableStateListOf<Event>()
    val events: List<Event> get() = _events


    // State Loading/Uploading
    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading


    // 1. Ambil Data Event (READ LIST)
    fun getEvents() {
        viewModelScope.launch {
            try {
                val data = SupabaseClient.client
                    .from("events")
                    .select()
                    .decodeList<Event>()
                _events.clear()
                _events.addAll(data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    // 2. Ambil 1 Event berdasarkan ID (DETAIL)
    fun getEventById(id: String): Event? {
        val idLong = id.toLongOrNull()
        return _events.find { it.id == idLong }
    }


    // 3. Upload Gambar & Simpan Data Event Baru (CREATE)
    fun uploadImageAndSaveEvent(
        context: Context,
        imageUri: Uri,
        userId: String?, // Ambil dari sesi pengguna yang sedang login
        title: String,
        eventDate: String,
        location: String,
        displayPrice: String,
        description: String,
        priceDetails: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                // A. Baca file gambar
                val imageBytes = context.contentResolver.openInputStream(imageUri)?.use {
                    it.readBytes()
                } ?: throw Exception("Gagal membaca file gambar")


                // B. Upload ke Storage (Gunakan bucket yang sesuai, misal: "event-images")
                val fileName = "event-${UUID.randomUUID()}.jpg"
                val bucket = SupabaseClient.client.storage.from("event-images")
                bucket.upload(fileName, imageBytes)
                val publicUrl = bucket.publicUrl(fileName)


                // C. Simpan ke Database
                val newEvent = Event(
                    userId = userId,
                    title = title,
                    eventDate = eventDate,
                    location = location,
                    displayPrice = displayPrice,
                    imageUrl = publicUrl,
                    // Gunakan takeIf untuk membuat kolom menjadi NULL jika string kosong
                    description = description.takeIf { it.isNotBlank() },
                    priceDetails = priceDetails.takeIf { it.isNotBlank() }
                )
                SupabaseClient.client.from("events").insert(newEvent)


                // D. Refresh & Selesai
                getEvents()
                onSuccess()


            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error menyimpan event: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _isUploading.value = false
            }
        }
    }


    // TODO: Tambahkan fungsi updateEvent jika Anda memerlukan fungsi Edit Event
}
