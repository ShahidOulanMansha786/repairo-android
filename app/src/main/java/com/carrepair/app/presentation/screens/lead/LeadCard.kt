package com.carrepair.app.presentation.screens.lead

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.carrepair.app.presentation.ui.theme.NavyDark as ThemeNavyDark
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.carrepair.app.data.dto.lead.LeadResponseDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// ==================== Design Tokens ====================

private val OrangePrimary = Color(0xFFFF6835)
private val NavyDark     = ThemeNavyDark
private val TextPrimary  = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF666666)
private val TextHint     = Color(0xFF999999)
private val BorderColor  = Color.White
private val BackgroundPage = Color.White
private val White        = Color(0xFF111827)

// ==================== Existing Helper Functions (unchanged) ====================

fun timeAgo(isoTimestamp: String): String {
    return try {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val dateTime = LocalDateTime.parse(isoTimestamp, formatter)
        val now = LocalDateTime.now()
        val minutes = ChronoUnit.MINUTES.between(dateTime, now)
        val hours = ChronoUnit.HOURS.between(dateTime, now)
        val days = ChronoUnit.DAYS.between(dateTime, now)
        when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes minutes ago"
            hours < 24 -> "$hours hours ago"
            days == 1L -> "Yesterday"
            else -> "$days days ago"
        }
    } catch (e: Exception) {
        isoTimestamp
    }
}

