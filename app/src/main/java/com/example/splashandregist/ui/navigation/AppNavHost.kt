package com.example.splashandregist.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.splashandregist.ui.common.AuthUiState
import com.example.splashandregist.ui.screens.BookingScreen
import com.example.splashandregist.ui.screens.DestinationScreen
import com.example.splashandregist.ui.screens.EventScreen
import com.example.splashandregist.ui.screens.LoginScreen
import com.example.splashandregist.ui.screens.RegisterScreen
import com.example.splashandregist.ui.screens.HomeScreen
import com.example.splashandregist.ui.screens.HotelScreen
import com.example.splashandregist.ui.screens.PromoScreen
import com.example.splashandregist.ui.screens.TransportScreen
import com.example.splashandregist.viewmodel.AuthViewModel
@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            navController.navigate(NavRoutes.Home.route) {
                popUpTo(0)
            }
        } else {
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(0)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Login.route
    ) {

        composable(NavRoutes.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateRegister = {
                    navController.navigate(NavRoutes.Register.route)
                }
            )
        }

        composable(NavRoutes.Register.route) {
            RegisterScreen(viewModel = authViewModel)
        }

        composable(NavRoutes.Home.route) {
            HomeScreen(
                onLogoutClick = { authViewModel.logout() },
                onBookingClick = { navController.navigate(NavRoutes.Booking.route) },
                onPromoClick = { navController.navigate(NavRoutes.Promo.route) },
                onDestinasiClick = { navController.navigate(NavRoutes.Destinasi.route) },
                onEventClick = { navController.navigate(NavRoutes.Event.route) },
                onHotelClick = { navController.navigate(NavRoutes.Hotel.route) },
                onTransportClick = { navController.navigate(NavRoutes.Transport.route) }
            )
        }

        composable(NavRoutes.Booking.route) { BookingScreen() }
        composable(NavRoutes.Promo.route) { PromoScreen() }
        composable(NavRoutes.Destinasi.route) { DestinationScreen() }
        composable(NavRoutes.Event.route) { EventScreen() }
        composable(NavRoutes.Hotel.route) { HotelScreen() }
        composable(NavRoutes.Transport.route) { TransportScreen() }
    }
}
