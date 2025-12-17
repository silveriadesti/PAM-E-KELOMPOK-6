package com.example.splashandregist.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splashandregist.data.model.Event
import com.example.splashandregist.data.repository.EventRepository
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {

    private val repository = EventRepository()

    private val _events = mutableStateListOf<Event>()
    val events: List<Event> get() = _events

    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading

    fun getEvents() {
        viewModelScope.launch {
            try {
                _events.clear()
                _events.addAll(repository.getEvents())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getEventById(id: String): Event? {
        val idLong = id.toLongOrNull()
        return _events.find { it.id == idLong }
    }

    fun uploadImageAndSaveEvent(
        context: Context,
        imageUri: Uri,
        userId: String?,
        title: String,
        eventDate: String,
        location: String,
        displayPrice: String,
        description: String,
        priceDetails: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                repository.uploadImageAndCreateEvent(
                    context,
                    imageUri,
                    userId,
                    title,
                    eventDate,
                    location,
                    displayPrice,
                    description,
                    priceDetails
                )
                getEvents()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUploading.value = false
            }
        }
    }
}
