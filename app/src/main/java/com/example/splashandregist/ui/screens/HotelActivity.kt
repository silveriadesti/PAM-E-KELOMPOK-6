package com.example.splashandregist.ui.screens

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.os.Bundle
import coil.compose.AsyncImage

// --- 1. STRUKTUR ACTIVITY ---
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

// --- 2. MODEL DATA (Sementara) ---
data class Hotel(
    val id: String,
    val name: String,
    val location: String,
    val price: String,
    val description: String,
    val imageUrl: String
)

// --- 3. VIEWMODEL SEDERHANA (Untuk Simulasi Data) ---
class HotelViewModel : ViewModel() {

    // PERBAIKAN: 'private val' harus dipisah spasi
    private val _hotels = mutableStateListOf(
        Hotel("1", "Hotel Majapahit", "Surabaya", "Rp 1.500.000", "Hotel bersejarah dengan arsitektur kolonial yang indah.", "https://upload.wikimedia.org/wikipedia/commons/thumb/6/66/Hotel_Majapahit_Surabaya.jpg/1200px-Hotel_Majapahit_Surabaya.jpg"),
        Hotel("2", "Grand Hyatt", "Jakarta", "Rp 2.800.000", "Pengalaman menginap mewah di pusat kota Jakarta.", "https://dynamic-media-cdn.tripadvisor.com/media/photo-o/29/0e/69/3b/grand-hyatt-jakarta.jpg?w=1200&h=-1&s=1")
    )

    val hotels: List<Hotel> get() = _hotels

    fun addHotel(hotel: Hotel) {
        _hotels.add(hotel)
    }

    fun getHotelById(id: String): Hotel? {
        return _hotels.find { it.id == id }
    }
}

// --- 4. NAVIGASI UTAMA ---
@Composable
fun HotelApp() {
    val navController = rememberNavController()
    val viewModel = remember { HotelViewModel() } // Inisialisasi ViewModel

    NavHost(navController = navController, startDestination = "hotel_list") {
        // Halaman List
        composable("hotel_list") {
            HotelListScreen(navController, viewModel)
        }
        // Halaman Tambah (Form)
        composable("add_hotel") {
            AddHotelScreen(navController, viewModel)
        }
        // Halaman Detail (Menerima ID)
        composable("hotel_detail/{hotelId}") { backStackEntry ->
            val hotelId = backStackEntry.arguments?.getString("hotelId")
            val hotel = viewModel.getHotelById(hotelId ?: "")
            if (hotel != null) {
                HotelDetailScreen(navController, hotel)
            }
        }
    }
}

// --- 5. LAYAR DAFTAR HOTEL (HotelListScreen) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelListScreen(navController: NavController, viewModel: HotelViewModel) {
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Gambar Hotel
            AsyncImage(
                model = hotel.imageUrl,
                contentDescription = hotel.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
            // Info Hotel
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

// --- 6. LAYAR TAMBAH HOTEL (AddHotelScreen) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHotelScreen(navController: NavController, viewModel: HotelViewModel) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    val context = LocalContext.current

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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nama Hotel") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = location, onValueChange = { location = it },
                label = { Text("Lokasi") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = price, onValueChange = { price = it },
                label = { Text("Harga per Malam (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("Deskripsi") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = imageUrl, onValueChange = { imageUrl = it },
                label = { Text("URL Gambar Hotel") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://...") }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotEmpty() && price.isNotEmpty()) {
                        // Simpan ke ViewModel (Nanti diganti Supabase)
                        val newHotel = Hotel(
                            id = System.currentTimeMillis().toString(),
                            name = name,
                            location = location,
                            price = "Rp $price",
                            description = description,
                            imageUrl = imageUrl
                        )
                        viewModel.addHotel(newHotel)
                        Toast.makeText(context, "Hotel Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Nama dan Harga wajib diisi!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Simpan Data Hotel")
            }
        }
    }
}

// --- 7. LAYAR DETAIL HOTEL (HotelDetailScreen) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailScreen(navController: NavController, hotel: Hotel) {
    Scaffold(
        bottomBar = {
            // Tombol Booking/Edit di bawah
            Button(
                onClick = { /* Nanti logika Edit atau Delete */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Edit Data Hotel")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Gambar Header Full
            Box {
                AsyncImage(
                    model = hotel.imageUrl,
                    contentDescription = hotel.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )
                // Tombol Back di atas gambar
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                ) {
                    Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.7f)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.padding(8.dp))
                    }
                }
            }

            // Konten Detail
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = hotel.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = hotel.location, style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Harga per Malam", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Text(text = hotel.price, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Deskripsi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = hotel.description, style = MaterialTheme.typography.bodyMedium, lineHeight = 24.sp)
            }
        }
    }
}