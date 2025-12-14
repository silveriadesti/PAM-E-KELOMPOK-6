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

class PromoViewModel : ViewModel() {

    /* ================= MODEL ================= */

    @Serializable
    data class Promo(
        val id: Long? = null,
        val title: String,
        val description: String,
        val discount: Int,
        @SerialName("image_url")
        val imageUrl: String? = null,
        val terms: String? = null // ✅ SYARAT & KETENTUAN
    )

    /* ================= STATE ================= */

    private val _promos = mutableStateListOf<Promo>()
    val promos: List<Promo> get() = _promos

    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading

    /* ================= READ LIST ================= */

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

    /* ================= READ DETAIL ================= */

    fun getPromoDetail(
        promoId: Long,
        onResult: (Promo?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val promo = SupabaseClient.client
                    .from("promos")
                    .select {
                        filter { eq("id", promoId) }
                    }
                    .decodeSingle<Promo>()

                onResult(promo)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    /* ================= CREATE ================= */

    fun savePromo(
        context: Context,
        imageUri: Uri?,
        title: String,
        description: String,
        discount: Int,
        terms: String? = null, // ✅ OPTIONAL
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val imageUrl = uploadImage(context, imageUri)

                val promo = Promo(
                    title = title,
                    description = description,
                    discount = discount,
                    imageUrl = imageUrl,
                    terms = terms
                )

                SupabaseClient.client
                    .from("promos")
                    .insert(promo)

                getPromos()
                onSuccess()

            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            } finally {
                _isUploading.value = false
            }
        }
    }

    /* ================= UPDATE ================= */

    fun updatePromo(
        context: Context,
        promoId: Long,
        title: String,
        description: String,
        discount: Int,
        newImageUri: Uri?,
        oldImageUrl: String?,
        terms: String? = null, // ✅ OPTIONAL
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val finalImageUrl = newImageUri?.let {
                    uploadImage(context, it)
                } ?: oldImageUrl

                SupabaseClient.client
                    .from("promos")
                    .update({
                        set("title", title)
                        set("description", description)
                        set("discount", discount)
                        set("image_url", finalImageUrl)
                        set("terms", terms)
                    }) {
                        filter { eq("id", promoId) }
                    }

                getPromos()
                onSuccess()

            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            } finally {
                _isUploading.value = false
            }
        }
    }

    /* ================= IMAGE UPLOAD ================= */

    private suspend fun uploadImage(
        context: Context,
        imageUri: Uri?
    ): String? {
        if (imageUri == null) return null

        val bytes = context.contentResolver
            .openInputStream(imageUri)
            ?.readBytes()
            ?: throw Exception("Gagal membaca gambar")

        val fileName = "${UUID.randomUUID()}.jpg"
        val bucket = SupabaseClient.client.storage.from("promo-images")

        bucket.upload(fileName, bytes)

        return bucket.publicUrl(fileName)
    }
}
