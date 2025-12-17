package com.example.splashandregist.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Promo(
    val id: Long? = null,
    val title: String,
    val description: String,
    val discount: Int,

    @SerialName("image_url")
    val imageUrl: String? = null,

    val terms: String? = null,

    @SerialName("user_id")
    val userId: String? = null
)