package com.example.splashandregist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.splashandregist.ui.navigation.AppNavHost
import com.example.splashandregist.viewmodel.AuthViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = viewModel()

            MaterialTheme {
                Surface {
                    AppNavHost(
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
