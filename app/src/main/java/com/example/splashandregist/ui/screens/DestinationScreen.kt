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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import com.example.splashandregist.viewmodel.Destinations
import com.example.splashandregist.viewmodel.DestinationViewModel

/* ================= ROOT SCREEN ================= */
@Composable
fun DestinationScreen() {
    val navController = rememberNavController()
    val viewModel: DestinationViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()

    NavHost(
        navController = navController,
        startDestination = "destination_list"
    ) {
        composable("destination_list") {
            DestinationListScreen(navController, viewModel)
        }
        composable("add_destination") {
            AddDestinationScreen(navController, viewModel)
        }
        composable("destination_detail/{id}") {
            val id = it.arguments?.getString("id")
            val destination = viewModel.getDestinationById(id)
            if (destination != null) {
                DestinationDetailScreen(navController, destination)
            }
        }
        composable("edit_destination/{id}") {
            val id = it.arguments?.getString("id")
            if (id != null) {
                EditDestinationScreen(navController, viewModel, id)
            }
        }
    }
}

/* ================= LIST ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationListScreen(
    navController: NavController,
    viewModel: DestinationViewModel
) {
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
                onClick = { navController.navigate("add_destination") }
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            AsyncImage(
                model = destination.image_url,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(16.dp)) {
                Text(destination.name, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(destination.location, color = Color.Gray)
                }
                Spacer(Modifier.height(8.dp))
                Text(destination.price, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/* ================= ADD ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDestinationScreen(
    navController: NavController,
    viewModel: DestinationViewModel
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { imageUri = it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Destinasi") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                    .clickable {
                        launcher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Pilih Gambar", color = Color.Gray)
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(name, { name = it }, label = { Text("Nama") })
            OutlinedTextField(location, { location = it }, label = { Text("Lokasi") })
            OutlinedTextField(
                price, { price = it },
                label = { Text("Harga") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(description, { description = it }, label = { Text("Deskripsi") })

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (imageUri != null && name.isNotEmpty()) {
                        viewModel.uploadImageAndSaveDestination(
                            context, imageUri!!, name, location, price, description
                        ) {
                            Toast.makeText(context, "Berhasil", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    }
                }
            ) {
                Text("Simpan")
            }
        }
    }
}

/* ================= DETAIL ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationDetailScreen(
    navController: NavController,
    destination: Destinations
) {
    Scaffold(
        bottomBar = {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = {
                    navController.navigate("edit_destination/${destination.id}")
                }
            ) {
                Text("Edit Destinasi")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = destination.image_url,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(300.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(16.dp)) {
                Text(destination.name, fontWeight = FontWeight.Bold)
                Text(destination.location)
                Spacer(Modifier.height(8.dp))
                Text(destination.price, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text(destination.description)
            }
        }
    }
}

/* ================= EDIT ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDestinationScreen(
    navController: NavController,
    viewModel: DestinationViewModel,
    id: String
) {
    val destination = viewModel.getDestinationById(id) ?: return
    val context = LocalContext.current

    var name by remember { mutableStateOf(destination.name) }
    var location by remember { mutableStateOf(destination.location) }
    var price by remember { mutableStateOf(destination.price) }
    var description by remember { mutableStateOf(destination.description) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Destinasi") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(name, { name = it }, label = { Text("Nama") })
            OutlinedTextField(location, { location = it }, label = { Text("Lokasi") })
            OutlinedTextField(price, { price = it }, label = { Text("Harga") })
            OutlinedTextField(description, { description = it }, label = { Text("Deskripsi") })

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.updateDestination(
                        context,
                        destination.id,
                        null,
                        destination.image_url,
                        name,
                        location,
                        price,
                        description
                    ) {
                        Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                        navController.popBackStack()
                    }
                }
            ) {
                Text("Update")
            }
        }
    }
}
