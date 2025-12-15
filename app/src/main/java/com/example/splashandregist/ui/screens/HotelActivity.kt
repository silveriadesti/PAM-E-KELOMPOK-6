package com.example.splashandregist.ui.screens

import android.net.Uri
import android.os.Bundle
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage

// --- 1. ACTIVITY UTAMA ---
class HotelActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HotelApp()
            }
        }
    }
}

// --- 2. PENGATURAN NAVIGASI (NavHost) ---
@Composable
fun HotelApp() {
    val navController = rememberNavController()
    // Pastikan HotelViewModel sudah ada di file HotelViewModel.kt
    val viewModel = remember { HotelViewModel() }

    // PENTING: startDestination harus "hotel_list" dan harus ada composable-nya di bawah
    NavHost(navController = navController, startDestination = "hotel_list") {

        // Rute 1: List Hotel
        composable("hotel_list") {
            HotelListScreen(navController, viewModel)
        }

        // Rute 2: Tambah Hotel
        composable("add_hotel") {
            AddHotelScreen(navController, viewModel)
        }

        // Rute 3: Detail Hotel
        composable("hotel_detail/{hotelId}") { backStackEntry ->
            val hotelId = backStackEntry.arguments?.getString("hotelId")
            if (hotelId != null) {
                val hotel = viewModel.getHotelById(hotelId)
                if (hotel != null) {
                    HotelDetailScreen(navController, hotel)
                }
            }
        }

        // Rute 4: Edit Hotel (Fitur Baru)
        composable("edit_hotel/{hotelId}") { backStackEntry ->
            val hotelId = backStackEntry.arguments?.getString("hotelId")
            if (hotelId != null) {
                EditHotelScreen(navController, viewModel, hotelId)
            }
        }
    }
}

// --- 3. LAYAR LIST HOTEL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelListScreen(navController: NavController, viewModel: HotelViewModel) {
    // Refresh data saat layar dibuka
    LaunchedEffect(Unit) {
        viewModel.getHotels()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Hotel Traveloka") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_hotel") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Hotel", tint = Color.White)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(paddingValues)
        ) {
            items(viewModel.hotels) { hotel ->
                HotelItem(hotel) {
                    navController.navigate("hotel_detail/${hotel.id}")
                }
            }
        }
    }
}

@Composable
fun HotelItem(hotel: Hotel, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            AsyncImage(
                model = hotel.imageUrl,
                contentDescription = hotel.name,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = hotel.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = hotel.location, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = hotel.price, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// --- 4. LAYAR TAMBAH HOTEL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHotelScreen(navController: NavController, viewModel: HotelViewModel) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val isUploading by viewModel.isUploading
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Hotel Baru") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Upload Image Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                        .clickable(enabled = !isUploading) {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(model = selectedImageUri, contentDescription = "Selected Image", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Text("Ketuk untuk pilih gambar", color = Color.Gray)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Inputs
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Hotel") }, modifier = Modifier.fillMaxWidth(), enabled = !isUploading)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Lokasi") }, modifier = Modifier.fillMaxWidth(), enabled = !isUploading)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga (Angka)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), enabled = !isUploading)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), minLines = 3, enabled = !isUploading)

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (name.isNotEmpty() && price.isNotEmpty() && selectedImageUri != null) {
                            viewModel.uploadImageAndSaveHotel(context, selectedImageUri!!, name, location, price, description) {
                                Toast.makeText(context, "Berhasil!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        } else {
                            Toast.makeText(context, "Lengkapi semua data!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isUploading
                ) {
                    Text(if (isUploading) "Menyimpan..." else "Simpan")
                }
            }
            if (isUploading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

// --- 5. LAYAR DETAIL HOTEL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailScreen(navController: NavController, hotel: Hotel) {
    Scaffold(
        bottomBar = {
            Button(
                onClick = {
                    // Navigasi ke Edit
                    navController.navigate("edit_hotel/${hotel.id}")
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Edit Data Hotel")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).verticalScroll(rememberScrollState())) {
            Box {
                AsyncImage(model = hotel.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(300.dp), contentScale = ContentScale.Crop)
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.padding(16.dp).align(Alignment.TopStart)) {
                    Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.7f)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.padding(8.dp))
                    }
                }
            }
            Column(modifier = Modifier.padding(24.dp)) {
                Text(hotel.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(hotel.location, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text(hotel.price, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Text(hotel.description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// --- 6. LAYAR EDIT HOTEL (NEW) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHotelScreen(navController: NavController, viewModel: HotelViewModel, hotelId: String) {
    val hotelToEdit = viewModel.getHotelById(hotelId)

    var name by remember { mutableStateOf(hotelToEdit?.name ?: "") }
    var location by remember { mutableStateOf(hotelToEdit?.location ?: "") }
    var price by remember { mutableStateOf(hotelToEdit?.price?.replace("Rp ", "") ?: "") }
    var description by remember { mutableStateOf(hotelToEdit?.description ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val currentImageUrl = hotelToEdit?.imageUrl ?: ""

    val isUploading by viewModel.isUploading
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("Edit Hotel") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Back") } }) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState())) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp).border(1.dp, Color.Gray, RoundedCornerShape(12.dp)).clickable(enabled = !isUploading) {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        AsyncImage(model = currentImageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    Surface(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(), color = Color.Black.copy(alpha = 0.5f)) {
                        Text("Ketuk untuk ganti foto", color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Lokasi") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (name.isNotEmpty() && price.isNotEmpty() && hotelToEdit != null) {
                            viewModel.updateHotel(context, hotelToEdit.id!!, selectedImageUri, currentImageUrl, name, location, if (price.startsWith("Rp")) price else "Rp $price", description) {
                                Toast.makeText(context, "Updated!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isUploading
                ) { Text(if (isUploading) "Updating..." else "Update") }
            }
            if (isUploading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}