@Composable
fun StatusBadge(status: String) {
    val backgroundColor = when (status.uppercase()) {
        "OPEN"      -> Color(0xFF4CAF50)
        "CANCELLED" -> Color(0xFFF44336)
        "CLOSED"    -> Color(0xFF9E9E9E)
        else        -> Color(0xFF9E9E9E)
    }
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status,
            color = White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun LeadCard(
    lead: LeadResponseDto,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (lead.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = lead.imageUrls.first(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = lead.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
                Text(
                    text = "${lead.carMake} ${lead.carModel} (${lead.carYear})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                StatusBadge(status = lead.status)
                Text(
                    text = timeAgo(lead.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==================== New Lead Creation UI (Figma consistent) ====================

data class LeadFormData(
    val carMake: String = "",
    val carModel: String = "",
    val year: String = "",
    val repairCategory: String = "",
    val issueDescription: String = "",
    val photoUris: List<Any> = emptyList(),
    val location: String = ""
)

// ---- Shared Header ----

@Composable
fun CreateLeadHeader(
    currentStep: Int,
    totalSteps: Int = 3,
    onBack: () -> Unit
) {
    Column {
        // Navy bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyDark)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Back arrow
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = White,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(22.dp)
                    .clickable { onBack() }
            )
            // Title centered
            Text(
                text = "Create Repair Lead",
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )
            // Step indicator right
            Text(
                text = "Step $currentStep of $totalSteps",
                color = White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        // Progress bar row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyDark)
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Left label
            Text(
                text = "Vehicle Information",
                color = White.copy(alpha = 0.70f),
                fontSize = 11.sp
            )
            // Progress track (fills remaining space)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(White.copy(alpha = 0.20f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = currentStep.toFloat() / totalSteps.toFloat())
                        .clip(RoundedCornerShape(2.dp))
                        .background(OrangePrimary)
                )
            }
        }
    }
}

// ---- Figma text-field colours helper ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun figmaTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = OrangePrimary,
    unfocusedBorderColor = BorderColor,
    focusedLabelColor    = OrangePrimary,
    cursorColor          = OrangePrimary
)

// ---- Main Screen ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLeadScreen(
    currentStep: Int = 1,
    formData: LeadFormData,
    onFormDataChange: (LeadFormData) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onSubmitLead: () -> Unit,
    onAddPhotos: () -> Unit,
    onRemovePhoto: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = BackgroundPage,
        topBar = {
            CreateLeadHeader(
                currentStep = currentStep,
                onBack = onPreviousStep
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            when (currentStep) {
                1 -> LeadStep1(
                    carMake = formData.carMake,
                    onCarMakeChange = { onFormDataChange(formData.copy(carMake = it)) },
                    carModel = formData.carModel,
                    onCarModelChange = { onFormDataChange(formData.copy(carModel = it)) },
                    year = formData.year,
                    onYearChange = { onFormDataChange(formData.copy(year = it)) },
                    selectedCategory = formData.repairCategory,
                    onCategorySelected = { onFormDataChange(formData.copy(repairCategory = it)) },
                    onContinue = onNextStep
                )
                2 -> LeadStep2(
                    description = formData.issueDescription,
                    onDescriptionChange = { onFormDataChange(formData.copy(issueDescription = it)) },
                    photos = formData.photoUris,
                    onAddPhotos = onAddPhotos,
                    onRemovePhoto = onRemovePhoto,
                    onBack = onPreviousStep,
                    onContinue = onNextStep
                )
                3 -> LeadStep3(
                    location = formData.location,
                    onLocationChange = { onFormDataChange(formData.copy(location = it)) },
                    onBack = onPreviousStep,
                    onSubmit = onSubmitLead
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==================== Step 1 ====================

private data class RepairCategoryItem(
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadStep1(
    carMake: String,
    onCarMakeChange: (String) -> Unit,
    carModel: String,
    onCarModelChange: (String) -> Unit,
    year: String,
    onYearChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onContinue: () -> Unit
) {
    val categories = listOf(
        RepairCategoryItem("Body Damage",  Icons.Default.DirectionsCar),
        RepairCategoryItem("Mechanical",   Icons.Default.Settings),
        RepairCategoryItem("Electrical",   Icons.Default.FlashOn),
        RepairCategoryItem("Paint",        Icons.Default.Brush),
        RepairCategoryItem("Interior",     Icons.Default.AirlineSeatReclineNormal)
    )

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

        // Section heading
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Vehicle Information",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Tell us about your vehicle to get started",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        // Car Make
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Car Make", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            OutlinedTextField(
                value = carMake,
                onValueChange = onCarMakeChange,
                placeholder = { Text("e.g., Toyota", color = Color(0xFFCCCCCC)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = figmaTextFieldColors(),
                trailingIcon = {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null,
                        tint = TextHint, modifier = Modifier.size(18.dp))
                },
                singleLine = true
            )
        }

        // Car Model
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Car Model", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            OutlinedTextField(
                value = carModel,
                onValueChange = onCarModelChange,
                placeholder = { Text("e.g., Camry", color = Color(0xFFCCCCCC)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = figmaTextFieldColors(),
                trailingIcon = {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null,
                        tint = TextHint, modifier = Modifier.size(18.dp))
                },
                singleLine = true
            )
        }

        // Year
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Year", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            OutlinedTextField(
                value = year,
                onValueChange = onYearChange,
                placeholder = { Text("e.g., 2022", color = Color(0xFFCCCCCC)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = figmaTextFieldColors(),
                trailingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = null,
                        tint = TextHint, modifier = Modifier.size(18.dp))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }

        // Repair Category
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Repair Category",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            // 2-column grid — last item (Interior) centred if odd count
            val rows = categories.chunked(2)
            rows.forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowItems.forEach { item ->
                        CategoryCard(
                            item = item,
                            isSelected = selectedCategory == item.label,
                            onSelect = { onCategorySelected(item.label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // If odd row, add an invisible spacer to keep the single card half-width
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Continue button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangePrimary,
                disabledContainerColor = Color(0xFFCCCCCC)
            ),
            enabled = carMake.isNotBlank() && carModel.isNotBlank()
                    && year.isNotBlank() && selectedCategory.isNotBlank()
        ) {
            Text(
                text = "Continue →",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = White
            )
        }
    }
}

@Composable
private fun CategoryCard(
    item: RepairCategoryItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) OrangePrimary else BorderColor
    val bgColor     = if (isSelected) OrangePrimary.copy(alpha = 0.06f) else White
    val iconTint    = if (isSelected) OrangePrimary else TextSecondary
    val textColor   = if (isSelected) OrangePrimary else TextPrimary
    val borderWidth = if (isSelected) 1.5.dp else 1.dp

    Surface(
        modifier = modifier
            .height(80.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = if (isSelected) 0.dp else 1.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}

// ==================== Step 2 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadStep2(
    description: String,
    onDescriptionChange: (String) -> Unit,
    photos: List<Any>,
    onAddPhotos: () -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

        // Section heading
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Describe the Issue",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Provide details to help shops understand the problem.",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        // Description field
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(
                text = "Issue Description",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 500) onDescriptionChange(it) },
                placeholder = { Text("Enter details about the issue...", color = Color(0xFFCCCCCC)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(10.dp),
                colors = figmaTextFieldColors(),
                maxLines = 6
            )
            // Bottom hint row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "min. 20 characters",
                    fontSize = 11.sp,
                    color = TextHint
                )
                Text(
                    text = "${description.length} / 500 characters",
                    fontSize = 11.sp,
                    color = if (description.length == 500) Color(0xFFFF5722) else TextHint
                )
            }
        }

        // Photos section
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Photos (${photos.size}/5)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "Optional",
                    fontSize = 12.sp,
                    color = TextHint
                )
            }

            // Existing photos row
            if (photos.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(photos.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF5F5F5))
                        ) {
                            val photo = photos[index]
                            when (photo) {
                                is String -> AsyncImage(
                                    model = photo,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                else -> Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(20.dp),
                                    tint = Color(0xFFCCCCCC)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove photo",
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .clickable { onRemovePhoto(index) }
                                    .padding(2.dp),
                                tint = White
                            )
                        }
                    }
                }
            }

            // Large "Add Photos" dashed box (Figma style)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clickable(enabled = photos.size < 5) { onAddPhotos() },
                shape = RoundedCornerShape(12.dp),
                color = OrangePrimary.copy(alpha = 0.04f),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (photos.size < 5) OrangePrimary.copy(alpha = 0.50f) else BorderColor
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = OrangePrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Add Photos",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OrangePrimary
                    )
                }
            }

            // Helper text
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TextHint,
                    modifier = Modifier
                        .size(14.dp)
                        .padding(top = 1.dp)
                )
                Text(
                    text = "Upload clear photos of the damage or issue. " +
                            "Good lighting and multiple angles help us provide a more accurate estimate.",
                    fontSize = 12.sp,
                    color = TextHint,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Back + Continue
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Text(
                    text = "Back",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .weight(2f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    disabledContainerColor = Color(0xFFCCCCCC)
                ),
                enabled = description.length >= 20
            ) {
                Text(
                    text = "Continue →",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = White
                )
            }
        }
    }
}

