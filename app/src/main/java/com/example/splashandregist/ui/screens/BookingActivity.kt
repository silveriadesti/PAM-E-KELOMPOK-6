package com.example.splashandregist

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
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

class BookingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { BookingApp() } }
    }
}

// 1. NAVIGASI
@Composable
fun BookingApp() {
    val navController = rememberNavController()
    val viewModel = remember { BookingViewModel() }

    NavHost(navController = navController, startDestination = "list") {
        composable("list") { BookingListScreen(viewModel, navController) }
        composable("add") { AddBookingScreen(viewModel) { navController.popBackStack() } }
        composable("detail/{bookingId}") { backStackEntry ->
            val bookingIdStr = backStackEntry.arguments?.getString("bookingId")
            val bookingId = bookingIdStr?.toLongOrNull()
            if (bookingId != null) {
                val booking = viewModel.bookings.find { it.id == bookingId }
                if (booking != null) {
                    BookingDetailScreen(navController, viewModel, booking)
                }
            }
        }
    }
}

// 2. LAYAR DAFTAR (LIST)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(viewModel: BookingViewModel, navController: NavController) {
    LaunchedEffect(Unit) { viewModel.fetchBookings() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Pesanan") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White),
                actions = { IconButton(onClick = { viewModel.fetchBookings() }) { Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add") }, containerColor = MaterialTheme.colorScheme.primary) { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF5F5F5)), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(viewModel.bookings) { booking ->
                BookingItemCard(
                    booking = booking,
                    onClick = { navController.navigate("detail/${booking.id}") } // Klik kartu langsung ke detail
                )
            }
        }
    }
}

// 3. KARTU PESANAN (VERSI BERSIH - KLIK KE DETAIL)
@Composable
fun BookingItemCard(booking: Booking, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(Color.White),
        modifier = Modifier.fillMaxWidth().clickable { onClick() } // Kartu bisa diklik
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(booking.hotelName ?: "-", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Surface(color = if (booking.status == "Confirmed") Color(0xFFE8F5E9) else Color(0xFFFFF3E0), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = booking.status ?: "Pending",
                        modifier = Modifier.padding(8.dp, 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (booking.status == "Confirmed") Color(0xFF2E7D32) else Color(0xFFEF6C00)
                    )                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(booking.customerName ?: "-")
            Text("Tap untuk lihat detail & aksi", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

// 4. LAYAR DETAIL (TOMBOL HAPUS & LUNAS)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(navController: NavController, viewModel: BookingViewModel, booking: Booking) {
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Detail & Aksi") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {

            // Info Booking
            // Judul Hotel
            Text(
                text = booking.hotelName ?: "-",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            // --- MENGGUNAKAN HELPER BARU AGAR SEJAJAR ---
            DetailRow("Nama Pemesan", booking.customerName ?: "-")
            DetailRow("Kontak", booking.customerContact ?: "-")

            // Contoh Check-In pakai warna Biru & Bold sesuai request kamu
            DetailRow(
                label = "Tanggal Check-In",
                value = booking.checkInDate ?: "-",
                valueColor = Color.Blue,
                isBold = true
            )

            DetailRow("Tanggal Check-Out", booking.checkOutDate ?: "-")

            Spacer(modifier = Modifier.height(24.dp))

            // --- TOTAL HARGA DENGAN BACKGROUND ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer, // Warna Background (Bisa diganti Color.Yellow, dll)
                        shape = RoundedCornerShape(12.dp) // Sudut membulat
                    )
                    .padding(16.dp), // Padding DI DALAM background (Biar teks ga nempel pinggir)
                horizontalArrangement = Arrangement.SpaceBetween, // Biar Label di Kiri, Harga di Kanan
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Harga",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = booking.totalPrice ?: "-",
                    style = MaterialTheme.typography.headlineSmall, // Ukuran font besar
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary // Warna teks harga
                )
            }


            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            // TAMPILKAN GAMBAR (JIKA ADA)
            if (booking.proofImageUrl != null) {
                Text("Bukti / Gambar:", fontWeight = FontWeight.Bold)
                AsyncImage(model = booking.proofImageUrl, null, Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Text("Belum ada gambar yang diupload.", color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- TOMBOL AKSI (MIRIP YANG DI CARD DULU) ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // Tombol Hapus (Selalu Ada)
                OutlinedButton(
                    onClick = {
                        booking.id?.let { viewModel.deleteBooking(it) }
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.Delete, null); Spacer(Modifier.width(8.dp)); Text("Hapus")
                }

                // Tombol Lunas (Hanya jika Pending)
                if (booking.status == "Pending") {
                    Button(
                        onClick = { booking.id?.let { viewModel.confirmBooking(it) } },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(Icons.Default.Check, null); Spacer(Modifier.width(8.dp)); Text("Lunas")
                    }
                } else {
                    // Penanda Sudah Lunas
                    Button(onClick = {}, enabled = false, colors = ButtonDefaults.buttonColors(disabledContainerColor = Color.LightGray)) {
                        Text("Sudah Lunas")
                    }
                }
            }
        }
    }
}

// Fungsi untuk membuat baris dengan titik dua yang sejajar
@Composable
fun DetailRow(label: String, value: String, valueColor: Color = Color.Black, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // Jarak antar baris
        verticalAlignment = Alignment.Top // Biar kalau teks panjang, tetap rapi dari atas
    ) {
        // 1. Label (Lebar Tetap) -> KUNCI SUPAYA SEJAJAR
        Text(
            text = label,
            modifier = Modifier.width(160.dp),
            color = Color.Gray,
            fontSize = 14.sp
        )

        // 2. Titik Dua
        Text(text = ": ", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        // 3. Isi Value
        Text(
            text = value,
            fontSize = 14.sp,
            color = valueColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f) // Mengisi sisa ruang ke kanan
        )
    }
}

