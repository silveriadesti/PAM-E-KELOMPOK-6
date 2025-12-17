package com.example.splashandregist.data.repository

import android.content.Context
import android.net.Uri
import com.example.splashandregist.data.SupabaseClient
import com.example.splashandregist.data.model.Transport
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import java.util.UUID

class TransportRepository {

    suspend fun uploadImage(
        context: Context,
        imageUri: Uri
    ): String {
        val fileName = "transport_${UUID.randomUUID()}.jpg"

        val bytes = context.contentResolver
            .openInputStream(imageUri)
            ?.readBytes()
            ?: throw Exception("File tidak ditemukan")

        val bucket = SupabaseClient.client.storage.from("transport-images")
        bucket.upload(fileName, bytes)

        return bucket.publicUrl(fileName)
    }

    suspend fun getTransports(): List<Transport> {
        return SupabaseClient.client
            .from("transports")
            .select()
            .decodeList<Transport>()
    }

    suspend fun insertTransport(t: Transport) {
        SupabaseClient.client.from("transports").insert(t)
    }

    suspend fun updateTransport(t: Transport) {
        SupabaseClient.client
            .from("transports")
            .update({
                set("name", t.name)
                set("type", t.type)
                set("route", t.route)
                set("capacity", t.capacity)
                set("price", t.price)
                set("image_url", t.imageUrl)
                set("description", t.description)
            }) {
                filter { eq("id", t.id!!) }
            }
    }

    suspend fun deleteTransport(id: String) {
        SupabaseClient.client
            .from("transports")
            .delete {
                filter { eq("id", id) }
            }
    }
}
