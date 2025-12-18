package com.example.splashandregist.viewmodel


import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splashandregist.data.model.Transport
import com.example.splashandregist.data.repository.TransportRepository
import kotlinx.coroutines.launch


// ViewModel untuk mengelola data Transport
class TransportViewModel : ViewModel() {


    // Membuat instance repository (komunikasi ke Supabase)
    private val repository = TransportRepository()


    // List internal yang bisa diamati oleh Compose
    // Mutable: Kalau isinya berubah, UI otomatis recompose
    private val _list = mutableStateListOf<Transport>()


    // List publik (read-only) untuk UI
    val transports: List<Transport> get() = _list


    // Mengambil data transport dari repository
    fun fetchTransports() {
        // Menjalankan proses async di background
        viewModelScope.launch {
            try {
                _list.clear() // Menghapus data lama sebelum isi baru
                _list.addAll(repository.getTransports())
                //Ambil data dari Supabase lalu masukkan ke list
            } catch (e: Exception) {
                // Tangani error kalau fetch gagal
                e.printStackTrace()
            }
        }
    }


    // Mengambil satu data transport berdasarkan id
    fun getTransportById(id: String): Transport? {
        // Cari transport di list berdasarkan ID
        return _list.find { it.id == id }
    }


    // Upload gambar ke Supabase Storage
    fun uploadImage(
        context: Context, // Context Android
        imageUri: Uri, // Lokasi gambar
        onSuccess: (String) -> Unit, // Callback jika sukses (URL)
        onError: (String) -> Unit // Callback jika gagal
    ) {
        viewModelScope.launch { //  Upload dijalankan di background
            try {
                // Repository upload gambar, dapat URL
                val url = repository.uploadImage(context, imageUri)
                onSuccess(url) // Kirim URL ke UI
            } catch (e: Exception) {
                onError(e.message ?: "Upload gagal")
                // Tangani error upload
            }
        }
    }


    // Menambahkan data transport baru
    fun addTransport(
        t: Transport,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Simpan data ke Supabase
        viewModelScope.launch {
            try {
                repository.insertTransport(t)
                fetchTransports() // Refresh data setelah insert
                onSuccess() //  Beri tahu UI kalau sukses
            } catch (e: Exception) {
                onError(e.message ?: "Insert gagal")
            }
        }
    }


    // Update data transport
    fun updateTransport(
        updated: Transport,
        onSuccess: () -> Unit
    ) {
// Update data di Supabase
        viewModelScope.launch {
            try {
                repository.updateTransport(updated)
                fetchTransports()  // Refresh list
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()// Untuk menangkap error (exception)
            }
        }
    }


    // Menghapus data transport
    fun deleteTransport(id: String, onSuccess: () -> Unit) {
        // Hapus data berdasarkan ID
        viewModelScope.launch {
            try {
                repository.deleteTransport(id)
                fetchTransports() // Refresh list setelah delete
                onSuccess()//  Beri tahu UI kalau sukses
            } catch (e: Exception) {
                // Untuk menangkap error (exception)
                e.printStackTrace()
            }
        }
    }
}

