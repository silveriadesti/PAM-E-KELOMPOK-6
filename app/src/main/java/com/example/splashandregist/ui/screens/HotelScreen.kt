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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.splashandregist.viewmodel.Hotel
import com.example.splashandregist.viewmodel.HotelViewModel

/* ================= ROOT HOTEL SCREEN ================= */
@Composable
fun HotelScreen() {
    val navController = rememberNavController()
    val viewModel: HotelViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "hotel_list"
    ) {
        composable("hotel_list") {
            HotelListScreen(navController, viewModel)
        }
        composable("add_hotel") {
            AddHotelScreen(navController, viewModel)
        }
        composable("hotel_detail/{id}") {
            HotelDetailScreen(
                navController = navController,
                viewModel = viewModel,
                hotelId = it.arguments?.getString("id")
            )
        }
    }
}

/* ================= 1. HOTEL LIST ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelListScreen(
    navController: NavController,
    viewModel: HotelViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.getHotels()
    }

    val hotels = viewModel.hotels
    val isUploading by viewModel.isUploading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hotel Dashboard", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_hotel") }
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        if (hotels.isEmpty() && !isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada hotel")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(hotels) { hotel ->
                    HotelCard(hotel) {
                        hotel.id?.let {
                            navController.navigate("hotel_detail/$it")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HotelCard(hotel: Hotel, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column {
            AsyncImage(
                model = hotel.imageUrl,
                contentDescription = hotel.name,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(16.dp)) {
                Text(hotel.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(hotel.location, color = Color.Gray)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    hotel.price,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/* ================= 2. ADD HOTEL ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHotelScreen(
    navController: NavController,
    viewModel: HotelViewModel
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val isUploading by viewModel.isUploading

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { imageUri = it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Hotel", color = Color.White) },
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

            HotelImagePickerBox(imageUri) {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(name, { name = it }, label = { Text("Nama Hotel") })
            OutlinedTextField(location, { location = it }, label = { Text("Lokasi") })
            OutlinedTextField(
                price,
                { price = it },
                label = { Text("Harga") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
            OutlinedTextField(description, { description = it }, label = { Text("Deskripsi") }, minLines = 3)

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading,
                onClick = {
                    if (imageUri == null) {
                        Toast.makeText(context, "Pilih gambar", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.uploadImageAndSaveHotel(
                        context,
                        imageUri!!,
                        name,
                        location,
                        price,
                        description
                    ) {
                        Toast.makeText(context, "Hotel ditambahkan", Toast.LENGTH_SHORT).show()
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

/* ================= 3. HOTEL DETAIL ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailScreen(
    navController: NavController,
    viewModel: HotelViewModel,
    hotelId: String?
) {
    val hotel = viewModel.getHotelById(hotelId ?: "")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(hotel?.name ?: "Detail Hotel", color = Color.White) },
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
        if (hotel == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Hotel tidak ditemukan")
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = hotel.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(250.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(16.dp)) {
                Text(hotel.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(hotel.price, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text("Lokasi: ${hotel.location}")
                Spacer(Modifier.height(16.dp))
                Text(hotel.description)
            }
        }
    }
}

/* ================= IMAGE PICKER ================= */
@Composable
fun HotelImagePickerBox(imageUri: Uri?, onClick: () -> Unit) {
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
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}
