package com.example.splashandregist.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splashandregist.data.model.Hotel
import com.example.splashandregist.data.repository.HotelRepository
import kotlinx.coroutines.launch

class HotelViewModel : ViewModel() {

    private val repository = HotelRepository()

    private val _hotels = mutableStateListOf<Hotel>()
    val hotels: List<Hotel> get() = _hotels

    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading

    fun getHotels() {
        viewModelScope.launch {
            try {
                _hotels.clear()
                _hotels.addAll(repository.getHotels())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getHotelById(id: String): Hotel? {
        val idLong = id.toLongOrNull()
        return _hotels.find { it.id == idLong }
    }

    fun uploadImageAndSaveHotel(
        context: Context,
        imageUri: Uri,
        name: String,
        location: String,
        price: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                repository.createHotel(
                    context,
                    imageUri,
                    name,
                    location,
                    price,
                    description
                )
                getHotels()
                onSuccess()
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun updateHotel(
        context: Context,
        hotelId: Long,
        imageUri: Uri?,
        currentImageUrl: String,
        name: String,
        location: String,
        price: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                repository.updateHotel(
                    context,
                    hotelId,
                    imageUri,
                    currentImageUrl,
                    name,
                    location,
                    price,
                    description
                )
                getHotels()
                onSuccess()
            } finally {
                _isUploading.value = false
            }
        }
    }
}
