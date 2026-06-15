package com.carrepair.app.presentation.screens.lead

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.carrepair.app.domain.viewmodels.LeadPostingViewModel
import com.carrepair.app.presentation.components.PrimaryButton
import com.carrepair.app.presentation.ui.theme.*

data class CategoryItem(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailsScreen(
    navController: NavController,
    viewModel: LeadPostingViewModel
) {
    val formState by viewModel.formState.collectAsState()

    var title by remember { mutableStateOf(formState.title) }
    var carMake by remember { mutableStateOf(formState.carMake) }
    var carModel by remember { mutableStateOf(formState.carModel) }
    var carYear by remember { mutableStateOf(formState.carYear) }

    // Category Options
    val categories = remember {
        listOf(
            CategoryItem("BODY DAMAGE", Icons.Default.DirectionsCar),
            CategoryItem("MECHANICAL", Icons.Default.Build),
            CategoryItem("ELECTRICAL", Icons.Default.FlashOn),
            CategoryItem("PAINT", Icons.Default.Brush),
            CategoryItem("INTERIOR", Icons.Default.Weekend)
        )
    }

    val isNextEnabled = carMake.isNotBlank() &&
            carModel.isNotBlank() &&
            carYear.isNotBlank() &&
            title.isNotBlank()

    // Screen Entrance Animation Trigger
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    // Step Progress Animation
    var progressTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        progressTarget = 1f / 3f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(500),
        label = "ProgressAnimation"
    )

    RepaiiroTheme(useDarkTheme = false) {
        Scaffold(
            containerColor = LightBackground,
            topBar = {
                Surface(
                    color = NavyDark,
                    shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(bottom = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp)
                        ) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Text(
                                text = "Create Repair Lead",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Center)
                            )
                            Text(
                                text = "Step 1 of 3",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 16.dp)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp, top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Step 1",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedProgress)
                                        .fillMaxHeight()
                                        .background(OrangePrimary)
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(animationSpec = tween(400)) { it / 3 } + fadeIn(animationSpec = tween(400)),
                modifier = Modifier.padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LightBackground)
                ) {
                    // CONTENT & GRID
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header Text Span
                        item(span = { GridItemSpan(2) }) {
                            Column {
                                Text(
                                    text = "Vehicle Information",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = TextDark,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tell us about your vehicle to get started",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSubtle
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // Inputs
                        item(span = { GridItemSpan(2) }) {
                            Column {
                                Text(
                                    text = "Car Make",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = carMake,
                                    onValueChange = { carMake = it },
                                    placeholder = { Text("e.g., Toyota") },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = TextMuted
                                        )
                                    },
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.small,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextDark,
                                        unfocusedTextColor = TextDark,
                                        focusedPlaceholderColor = TextMuted,
                                        unfocusedPlaceholderColor = TextMuted,
                                        focusedBorderColor = OrangePrimary,
                                        unfocusedBorderColor = LightBorder,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        item(span = { GridItemSpan(2) }) {
                            Column {
                                Text(
                                    text = "Car Model",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = carModel,
                                    onValueChange = { carModel = it },
                                    placeholder = { Text("e.g., Camry") },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = TextMuted
                                        )
                                    },
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.small,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextDark,
                                        unfocusedTextColor = TextDark,
                                        focusedPlaceholderColor = TextMuted,
                                        unfocusedPlaceholderColor = TextMuted,
                                        focusedBorderColor = OrangePrimary,
                                        unfocusedBorderColor = LightBorder,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        item(span = { GridItemSpan(2) }) {
                            Column {
                                Text(
                                    text = "Year",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = carYear,
                                    onValueChange = { carYear = it },
                                    placeholder = { Text("e.g., 2022") },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = TextMuted
                                        )
                                    },
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.small,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextDark,
                                        unfocusedTextColor = TextDark,
                                        focusedPlaceholderColor = TextMuted,
                                        unfocusedPlaceholderColor = TextMuted,
                                        focusedBorderColor = OrangePrimary,
                                        unfocusedBorderColor = LightBorder,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        item(span = { GridItemSpan(2) }) {
                            Column {
                                Text(
                                    text = "What is the primary issue?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    placeholder = { Text("e.g., Front bumper repair") },
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.small,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextDark,
                                        unfocusedTextColor = TextDark,
                                        focusedPlaceholderColor = TextMuted,
                                        unfocusedPlaceholderColor = TextMuted,
                                        focusedBorderColor = OrangePrimary,
                                        unfocusedBorderColor = LightBorder,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = "Repair Category",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextDark,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(categories) { category ->
                            val isSelected = formState.category == category.name
                            Surface(
                                onClick = { viewModel.updateCategory(category.name) },
                                color = if (isSelected) OrangeSubtle else Color.White,
                                shape = MaterialTheme.shapes.medium,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) OrangePrimary else LightBorder
                                ),
                                modifier = Modifier
                                    .height(100.dp)
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) OrangePrimary else TextSubtle,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) OrangePrimary else TextDark,
                                        textAlign = TextAlign.Center,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        item(span = { GridItemSpan(2) }) {
                            Spacer(modifier = Modifier.height(16.dp))
                            PrimaryButton(
                                text = "Continue →",
                                onClick = {
                                    viewModel.updateCarDetails(carMake, carModel, carYear, title)
                                    navController.navigate("post_lead/step2")
                                },
                                enabled = isNextEnabled,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
