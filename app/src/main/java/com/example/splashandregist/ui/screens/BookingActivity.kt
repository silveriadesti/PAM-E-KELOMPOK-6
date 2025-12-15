package com.example.splashandregist

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check   // ✅ INI YANG TADI HILANG
import androidx.compose.material.icons.filled.Delete  // ✅ INI YANG TADI HILANG
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.splashandregist.data.model.Booking
import com.example.splashandregist.viewmodel.BookingViewModel

class BookingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                BookingApp()
            }
        }
    }
}

// 1. NAVIGASI UTAMA
@Composable
fun BookingApp() {
    val navController = rememberNavController()
    val viewModel = remember { BookingViewModel() }

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            BookingListScreen(
                viewModel = viewModel,
                onNavigateToAdd = { navController.navigate("add") }
            )
        }
        composable("add") {
            AddBookingScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// 2. LAYAR DAFTAR (LIST)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(viewModel: BookingViewModel, onNavigateToAdd: () -> Unit) {
    LaunchedEffect(Unit) { viewModel.fetchBookings() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Pesanan") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White),
                actions = {
                    IconButton(onClick = { viewModel.fetchBookings() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF5F5F5))
        ) {
            items(viewModel.bookings) { booking ->
                // ✅ PERBAIKAN DI SINI (Memasukkan 3 Parameter)
                BookingItemCard(
                    booking = booking,
                    // Parameter 2: Aksi saat tombol Hapus diklik
                    onDelete = { id -> viewModel.deleteBooking(id) },
                    // Parameter 3: Aksi saat tombol Lunas diklik
                    onConfirm = { id -> viewModel.confirmBooking(id) }
                )
            }
        }
    }
}

// 3. LAYAR TAMBAH (FORM CREATE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookingScreen(viewModel: BookingViewModel, onBack: () -> Unit) {
    var customerName by remember { mutableStateOf("") }
    var customerContact by remember { mutableStateOf("") }
    var hotelName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var checkIn by remember { mutableStateOf("2024-01-01") }
    var checkOut by remember { mutableStateOf("2024-01-03") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Input Pesanan Manual") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Nama Tamu") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = customerContact, onValueChange = { customerContact = it }, label = { Text("No HP / Kontak") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = hotelName, onValueChange = { hotelName = it }, label = { Text("Nama Hotel") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = checkIn, onValueChange = { checkIn = it }, label = { Text("Tanggal Check In") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = checkOut, onValueChange = { checkOut = it }, label = { Text("Tanggal Check Out") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Total Harga (Rp)") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    if (customerName.isNotEmpty() && hotelName.isNotEmpty()) {
                        val newBooking = Booking(
                            customerName = customerName,
                            customerContact = customerContact,
                            hotelName = hotelName,
                            hotelId = 1,
                            checkInDate = checkIn,
                            checkOutDate = checkOut,
                            totalPrice = price,
                            status = "Confirmed"
                        )
                        viewModel.addBooking(newBooking)
                        Toast.makeText(context, "Pesanan Disimpan!", Toast.LENGTH_SHORT).show()
                        onBack()
                    } else {
                        Toast.makeText(context, "Lengkapi data dulu!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Simpan Pesanan")
            }
        }
    }
}

// 4. KOMPONEN KARTU (ITEM CARD)
@Composable
fun BookingItemCard(
    booking: Booking,
    onDelete: (Long) -> Unit,
    onConfirm: (Long) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.hotelName ?: "Nama Hotel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = if (booking.status == "Confirmed") Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = booking.status ?: "Pending", // <--- Added Elvis operator
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (booking.status == "Confirmed") Color(0xFF2E7D32) else Color(0xFFEF6C00)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Detail
            Text(text = "Tamu: ${booking.customerName}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Kontak: ${booking.customerContact}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(text = "${booking.checkInDate} - ${booking.checkOutDate}", style = MaterialTheme.typography.bodySmall)
            booking.totalPrice?.let { Text(text = it, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Tombol Aksi
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                // Delete
                OutlinedButton(
                    onClick = { booking.id?.let { onDelete(it) } },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    modifier = Modifier.height(35.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Hapus", fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Confirm (Cuma muncul kalau Pending)
                if (booking.status == "Pending") {
                    Button(
                        onClick = { booking.id?.let { onConfirm(it) } },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.height(35.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Lunas", fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        }
    }
}