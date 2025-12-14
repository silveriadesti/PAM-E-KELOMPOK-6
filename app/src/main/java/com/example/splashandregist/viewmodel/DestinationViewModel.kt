package com.example.splashandregist.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.splashandregist.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Destinations(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val price: String = "",
    val description: String = "",
    val image_url: String = "",
    val user_id: String = ""
)

class DestinationViewModel {
    var destinations = mutableStateListOf<Destinations>()
        private set

    var isUploading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Get all destinations
    fun getDestinations() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = SupabaseClient.client
                    .from("destinations")
                    .select()
                    .decodeList<Destinations>()

                withContext(Dispatchers.Main) {
                    destinations.clear()
                    destinations.addAll(result)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = e.message
                }
            }
        }
    }

    // Get destination by ID
    fun getDestinationById(id: String): Destinations? {
        return destinations.find { it.id == id }
    }

    // Upload image and save destination
    fun uploadImageAndSaveDestination(
        context: Context,
        imageUri: Uri,
        name: String,
        location: String,
        price: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        isUploading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: ""

                // Upload image
                val fileName = "${UUID.randomUUID()}.jpg"
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    SupabaseClient.client.storage
                        .from("destination-images")
                        .upload(fileName, bytes)

                    val imageUrl = SupabaseClient.client.storage
                        .from("destination-images")
                        .publicUrl(fileName)

                    // Save to database
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

                    withContext(Dispatchers.Main) {
                        getDestinations()
                        isUploading = false
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = e.message
                    isUploading = false
                }
            }
        }
    }

    // Update destination
    fun updateDestination(
        context: Context,
        destinationId: String,
        newImageUri: Uri?,
        currentImageUrl: String,
        name: String,
        location: String,
        price: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        isUploading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var finalImageUrl = currentImageUrl

                // Upload new image if selected
                if (newImageUri != null) {
                    val fileName = "${UUID.randomUUID()}.jpg"
                    val inputStream = context.contentResolver.openInputStream(newImageUri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        SupabaseClient.client.storage
                            .from("destination-images")
                            .upload(fileName, bytes)

                        finalImageUrl = SupabaseClient.client.storage
                            .from("destination-images")
                            .publicUrl(fileName)
                    }
                }

                // Update database
                val updatedDestination = Destinations(
                    id = destinationId,
                    name = name,
                    location = location,
                    price = if (price.startsWith("Rp")) price else "Rp $price",
                    description = description,
                    image_url = finalImageUrl,
                    user_id = SupabaseClient.client.auth.currentUserOrNull()?.id ?: ""
                )

                SupabaseClient.client
                    .from("destinations")
                    .update(updatedDestination) {
                        filter {
                            eq("id", destinationId)
                        }
                    }

                withContext(Dispatchers.Main) {
                    getDestinations()
                    isUploading = false
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = e.message
                    isUploading = false
                }
            }
        }
    }

    // Delete destination
    fun deleteDestination(destinationId: String, onSuccess: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SupabaseClient.client
                    .from("destinations")
                    .delete {
                        filter {
                            eq("id", destinationId)
                        }
                    }

                withContext(Dispatchers.Main) {
                    getDestinations()
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = e.message
                }
            }
        }
    }
}