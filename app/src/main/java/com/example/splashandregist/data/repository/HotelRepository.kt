package com.example.splashandregist.data.repository

import android.content.Context
import android.net.Uri
import com.example.splashandregist.data.SupabaseClient
import com.example.splashandregist.data.model.Hotel
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import java.util.UUID

class HotelRepository {

    suspend fun getHotels(): List<Hotel> {
        return SupabaseClient.client
            .from("hotels")
            .select()
            .decodeList<Hotel>()
    }

    suspend fun createHotel(
        context: Context,
        imageUri: Uri,
        name: String,
        location: String,
        price: String,
        description: String
    ) {
        val bytes = context.contentResolver
            .openInputStream(imageUri)
            ?.readBytes()
            ?: throw Exception("Gagal membaca file gambar")

        val fileName = "${UUID.randomUUID()}.jpg"
        val bucket = SupabaseClient.client.storage.from("hotel-images")

        bucket.upload(fileName, bytes)
        val imageUrl = bucket.publicUrl(fileName)

        val hotel = Hotel(
            name = name,
            location = location,
            price = if (price.startsWith("Rp")) price else "Rp $price",
            description = description,
            imageUrl = imageUrl
        )

        SupabaseClient.client.from("hotels").insert(hotel)
    }

    suspend fun updateHotel(
        context: Context,
        hotelId: Long,
        newImageUri: Uri?,
        currentImageUrl: String,
        name: String,
        location: String,
        price: String,
        description: String
    ) {
        var finalImageUrl = currentImageUrl

        if (newImageUri != null) {
            val bytes = context.contentResolver
                .openInputStream(newImageUri)
                ?.readBytes()
                ?: throw Exception("Gagal membaca file gambar")

            val fileName = "${UUID.randomUUID()}.jpg"
            val bucket = SupabaseClient.client.storage.from("hotel-images")

            bucket.upload(fileName, bytes)
            finalImageUrl = bucket.publicUrl(fileName)
        }

        SupabaseClient.client.from("hotels").update(
            {
                set("name", name)
                set("location", location)
                set("price", price)
                set("description", description)
                set("image_url", finalImageUrl)
            }
        ) {
            filter { eq("id", hotelId) }
        }
    }
}
