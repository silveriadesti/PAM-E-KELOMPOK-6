package com.example.splashandregist.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Destinations(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val price: String = "",
    val description: String = "",
    val image_url: String = "",
    val user_id: String? = null  // ← UBAH: Sekarang nullable (boleh null)
)