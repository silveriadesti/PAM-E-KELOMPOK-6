package com.example.splashandregist.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.splashandregist.data.model.Booking
import com.example.splashandregist.viewmodel.BookingViewModel

/* ================= ROOT ================= */
@Composable
fun BookingScreen() {
    val navController = rememberNavController()
    val viewModel: BookingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    LaunchedEffect(Unit) {
        viewModel.fetchBookings()
    }

    NavHost(navController, startDestination = "list") {
        composable("list") {
            BookingListScreen(navController, viewModel)
        }
        composable("add") {
            AddBookingScreen(viewModel) {
                navController.popBackStack()
            }
        }
        composable("detail/{id}") { backStack ->
            val id = backStack.arguments?.getString("id")?.toLongOrNull()
            val booking = viewModel.bookings.find { it.id == id }
            booking?.let {
                BookingDetailScreen(navController, viewModel, it)
            }
        }
    }
}

/* ================= LIST ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(
    navController: NavController,
    viewModel: BookingViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Booking", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add") }
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.bookings) { booking ->
                BookingItemCard(booking) {
                    navController.navigate("detail/${booking.id}")
                }
            }
        }
    }
}

@Composable
fun BookingItemCard(booking: Booking, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(booking.hotelName ?: "-", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(booking.customerName ?: "-")
            Text(
                booking.status ?: "Pending",
                fontSize = 12.sp,
                color = if (booking.status == "Confirmed")
                    Color(0xFF2E7D32) else Color(0xFFEF6C00)
            )
        }
    }
}

/* ================= DETAIL ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    navController: NavController,
    viewModel: BookingViewModel,
    booking: Booking
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Booking") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(booking.hotelName ?: "-", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            DetailRow("Nama", booking.customerName ?: "-")
            DetailRow("Kontak", booking.customerContact ?: "-")
            DetailRow("Total", booking.totalPrice ?: "-")

            Spacer(Modifier.height(16.dp))

            booking.proofImageUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

/* ================= ADD ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookingScreen(
    viewModel: BookingViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.fetchHotelOptions()
    }

    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var hotelId by remember { mutableStateOf<Long?>(null) }
    var hotelName by remember { mutableStateOf("Pilih Hotel") }
    var expanded by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { imageUri = it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Booking") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
                OutlinedTextField(
                    hotelName,
                    {},
                    readOnly = true,
                    label = { Text("Hotel") },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded, { expanded = false }) {
                    viewModel.hotelOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(it.name) },
                            onClick = {
                                hotelName = it.name
                                hotelId = it.id
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(name, { name = it }, label = { Text("Nama") })
            OutlinedTextField(contact, { contact = it }, label = { Text("Kontak") })
            OutlinedTextField(price, { price = it }, label = { Text("Harga") })

            Spacer(Modifier.height(12.dp))

            Button(onClick = {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Text("Pilih Bukti Pembayaran")
            }

            imageUri?.let {
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (name.isBlank() || hotelId == null) {
                        Toast.makeText(context, "Lengkapi data", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val booking = Booking(
                        customerName = name,
                        customerContact = contact,
                        hotelName = hotelName,
                        hotelId = hotelId,
                        totalPrice = price,
                        status = "Pending"
                    )

                    viewModel.addBookingWithImage(context, booking, imageUri) {
                        Toast.makeText(context, "Booking disimpan", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                }
            ) {
                Text("Simpan")
            }
        }
    }
}

/* ================= HELPER ================= */
@Composable
fun DetailRow(label: String, value: String) {
    Row {
        Text(label, Modifier.width(100.dp), color = Color.Gray)
        Text(": $value")
    }
}
