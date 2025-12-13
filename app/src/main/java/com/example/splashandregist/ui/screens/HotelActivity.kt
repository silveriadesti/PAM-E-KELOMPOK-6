package com.example.splashandregist.ui.screens

import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.splashandregist.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

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

// --- 2. VIEWMODEL (LOGIKA APLIKASI) ---
class HotelViewModel : ViewModel() {

    // List untuk menampung data dari Supabase
    private val _hotels = mutableStateListOf<Hotel>()
    val hotels: List<Hotel> get() = _hotels

    // State untuk Loading Upload (Biar tombol bisa dimatikan saat loading)
    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading

    // Fungsi untuk MENARIK DATA (READ)
    fun getHotels() {
        viewModelScope.launch {
            try {
                val data = SupabaseClient.client
                    .from("hotels")
                    .select()
                    .decodeList<Hotel>()

                _hotels.clear()
                _hotels.addAll(data)

            } catch (e: Exception) {
                e.printStackTrace()
                println("Error mengambil data: ${e.message}")
            }
        }
    }

    fun getHotelById(id: String): Hotel? {
        val idLong = id.toLongOrNull()
        return _hotels.find { it.id == idLong }
    }

    // --- FUNGSI UPLOAD GAMBAR KE SUPABASE STORAGE ---
    fun uploadImageAndSaveHotel(
        context: Context,
        imageUri: Uri,
        name: String,
        location: String,
        price: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true // Mulai loading
            try {
                // 1. Ubah gambar di HP jadi kumpulan bita (byte array)
                val imageBytes = context.contentResolver.openInputStream(imageUri)?.use {
                    it.readBytes()
                } ?: throw Exception("Gagal membaca file gambar")

                // 2. Buat nama file unik (pakai UUID biar gak kembar)
                val fileName = "${UUID.randomUUID()}.jpg"

                // 3. Upload ke Supabase Storage (Bucket 'hotel-images')
                val bucket = SupabaseClient.client.storage.from("hotel-images")
                bucket.upload(fileName, imageBytes)

                // 4. Ambil URL Publik gambar yang baru diupload
                val publicUrl = bucket.publicUrl(fileName)

                // 5. Buat objek Hotel dengan URL gambar yang asli
                val newHotel = Hotel(
                    name = name,
                    location = location,
                    price = "Rp $price",
                    description = description,
                    imageUrl = publicUrl // <-- Pakai URL dari Supabase
                )

                // 6. Simpan data teks ke Database
                SupabaseClient.client.from("hotels").insert(newHotel)

                // 7. Refresh list dan kabari kalau sukses
                getHotels()
                onSuccess()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _isUploading.value = false // Selesai loading
            }
        }
    }
}

// --- 3. NAVIGASI UTAMA ---
@Composable
fun HotelApp() {
    val navController = rememberNavController()
    val viewModel = remember { HotelViewModel() }

    NavHost(navController = navController, startDestination = "hotel_list") {
        composable("hotel_list") { HotelListScreen(navController, viewModel) }
        composable("add_hotel") { AddHotelScreen(navController, viewModel) }
        composable("hotel_detail/{hotelId}") { backStackEntry ->
            val hotelId = backStackEntry.arguments?.getString("hotelId")
            if (hotelId != null) {
                val hotel = viewModel.getHotelById(hotelId)
                if (hotel != null) {
                    HotelDetailScreen(navController, hotel)
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: Hotel with ID $hotelId not found.")
                    }
                }
            }
        }
    }
}

// --- 4. LAYAR DAFTAR HOTEL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelListScreen(navController: NavController, viewModel: HotelViewModel) {
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

// --- 5. LAYAR TAMBAH HOTEL (YANG TADI ERROR) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHotelScreen(navController: NavController, viewModel: HotelViewModel) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Variabel ini yang tadi hilang:
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Ambil status loading dari ViewModel
    val isUploading by viewModel.isUploading
    val context = LocalContext.current

    // Launcher untuk buka Galeri HP
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Hotel Baru") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }, enabled = !isUploading) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->

        // Box utama agar loading bisa muncul di tengah
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // --- KOTAK PILIH GAMBAR ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                        .clickable(enabled = !isUploading) {
                            // Buka Galeri saat diklik
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        // Kalau sudah pilih gambar, tampilkan preview
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Kalau belum, tampilkan instruksi
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Text("Ketuk untuk pilih gambar", color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input Nama
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nama Hotel") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Input Lokasi
                OutlinedTextField(
                    value = location, onValueChange = { location = it },
                    label = { Text("Lokasi") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Input Harga
                OutlinedTextField(
                    value = price, onValueChange = { price = it },
                    label = { Text("Harga per Malam (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Input Deskripsi
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = !isUploading
                )

                Spacer(modifier = Modifier.height(24.dp))

                // TOMBOL SIMPAN
                Button(
                    onClick = {
                        if (name.isNotEmpty() && price.isNotEmpty() && selectedImageUri != null) {
                            // Panggil fungsi Upload & Save
                            viewModel.uploadImageAndSaveHotel(
                                context = context,
                                imageUri = selectedImageUri!!,
                                name = name,
                                location = location,
                                price = price,
                                description = description,
                                onSuccess = {
                                    Toast.makeText(context, "Berhasil Upload & Simpan!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Nama, Harga, dan Gambar wajib diisi!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isUploading // Tombol mati saat loading
                ) {
                    Text(if (isUploading) "Sedang Mengupload..." else "Simpan Data Hotel")
                }
            }

            // Indikator Loading (Muncul di tengah kalau sedang upload)
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

// --- 6. LAYAR DETAIL HOTEL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailScreen(navController: NavController, hotel: Hotel) {
    Scaffold(
        bottomBar = {
            Button(
                onClick = { /* Logic Edit */ },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Edit Data Hotel")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).verticalScroll(rememberScrollState())
        ) {
            Box {
                AsyncImage(
                    model = hotel.imageUrl,
                    contentDescription = hotel.name,
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                ) {
                    Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.7f)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.padding(8.dp))
                    }
                }
            }
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

// --- 7. DATA CLASS ---
@Serializable
data class Hotel(
    val id: Long? = null,
    @SerialName("name") val name: String,
    @SerialName("location") val location: String,
    @SerialName("price") val price: String,
    @SerialName("description") val description: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("user_id") val userId: String? = null
)