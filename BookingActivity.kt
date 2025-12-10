package com.example.travelapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.*

// ----------------------------
// DATA MODELS
// ----------------------------
data class Booking(
    val id: String = "",
    val userId: String = "",          // akan diisi dari session auth
    val destinationId: String = "",   // id destinasi yg dipesan
    val destinationTitle: String = "",// judul destinasi (untuk tampil)
    val date: String = "",            // tanggal booking (string sederhana)
    val pax: Int = 1,                 // jumlah orang
    val totalPrice: Double = 0.0,
    val proofImageUrl: String? = null,// URL bukti pembayaran (optional)
    val createdAt: String = ""
)

data class DestinationSimple(
    val id: String,
    val title: String,
    val price: Double
)

// ----------------------------
// MAIN ACTIVITY
// ----------------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                BookingAppEntry()
            }
        }
    }
}

// ----------------------------
// NAV GRAPH & APP STATE
// ----------------------------
@Composable
fun BookingAppEntry() {
    val navController = rememberNavController()

    // Dummy destinations (nanti ambil dari Supabase / modul Destinasi)
    val availableDestinations = remember {
        listOf(
            DestinationSimple("d1","Pantai Kuta", 50000.0),
            DestinationSimple("d2","Candi Borobudur", 75000.0),
            DestinationSimple("d3","Danau Toba", 100000.0)
        )
    }

    // dummy bookings state (mutable)
    val bookings = remember { mutableStateListOf<Booking>() }

    // Simulasi userId dari auth (nanti ganti dengan Supabase auth.currentUser?.id)
    val currentUserId = "user-demo-001"

    NavHost(navController = navController, startDestination = "booking_list") {

        composable("booking_list") {
            BookingListScreen(
                bookings = bookings,
                onAdd = { navController.navigate("booking_add") },
                onClickItem = { id -> navController.navigate("booking_detail/$id") }
            )
        }

        composable("booking_add") {
            AddBookingScreen(
                destinations = availableDestinations,
                onSave = { bookingInput ->
                    // saat save: tambahkan userId, createdAt, id unik
                    val id = "b${System.currentTimeMillis()}"
                    val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                    val bookingToSave = bookingInput.copy(
                        id = id,
                        userId = currentUserId,
                        createdAt = now
                    )
                    bookings.add(0, bookingToSave)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            "booking_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStack ->
            val id = backStack.arguments?.getString("id") ?: ""
            val booking = bookings.find { it.id == id }
            BookingDetailScreen(
                booking = booking,
                onBack = { navController.popBackStack() },
                onDelete = { delId ->
                    bookings.removeIf { it.id == delId }
                    navController.popBackStack()
                }
            )
        }
    }
}

// ----------------------------
// BOOKING LIST SCREEN
// ----------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(
    bookings: List<Booking>,
    onAdd: () -> Unit,
    onClickItem: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Saya") },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Booking")
            }
        }
    ) { padding ->
        if (bookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Belum ada booking. Tekan + untuk buat booking baru.")
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bookings) { b ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClickItem(b.id) }
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        // placeholder image (bisa replace painter nanti)
                        Image(
                            painter = rememberAsyncImagePainter("https://picsum.photos/200?random=${b.destinationId.hashCode()}"),
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(b.destinationTitle, style = MaterialTheme.typography.titleMedium)
                            Text("Tanggal: ${b.date}", fontSize = 12.sp)
                            Text("Orang: ${b.pax}", fontSize = 12.sp)
                        }
                        Text(
                            "Rp ${String.format("%,.0f", b.totalPrice)}",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------
// ADD BOOKING SCREEN
// ----------------------------
@Composable
fun AddBookingScreen(
    destinations: List<DestinationSimple>,
    onSave: (Booking) -> Unit,
    onCancel: () -> Unit
) {
    var selectedDestId by remember { mutableStateOf(destinations.firstOrNull()?.id ?: "") }
    var dateText by remember { mutableStateOf("") }
    var paxText by remember { mutableStateOf("1") }
    var proofUrl by remember { mutableStateOf("") } // sementara: input URL bukti; nanti ganti image picker + upload
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Tambah Booking", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        // destination dropdown simple
        Text("Pilih Destinasi")
        DropdownMenuBox(
            items = destinations,
            selectedId = selectedDestId,
            onSelected = { selectedDestId = it }
        )

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = dateText, onValueChange = { dateText = it }, label = { Text("Tanggal (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = paxText, onValueChange = { if (it.all { ch -> ch.isDigit() }) paxText = it }, label = { Text("Jumlah Orang") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = proofUrl, onValueChange = { proofUrl = it }, label = { Text("URL Bukti Pembayaran (opsional)") }, modifier = Modifier.fillMaxWidth())

        error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                // validasi sederhana
                val pax = paxText.toIntOrNull() ?: 0
                val dest = destinations.find { it.id == selectedDestId }
                if (dest == null) {
                    error = "Pilih destinasi"
                    return@Button
                }
                if (dateText.isBlank()) {
                    error = "Isi tanggal booking"
                    return@Button
                }
                if (pax <= 0) {
                    error = "Jumlah orang minimal 1"
                    return@Button
                }
                error = null
                val total = dest.price * pax
                // prepare booking object (id, userId will ditambahkan di caller)
                val b = Booking(
                    destinationId = dest.id,
                    destinationTitle = dest.title,
                    date = dateText,
                    pax = pax,
                    totalPrice = total,
                    proofImageUrl = proofUrl.ifBlank { null }
                )
                onSave(b)
            }) {
                Text("Simpan")
            }

            OutlinedButton(onClick = onCancel) {
                Text("Batal")
            }
        }
    }
}

