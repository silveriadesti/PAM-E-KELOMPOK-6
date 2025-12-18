package com.example.splashandregist.ui.screens

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.splashandregist.data.model.Transport
import com.example.splashandregist.viewmodel.TransportViewModel
import androidx.compose.ui.Alignment


/* ================= ACTIVITY ================= */
/* Entry point sebelum masuk UI compose (urutan: user buka aplikasi - android menjalankan Transport Activity - onCreate() dipanggil - setContent - Compose UI ditampilkan */
class TransportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Atur layout agar bisa menyesuaikan system windows
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Atur warna status bar
        window.statusBarColor = android.graphics.Color.parseColor("#2196F3")

        // Menjalankan UI Jetpack Compose
        setContent {
            MaterialTheme {
                TransportScreen()
            }
        }
    }
}


/* ================= TOP BAR REUSABLE ================= */
// Menampilkan judul halaman “Admin Transport” dan tombol kembali di setiap halaman transport
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportTopBar(
    title: String,
    navController: NavController
) {
    TopAppBar(
        // Judul Halaman
        title = { Text(title) },
        // Tombol Kembali
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        // Atur warna AppBar
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF2196F3),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}

/* ================= ROOT & NAVIGATION ================= */
// Pusat kontrol semua halaman transport
// Mengatur navigasi dan menyediakan data melalui viewmodel
@Composable
fun TransportScreen() {
    // Membuat controller navigasi antar halaman transport
    val navController = rememberNavController()
    // Mengambil instance/objek ViewModel untuk mengelola data transport
    val viewModel: TransportViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    // Mengambil data transport saat halaman dibuka pertama kali
    LaunchedEffect(Unit) {
        viewModel.fetchTransports()
    }

    // Navigasi antar halaman
    NavHost(navController = navController, startDestination = "transport_list") { // Halaman pertama yang dibuka

        // Composable: pendaftaran halaman ke navigasi
        // Halaman Daftar Transport (semua daftar transport)
        composable("transport_list") {
            TransportListScreen(navController, viewModel)
        }
        // Halaman Tambah Transport
        composable("add_transport") {
            AddTransportScreen(navController, viewModel)
        }
        // Halaman Detail Transport
        composable("transport_detail/{id}") { backStack ->
            val id = backStack.arguments?.getString("id") ?: return@composable // Ambil ID transport yang diklik
            viewModel.getTransportById(id)?.let { // Cari data transport berdasarkan ID
                TransportDetailScreen(navController, viewModel, it)
            }
        }
        // Halaman edit transport
        composable("edit_transport/{id}") { backStack ->
            val id = backStack.arguments?.getString("id") ?: return@composable
            viewModel.getTransportById(id)?.let {
                EditTransportScreen(navController, viewModel, it)
            }
        }
    }
}

/* ================= LIST (HALAMAN UTAMA ADMIN) ================= */
// Halaman utama yang menampilkan daftar transport
/* Mengizinkan penggunaan API Material3 yang masih bersifat experimental (belum stabil). Con: ToAppBar, Scaffold*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportListScreen(navController: NavController, viewModel: TransportViewModel) {
    Scaffold(
        // AppBarr Halaman
        topBar = {
            TopAppBar(
                title = { Text("Admin Transport") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
                )
            )
        },
        // Tombol Tambah Transport
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_transport") }, // Tombol diklik, pindah ke AddTransportScreen
                containerColor = Color(0xFF2196F3)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
        }
    ) { padding -> // Supaya konten tidak ketimpa AppBar
        // Menampilkan list/daftar Transport secara vertikal dan bisa scroll
        // Arti lazy column: tidak langsung menampilkan semua item. Jadi item dibuat saat dibutuhkan, item yang belum di scroll tidak akan di render / ditampilkan ke layar
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.transports) { t -> // Ambil data transport dari ViewModel
                TransportItem(t) {
                    // Navigasi ke halaman detail transport saat klik card
                    navController.navigate("transport_detail/${t.id}")
                }
            }
        }
    }
}

/* ================= ITEM TRANSPORT ================= */
// Komponen Card untuk data transport
@Composable
fun TransportItem(t: Transport, onClick: () -> Unit) { // Fungsi ketika card di daftar transport diklik
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp) // Efek bayangan
    ) {
        Column {
            // Menampilkan gambar transport
            AsyncImage(
                model = t.imageUrl, // Gambar dari Supabase
                contentDescription = t.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )
            // Informasi transport
            Column(Modifier.padding(16.dp)) {
                Text(t.name, fontWeight = FontWeight.Bold)
                Text(t.type, color = Color.Gray)
                Text("Kapasitas: ${t.capacity}")
                Text(t.price, color = Color(0xFF2196F3))
            }
        }
    }
}

