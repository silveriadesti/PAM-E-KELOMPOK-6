package com.example.splashandregist.data.repository

import android.content.Context
import android.net.Uri
import com.example.splashandregist.data.SupabaseClient
import com.example.splashandregist.data.model.Event
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import java.util.UUID

class EventRepository {

    suspend fun getEvents(): List<Event> {
        return SupabaseClient.client
            .from("events")
            .select()
            .decodeList<Event>()
    }

    suspend fun uploadImageAndCreateEvent(
        context: Context,
        imageUri: Uri,
        userId: String?,
        title: String,
        eventDate: String,
        location: String,
        displayPrice: String,
        description: String,
        priceDetails: String
    ) {
        // A. Baca gambar
        val imageBytes = context.contentResolver
            .openInputStream(imageUri)
            ?.readBytes()
            ?: throw Exception("Gagal membaca file gambar")

        // B. Upload ke Storage
        val fileName = "event-${UUID.randomUUID()}.jpg"
        val bucket = SupabaseClient.client.storage.from("event-images")
        bucket.upload(fileName, imageBytes)
        val publicUrl = bucket.publicUrl(fileName)

        // C. Insert DB
        val event = Event(
            userId = userId,
            title = title,
            eventDate = eventDate,
            location = location,
            displayPrice = displayPrice,
            imageUrl = publicUrl,
            description = description.takeIf { it.isNotBlank() },
            priceDetails = priceDetails.takeIf { it.isNotBlank() }
        )

        SupabaseClient.client.from("events").insert(event)
    }
}
