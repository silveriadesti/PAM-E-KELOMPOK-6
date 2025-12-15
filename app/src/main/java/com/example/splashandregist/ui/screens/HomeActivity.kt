package com.example.splashandregist.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HomeScreen(
                onDestinasiClick = {
                    startActivity(Intent(this, DestinationActivity::class.java))
                },
                onEventClick = {
                    startActivity(Intent(this, EventActivity::class.java))
                },
                onHotelClick = {
                    startActivity(Intent(this, HotelActivity::class.java))
                },
                onTransportClick = {
                    startActivity(Intent(this, TransportActivity::class.java))
                },
                onPromoClick = {
                    startActivity(Intent(this, PromoActivity::class.java))
                },
                onBookingClick = {
                    startActivity(Intent(this, TransportActivity::class.java))
                },
                onLogoutClick = {        // ✅ LOGOUT
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            )
        }
    }
}

