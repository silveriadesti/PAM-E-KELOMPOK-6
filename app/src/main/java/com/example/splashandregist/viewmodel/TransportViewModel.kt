package com.example.splashandregist.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splashandregist.data.model.Transport
import com.example.splashandregist.data.repository.TransportRepository
import kotlinx.coroutines.launch

class TransportViewModel : ViewModel() {

    private val repository = TransportRepository()

    private val _list = mutableStateListOf<Transport>()
    val transports: List<Transport> get() = _list

    fun fetchTransports() {
        viewModelScope.launch {
            try {
                _list.clear()
                _list.addAll(repository.getTransports())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getTransportById(id: String): Transport? {
        return _list.find { it.id == id }
    }

    fun uploadImage(
        context: Context,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val url = repository.uploadImage(context, imageUri)
                onSuccess(url)
            } catch (e: Exception) {
                onError(e.message ?: "Upload gagal")
            }
        }
    }

    fun addTransport(
        t: Transport,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.insertTransport(t)
                fetchTransports()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Insert gagal")
            }
        }
    }

    fun updateTransport(
        updated: Transport,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updateTransport(updated)
                fetchTransports()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteTransport(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteTransport(id)
                fetchTransports()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
