package com.example.splashandregist.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transport(
    val id: String? = null, // 🔥 WAJIB NULLABLE
    val name: String,
    val type: String,
    val route: String,
    val capacity: Int,
    val price: String,
    @SerialName("image_url")
    val imageUrl: String,
    val description: String
)