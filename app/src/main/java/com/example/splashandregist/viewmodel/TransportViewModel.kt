package com.example.splashandregist.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splashandregist.data.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import android.content.Context
import android.net.Uri
import io.github.jan.supabase.storage.storage
import java.util.UUID

@Serializable
data class Transport(
    val id: String? = null, // 🔥 WAJIB NULLABLE
    val name: String,
    val type: String,
    val route: String,
    val capacity: Int,
    val price: String,
    @SerialName("image_url")
    val imageUrl: String,
    val description: String
)


class TransportViewModel : ViewModel() {

    private val _list = mutableStateListOf<Transport>()
    val transports: List<Transport> = _list

    // ================= UPLOAD IMAGE =================
    fun uploadImage(
        context: Context,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val fileName = "transport_${UUID.randomUUID()}.jpg"

                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: throw Exception("File tidak ditemukan")

                SupabaseClient.client
                    .storage
                    .from("transport-images")
                    .upload(
                        path = fileName,
                        data = inputStream.readBytes()
                    ) {
                        upsert = true
                    }

                val publicUrl = SupabaseClient.client
                    .storage
                    .from("transport-images")
                    .publicUrl(fileName)

                onSuccess(publicUrl)

            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Upload gagal")
            }
        }
    }

    /* ================= READ ================= */
    fun fetchTransports() {
        viewModelScope.launch {
            try {
                val result = SupabaseClient.client
                    .from("transports")
                    .select()
                    .decodeList<Transport>()

                _list.clear()
                _list.addAll(result)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getTransportById(id: String): Transport? {
        return _list.find { it.id == id }
    }

    /* ================= CREATE ================= */
    fun addTransport(
        t: Transport,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                SupabaseClient.client
                    .from("transports")
                    .insert(t)

                fetchTransports()
                onSuccess()

            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Insert gagal")
            }
        }
    }

    /* ================= UPDATE ================= */
    fun updateTransport(updated: Transport, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client
                    .from("transports")
                    .update({
                        set("name", updated.name)
                        set("type", updated.type)
                        set("route", updated.route)
                        set("capacity", updated.capacity)
                        set("price", updated.price)
                        set("image_url", updated.imageUrl)
                        set("description", updated.description)
                    }) {
                        filter { eq("id", updated.id!!) }
                    }

                fetchTransports()
                onSuccess()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /* ================= DELETE ================= */
    fun deleteTransport(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client
                    .from("transports")
                    .delete {
                        filter { eq("id", id) }
                    }

                fetchTransports()
                onSuccess()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}