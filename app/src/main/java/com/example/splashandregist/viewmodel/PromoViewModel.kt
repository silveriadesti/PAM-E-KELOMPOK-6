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
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

/* ================= MODEL ================= */

@Serializable
data class Promo(
    val id: Long? = null,
    val title: String,
    val description: String,
    val discount: Int,

    @SerialName("image_url")
    val imageUrl: String? = null,

    val terms: String? = null,

    @SerialName("user_id")
    val userId: String? = null
)

/* ================= VIEWMODEL ================= */

class PromoViewModel : ViewModel() {

    /* ================= STATE ================= */

    private val _promos = mutableStateListOf<Promo>()
    val promos: List<Promo> get() = _promos

    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading

    /* ================= LOCAL DUMMY (STATIC) ================= */

    private val dummyPromos = listOf(
        Promo(
            id = -1,
            title = "Diskon Tahun Baru",
            description = "Promo spesial tahun baru",
            discount = 20,
            imageUrl = "https://thumbs.dreamstime.com/b/new-year-sale-promo-banner-template-winter-holiday-special-offer-text-new-year-sale-promo-banner-template-winter-holiday-special-340115150.jpg",
            terms = "Berlaku sampai 31 Desember"
        ),
        Promo(
            id = -2,
            title = "Promo Akhir Pekan",
            description = "Diskon khusus weekend",
            discount = 15,
            imageUrl = "https://www.shutterstock.com/shutterstock/videos/1111436425/thumb/5.jpg?ip=x480",
            terms = "Sabtu & Minggu"
        )
    )

    /* ================= GET PROMOS ================= */

    fun getPromos() {
        viewModelScope.launch {
            try {
                val user = SupabaseClient.client.auth.currentUserOrNull()

                // ❌ Kalau belum login, hanya tampilkan dummy
                if (user == null) {
                    _promos.clear()
                    _promos.addAll(dummyPromos)
                    return@launch
                }

                // ✅ Ambil data dari Supabase (RLS akan filter otomatis)
                val serverPromos = SupabaseClient.client
                    .from("promos")
                    .select()
                    .decodeList<Promo>()

                _promos.clear()
                _promos.addAll(dummyPromos)   // dummy global
                _promos.addAll(serverPromos)  // hasil RLS (user sendiri)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /* ================= GET BY ID ================= */

    fun getPromoById(id: String): Promo? {
        val idLong = id.toLongOrNull()
        return _promos.find { it.id == idLong }
    }

    /* ================= INSERT PROMO ================= */

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
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: throw Exception("User belum login")

                // Baca image
                val bytes = context.contentResolver
                    .openInputStream(imageUri)
                    ?.use { it.readBytes() }
                    ?: throw Exception("Gagal membaca gambar")

                // Upload ke storage
                val fileName = "promo-${UUID.randomUUID()}.jpg"
                val bucket = SupabaseClient.client.storage.from("promo-images")
                bucket.upload(fileName, bytes)

                val imageUrl = bucket.publicUrl(fileName)

                // Insert promo (WAJIB user_id)
                val promo = Promo(
                    title = title,
                    description = description,
                    discount = discount,
                    imageUrl = imageUrl,
                    terms = terms,
                    userId = userId
                )

                SupabaseClient.client
                    .from("promos")
                    .insert(promo)

                getPromos()
                onSuccess()

            } catch (e: Exception) {
                Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
            } finally {
                _isUploading.value = false
            }
        }
    }
}
