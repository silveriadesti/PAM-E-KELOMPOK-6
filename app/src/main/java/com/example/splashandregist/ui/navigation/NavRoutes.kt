package com.example.splashandregist.ui.navigation

sealed class NavRoutes(val route: String) {

    object Login : NavRoutes("login")
    object Register : NavRoutes("register")
    object Home : NavRoutes("home")

    object Destinasi : NavRoutes("destinasi")
    object Event : NavRoutes("event")
    object Hotel : NavRoutes("hotel")
    object Transport : NavRoutes("transport")
    object Promo : NavRoutes("promo")
    object Booking : NavRoutes("booking")
}

