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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.splashandregist.viewmodel.Destinations
import com.example.splashandregist.viewmodel.DestinationViewModel

// --- 1. ACTIVITY UTAMA ---
class DestinationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DestinationApp()
            }
        }
    }
}

// --- 2. PENGATURAN NAVIGASI (NavHost) ---
@Composable
fun DestinationApp() {
    val navController = rememberNavController()
    val viewModel = remember { DestinationViewModel() }

    NavHost(navController = navController, startDestination = "destination_list") {

        // Rute 1: List Destination
        composable("destination_list") {
            DestinationListScreen(navController, viewModel)
        }

        // Rute 2: Tambah Destination
        composable("add_destination") {
            AddDestinationScreen(navController, viewModel)
        }

        // Rute 3: Detail Destination
        composable("destination_detail/{destinationId}") { backStackEntry ->
            val destinationId = backStackEntry.arguments?.getString("destinationId")
            if (destinationId != null) {
                val destination = viewModel.getDestinationById(destinationId)
                if (destination != null) {
                    DestinationDetailScreen(navController, destination)
                }
            }
        }

        // Rute 4: Edit Destination
        composable("edit_destination/{destinationId}") { backStackEntry ->
            val destinationId = backStackEntry.arguments?.getString("destinationId")
            if (destinationId != null) {
                EditDestinationScreen(navController, viewModel, destinationId)
            }
        }
    }
}

// --- 3. LAYAR LIST DESTINATION ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationListScreen(navController: NavController, viewModel: DestinationViewModel) {
    LaunchedEffect(Unit) {
        viewModel.getDestinations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Destinasi Wisata") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_destination") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Destinasi", tint = Color.White)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(paddingValues)
        ) {
            items(viewModel.destinations) { destination ->
                DestinationItem(destination) {
                    navController.navigate("destination_detail/${destination.id}")
                }
            }
        }
    }
}

@Composable
fun DestinationItem(destination: Destinations, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            AsyncImage(
                model = destination.image_url,
                contentDescription = destination.name,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = destination.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = destination.location, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = destination.price, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// --- 4. LAYAR TAMBAH DESTINATION ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDestinationScreen(navController: NavController, viewModel: DestinationViewModel) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val isUploading = viewModel.isUploading
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    // Tampilkan error jika ada
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { error ->
            Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Destinasi Baru") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }, enabled = !isUploading) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Text("Ketuk untuk pilih gambar", color = Color.Gray)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Destinasi") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lokasi") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Harga (Angka)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = !isUploading
                )

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (name.isNotEmpty() && price.isNotEmpty() && selectedImageUri != null) {
                            viewModel.uploadImageAndSaveDestination(context, selectedImageUri!!, name, location, price, description) {
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
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

// --- 5. LAYAR DETAIL DESTINATION ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationDetailScreen(navController: NavController, destination: Destinations) {
    Scaffold(
        bottomBar = {
            Button(
                onClick = {
                    navController.navigate("edit_destination/${destination.id}")
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Edit Data Destinasi")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).verticalScroll(rememberScrollState())) {
            Box {
                AsyncImage(
                    model = destination.image_url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                ) {
                    Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.7f)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.padding(8.dp))
                    }
                }
            }
            Column(modifier = Modifier.padding(24.dp)) {
                Text(destination.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(destination.location, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text(destination.price, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Text(destination.description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// --- 6. LAYAR EDIT DESTINATION ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDestinationScreen(navController: NavController, viewModel: DestinationViewModel, destinationId: String) {
    val destinationToEdit = viewModel.getDestinationById(destinationId)

    var name by remember { mutableStateOf(destinationToEdit?.name ?: "") }
    var location by remember { mutableStateOf(destinationToEdit?.location ?: "") }
    var price by remember { mutableStateOf(destinationToEdit?.price?.replace("Rp ", "") ?: "") }
    var description by remember { mutableStateOf(destinationToEdit?.description ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val currentImageUrl = destinationToEdit?.image_url ?: ""

    val isUploading = viewModel.isUploading
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Destinasi") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = currentImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                        color = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Text(
                            "Ketuk untuk ganti foto",
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lokasi") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Harga") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (name.isNotEmpty() && price.isNotEmpty() && destinationToEdit != null) {
                            viewModel.updateDestination(
                                context,
                                destinationToEdit.id,
                                selectedImageUri,
                                currentImageUrl,
                                name,
                                location,
                                if (price.startsWith("Rp")) price else "Rp $price",
                                description
                            ) {
                                Toast.makeText(context, "Updated!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isUploading
                ) {
                    Text(if (isUploading) "Updating..." else "Update")
                }
            }
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}