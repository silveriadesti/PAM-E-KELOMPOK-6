package com.example.splashandregist.ui.screens // Pastikan sesuai package kamu

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.example.splashandregist.viewmodel.LoginViewModel
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextDecoration
import kotlin.jvm.java


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Kita pakai tema bawaan MaterialTheme biar aman & ga error
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        onNavigateToAdmin = {
                            // Pindah ke HotelActivity (Halaman Admin)
                            startActivity(Intent(this, HotelActivity::class.java))
                            finish() // Agar pas diback gak balik ke login
                        },
                        onNavigateToCustomer = {
                            // NANTI: Pindah ke CustomerActivity
                            Toast.makeText(this, "Halo Customer! Halaman Booking belum dibuat.", Toast.LENGTH_LONG).show()
                            // startActivity(Intent(this, CustomerActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun LoginScreen(
    onNavigateToAdmin: () -> Unit,
    onNavigateToCustomer: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { LoginViewModel() }
    val isLoading by viewModel.isLoading

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Input Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Rounded.AccountCircle, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Login
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    // Panggil fungsi Login di ViewModel
                    viewModel.login(
                        context = context,
                        emailInput = email,
                        passwordInput = password,
                        onLoginSuccess = { role ->
                            // LOGIKA PENENTU HALAMAN 👇
                            if (role == "admin") {
                                Toast.makeText(context, "Welcome Admin!", Toast.LENGTH_SHORT).show()
                                onNavigateToAdmin()
                            } else {
                                Toast.makeText(context, "Welcome Customer!", Toast.LENGTH_SHORT).show()
                                onNavigateToCustomer()
                            }
                        }
                    )
                } else {
                    Toast.makeText(context, "Email dan Password wajib diisi!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "LOGIN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        val annotatedText = buildAnnotatedString {
            append("Belum punya akun? ")

            pushStringAnnotation(
                tag = "REGISTER",
                annotation = "register"
            )
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append("Register")
            }
            pop()
        }

        ClickableText(
            text = annotatedText,
            onClick = { offset ->
                annotatedText.getStringAnnotations(
                    tag = "REGISTER",
                    start = offset,
                    end = offset
                ).firstOrNull()?.let {
                    // Navigasi ke RegisterScreen
                    context.startActivity(
                        Intent(context, RegisterActivity::class.java)
                    )
                }
            }
        )

    }
}