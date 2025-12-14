package com.example.splashandregist.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.splashandregist.ui.screens.OnboardingScreen
import com.example.splashandregist.ui.screens.RegisterScreen

// Route constants
object Routes {
    const val ONBOARDING = "onboarding"
    const val REGISTER = "register"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.ONBOARDING  // Langsung ke Onboarding
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER) {
                        // Remove onboarding from back stack
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen()
        }
    }
}