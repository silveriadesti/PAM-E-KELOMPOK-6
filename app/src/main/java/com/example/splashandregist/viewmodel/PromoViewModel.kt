package com.example.splashandregist.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splashandregist.data.model.Promo
import com.example.splashandregist.data.repository.PromoRepository
import kotlinx.coroutines.launch

class PromoViewModel : ViewModel() {

    private val repository = PromoRepository()

    private val _promos = mutableStateListOf<Promo>()
    val promos: List<Promo> get() = _promos

    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading

    fun getPromos() {
        viewModelScope.launch {
            try {
                _promos.clear()
                _promos.addAll(repository.getPromos())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getPromoById(id: String): Promo? {
        val idLong = id.toLongOrNull()
        return _promos.find { it.id == idLong }
    }

    fun uploadImageAndSavePromo(
        context: Context,
        imageUri: Uri,
        title: String,
        description: String,
        discount: Int,
        terms: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                repository.createPromo(
                    context,
                    imageUri,
                    title,
                    description,
                    discount,
                    terms
                )
                getPromos()
                onSuccess()
            } finally {
                _isUploading.value = false
            }
        }
    }
}
