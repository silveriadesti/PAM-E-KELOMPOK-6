package com.example.splashandregist.data.model// Pastikan nama package ini benar

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: Long? = null,

    @SerialName("customer_name")
    val customerName: String? = "", // Tambah ? dan default value

    @SerialName("customer_contact")
    val customerContact: String? = "",

    @SerialName("hotel_id")
    val hotelId: Long? = 0,

    @SerialName("hotel_name")
    val hotelName: String? = "Unknown Hotel", // Jaga-jaga kalau hotelnya null

    @SerialName("check_in_date")
    val checkInDate: String? = "",

    @SerialName("check_out_date")
    val checkOutDate: String? = "",

    @SerialName("total_price")
    val totalPrice: String? = "",

    @SerialName("status")
    val status: String = "Pending",

    @SerialName("user_id")
    val userId: String? = null
)