@Composable
fun <T> DropdownMenuBox(items: List<T>, selectedId: String, onSelected: (String) -> Unit) where T : DestinationSimple {
    var expanded by remember { mutableStateOf(false) }
    val selected = items.find { it.id == selectedId } ?: items.firstOrNull()
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected?.title ?: "Pilih destinasi")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(text = { Text("${item.title} - Rp ${String.format("%,.0f", item.price)}") }, onClick = {
                    onSelected(item.id)
                    expanded = false
                })
            }
        }
    }
}

// ----------------------------
// BOOKING DETAIL SCREEN
// ----------------------------
@Composable
fun BookingDetailScreen(
    booking: Booking?,
    onBack: () -> Unit,
    onDelete: (String) -> Unit
) {
    if (booking == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Booking tidak ditemukan")
        }
        return
    }

    var showConfirm by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Detail Booking") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            }
        )
    }) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {

            Image(
                painter = rememberAsyncImagePainter("https://picsum.photos/400?random=${booking.destinationId.hashCode()}"),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(12.dp))
            Text(booking.destinationTitle, style = MaterialTheme.typography.titleLarge)
            Text("Tanggal: ${booking.date}")
            Text("Jumlah orang: ${booking.pax}")
            Text("Total: Rp ${String.format("%,.0f", booking.totalPrice)}")
            Spacer(Modifier.height(12.dp))
            Text("Bukti Pembayaran:")
            booking.proofImageUrl?.let { url ->
                // Tampilkan gambar bukti (jika ada)
                Image(
                    painter = rememberAsyncImagePainter(url),
                    contentDescription = "Bukti Pembayaran",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            } ?: Text("Belum ada bukti pembayaran")

            Spacer(Modifier.height(16.dp))
            Button(onClick = { showConfirm = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Hapus Booking")
            }
        }

        if (showConfirm) {
            AlertDialog(onDismissRequest = { showConfirm = false }, title = { Text("Konfirmasi") }, text = { Text("Yakin ingin menghapus booking ini?") }, confirmButton = {
                TextButton(onClick = {
                    onDelete(booking.id)
                    showConfirm = false
                }) { Text("Hapus") }
            }, dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Batal") }
            })
        }
    }
}
