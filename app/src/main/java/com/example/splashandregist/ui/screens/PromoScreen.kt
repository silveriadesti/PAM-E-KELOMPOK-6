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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.splashandregist.data.model.Promo
import com.example.splashandregist.viewmodel.PromoViewModel

/* ================= ROOT PROMO SCREEN ================= */
@Composable
fun PromoScreen() {
    val navController = rememberNavController()
    val viewModel: PromoViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "promo_list"
    ) {
        composable("promo_list") {
            PromoDashboardScreen(navController, viewModel)
        }
        composable("add_promo") {
            AddPromoScreen(navController, viewModel)
        }
        composable("promo_detail/{id}") {
            PromoDetailScreen(
                navController = navController,
                viewModel = viewModel,
                promoId = it.arguments?.getString("id")
            )
        }
    }
}

/* ================= 1. PROMO LIST ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoDashboardScreen(
    navController: NavController,
    viewModel: PromoViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.getPromos()
    }

    val promos = viewModel.promos
    val isUploading by viewModel.isUploading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Promo Dashboard", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_promo") }
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        if (promos.isEmpty() && !isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada promo")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(promos) { promo ->
                    PromoCard(promo) {
                        promo.id?.let {
                            navController.navigate("promo_detail/$it")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PromoCard(promo: Promo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column {
            AsyncImage(
                model = promo.imageUrl,
                contentDescription = promo.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(16.dp)) {
                Text(promo.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Diskon ${promo.discount}%",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/* ================= 2. ADD PROMO ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPromoScreen(
    navController: NavController,
    viewModel: PromoViewModel
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var terms by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val isUploading by viewModel.isUploading

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { imageUri = it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Promo", color = Color.White) },
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

            PromoImagePickerBox(imageUri) {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(title, { title = it }, label = { Text("Judul Promo") })
            OutlinedTextField(description, { description = it }, label = { Text("Deskripsi") }, minLines = 3)
            OutlinedTextField(discount, { discount = it }, label = { Text("Diskon (%)") })
            OutlinedTextField(terms, { terms = it }, label = { Text("Syarat & Ketentuan") }, minLines = 3)

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading,
                onClick = {
                    if (imageUri == null) {
                        Toast.makeText(context, "Pilih gambar", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.uploadImageAndSavePromo(
                        context,
                        imageUri!!,
                        title,
                        description,
                        discount.toIntOrNull() ?: 0,
                        terms
                    ) {
                        Toast.makeText(context, "Promo ditambahkan", Toast.LENGTH_SHORT).show()
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

/* ================= IMAGE PICKER ================= */
@Composable
fun PromoImagePickerBox(imageUri: Uri?, onClick: () -> Unit) {
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

/* ================= 3. PROMO DETAIL ================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoDetailScreen(
    navController: NavController,
    viewModel: PromoViewModel,
    promoId: String?
) {
    val promo = viewModel.getPromoById(promoId ?: "")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(promo?.title ?: "Detail Promo", color = Color.White) },
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
        if (promo == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Promo tidak ditemukan")
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = promo.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(16.dp)) {
                Text(promo.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Diskon ${promo.discount}%", color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text(promo.description)
                Spacer(Modifier.height(16.dp))
                Text(promo.terms ?: "-")
            }
        }
    }
}
