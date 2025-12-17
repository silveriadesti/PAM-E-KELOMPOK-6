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
import androidx.compose.ui.Alignment
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
import com.example.splashandregist.data.model.Transport
import com.example.splashandregist.viewmodel.TransportViewModel

/* ================= ACTIVITY ================= */
class TransportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.parseColor("#2196F3")
        setContent {
            MaterialTheme {
                TransportScreen()
            }
        }
    }
}

/* ================= TOP BAR REUSABLE ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportTopBar(
    title: String,
    navController: NavController
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF2196F3),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}

/* ================= ROOT ================= */
@Composable
fun TransportScreen() {
    val navController = rememberNavController()
    val viewModel: TransportViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    LaunchedEffect(Unit) {
        viewModel.fetchTransports()
    }

    NavHost(navController = navController, startDestination = "transport_list") {

        composable("transport_list") {
            TransportListScreen(navController, viewModel)
        }

        composable("add_transport") {
            AddTransportScreen(navController, viewModel)
        }

        composable("transport_detail/{id}") { backStack ->
            val id = backStack.arguments?.getString("id") ?: return@composable
            viewModel.getTransportById(id)?.let {
                TransportDetailScreen(navController, viewModel, it)
            }
        }

        composable("edit_transport/{id}") { backStack ->
            val id = backStack.arguments?.getString("id") ?: return@composable
            viewModel.getTransportById(id)?.let {
                EditTransportScreen(navController, viewModel, it)
            }
        }
    }
}

/* ================= LIST ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportListScreen(navController: NavController, viewModel: TransportViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Transport") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_transport") },
                containerColor = Color(0xFF2196F3)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.transports) { t ->
                TransportItem(t) {
                    navController.navigate("transport_detail/${t.id}")
                }
            }
        }
    }
}

@Composable
fun TransportItem(t: Transport, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            AsyncImage(
                model = t.imageUrl,
                contentDescription = t.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(16.dp)) {
                Text(t.name, fontWeight = FontWeight.Bold)
                Text(t.type, color = Color.Gray)
                Text("Kapasitas: ${t.capacity}")
                Text(t.price, color = Color(0xFF2196F3))
            }
        }
    }
}

/* ================= ADD ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransportScreen(navController: NavController, viewModel: TransportViewModel) {

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var route by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> imageUri = uri }

    Scaffold(
        topBar = {
            TransportTopBar("Tambah Transport", navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            OutlinedTextField(name, { name = it }, label = { Text("Nama") })
            OutlinedTextField(type, { type = it }, label = { Text("Jenis") })
            OutlinedTextField(route, { route = it }, label = { Text("Rute") })
            OutlinedTextField(
                capacity, { capacity = it },
                label = { Text("Kapasitas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(price, { price = it }, label = { Text("Harga") })

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi") },
                minLines = 3
            )

            Spacer(Modifier.height(12.dp))

            Button(onClick = {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Text("Pilih Gambar")
            }

            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    if (imageUri == null) {
                        Toast.makeText(context, "Pilih gambar dulu", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // 🔥 1. UPLOAD GAMBAR KE SUPABASE
                    viewModel.uploadImage(
                        context = context,
                        imageUri = imageUri!!,
                        onSuccess = { imageUrl ->

                            // 🔥 2. BARU SIMPAN KE DATABASE
                            val newTransport = Transport(
                                name = name,
                                type = type,
                                route = route,
                                capacity = capacity.toIntOrNull() ?: 0,
                                price = "Rp $price",
                                imageUrl = imageUrl,
                                description = description
                            )


                            viewModel.addTransport(
                                newTransport,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Transport Ditambahkan",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
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
                        onError = { error ->
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransportScreen(
    navController: NavController,
    viewModel: TransportViewModel,
    t: Transport
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(t.name) }
    var type by remember { mutableStateOf(t.type) }
    var route by remember { mutableStateOf(t.route) }
    var capacity by remember { mutableStateOf(t.capacity.toString()) }
    var price by remember { mutableStateOf(t.price.replace("Rp ", "")) }
    var description by remember { mutableStateOf(t.description) }
    var imageUri by remember { mutableStateOf<Uri?>(Uri.parse(t.imageUrl)) }
    var isImageChanged by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
            isImageChanged = true
        }
    }


    Scaffold(
        topBar = {
            TransportTopBar("Edit Transport", navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // 🔹 PREVIEW GAMBAR
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(8.dp))

            Button(onClick = {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Text("Ganti Gambar")
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(name, { name = it }, label = { Text("Nama") })
            OutlinedTextField(type, { type = it }, label = { Text("Jenis") })
            OutlinedTextField(route, { route = it }, label = { Text("Rute") })

            OutlinedTextField(
                capacity,
                { capacity = it },
                label = { Text("Kapasitas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(price, { price = it }, label = { Text("Harga") })

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi") },
                minLines = 3
            )

            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    if (isImageChanged && imageUri != null) {

                        viewModel.uploadImage(
                            context = context,
                            imageUri = imageUri!!,
                            onSuccess = { imageUrl ->

                                val updated = t.copy(
                                    name = name,
                                    type = type,
                                    route = route,
                                    capacity = capacity.toIntOrNull() ?: t.capacity,
                                    price = "Rp $price",
                                    imageUrl = imageUrl,
                                    description = description
                                )

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

                        val updated = t.copy(
                            name = name,
                            type = type,
                            route = route,
                            capacity = capacity.toIntOrNull() ?: t.capacity,
                            price = "Rp $price",
                            description = description
                        )

                        viewModel.updateTransport(updated) {
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
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TransportTopBar("Detail Transport", navController)
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            AsyncImage(
                model = t.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Crop
            )

            Column(Modifier.padding(16.dp)) {
                Text(t.name, fontWeight = FontWeight.Bold)
                Text(t.route)
                Text(t.price)

                Spacer(Modifier.height(8.dp))
                Text(t.description, fontSize = 14.sp, color = Color.Gray)

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    Button(onClick = {
                        navController.navigate("edit_transport/${t.id!!}")
                    }) {
                        Text("Edit")
                    }

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