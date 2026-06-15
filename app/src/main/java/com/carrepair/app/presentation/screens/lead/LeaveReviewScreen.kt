package com.carrepair.app.presentation.screens.lead

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.carrepair.app.domain.viewmodels.ReviewViewModel
import com.carrepair.app.domain.viewmodels.SubmitReviewUiState
import com.carrepair.app.presentation.components.DarkNavHeader
import com.carrepair.app.presentation.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveReviewScreen(
    leadId: Long,
    shopName: String,
    shopId: Long,
    carInfo: String,
    navController: NavController,
    viewModel: ReviewViewModel
) {
    val submitState by viewModel.submitState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    val maxChars = 500

    LaunchedEffect(submitState) {
        when (val state = submitState) {
            is SubmitReviewUiState.Success -> {
                snackbarHostState.showSnackbar("Review submitted successfully!")
                viewModel.resetSubmitState()
                navController.popBackStack()
            }
            is SubmitReviewUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetSubmitState()
            }
            else -> {}
        }
    }

    val todayDate = remember {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date())
    }

    RepaiiroTheme(useDarkTheme = false) {
        Scaffold(
            containerColor = LightBackground,
            topBar = {
                DarkNavHeader(
                    title = "Leave a Review",
                    onBack = { navController.popBackStack() }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(LightBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Shop Header Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NavyDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Completed on $todayDate",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = shopName,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = carInfo,
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Card 1: Experience Rating
                    ReviewSectionCard(title = "How was your experience?") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 1..5) {
                                val isSelected = i <= rating
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Star $i",
                                    tint = if (isSelected) OrangePrimary else Color.LightGray,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clickable { rating = i }
                                )
                            }
                        }
                    }

                    // Card 2: Comments Area
                    ReviewSectionCard(title = "Share your experience (Optional)") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = comment,
                                onValueChange = {
                                    if (it.length <= maxChars) {
                                        comment = it
                                    }
                                },
                                placeholder = {
                                    Text(
                                        text = "Tell us about the quality of work, customer service, timeliness, etc.",
                                        color = TextSubtle,
                                        fontSize = 14.sp
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = TextDark),
                                maxLines = 8,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFF2F4F7),
                                    unfocusedBorderColor = Color(0xFFF2F4F7),
                                    focusedContainerColor = Color(0xFFF9FAFB),
                                    unfocusedContainerColor = Color(0xFFF9FAFB),
                                    cursorColor = OrangePrimary
                                )
                            )

                            Text(
                                text = "${comment.length}/$maxChars characters",
                                color = TextSubtle,
                                fontSize = 11.sp,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }

                    // Tip Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0F5FF))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF2F80ED),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Your review helps others: Share your honest experience to help other car owners make informed decisions.",
                            color = Color(0xFF1E3A8A),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Bottom Submit Button
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.submitReview(
                                leadId = leadId,
                                rating = rating,
                                comment = comment,
                                imageUri = null
                            )
                        },
                        enabled = rating > 0 && submitState !is SubmitReviewUiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            disabledContainerColor = Color.LightGray
                        )
                    ) {
                        if (submitState is SubmitReviewUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Submit Review",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ReviewSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = LightSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, LightBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            content()
        }
    }
}
