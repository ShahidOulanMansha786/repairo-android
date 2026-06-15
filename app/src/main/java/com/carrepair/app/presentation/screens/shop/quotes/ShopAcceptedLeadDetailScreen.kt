package com.carrepair.app.presentation.screens.shop.quotes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.carrepair.app.data.apis.LeadApi
import com.carrepair.app.domain.viewmodels.quotes.AcceptedLeadDetailViewModel
import com.carrepair.app.domain.viewmodels.quotes.AcceptedLeadDetailViewModelFactory
import com.carrepair.app.presentation.components.StatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopAcceptedLeadDetailScreen(
    leadId: Long,
    onBack: () -> Unit,
    leadApi: LeadApi
) {
    val viewModel: AcceptedLeadDetailViewModel = viewModel(
        factory = AcceptedLeadDetailViewModelFactory(leadApi, leadId)
    )
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF5F6FA),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyDark)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
                Text(
                    "Lead Details",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }

            error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(48.dp))
                        Text(text = "Failed to load details", color = Color(0xFF757575))
                        TextButton(onClick = { viewModel.fetchDetail() }) {
                            Text("Retry", color = OrangeAccent)
                        }
                    }
                }
            }

            detail != null -> {
                val d = detail!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Lead Info Card ──
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Tags row
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                LeadTagChip(text = d.carMake, bgColor = Color(0xFFFFF3E0), textColor = Color(0xFFE65100))
                                LeadTagChip(text = "Unlocked", bgColor = Color(0xFFE8F5E9), textColor = Color(0xFF2E7D32))
                            }

                            Spacer(Modifier.height(12.dp))

                            // Title
                            Text(
                                "${d.carMake} ${d.carModel} ${d.carYear}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1A1A2E)
                            )

                            Spacer(Modifier.height(8.dp))

                            // Description
                            Text(
                                d.description,
                                fontSize = 14.sp,
                                color = Color(0xFF555555),
                                lineHeight = 21.sp
                            )

                            Spacer(Modifier.height(14.dp))

                            // Location + Date row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        null,
                                        tint = Color(0xFF9E9E9E),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        d.address,
                                        fontSize = 13.sp,
                                        color = Color(0xFF9E9E9E),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 180.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ── Damage Photos Card ──
                    if (d.imageUrls.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(OrangeAccent.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.PhotoCamera,
                                                null,
                                                tint = OrangeAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Text(
                                            "Damage Photos",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1A1A2E)
                                        )
                                    }
                                    Surface(
                                        color = OrangeAccent.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text(
                                            "${d.imageUrls.size} photo${if (d.imageUrls.size > 1) "s" else ""}",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontSize = 12.sp,
                                            color = OrangeAccent,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    "Review these photos to assess the damage before submitting your quote",
                                    fontSize = 13.sp,
                                    color = Color(0xFF9E9E9E),
                                    lineHeight = 18.sp
                                )

                                Spacer(Modifier.height(14.dp))

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    itemsIndexed(d.imageUrls) { index, url ->
                                        Box(
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                        ) {
                                            AsyncImage(
                                                model = url,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            // Number badge
                                            Box(
                                                modifier = Modifier
                                                    .padding(6.dp)
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Black.copy(alpha = 0.6f))
                                                    .align(Alignment.TopStart),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "${index + 1}",
                                                    fontSize = 11.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Customer Contact Card ──
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Customer Contact",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A2E)
                            )

                            Spacer(Modifier.height(16.dp))

                            ContactRow(
                                label = "Name",
                                value = d.customerContact.name,
                                icon = null,
                                valueColor = Color(0xFF1A1A2E),
                                isClickable = false,
                                onClick = {}
                            )

                            Divider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 12.dp))

                            ContactRow(
                                label = "Phone",
                                value = d.customerContact.phone ?: "—",
                                icon = Icons.Default.Phone,
                                valueColor = OrangeAccent,
                                isClickable = d.customerContact.phone != null,
                                onClick = {}
                            )

                            Divider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 12.dp))

                            ContactRow(
                                label = "Email",
                                value = d.customerContact.email,
                                icon = Icons.Default.Email,
                                valueColor = OrangeAccent,
                                isClickable = true,
                                onClick = {}
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun LeadTagChip(text: String, bgColor: Color, textColor: Color) {
    Surface(color = bgColor, shape = RoundedCornerShape(20.dp)) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun ContactRow(
    label: String,
    value: String,
    icon: ImageVector?,
    valueColor: Color,
    isClickable: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(icon, null, tint = Color(0xFF9E9E9E), modifier = Modifier.size(15.dp))
            }
            Text(label, fontSize = 14.sp, color = Color(0xFF9E9E9E))
        }
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            modifier = if (isClickable) Modifier.clickable(onClick = onClick) else Modifier
        )
    }
}