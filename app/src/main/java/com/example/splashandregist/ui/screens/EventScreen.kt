package com.example.splashandregist.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.splashandregist.viewmodel.Event
import com.example.splashandregist.viewmodel.EventViewModel

/* ================= ROOT EVENT SCREEN ================= */
@Composable
fun EventScreen() {
    val navController = rememberNavController()
    val viewModel: EventViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "event_list"
    ) {
        composable("event_list") {
            EventDashboardScreen(navController, viewModel)
        }
        composable("add_event") {
            AddEventScreen(navController, viewModel)
        }
        composable("event_detail/{id}") {
            EventDetailScreen(
                navController = navController,
                viewModel = viewModel,
                eventId = it.arguments?.getString("id")
            )
        }
    }
}

/* ================= 1. EVENT LIST ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDashboardScreen(
    navController: NavController,
    viewModel: EventViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.getEvents()
    }

    val events = viewModel.events
    val isUploading by viewModel.isUploading

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
                onClick = { navController.navigate("add_event") }
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        if (events.isEmpty() && !isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada event")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events) { event ->
                    EventCard(event) {
                        event.id?.let {
                            navController.navigate("event_detail/$it")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.title,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(16.dp)) {
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Text("${event.eventDate} • ${event.location}", color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Text(
                    event.displayPrice,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/* ================= 2. ADD EVENT ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    navController: NavController,
    viewModel: EventViewModel
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var displayPrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceDetails by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val isUploading by viewModel.isUploading

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { imageUri = it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Event", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            ImagePickerBox(imageUri) {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(title, { title = it }, label = { Text("Judul Event") })
            OutlinedTextField(date, { date = it }, label = { Text("Tanggal Event") })
            OutlinedTextField(location, { location = it }, label = { Text("Lokasi") })
            OutlinedTextField(displayPrice, { displayPrice = it }, label = { Text("Harga Tampilan") })
            OutlinedTextField(description, { description = it }, label = { Text("Deskripsi") }, minLines = 3)
            OutlinedTextField(priceDetails, { priceDetails = it }, label = { Text("Detail Harga") }, minLines = 3)

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading,
                onClick = {
                    if (imageUri == null) {
                        Toast.makeText(context, "Pilih gambar", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.uploadImageAndSaveEvent(
                        context,
                        imageUri!!,
                        null,
                        title,
                        date,
                        location,
                        displayPrice,
                        description,
                        priceDetails
                    ) {
                        Toast.makeText(context, "Event ditambahkan", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                }
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Simpan")
                }
            }
        }
    }
}

/* ================= IMAGE PICKER ================= */
@Composable
fun ImagePickerBox(imageUri: Uri?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Add, null)
                Text("Pilih Gambar")
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

/* ================= 3. EVENT DETAIL ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    navController: NavController,
    viewModel: EventViewModel,
    eventId: String?
) {
    val event = viewModel.getEventById(eventId ?: "")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.title ?: "Detail Event", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        if (event == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Event tidak ditemukan")
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(250.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(16.dp)) {
                Text(event.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(event.displayPrice, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Text("Tanggal: ${event.eventDate}")
                Text("Lokasi: ${event.location}")
                Spacer(Modifier.height(16.dp))
                Text(event.description ?: "-")
                Spacer(Modifier.height(16.dp))
                Text(event.priceDetails ?: "-")
            }
        }
    }
}
