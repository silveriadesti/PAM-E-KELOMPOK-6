package com.example.splashandregist.data.repository

import android.content.Context
import android.net.Uri
import com.example.splashandregist.data.SupabaseClient
import com.example.splashandregist.data.model.Destinations
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import java.util.UUID

class DestinationRepository {

    suspend fun getDestinations(): List<Destinations> {
        return SupabaseClient.client
            .from("destinations")
            .select()
            .decodeList<Destinations>()
    }

    suspend fun uploadImageAndCreateDestination(
        context: Context,
        imageUri: Uri,
        name: String,
        location: String,
        price: String,
        description: String
    ) {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id

        val bytes = context.contentResolver
            .openInputStream(imageUri)
            ?.readBytes()
            ?: throw Exception("Gagal membaca file gambar")

        val fileName = "${UUID.randomUUID()}.jpg"

        SupabaseClient.client.storage
            .from("destination-images")
            .upload(fileName, bytes)

        val imageUrl = SupabaseClient.client.storage
            .from("destination-images")
            .publicUrl(fileName)

        val newDestination = Destinations(
            id = UUID.randomUUID().toString(),
            name = name,
            location = location,
            price = if (price.startsWith("Rp")) price else "Rp $price",
            description = description,
            image_url = imageUrl,
            user_id = userId
        )

        SupabaseClient.client
            .from("destinations")
            .insert(newDestination)
    }

    suspend fun updateDestination(
        context: Context,
        destinationId: String,
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

            SupabaseClient.client.storage
                .from("destination-images")
                .upload(fileName, bytes)

            finalImageUrl = SupabaseClient.client.storage
                .from("destination-images")
                .publicUrl(fileName)
        }

        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id

        val updated = Destinations(
            id = destinationId,
            name = name,
            location = location,
            price = if (price.startsWith("Rp")) price else "Rp $price",
            description = description,
            image_url = finalImageUrl,
            user_id = userId
        )

        SupabaseClient.client
            .from("destinations")
            .update(updated) {
                filter { eq("id", destinationId) }
            }
    }

    suspend fun deleteDestination(destinationId: String) {
        SupabaseClient.client
            .from("destinations")
            .delete {
                filter { eq("id", destinationId) }
            }
    }
}
