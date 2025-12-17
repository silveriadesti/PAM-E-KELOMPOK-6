package com.example.splashandregist.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Hotel(
    val id: Long? = null,
    @SerialName("name") val name: String,
    @SerialName("location") val location: String,
    @SerialName("price") val price: String,
    @SerialName("description") val description: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("user_id") val userId: String? = null
)