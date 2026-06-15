package com.carrepair.app.presentation.screens.shop.quotes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carrepair.app.data.RetrofitClient
import com.carrepair.app.data.dto.quote.ShopQuoteResponse
import com.carrepair.app.domain.viewmodels.quotes.ShopQuotesViewModel
import com.carrepair.app.domain.viewmodels.quotes.ShopQuotesViewModelFactory
import androidx.navigation.NavController

@Composable
fun MyQuotesScreen(
    onNavigateBack: () -> Unit,
    navController: NavController,
    viewModel: ShopQuotesViewModel = viewModel(factory = ShopQuotesViewModelFactory(RetrofitClient.repairShopApi))
) {
    val quotes by viewModel.quotes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val filters = listOf("All", "Pending", "Accepted", "Rejected")

    LaunchedEffect(Unit) { viewModel.loadQuotes() }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1B2A))
                    .padding(horizontal = 20.dp, vertical = 28.dp)
            ) {
                Column {
                    Text(
                        text = "My Quotes",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Track submitted quotes",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            // Filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = viewModel.selectedFilter == filter
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) Color(0xFFE86A2E) else Color.White,
                        shadowElevation = if (isSelected) 0.dp else 2.dp,
                        modifier = Modifier.clickable { viewModel.setFilter(filter) }
                    ) {
                        Text(
                            text = filter,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                            color = if (isSelected) Color.White else Color(0xFF333333),
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFE86A2E))
                    }
                }
                error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = error ?: "Something went wrong", color = Color.Red)
                    }
                }
                quotes.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No quotes found", color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(quotes) { quote ->
                            QuoteCard(
                                quote = quote,
                                onClick = { navController.navigate("shop/quote-detail/${quote.quoteId}/${quote.leadId}") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteCard(
    quote: ShopQuoteResponse,
    onClick: () -> Unit = {}
) {
    val statusColor = when (quote.status) {
        "PENDING" -> Color(0xFFF59E0B)
        "ACCEPTED" -> Color(0xFF10B981)
        "REJECTED" -> Color(0xFF6B7280)
        else -> Color.Gray
    }

    val iconBgColor = when (quote.status) {
        "ACCEPTED" -> Color(0xFF10B981)
        else -> Color(0xFFE8EAF0)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable{onClick()},
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(iconBgColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (quote.status == "ACCEPTED")
                            Icons.Default.CheckCircle else Icons.Default.Description,
                        contentDescription = null,
                        tint = if (quote.status == "ACCEPTED") Color.White else Color(0xFF6B7280),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Category tag — lead title se pehla word
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFF3E0)
                        ) {
                            Text(
                                text = quote.leadTitle.uppercase(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE86A2E)
                            )
                        }
                        // Status tag
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = statusColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = quote.status,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${quote.carMake} ${quote.carModel} ${quote.carYear}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1A1A2E)
                    )
                    Text(
                        text = quote.carOwnerName,
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$ ${quote.price}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFFE86A2E)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Divider(color = Color(0xFFF0F0F0))

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SUBMITTED ${quote.createdAt.take(10).uppercase()}",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF),
                letterSpacing = 0.5.sp
            )
        }
    }
}