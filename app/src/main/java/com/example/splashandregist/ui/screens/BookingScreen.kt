package com.example.splashandregist.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
// Pastikan import ini ada untuk viewModel()
import androidx.lifecycle.viewmodel.compose.viewModel

// --- 1. ROOT COMPOSABLE (PENGGANTI ACTIVITY) ---
// Ini yang nanti dipanggil di MainActivity atau NavHost utama kamu
@Composable
fun BookingScreen() {
    // Kita pakai viewModel() supaya data tidak hilang saat layar diputar
    val viewModel: BookingViewModel = viewModel()
    val navController = rememberNavController()

    // NavHost INTERNAL untuk fitur Booking (List -> Add -> Detail)
    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            BookingListScreen(viewModel, navController)
        }
        composable("add") {
            AddBookingScreen(viewModel) { navController.popBackStack() }
        }
        composable("detail/{bookingId}") { backStackEntry ->
            val bookingIdStr = backStackEntry.arguments?.getString("bookingId")
            val bookingId = bookingIdStr?.toLongOrNull()
            if (bookingId != null) {
                // Cari data booking dari list yang sudah ada di ViewModel
                val booking = viewModel.bookings.find { it.id == bookingId }
                if (booking != null) {
                    BookingDetailScreen(navController, viewModel, booking)
                }
            }
        }
    }
}

// --- 2. LAYAR DAFTAR (LIST) ---
// (Logika SAMA PERSIS dengan BookingActivity kamu)
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
                    onClick = { navController.navigate("detail/${booking.id}") }
                )
            }
        }
    }
}

// --- 3. KARTU PESANAN ---
@Composable
fun BookingItemCard(booking: Booking, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(Color.White),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
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
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(booking.customerName ?: "-")
            Text("Tap untuk lihat detail & aksi", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

// --- 4. LAYAR DETAIL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(navController: NavController, viewModel: BookingViewModel, booking: Booking) {
    val context = LocalContext.current
    // State untuk upload di detail (kasus Pending -> Lunas)
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(), onResult = { uri -> selectedImageUri = uri })


    Scaffold(
        topBar = { TopAppBar(title = { Text("Detail & Aksi") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {

            Text(text = booking.hotelName ?: "-", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            DetailRow("Nama Pemesan", booking.customerName ?: "-")
            DetailRow("Kontak", booking.customerContact ?: "-")
            DetailRow("Tanggal Check-In", booking.checkInDate ?: "-", valueColor = Color.Blue, isBold = true)
            DetailRow("Tanggal Check-Out", booking.checkOutDate ?: "-")

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().background(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)).padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Total Harga", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(text = booking.totalPrice ?: "-", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            // TAMPILKAN GAMBAR (JIKA ADA)
            if (booking.proofImageUrl != null) {
                Text("Bukti / Gambar:", fontWeight = FontWeight.Bold)
                AsyncImage(model = booking.proofImageUrl, null, Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // LOGIKA AKSI (HAPUS & LUNAS)
            if (booking.status == "Pending") {
                Text("Verifikasi Pembayaran", fontWeight = FontWeight.Bold)
                Text("Upload bukti untuk melunasi.", color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))

                // Upload Area di Detail
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp).background(Color(0xFFEEEEEE)).clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) AsyncImage(model = selectedImageUri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Upload, null); Text("Pilih Bukti") }
                }
                Spacer(Modifier.height(16.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // Tombol Hapus
                OutlinedButton(
                    onClick = {
                        booking.id?.let { viewModel.deleteBooking(it) }
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.Delete, null); Spacer(Modifier.width(8.dp)); Text("Hapus")
                }

                // Tombol Lunas
                if (booking.status == "Pending") {
                    Button(
                        // Hanya bisa diklik kalau sudah pilih gambar (atau logic kamu mau confirmOnly juga boleh, tapi di sini pakai upload)
                        enabled = selectedImageUri != null && !viewModel.isUploading,
                        onClick = {
                            booking.id?.let { id ->
                                selectedImageUri?.let { uri ->
                                    viewModel.confirmPayment(context, id, uri) { navController.popBackStack() }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        if(viewModel.isUploading) CircularProgressIndicator(color=Color.White, modifier=Modifier.size(20.dp))
                        else { Icon(Icons.Default.Check, null); Spacer(Modifier.width(8.dp)); Text("Lunas") }
                    }
                } else {
                    Button(onClick = {}, enabled = false, colors = ButtonDefaults.buttonColors(disabledContainerColor = Color.LightGray)) { Text("Sudah Lunas") }
                }
            }
        }
    }
}

// --- 5. LAYAR TAMBAH (INPUT MANUAL) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookingScreen(viewModel: BookingViewModel, onBack: () -> Unit) {
    // Logic untuk dropdown hotel
    LaunchedEffect(Unit) { viewModel.fetchHotelOptions() }

    val context = LocalContext.current

    // VARIABEL STATE (Ini yang kemarin mungkin error di kode temanmu, di sini kita pakai cara "Perfect Code" kamu)
    var customerName by remember { mutableStateOf("") }
    var customerContact by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    // Tanggal Default
    var checkIn by remember { mutableStateOf("2024-01-01") }
    var checkOut by remember { mutableStateOf("2024-01-03") }

    // Dropdown Logic
    var expanded by remember { mutableStateOf(false) }
    var selectedHotelName by remember { mutableStateOf("Pilih Hotel") }
    var selectedHotelId by remember { mutableStateOf<Long?>(null) }

    // Upload Logic (Opsional di awal)
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { selectedImageUri = it }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Input Pesanan") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Dropdown Hotel
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

            // INI BAGIAN TANGGAL YANG KEMARIN ERROR DI KODE TEMANMU
            // Di sini kita pastikan Input Field mengupdate variabel `checkIn` dan `checkOut`
            OutlinedTextField(value = checkIn, onValueChange = { checkIn = it }, label = { Text("Check In (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = checkOut, onValueChange = { checkOut = it }, label = { Text("Check Out (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga (Rp)") }, modifier = Modifier.fillMaxWidth())

            // Upload Gambar (Opsional)
            Text("Bukti Transfer (Opsional)", fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp).background(Color(0xFFEEEEEE)).clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) AsyncImage(model = selectedImageUri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Image, null); Text("Klik Pilih Gambar") }
            }

            Button(
                onClick = {
                    if (customerName.isNotEmpty() && selectedHotelId != null && checkIn.isNotEmpty()) {
                        val newBooking = Booking(
                            customerName = customerName,
                            customerContact = customerContact,
                            hotelName = selectedHotelName,
                            hotelId = selectedHotelId,
                            // KITA MASUKKAN VARIABEL STATE TANGGAL DI SINI
                            checkInDate = checkIn,
                            checkOutDate = checkOut,
                            totalPrice = price,
                            status = "Pending"
                        )
                        // Panggil ViewModel
                        viewModel.addBookingWithImage(context, newBooking, selectedImageUri) {
                            Toast.makeText(context, "Disimpan!", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    } else {
                        Toast.makeText(context, "Mohon lengkapi Nama, Hotel, dan Tanggal!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !viewModel.isUploading
            ) {
                if(viewModel.isUploading) CircularProgressIndicator(color=Color.White) else Text("Simpan Pesanan")
            }
        }
    }
}

// Helper Row
@Composable
fun DetailRow(label: String, value: String, valueColor: Color = Color.Black, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Text(text = label, modifier = Modifier.width(160.dp), color = Color.Gray, fontSize = 14.sp)
        Text(text = ": ", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 14.sp, color = valueColor, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
    }
}