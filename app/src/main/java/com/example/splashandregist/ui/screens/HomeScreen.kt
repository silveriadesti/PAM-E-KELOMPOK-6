package com.example.splashandregist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api

private val PrimaryBlue = Color(0xFF2196F3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onDestinasiClick: () -> Unit,
    onEventClick: () -> Unit,
    onHotelClick: () -> Unit,
    onTransportClick: () -> Unit,
    onPromoClick: () -> Unit,
    onBookingClick: () -> Unit,
    onLogoutClick: () -> Unit   // ✅ TAMBAHAN
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F9FF))
    ) {

        /* ===== TOP BAR ===== */
        TopAppBar(
            title = { },
            actions = {
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PrimaryBlue
            )
        )

        /* ===== HEADER ===== */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryBlue)
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.FlightTakeoff,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ButuhHealing.com",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Solusi perjalanan & healing kamu",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        /* ===== CONTENT ===== */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            FeatureCard(
                icon = Icons.Default.Place,
                title = "Destinasi",
                subtitle = "Temukan tempat wisata terbaik",
                onClick = onDestinasiClick
            )

            FeatureCard(
                icon = Icons.Default.Event,
                title = "Event",
                subtitle = "Event menarik di destinasi pilihan",
                onClick = onEventClick
            )

            FeatureCard(
                icon = Icons.Default.Hotel,
                title = "Hotel",
                subtitle = "Penginapan nyaman & terjangkau",
                onClick = onHotelClick
            )

            FeatureCard(
                icon = Icons.Default.DirectionsBus,
                title = "Transport",
                subtitle = "Pilih transport perjalananmu",
                onClick = onTransportClick
            )

            FeatureCard(
                icon = Icons.Default.LocalOffer,
                title = "Promo",
                subtitle = "Diskon & penawaran spesial",
                onClick = onPromoClick
            )

            FeatureCard(
                icon = Icons.Default.ConfirmationNumber,
                title = "Booking",
                subtitle = "Kelola semua pesanan kamu",
                onClick = onBookingClick
            )
        }
    }
}

/* ===== CARD ===== */
@Composable
fun FeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        PrimaryBlue.copy(alpha = 0.15f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = PrimaryBlue
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next",
                tint = Color.Gray
            )
        }
    }
}
