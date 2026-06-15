package com.carrepair.app.presentation.screens.lead

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.carrepair.app.domain.viewmodels.LeadPostingViewModel
import com.carrepair.app.presentation.components.PrimaryButton
import com.carrepair.app.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDescriptionScreen(
    navController: NavController,
    viewModel: LeadPostingViewModel
) {
    val formState by viewModel.formState.collectAsState()
    var description by remember { mutableStateOf(formState.description) }
    val isNextEnabled = description.length >= 20

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.addImageUri(it) }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    var progressTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        progressTarget = 2f / 3f
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
                                    imageVector = Icons.Default.ArrowBack,
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
                                text = "Step 2 of 3",
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
                                text = "Step 2",
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
        ) { paddingValues ->
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(animationSpec = tween(400)) { it / 3 } + fadeIn(animationSpec = tween(400)),
                modifier = Modifier.padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LightBackground)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Describe the Issue",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextDark,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Provide details to help shops understand the problem.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSubtle
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Column {
                        Text(
                            text = "Issue Description",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (it.length <= 500) description = it },
                            placeholder = { Text("Enter details about the issue...") },
                            minLines = 5,
                            maxLines = 8,
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
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "(min. 20 characters)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (description.length < 20) StatusRed else TextSubtle
                            )
                            Text(
                                text = "${description.length} / 500 characters",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSubtle
                            )
                        }
                    }

                    Text(
                        text = "Photos (${formState.imageUris.size}/5)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextDark,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        onClick = { if (formState.imageUris.size < 5) imagePicker.launch(arrayOf("image/*")) },
                        color = OrangeSubtle,
                        border = BorderStroke(1.dp, OrangePrimary),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add Photos",
                                style = MaterialTheme.typography.labelLarge,
                                color = OrangePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (formState.imageUris.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(formState.imageUris) { uri ->
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(LightSurface)
                                        .border(1.dp, LightBorder, RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeImageUri(uri) },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.TopEnd)
                                            .padding(2.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Surface(
                        color = StatusBlueTint,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = StatusBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Upload clear photos of the damage or issue. Good lighting help us provide an accurate estimate.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDark
                            )
                        }
                    }

                    PrimaryButton(
                        text = "Continue →",
                        onClick = {
                            viewModel.updateDescription(description)
                            navController.navigate("post_lead/step3")
                        },
                        enabled = isNextEnabled,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
}
