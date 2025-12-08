package com.example.travelapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DestinationApp()
                }
            }
        }
    }
}

@Composable
fun TravelAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2196F3),
            secondary = Color(0xFF03A9F4),
            background = Color(0xFFF5F5F5)
        ),
        content = content
    )
}

// Data Model
data class Destination(
    val id: String = "",
    val title: String = "",
    val location: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val userId: String = "",
    val createdAt: String = ""
)

// Main App
@Composable
fun DestinationApp() {
    // State untuk menyimpan data
    val destinations = remember {
        mutableStateListOf(
            Destination(
                id = "1",
                title = "Pantai Kuta",
                location = "Bali",
                description = "Pantai terkenal dengan sunset yang indah dan ombak yang cocok untuk surfing. Tempat yang sempurna untuk bersantai dan menikmati keindahan alam.",
                price = 50000.0,
                imageUrl = "kuta",
                userId = "user1",
                createdAt = "2024-12-08"
            ),
            Destination(
                id = "2",
                title = "Candi Borobudur",
                location = "Yogyakarta",
                description = "Candi Buddha terbesar di dunia dengan arsitektur yang menakjubkan. Situs warisan dunia UNESCO yang wajib dikunjungi.",
                price = 75000.0,
                imageUrl = "borobudur",
                userId = "user1",
                createdAt = "2024-12-07"
            ),
            Destination(
                id = "3",
                title = "Danau Toba",
                location = "Sumatera Utara",
                description = "Danau vulkanik terbesar di Indonesia dengan pemandangan yang memukau. Udara sejuk dan pemandangan yang sangat indah.",
                price = 100000.0,
                imageUrl = "toba",
                userId = "user1",
                createdAt = "2024-12-06"
            )
        )
    }

    var currentScreen by remember { mutableStateOf("list") }
    var selectedDestinationId by remember { mutableStateOf<String?>(null) }

    when (currentScreen) {
        "list" -> {
            DestinationListScreen(
                destinations = destinations,
                onNavigateToDetail = { id ->
                    selectedDestinationId = id
                    currentScreen = "detail"
                },
                onNavigateToAdd = {
                    currentScreen = "add"
                }
            )
        }
        "detail" -> {
            DestinationDetailScreen(
                destination = destinations.find { it.id == selectedDestinationId },
                onNavigateBack = {
                    currentScreen = "list"
                },
                onDelete = { id ->
                    destinations.removeIf { it.id == id }
                    currentScreen = "list"
                }
            )
        }
        "add" -> {
            AddDestinationScreen(
                onSaveDestination = { newDestination ->
                    val destination = newDestination.copy(
                        id = (destinations.size + 1).toString(),
                        createdAt = System.currentTimeMillis().toString(),
                        userId = "user1"
                    )
                    destinations.add(0, destination)
                    currentScreen = "list"
                },
                onNavigateBack = {
                    currentScreen = "list"
                }
            )
        }
    }
}

// List Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationListScreen(
    destinations: List<Destination>,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAdd: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Destinasi Wisata", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Destinasi", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(destinations) { destination ->
                DestinationCard(
                    destination = destination,
                    onClick = { onNavigateToDetail(destination.id) }
                )
            }
        }
    }
}

@Composable
fun DestinationCard(destination: Destination, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Placeholder image dengan gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2196F3),
                                Color(0xFF03A9F4)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White.copy(alpha = 0.5f)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = destination.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = destination.location,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = destination.description,
                    fontSize = 14.sp,
                    maxLines = 2,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rp ${String.format("%,.0f", destination.price)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

// Detail Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationDetailScreen(
    destination: Destination?,
    onNavigateBack: () -> Unit,
    onDelete: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Destinasi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        destination?.let { dest ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                item {
                    // Placeholder image dengan gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF2196F3),
                                        Color(0xFF03A9F4)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = dest.title,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF2196F3)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = dest.location,
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Deskripsi",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dest.description,
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE3F2FD)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Harga Tiket Masuk",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Rp ${String.format("%,.0f", dest.price)}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2196F3)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Tombol Delete untuk Admin
                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDC3545)
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hapus Destinasi", fontSize = 16.sp)
                        }
                    }
                }
            }

            // Dialog Konfirmasi Hapus
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Hapus Destinasi") },
                    text = { Text("Apakah Anda yakin ingin menghapus destinasi ini?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                dest.id.let { onDelete(it) }
                                showDeleteDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDC3545)
                            )
                        ) {
                            Text("Hapus")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Destinasi tidak ditemukan")
            }
        }
    }
}

// Add Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDestinationScreen(
    onSaveDestination: (Destination) -> Unit,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Destinasi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Destinasi") },
                    placeholder = { Text("Contoh: Pantai Kuta") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) }
                )
            }
            item {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lokasi") },
                    placeholder = { Text("Contoh: Bali") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                )
            }
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    placeholder = { Text("Jelaskan tentang destinasi ini...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 6
                )
            }
            item {
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            price = it
                        }
                    },
                    label = { Text("Harga (Rp)") },
                    placeholder = { Text("50000") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) }
                )
            }
            item {
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL Gambar (opsional)") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) }
                )
                Text(
                    text = "* Nanti akan diintegrasikan dengan Supabase Storage untuk upload gambar",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Button(
                    onClick = {
                        if (title.isNotEmpty() && location.isNotEmpty() &&
                            description.isNotEmpty() && price.isNotEmpty()) {
                            val newDestination = Destination(
                                title = title,
                                location = location,
                                description = description,
                                price = price.toDoubleOrNull() ?: 0.0,
                                imageUrl = imageUrl.ifEmpty { "placeholder" }
                            )
                            onSaveDestination(newDestination)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = title.isNotEmpty() && location.isNotEmpty() &&
                            description.isNotEmpty() && price.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Destinasi", fontSize = 16.sp)
                }
            }
        }
    }
}