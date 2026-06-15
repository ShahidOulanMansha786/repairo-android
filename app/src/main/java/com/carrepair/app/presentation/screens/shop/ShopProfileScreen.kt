package com.carrepair.app.presentation.screens.shop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.carrepair.app.data.dto.ReviewResponseDto
import com.carrepair.app.domain.viewmodels.ReviewViewModel
import com.carrepair.app.presentation.ui.theme.*
import com.carrepair.app.utils.resolveImageUrl
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopProfileScreen(
    shopId: Long,
    shopName: String,
    logoUrl: String?,
    navController: NavController,
    viewModel: ReviewViewModel
) {
    val reviews by viewModel.reviews.collectAsState()
    val isLoading by viewModel.isLoadingReviews.collectAsState()
    val reviewsError by viewModel.reviewsError.collectAsState()

    LaunchedEffect(shopId) {
        viewModel.loadReviewsForShop(shopId)
    }

    val computedAverage = if (reviews.isNotEmpty()) {
        reviews.map { it.rating }.average()
    } else 0.0

    val computedCount = reviews.size

    RepaiiroTheme(useDarkTheme = false) {
        Scaffold(
            containerColor = LightBackground
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF6F7FB))
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OrangePrimary, strokeWidth = 3.dp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        // ── Header ──────────────────────────────────────────
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = 12.dp,
                                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                                        clip = false
                                    )
                                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFF0D1B2A),
                                                Color(0xFF1C3553),
                                                Color(0xFF1F3E63)
                                            )
                                        )
                                    )
                            ) {
                                // Subtle decorative circle top-right
                                Box(
                                    modifier = Modifier
                                        .size(180.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 60.dp, y = (-40).dp)
                                        .background(
                                            Color.White.copy(alpha = 0.04f),
                                            CircleShape
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .align(Alignment.BottomStart)
                                        .offset(x = (-30).dp, y = 30.dp)
                                        .background(
                                            OrangePrimary.copy(alpha = 0.07f),
                                            CircleShape
                                        )
                                )

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Top bar
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .statusBarsPadding()
                                            .padding(horizontal = 8.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { navController.popBackStack() }) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .background(
                                                        Color.White.copy(alpha = 0.1f),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Back",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Shop Profile",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White.copy(alpha = 0.95f),
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.3.sp,
                                            modifier = Modifier.padding(start = 6.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Logo + Name + Rating
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Avatar ring
                                        Box(
                                            modifier = Modifier
                                                .size(94.dp)
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        listOf(OrangePrimary.copy(alpha = 0.7f), Color.White.copy(alpha = 0.2f))
                                                    ),
                                                    shape = CircleShape
                                                )
                                                .padding(3.dp)
                                                .clip(CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF1A2F50))
                                                    .border(2.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                AsyncImage(
                                                    model = resolveImageUrl(logoUrl),
                                                    contentDescription = "$shopName logo",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape)
                                                )
                                            }
                                        }

                                        Text(
                                            text = shopName,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            letterSpacing = 0.2.sp,
                                            modifier = Modifier.padding(horizontal = 32.dp)
                                        )

                                        if (computedCount > 0) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(24.dp))
                                                    .background(Color.White.copy(alpha = 0.12f))
                                                    .border(
                                                        1.dp,
                                                        Color.White.copy(alpha = 0.18f),
                                                        RoundedCornerShape(24.dp)
                                                    )
                                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFC107),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Text(
                                                    text = "%,.1f".format(computedAverage),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(3.dp)
                                                        .background(Color.White.copy(alpha = 0.4f), CircleShape)
                                                )
                                                Text(
                                                    text = "$computedCount ${if (computedCount == 1) "review" else "reviews"}",
                                                    color = Color.White.copy(alpha = 0.85f),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        } else {
                                            Surface(
                                                color = Color.White.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(24.dp),
                                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                                            ) {
                                                Text(
                                                    text = "No reviews yet",
                                                    color = Color.White.copy(alpha = 0.75f),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ── Section Label ────────────────────────────────────
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "CUSTOMER REVIEWS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSubtle,
                                    letterSpacing = 1.4.sp
                                )
                                if (computedCount > 0) {
                                    Text(
                                        text = "$computedCount total",
                                        fontSize = 11.sp,
                                        color = OrangePrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // ── Error ────────────────────────────────────────────
                        if (reviewsError != null) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = reviewsError ?: "Error loading reviews",
                                        color = Color.Red,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // ── Empty State ──────────────────────────────────────
                        else if (reviews.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 60.dp, bottom = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .background(Color(0xFFF0F1F5), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RateReview,
                                            contentDescription = null,
                                            tint = Color(0xFFBCC0CC),
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No Reviews Yet",
                                        color = TextDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Be the first to share your experience!",
                                        color = TextSubtle,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // ── Review Cards ─────────────────────────────────────
                        else {
                            items(reviews) { review ->
                                ReviewItemRow(
                                    review = review,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewItemRow(
    review: ReviewResponseDto,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF0F1F5))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar circle with gradient
                    val firstChar = review.carOwnerName.firstOrNull()?.toString() ?: "U"
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFE0CC), Color(0xFFFFCDAF))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = firstChar.uppercase(),
                            color = OrangePrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = review.carOwnerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextDark,
                            letterSpacing = 0.1.sp
                        )
                        // Star row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 1..5) {
                                val active = i <= review.rating
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (active) Color(0xFFFFC107) else Color(0xFFE5E7EB),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${review.rating}.0",
                                fontSize = 11.sp,
                                color = TextSubtle,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Date chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF6F7FB))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = formatDateTime(review.createdAt),
                        color = TextSubtle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (!review.comment.isNullOrBlank()) {
                // Divider line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFF2F4F7))
                )
                Text(
                    text = review.comment,
                    color = Color(0xFF4A5568),
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.1.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

private fun formatDateTime(dateTimeStr: String): String {
    return try {
        val parsed = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        parsed.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    } catch (e: Exception) {
        dateTimeStr.substringBefore("T")
    }
}