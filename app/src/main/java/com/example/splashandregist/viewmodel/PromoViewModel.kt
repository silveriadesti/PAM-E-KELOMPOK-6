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

@Serializable
data class Promo(
    val id: Long? = null,
    val title: String,
    val description: String,
    val discount: Int,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val terms: String? = null
)

class PromoViewModel : ViewModel() {

    private val _promos = mutableStateListOf<Promo>()
    val promos: List<Promo> get() = _promos

    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading

    fun getPromos() {
        viewModelScope.launch {
            try {
                val data = SupabaseClient.client
                    .from("promos")
                    .select()
                    .decodeList<Promo>()
                _promos.clear()
                _promos.addAll(data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getPromoById(id: String): Promo? {
        val idLong = id.toLongOrNull()
        return _promos.find { it.id == idLong }
    }

    fun uploadImageAndSavePromo(
        context: Context,
        imageUri: Uri,
        title: String,
        description: String,
        discount: Int,
        terms: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val bytes = context.contentResolver.openInputStream(imageUri)?.use {
                    it.readBytes()
                } ?: throw Exception("Gagal membaca gambar")

                val fileName = "promo-${UUID.randomUUID()}.jpg"
                val bucket = SupabaseClient.client.storage.from("promo-images")
                bucket.upload(fileName, bytes)
                val imageUrl = bucket.publicUrl(fileName)

                val promo = Promo(
                    title = title,
                    description = description,
                    discount = discount,
                    imageUrl = imageUrl,
                    terms = terms
                )

                SupabaseClient.client.from("promos").insert(promo)

                getPromos()
                onSuccess()

            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            } finally {
                _isUploading.value = false
            }
        }
    }
}
