package com.example.splashandregist.ui.screens

import android.os.Bundle
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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import androidx.core.view.WindowCompat

// --- 1. ACTIVITY OPSIONAL JIKA MAU DIPISAH ---
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

// --- 2. DATA MODEL ---
data class Transport(
    val id: String,
    val name: String,
    val type: String,
    val route: String,
    val capacity: Int,
    val price: String,
    val imageUrl: String,
    val description: String
)

// --- 3. VIEWMODEL ---
class TransportViewModel : ViewModel() {

    private val _list = mutableStateListOf(
        Transport(
            id = "1",
            name = "Bus Trans Jawa",
            type = "Bus",
            route = "Surabaya - Jakarta",
            capacity = 40,
            price = "Rp 350.000",
            imageUrl = "https://asset.kompas.com/crops/iUxhFS5brWKrCA23PJPVQfLoCdw=/0x0:0x0/750x500/data/photo/2023/04/13/6437c7965f630.jpg",
            description = "Bus Eksekutif dengan fasilitas AC, reclining seat, dan toilet."
        ),
        Transport(
            id = "2",
            name = "Kereta Api Argo Bromo",
            type = "Kereta",
            route = "Malang - Jakarta",
            capacity = 100,
            price = "Rp 580.000",
            imageUrl = "https://asset.kompas.com/crops/v823UeJ8V4aHcRUW2KIiMzFxLbc=/0x0:0x0/750x500/data/photo/2023/06/20/64912b40a8fb7.jpg",
            description = "Kereta kelas eksekutif dengan jadwal cepat dan nyaman."
        ),
    )

    val transports: List<Transport> get() = _list

    fun addTransport(t: Transport) {
        _list.add(t)
    }

    fun getTransportById(id: String) = _list.find { it.id == id }

    fun updateTransport(updated: Transport) {
        val index = _list.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            _list[index] = updated
        }
    }

    fun deleteTransport(id: String) {
        _list.removeAll { it.id == id }
    }

}

// --- 4. ROOT SCREEN DENGAN NAV ---
@Composable
fun TransportScreen() {
    val navController = rememberNavController()
    val viewModel = remember { TransportViewModel() }

    NavHost(navController = navController, startDestination = "transport_list") {

        composable("transport_list") {
            TransportListScreen(navController, viewModel)
        }

        composable("add_transport") {
            AddTransportScreen(navController, viewModel)
        }

        composable("transport_detail/{id}") { backStack ->
            val id = backStack.arguments?.getString("id")
            val transport = viewModel.getTransportById(id ?: "")
            if (transport != null) {
                TransportDetailScreen(navController, viewModel, transport)
            }
        }

        composable("edit_transport/{id}") { backStack ->
            val id = backStack.arguments?.getString("id")
            val transport = viewModel.getTransportById(id ?: "")
            if (transport != null) {
                EditTransportScreen(navController, viewModel, transport)
            }
        }

    }
}

// --- 5. LIST SCREEN ---
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
                Icon(Icons.Default.Add, contentDescription = "Tambah Transport", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding)
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
            Column(modifier = Modifier.padding(16.dp)) {
                Text(t.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(t.type, color = Color.Gray)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(t.route, color = Color.Gray)
                    Text(
                        text = "Kapasitas: ${t.capacity} penumpang",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(t.price, style = MaterialTheme.typography.titleMedium, color = Color(0xFF2196F3))
            }
        }
    }
}

// --- 6. ADD SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransportScreen(navController: NavController, viewModel: TransportViewModel) {

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var route by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Transport") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Transport") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Jenis (Bus / Kereta / Pesawat)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(value = route, onValueChange = { route = it }, label = { Text("Rute") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = capacity,
                onValueChange = { capacity = it },
                label = { Text("Kapasitas Penumpang") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))


            OutlinedTextField(
                value = price, onValueChange = { price = it },
                label = { Text("Harga") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("URL Gambar") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        val newTransport = Transport(
                            id = System.currentTimeMillis().toString(),
                            name = name,
                            type = type,
                            route = route,
                            capacity = capacity.toIntOrNull() ?: 0,
                            price = "Rp $price",
                            imageUrl = imageUrl,
                            description = description
                        )
                        viewModel.addTransport(newTransport)
                        Toast.makeText(context, "Transport Ditambahkan!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("Simpan Transport")
            }
        }
    }
}

// --- 7. EDIT TRANSPORT SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransportScreen(navController: NavController, viewModel: TransportViewModel, t: Transport) {

    var name by remember { mutableStateOf(t.name) }
    var type by remember { mutableStateOf(t.type) }
    var route by remember { mutableStateOf(t.route) }
    var capacity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf(t.price.replace("Rp ", "")) }
    var imageUrl by remember { mutableStateOf(t.imageUrl) }
    var description by remember { mutableStateOf(t.description) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Transport") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Transport") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Jenis (Bus/Kereta/Pesawat)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(value = route, onValueChange = { route = it }, label = { Text("Rute") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = capacity,
                onValueChange = { capacity = it },
                label = { Text("Kapasitas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Harga") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("URL Gambar") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val updated = Transport(
                        id = t.id,
                        name = name,
                        type = type,
                        route = route,
                        capacity = capacity.toInt(),
                        price = "Rp $price",
                        imageUrl = imageUrl,
                        description = description
                    )
                    viewModel.updateTransport(updated)
                    Toast.makeText(context, "Transport Berhasil Diedit!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("Simpan Perubahan")
            }
        }
    }
}

// --- 8. DETAIL SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportDetailScreen(navController: NavController, viewModel: TransportViewModel, t: Transport) {

    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Edit
                Button(
                    onClick = { navController.navigate("edit_transport/${t.id}") },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("Edit")
                }

                // Delete
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        viewModel.deleteTransport(t.id)
                        Toast.makeText(context, "Data berhasil dihapus!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Hapus", color = Color.White)
                }
            }
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            Box {
                AsyncImage(
                    model = t.imageUrl,
                    contentDescription = t.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.7f)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.padding(8.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text(t.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))

                Text(t.type, color = Color.Gray)
                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    Text(t.route)
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "Kapasitas",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
                Text(
                    "${t.capacity} penumpang",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(20.dp))
                Text("Harga", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Text(t.price, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

                Spacer(Modifier.height(24.dp))
                Text("Deskripsi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(t.description, lineHeight = 22.sp)
            }
        }
    }
}
