package com.example.splashandregist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.splashandregist.ui.common.UiResult
import com.example.splashandregist.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateRegister: () -> Unit
) {
    val state by viewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is UiResult.Success && (state as UiResult.Success).data) {
            onLoginSuccess()
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Login", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )

            Button(
                onClick = { viewModel.login(email, password) },
                enabled = state !is UiResult.Loading
            ) {
                Text(if (state is UiResult.Loading) "Loading..." else "Login")
            }

            TextButton(onClick = onNavigateRegister) {
                Text("Belum punya akun? Register")
            }

            if (state is UiResult.Error) {
                Text(
                    text = (state as UiResult.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
