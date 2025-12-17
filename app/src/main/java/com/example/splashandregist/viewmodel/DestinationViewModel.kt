package com.example.splashandregist.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splashandregist.data.model.Destinations
import com.example.splashandregist.data.repository.DestinationRepository
import kotlinx.coroutines.launch

class DestinationViewModel : ViewModel() {

    private val repository = DestinationRepository()

    var destinations = mutableStateListOf<Destinations>()
        private set

    var isUploading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun getDestinations() {
        viewModelScope.launch {
            try {
                destinations.clear()
                destinations.addAll(repository.getDestinations())
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    fun getDestinationById(id: String?): Destinations? {
        return destinations.find { it.id == id }
    }

    fun uploadImageAndSaveDestination(
        context: Context,
        imageUri: Uri,
        name: String,
        location: String,
        price: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isUploading = true
            try {
                repository.uploadImageAndCreateDestination(
                    context,
                    imageUri,
                    name,
                    location,
                    price,
                    description
                )
                getDestinations()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isUploading = false
            }
        }
    }

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
        viewModelScope.launch {
            isUploading = true
            try {
                repository.updateDestination(
                    context,
                    destinationId,
                    newImageUri,
                    currentImageUrl,
                    name,
                    location,
                    price,
                    description
                )
                getDestinations()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isUploading = false
            }
        }
    }

    fun deleteDestination(destinationId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteDestination(destinationId)
                getDestinations()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }
}
