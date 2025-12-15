package com.example.splashandregist // Pastikan nama package ini benar

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable // <-- Stempel Wajib: "Data ini boleh dikirim lewat Internet"
data class Booking(
    // 1. ID Pesanan (Dibuat otomatis oleh Supabase, jadi boleh kosong/null saat kita baru buat)
    val id: Long? = null,

    // 2. Data Tamu
    @SerialName("customer_name") // Ini nama di Supabase (pake garis bawah)
    val customerName: String,    // Ini nama di Kotlin (pake huruf besar di tengah)

    @SerialName("customer_contact")
    val customerContact: String,

    // 3. Info Hotel (Relasi)
    @SerialName("hotel_id")
    val hotelId: Long, // ID Hotel yang dipesan

    @SerialName("hotel_name")
    val hotelName: String, // Nama Hotel (disimpan juga biar gampang ditampilkan)

    // 4. Tanggal & Harga
    @SerialName("check_in_date")
    val checkInDate: String, // Kita pakai String dulu biar mudah (format: "2023-12-25")

    @SerialName("check_out_date")
    val checkOutDate: String,

    @SerialName("total_price")
    val totalPrice: String,

    // 5. Status Booking (Pending, Confirmed, Cancelled)
    @SerialName("status")
    val status: String = "Pending", // Defaultnya "Pending" kalau tidak diisi

    // 6. User ID (Keamanan)
    @SerialName("user_id")
    val userId: String? = null
)