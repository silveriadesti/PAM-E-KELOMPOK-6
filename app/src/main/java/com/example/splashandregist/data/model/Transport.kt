package com.example.splashandregist.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// data Transport bisa dikonversi ke JSON untuk dikirim ke database dan dikonversi kembali ke object Kotlin saat diambil.
@Serializable
data class Transport(
    val id: String? = null, // ID unik data transport
    val name: String, // Nama transport
    val type: String, // Jenis transport
    val route: String, // Rute perjalanan transport
    val capacity: Int, // Kapasitas penumpang (dalam angka)
    val price: String, // Harga transport
    // Menyambungkan nama field Kotlin (imageUrl) dengan nama kolom di database (image_url)
    @SerialName("image_url")
    val imageUrl: String,
    val description: String  // Deskripsi lengkap tentang transport
)