// ==================== Step 3 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadStep3(
    location: String,
    onLocationChange: (String) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {

        // Section heading
        Text(
            text = "Location",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ---- MAP VIEW — kept exactly as-is from original logic ----
        // (The caller / parent composable renders the map component here.
        //  This placeholder matches the Figma green map block height.)
        // If you have a MapView / AndroidView composable, place it here.
        // Example stub that preserves the same slot shape:
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFB2C9A5))  // fallback colour while map loads
        ) {
            // Your existing MapView / GoogleMap composable goes here — NOT changed.
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Address label + hint
        Text(
            text = "Street Address, City, or ZIP Code",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = TextHint,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "We'll use this to find the nearest certified repair shop.",
                fontSize = 12.sp,
                color = TextHint
            )
        }

        OutlinedTextField(
            value = location,
            onValueChange = onLocationChange,
            placeholder = { Text("e.g. 123 Main St, New York, NY", color = Color(0xFFCCCCCC)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = figmaTextFieldColors(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TextHint,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Submit Lead — full width, no Back button on Step 3 (matches Figma)
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangePrimary,
                disabledContainerColor = Color(0xFFCCCCCC)
            ),
            enabled = location.isNotBlank()
        ) {
            Text(
                text = "Submit Lead",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = White
            )
        }
    }
}