/* ================= ADD (TAMBAH TRANSPORT)  ================= */
// Halaman untuk menambah data transport baru
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransportScreen(navController: NavController, viewModel: TransportViewModel) {
    // State (nilai yang mempengaruhi tampilan UI) untuk input form
    // Var: variabel untuk menyimpan isi input
    // mutableStateOf(""): membuat state, mutable (bisa berubah)
    // remember: menyimpan nilai state supaya tidak reset saat UI di
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var route by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Digunakan untuk menampilkan Toast dan akses fitur Android
    val context = LocalContext.current

    // Launcher untuk memilih gambar dari galeri
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> imageUri = uri }

    // Judul “Tambah Transport” pada atas halaman
    Scaffold(
        topBar = {
            TransportTopBar("Tambah Transport", navController)
        }
    // mengatur jarak konten dan topbar agar konten tidak tumpang tindih
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                // Scroll layout secara vertikal
                .verticalScroll(rememberScrollState())
        ) {

            // ================= PREVIEW GAMBAR (PALING ATAS) =================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, Color(0xFF7E57C2)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F1FF))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri == null) {
                        // ===== BELUM ADA GAMBAR =====
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFF7E57C2),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "Pilih Gambar",
                                color = Color(0xFF7E57C2),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        // ===== SUDAH ADA GAMBAR =====
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Preview Gambar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Tombol pilih gambar
            Button(onClick = {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Text("Pilih Gambar")
            }

            Spacer(Modifier.height(20.dp))

            // field input data transport
            OutlinedTextField(name, { name = it }, label = { Text("Nama") })
            OutlinedTextField(type, { type = it }, label = { Text("Jenis") })
            OutlinedTextField(route, { route = it }, label = { Text("Rute") })
            OutlinedTextField(
                capacity, { capacity = it },
                label = { Text("Kapasitas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(price, { price = it }, label = { Text("Harga") })

            Spacer(Modifier.height(12.dp)) //memberi jarak (spasi) antar komponen UI

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi") },
                minLines = 3
                // Kolom input deskripsi dengan tinggi minimal 3 baris
            )

            Spacer(Modifier.height(12.dp))

            Spacer(Modifier.height(16.dp))

            // Simpan Data
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    // 1. VALIDASI GAMBAR (cek apakah user sudah pilih gambar)
                    if (imageUri == null) {
                        Toast.makeText(context, "Pilih gambar dulu", Toast.LENGTH_SHORT).show() //kalau belum, muncul pesan (toast)
                        return@Button
                    }

                    // 2. UPLOAD GAMBAR KE SUPABASE STORAGE
                    viewModel.uploadImage( // Panggil fungsi upload di ViewModel
                        context = context,
                        imageUri = imageUri!!,
                        onSuccess = { imageUrl -> // Callback berhasil, muncul URL gambar di Supabase

                            // 3. MEMBUAT OBJEK TRANSPORT BARU DI MEMORI DARI STATE FORM
                            val newTransport = Transport(
                                name = name,
                                type = type,
                                route = route,
                                capacity = capacity.toIntOrNull() ?: 0,
                                price = "Rp $price",
                                imageUrl = imageUrl,
                                description = description
                            )

                            // 4. MINTA VIEWMODEL SIMPAN OBJEK KE SUPABASE
                            viewModel.addTransport( //Memanggil Repository untuk insert tabel di supabase
                                newTransport,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Transport Ditambahkan",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack() // kembali ke 1 halaman sebelumnya
                                },
                                onError = { error ->
                                    Toast.makeText(
                                        context,
                                        error,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            )

                        },
                        onError = { error -> // Pesan error jika upload gambar gagal
                            Toast.makeText(
                                context,
                                "Upload gagal: $error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            ) {
                Text("Simpan")
            }
        }
    }
}

/* ================= EDIT ================= */
// Halaman untuk merubah data transport
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransportScreen(
    navController: NavController,
    // ViewModel sebagai penghubung ke data di Supabase
    viewModel: TransportViewModel,
    // Data transport yang akan diedit
    t: Transport
) {
    // Mengambil Context Android yang sedang aktif
    // Digunakan untuk menampilkan Toast dan akses fitur bawaan Android (misal buka galeri)
    val context = LocalContext.current

    // ================= STATE FORM =================
    // State ini menyimpan nilai input user dan memastikan UI selalu sinkron dengan data
    var name by remember { mutableStateOf(t.name) }
    var type by remember { mutableStateOf(t.type) }
    var route by remember { mutableStateOf(t.route) }
    var capacity by remember { mutableStateOf(t.capacity.toString()) }
    var price by remember { mutableStateOf(t.price.replace("Rp ", "")) }
    var description by remember { mutableStateOf(t.description) }
    var imageUri by remember { mutableStateOf<Uri?>(Uri.parse(t.imageUrl)) }
    // Penanda apakah gambar diganti
    var isImageChanged by remember { mutableStateOf(false) }



    // Launcher untuk membuka galeri dan memilih gambar
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageUri = uri // Update gambar
            isImageChanged = true  // Tandai bahwa gambar berubah
        }
    }

    // AppBar dengan tombol back
    Scaffold(
        topBar = {
            TransportTopBar("Edit Transport", navController)
        }
    ) { padding ->
        // Menyusun komponen secara vertikal
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // PREVIEW GAMBAR
            // Menampilkan gambar lama (URL) / gambar baru yang dipilih (URI)
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(8.dp))

            // Tombol untuk mengganti gambar
            Button(onClick = {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Text("Ganti Gambar")
            }

            Spacer(Modifier.height(16.dp))

            // ================= FORM INPUT =================
            OutlinedTextField(name, { name = it }, label = { Text("Nama") })
            OutlinedTextField(type, { type = it }, label = { Text("Jenis") })
            OutlinedTextField(route, { route = it }, label = { Text("Rute") })

            OutlinedTextField(
                capacity,
                { capacity = it },
                label = { Text("Kapasitas") },
                // Input kapasitas hanya angka
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(price, { price = it }, label = { Text("Harga") })

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi") },
                minLines = 3 // Input deskripsi dengan tinggi minimal 3 baris
            )

            Spacer(Modifier.height(16.dp))

            // ================= TOMBOL SIMPAN =================
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    // Jika gambar diganti, upload dulu ke Supabase Storage
                    if (isImageChanged && imageUri != null) {

                        // 1. UPLOAD GAMBAR KE SUPABASE STORAGE
                        viewModel.uploadImage(
                            context = context,
                            imageUri = imageUri!!,
                            onSuccess = { imageUrl ->

                                // 2. MEMBUAT OBJEK DI MEMORI
                                // Buat data baru dan URL gambar baru
                                val updated = t.copy(
                                    name = name,
                                    type = type,
                                    route = route,
                                    capacity = capacity.toIntOrNull() ?: t.capacity,
                                    price = "Rp $price",
                                    imageUrl = imageUrl,
                                    description = description
                                )

                                // 3. MINTA VIEWMODEL UPDATE DATA KE SUPABASE
                                viewModel.updateTransport(updated) {
                                    Toast.makeText(context, "Berhasil Update", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                            },
                            onError = {
                                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                            }
                        )

                    } else {

                        // Jika gambar tidak diganti, langsung update data teks saja
                        val updated = t.copy(
                            name = name,
                            type = type,
                            route = route,
                            capacity = capacity.toIntOrNull() ?: t.capacity,
                            price = "Rp $price",
                            description = description
                        )

                        viewModel.updateTransport(updated) { // Update data transport ke supabase
                            Toast.makeText(context, "Berhasil Update", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    }
                }
            ) {
                Text("Simpan Perubahan")
            }
        }
    }
}


/* ================= DETAIL ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportDetailScreen(
    navController: NavController,
    viewModel: TransportViewModel,
    t: Transport
) {
    // Menampilkan Toast dan akses fitur Android
    val context = LocalContext.current

    Scaffold(
        topBar = { // AppBar
            TransportTopBar("Detail Transport", navController)
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState()) // Scroll kalau konten panjang
        ) {

            // Menampilkan gambar dari URL secara asynchronous
            AsyncImage(
                model = t.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Crop
            )

            // Informasi Transport
            Column(Modifier.padding(16.dp)) {
                Text(t.name, fontWeight = FontWeight.Bold)
                Text(t.route)
                Text(t.price)

                Spacer(Modifier.height(8.dp))
                Text(t.description, fontSize = 14.sp, color = Color.Gray)

                Spacer(Modifier.height(16.dp))

                // Tombol aksi sejajar (edit & hapus data)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Tombol Edit
                    Button(onClick = {
                        navController.navigate("edit_transport/${t.id!!}")
                    }) {
                        Text("Edit")
                    }

                    // Tombol Hapus
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        onClick = {
                            viewModel.deleteTransport(t.id!!) {
                            Toast.makeText(context, "Data Dihapus", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Text("Hapus")
                    }
                }
            }
        }
    }
}