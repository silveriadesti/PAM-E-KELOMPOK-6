package com.example.splashandregist.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.splashandregist.viewmodel.PromoViewModel
import com.example.splashandregist.viewmodel.PromoViewModel.Promo

// --- ACTIVITY UTAMA ---
class PromoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PromoApp()
            }
        }
    }
}

// --- NAVIGASI ---
@Composable
fun PromoApp() {
    val navController = rememberNavController()
    val viewModel = remember { PromoViewModel() }

    NavHost(navController, startDestination = "list") {
        composable("list") { PromoListScreen(navController, viewModel) }
        composable("add") { AddPromoScreen(navController, viewModel) }
        composable("detail/{id}") {
            PromoDetailScreen(
                navController,
                viewModel,
                it.arguments!!.getString("id")!!.toLong()
            )
        }
        composable("edit/{id}") {
            EditPromoScreen(
                navController,
                viewModel,
                it.arguments!!.getString("id")!!.toLong()
            )
        }
    }
}

/* ================= LIST ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoListScreen(navController: NavController, viewModel: PromoViewModel) {
    LaunchedEffect(Unit) { viewModel.getPromos() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Promo Hotel", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Promo", tint = Color.White)
            }
        }
    ) { padding ->

        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 16.dp,
                bottom = padding.calculateBottomPadding() + 80.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.promos) { promo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("detail/${promo.id}") },
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column {
                        promo.imageUrl?.let {
                            AsyncImage(
                                model = it,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                promo.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Diskon ${promo.discount}%",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}



/* ================= DETAIL ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoDetailScreen(
    navController: NavController,
    viewModel: PromoViewModel,
    promoId: Long
) {
    var promo by remember { mutableStateOf<Promo?>(null) }

    LaunchedEffect(promoId) { viewModel.getPromoDetail(promoId) { promo = it } }

    if (promo == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Promo", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("edit/$promoId") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
            }
        }
    ) { padding ->

        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 16.dp,
                bottom = padding.calculateBottomPadding() + 80.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box {
                    promo!!.imageUrl?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = "Gambar Promo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Promo Spesial!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            item {
                Text(promo!!.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            item {
                Text("Diskon: ${promo!!.discount}%", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            }

            item {
                Text(promo!!.description, fontSize = 16.sp, lineHeight = 20.sp, color = Color.Black)
            }

            promo!!.terms?.let { terms ->
                item {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    Spacer(Modifier.height(8.dp))
                    Text("Syarat & Ketentuan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        terms,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}


/* ================= ADD ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPromoScreen(navController: NavController, viewModel: PromoViewModel) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var terms by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { imageUri = it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Promo", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // IMAGE PICKER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clickable {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    .border(width = if (imageUri == null) 1.5.dp else 0.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Tambah Gambar Promo", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                        Text("Klik untuk memilih gambar", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }

            Spacer(Modifier.height(24.dp))

            // FORM
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Promo") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = discount, onValueChange = { discount = it }, label = { Text("Diskon (%)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = terms, onValueChange = { terms = it }, label = { Text("Syarat & Ketentuan") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    viewModel.savePromo(context, imageUri, title, desc, discount.toIntOrNull() ?: 0, terms = terms) {
                        Toast.makeText(context, "Promo berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Simpan Promo", color = Color.White)
            }
        }
    }
}

/* ================= EDIT ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPromoScreen(navController: NavController, viewModel: PromoViewModel, promoId: Long) {
    val context = LocalContext.current
    var promo by remember { mutableStateOf<Promo?>(null) }

    LaunchedEffect(promoId) { viewModel.getPromoDetail(promoId) { promo = it } }

    if (promo == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var title by remember { mutableStateOf(promo!!.title) }
    var desc by remember { mutableStateOf(promo!!.description) }
    var discount by remember { mutableStateOf(promo!!.discount.toString()) }
    var terms by remember { mutableStateOf(promo!!.terms ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { imageUri = it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Promo", color = Color.White) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // IMAGE PICKER
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(model = imageUri ?: promo!!.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ganti Gambar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Promo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            OutlinedTextField(value = discount, onValueChange = { discount = it }, label = { Text("Diskon (%)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = terms, onValueChange = { terms = it }, label = { Text("Syarat & Ketentuan") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.updatePromo(context, promoId, title, desc, discount.toIntOrNull() ?: 0, imageUri, promo!!.imageUrl, terms = terms) {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Update Promo", color = Color.White)
            }
        }
    }
}
