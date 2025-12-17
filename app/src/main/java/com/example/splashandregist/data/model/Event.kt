package com.example.splashandregist.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: Long? = null,
    @SerialName("user_id")
    val userId: String? = null,
    val title: String,
    @SerialName("event_date")
    val eventDate: String,
    val location: String,
    @SerialName("display_price")
    val displayPrice: String,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val description: String? = null,
    @SerialName("price_details")
    val priceDetails: String? = null
)