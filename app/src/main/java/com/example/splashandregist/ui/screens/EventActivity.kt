package com.example.splashandregist.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.splashandregist.viewmodel.EventViewModel // Pastikan path ini benar
import com.example.splashandregist.viewmodel.Event // Pastikan path ini benar


// --- ACTIVITY UTAMA HOST  ---
class EventActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                EventApp()
            }
        }
    }
}


// --- NAVIGASI HOST ---
@Composable
fun EventApp() {
    val navController = rememberNavController()
    // Inisialisasi ViewModel di level NavHost
    val viewModel: EventViewModel = viewModel()


    NavHost(navController, startDestination = "event_dashboard") {
        composable("event_dashboard") { EventDashboardScreen(navController, viewModel) }
        composable("add_event") { AddEventScreen(navController, viewModel) }
        composable(
            "event_detail/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            EventDetailScreen(
                navController = navController,
                viewModel = viewModel,
                eventId = backStackEntry.arguments?.getString("eventId")
            )
        }
    }
}




/* ================= 1. EVENT DASHBOARD SCREEN (LIST) ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDashboardScreen(navController: NavController, viewModel: EventViewModel) {
    // Ambil data dari ViewModel
    val events = viewModel.events


    // Ambil data setiap kali screen dimuat
    LaunchedEffect(Unit) { viewModel.getEvents() }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Dashboard", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_event") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Event", tint = Color.White)
            }
        }
    ) { padding ->


        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 16.dp,
                bottom = padding.calculateBottomPadding() + 80.dp,
                start = 16.dp, end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(events) { event ->
                EventCard(
                    event = event,
                    onClick = {
                        event.id?.let {
                            navController.navigate("event_detail/$it")
                        }
                    }
                )
            }
        }


        if (events.isEmpty() && !viewModel.isUploading.value) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tidak ada event, klik '+' untuk menambah.")
            }
        }
    }
}


@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Gambar
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )


            // Detail Card
            Column(Modifier.padding(16.dp)) {
                Text(
                    event.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(event.eventDate, fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.width(8.dp))
                    Text("|")
                    Spacer(Modifier.width(8.dp))
                    Text(event.location, fontSize = 14.sp, color = Color.Gray)
                }


                Spacer(Modifier.height(8.dp))
                // Harga (Display Price)
                Text(
                    event.displayPrice,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.End)
                )
            }
        }
    }
}




/* ================= 2. ADD EVENT SCREEN (FORM) ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(navController: NavController, viewModel: EventViewModel) {
    val context = LocalContext.current


    // State lokal untuk semua input form (Sesuai pola HotelViewModel)
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var displayPrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceDetails by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }


    // State dari ViewModel
    val isUploading by viewModel.isUploading


    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { imageUri = it }


    // Misalnya dari Supabase Auth Session
    val currentUserId: String? = null
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Kartu Event", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            // Image Upload
            ImagePickerBox(imageUri = imageUri, onClick = {
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            })


            Spacer(Modifier.height(24.dp))


            // FORM INPUT
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Event") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))


            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Tanggal Event (Ex: 02 - 03 Mei 2026)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))


            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Lokasi Event") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))


            OutlinedTextField(
                value = displayPrice,
                onValueChange = { displayPrice = it },
                label = { Text("Harga Tampilan Card (Ex: IDR 1.378.850)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))


            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi Detail Event") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Spacer(Modifier.height(12.dp))


            OutlinedTextField(
                value = priceDetails,
                onValueChange = { priceDetails = it },
                label = { Text("Detail Harga (Ex: VIP 1 - 2.000.000)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )


            Spacer(Modifier.height(28.dp))


            // TOMBOL SIMPAN
            Button(
                onClick = {
                    if (imageUri != null) {
                        viewModel.uploadImageAndSaveEvent(
                            context = context,
                            imageUri = imageUri!!,
                            userId = currentUserId, // Menggunakan ID dummy
                            title = title,
                            eventDate = date,
                            location = location,
                            displayPrice = displayPrice,
                            description = description,
                            priceDetails = priceDetails,
                            onSuccess = {
                                Toast.makeText(context, "Event berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Harap pilih gambar event", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isUploading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Simpan Kartu Event", color = Color.White)
                }
            }
        }
    }
}


// Helper Composable untuk Image Picker
@Composable
fun ImagePickerBox(imageUri: Uri?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clickable(onClick = onClick)
            .border(
                width = if (imageUri == null) 1.5.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tambah Gambar Event",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Klik untuk memilih gambar",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}


/* ================= 3. EVENT DETAIL SCREEN (BARU) ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    navController: NavController,
    viewModel: EventViewModel,
    eventId: String?
) {
    // 1. Ambil data event berdasarkan ID
    // Gunakan derivedStateOf untuk mengamati perubahan pada list event di ViewModel
    val event = remember {
        derivedStateOf { viewModel.getEventById(eventId ?: "") }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event.value?.title ?: "Detail Event", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        val currentEvent = event.value


        if (currentEvent == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Event tidak ditemukan.")
            }
            return@Scaffold
        }


        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Gambar Event
            AsyncImage(
                model = currentEvent.imageUrl,
                contentDescription = currentEvent.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )


            Column(Modifier.padding(16.dp)) {


                // Judul & Harga Tampilan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        currentEvent.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        currentEvent.displayPrice,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                }


                Spacer(Modifier.height(16.dp))


                // Tanggal & Lokasi
                Text("Tanggal: ${currentEvent.eventDate}", fontWeight = FontWeight.Medium)
                Text("Lokasi: ${currentEvent.location}")


                Spacer(Modifier.height(24.dp))


                // Deskripsi Detail
                Text("Deskripsi Event", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text(currentEvent.description ?: "Tidak ada deskripsi.")


                Spacer(Modifier.height(24.dp))


                // Detail Harga
                Text("Detail Harga", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text(currentEvent.priceDetails ?: "Tidak ada detail harga tambahan.")
            }
        }
    }
}
