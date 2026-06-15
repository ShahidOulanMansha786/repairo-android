package com.carrepair.app.presentation.screens.shop

import android.location.Geocoder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carrepair.app.domain.viewmodels.ShopRegistrationViewModel
import com.carrepair.app.presentation.components.AuthInputField
import com.carrepair.app.presentation.components.PrimaryButton
import com.carrepair.app.presentation.ui.theme.LightBackground
import com.carrepair.app.presentation.ui.theme.LightBorder
import com.carrepair.app.presentation.ui.theme.LightSurface
import com.carrepair.app.presentation.ui.theme.OrangePrimary
import com.carrepair.app.presentation.ui.theme.RepaiiroTheme
import com.carrepair.app.presentation.ui.theme.TextDark
import com.carrepair.app.presentation.ui.theme.TextSubtle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDetailsScreen(
    viewModel: ShopRegistrationViewModel,
    navController: NavController,
    onNext: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var shopName       by remember { mutableStateOf(formState.shopName) }
    var description    by remember { mutableStateOf(formState.description) }
    var address        by remember { mutableStateOf(formState.address) }
    var searchQuery    by remember { mutableStateOf("") }
    var searchError    by remember { mutableStateOf(false) }
    var markerPosition by remember {
        mutableStateOf(
            if (formState.latitude != 0.0 && formState.longitude != 0.0)
                LatLng(formState.latitude, formState.longitude)
            else null
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(30.3753, 69.3451), 5f)
    }

    val isNextEnabled = shopName.isNotBlank() && markerPosition != null

    // Geocodes a search query and moves the camera to the result
    fun searchLocation() {
        if (searchQuery.isBlank()) return
        searchError = false

        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val results = geocoder.getFromLocationName(searchQuery, 1)

            if (results.isNullOrEmpty()) {
                searchError = true
                return
            }

            val result = results.first()
            val latLng = LatLng(result.latitude, result.longitude)
            val resolvedAddress = result.getAddressLine(0) ?: searchQuery

            markerPosition = latLng
            address = resolvedAddress

            viewModel.updateShopDetails(
                shopName = shopName,
                description = description,
                address = resolvedAddress,
                lat = latLng.latitude,
                lng = latLng.longitude
            )

            coroutineScope.launch {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                    durationMs = 800
                )
            }

        } catch (e: Exception) {
            searchError = true
        }
    }

    // Reverse geocodes a tapped LatLng to a readable address
    fun reverseGeocode(latLng: LatLng) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val results = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            address = results?.firstOrNull()?.getAddressLine(0) ?: ""
        } catch (e: Exception) {
            address = ""
        }

        viewModel.updateShopDetails(
            shopName = shopName,
            description = description,
            address = address,
            lat = latLng.latitude,
            lng = latLng.longitude
        )
    }

    var screenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        screenVisible = true
    }

    RepaiiroTheme(useDarkTheme = false) {
        Scaffold(
            containerColor = LightBackground,
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextDark
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = LightBackground
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedVisibility(
                    visible = screenVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(400)
                    ) + fadeIn(animationSpec = tween(400))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Progress Indicator & Labels
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Step 2 of 3",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSubtle,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "66% Complete",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OrangePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            LinearProgressIndicator(
                                progress = { 0.66f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = OrangePrimary,
                                trackColor = LightBorder,
                                strokeCap = StrokeCap.Round
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Heading
                        Text(
                            text = "Shop Details",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextDark
                        )

                        // Custom AuthInputField for Shop Name
                        AuthInputField(
                            value = shopName,
                            onValueChange = { shopName = it },
                            label = "Shop Name",
                            leadingIcon = Icons.Default.Store,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Custom AuthInputField for Description (Multi-line)
                        AuthInputField(
                            value = description,
                            onValueChange = { description = it },
                            label = "Description",
                            leadingIcon = Icons.Default.Info,
                            singleLine = false,
                            minLines = 3,
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Search or tap on the map to set your shop location",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSubtle
                        )

                        // Search box with custom colors matching design system
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                searchError = false
                            },
                            label = { Text("Search city or area", style = MaterialTheme.typography.bodyMedium) },
                            singleLine = true,
                            isError = searchError,
                            shape = MaterialTheme.shapes.small,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            supportingText = if (searchError) {
                                { Text("Location not found. Try a different name.", color = MaterialTheme.colorScheme.error) }
                            } else null,
                            trailingIcon = {
                                IconButton(onClick = { searchLocation() }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = OrangePrimary
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = { searchLocation() }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = LightBorder,
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                focusedLabelColor = OrangePrimary,
                                unfocusedLabelColor = TextSubtle,
                                cursorColor = OrangePrimary,
                                focusedContainerColor = LightSurface,
                                unfocusedContainerColor = LightSurface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Map card container with rounded corners and border
                        Card(
                            shape = MaterialTheme.shapes.large,
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .border(width = 1.dp, color = LightBorder, shape = MaterialTheme.shapes.large)
                                .clip(MaterialTheme.shapes.large)
                        ) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                onMapClick = { latLng ->
                                    markerPosition = latLng
                                    reverseGeocode(latLng)
                                }
                            ) {
                                markerPosition?.let { pos ->
                                    Marker(
                                        state = MarkerState(position = pos),
                                        title = shopName.ifBlank { "Shop Location" }
                                    )
                                }
                            }
                        }

                        // Address field — auto-filled by map tap or search, still editable
                        AuthInputField(
                            value = address,
                            onValueChange = {
                                address = it
                                markerPosition?.let { pos ->
                                    viewModel.updateShopDetails(
                                        shopName = shopName,
                                        description = description,
                                        address = it,
                                        lat = pos.latitude,
                                        lng = pos.longitude
                                    )
                                }
                            },
                            label = "Address",
                            leadingIcon = Icons.Default.Home,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Next button
                        PrimaryButton(
                            text = "Next",
                            onClick = {
                                viewModel.updateShopDetails(
                                    shopName = shopName,
                                    description = description,
                                    address = address,
                                    lat = markerPosition!!.latitude,
                                    lng = markerPosition!!.longitude
                                )
                                onNext()
                            },
                            enabled = isNextEnabled,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}