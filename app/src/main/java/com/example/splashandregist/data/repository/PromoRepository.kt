package com.example.splashandregist.data.repository

import android.content.Context
import android.net.Uri
import com.example.splashandregist.data.SupabaseClient
import com.example.splashandregist.data.model.Promo
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import java.util.UUID

class PromoRepository {

    // Dummy global (tidak tergantung user)
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

    suspend fun getPromos(): List<Promo> {
        val user = SupabaseClient.client.auth.currentUserOrNull()

        // Kalau belum login → dummy saja
        if (user == null) {
            return dummyPromos
        }

        val serverPromos = SupabaseClient.client
            .from("promos")
            .select()
            .decodeList<Promo>()

        return dummyPromos + serverPromos
    }

    suspend fun createPromo(
        context: Context,
        imageUri: Uri,
        title: String,
        description: String,
        discount: Int,
        terms: String?
    ) {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: throw Exception("User belum login")

        val bytes = context.contentResolver
            .openInputStream(imageUri)
            ?.readBytes()
            ?: throw Exception("Gagal membaca gambar")

        val fileName = "promo-${UUID.randomUUID()}.jpg"
        val bucket = SupabaseClient.client.storage.from("promo-images")

        bucket.upload(fileName, bytes)
        val imageUrl = bucket.publicUrl(fileName)

        val promo = Promo(
            title = title,
            description = description,
            discount = discount,
            imageUrl = imageUrl,
            terms = terms,
            userId = userId
        )

        SupabaseClient.client.from("promos").insert(promo)
    }
}