// 5. LAYAR TAMBAH (DROPDOWN HOTEL & UPLOAD GAMBAR)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookingScreen(viewModel: BookingViewModel, onBack: () -> Unit) {
    // Panggil data hotel pas layar dibuka
    LaunchedEffect(Unit) { viewModel.fetchHotelOptions() }

    var customerName by remember { mutableStateOf("") }
    var customerContact by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var checkIn by remember { mutableStateOf("2024-01-01") }
    var checkOut by remember { mutableStateOf("2024-01-03") }

    // Dropdown State
    var expanded by remember { mutableStateOf(false) }
    var selectedHotelName by remember { mutableStateOf("Pilih Hotel") }
    var selectedHotelId by remember { mutableStateOf<Long?>(null) }

    // Image State
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(), onResult = { uri -> selectedImageUri = uri })
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Input Pesanan") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // --- DROPDOWN HOTEL ---
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedHotelName, onValueChange = {}, readOnly = true, label = { Text("Nama Hotel") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    viewModel.hotelOptions.forEach { hotel ->
                        DropdownMenuItem(
                            text = { Text(hotel.name) },
                            onClick = {
                                selectedHotelName = hotel.name
                                selectedHotelId = hotel.id
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Nama Tamu") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = customerContact, onValueChange = { customerContact = it }, label = { Text("Kontak") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga (Rp)") }, modifier = Modifier.fillMaxWidth())

            // --- INPUT GAMBAR ---
            Text("Upload Bukti / Foto (Opsional)", fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp).background(Color(0xFFEEEEEE)).clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) AsyncImage(model = selectedImageUri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Image, null); Text("Klik Pilih Gambar") }
            }

            // --- TOMBOL SIMPAN ---
            Button(
                onClick = {
                    if (customerName.isNotEmpty() && selectedHotelId != null) {
                        val newBooking = Booking(
                            customerName = customerName, customerContact = customerContact,
                            hotelName = selectedHotelName, hotelId = selectedHotelId,
                            checkInDate = checkIn, checkOutDate = checkOut, totalPrice = price, status = "Pending"
                        )
                        // Panggil fungsi Add dengan Gambar
                        viewModel.addBookingWithImage(context, newBooking, selectedImageUri) {
                            Toast.makeText(context, "Disimpan!", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    } else {
                        Toast.makeText(context, "Pilih Hotel & Isi Nama!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !viewModel.isUploading
            ) {
                if (viewModel.isUploading) CircularProgressIndicator(color = Color.White) else Text("Simpan Pesanan")
            }
        }
    }
}