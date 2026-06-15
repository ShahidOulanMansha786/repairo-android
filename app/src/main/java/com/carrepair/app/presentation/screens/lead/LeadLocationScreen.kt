package com.carrepair.app.presentation.screens.lead

import android.Manifest
import android.location.Geocoder
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carrepair.app.domain.viewmodels.LeadPostingUiState
import com.carrepair.app.domain.viewmodels.LeadPostingViewModel
import com.carrepair.app.presentation.components.PrimaryButton
import com.carrepair.app.presentation.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LeadLocationScreen(
    navController: NavController,
    viewModel: LeadPostingViewModel
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var address by remember { mutableStateOf(formState.address) }
    var markerPosition by remember {
        mutableStateOf(
            if (formState.latitude != 0.0)
                LatLng(formState.latitude, formState.longitude)
            else null
        )
    }

    val defaultLocation = LatLng(31.5204, 74.3587)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            markerPosition ?: defaultLocation, 12f
        )
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    fun reverseGeocode(latLng: LatLng): String {
        return try {
            val results = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            results?.firstOrNull()?.getAddressLine(0) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun fetchCurrentLocation() {
        try {
            val locationRequest = com.google.android.gms.location.CurrentLocationRequest.Builder()
                .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)
                .build()

            fusedLocationClient.getCurrentLocation(locationRequest, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        markerPosition = latLng
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                            )
                            val resolvedAddress = reverseGeocode(latLng)
                            address = resolvedAddress
                            viewModel.updateLocation(
                                resolvedAddress,
                                latLng.latitude,
                                latLng.longitude
                            )
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Could not get location. Tap map to set manually.")
                        }
                    }
                }
                .addOnFailureListener {
                    scope.launch {
                        snackbarHostState.showSnackbar("Location error: ${it.message}")
                    }
                }
        } catch (e: SecurityException) {
            scope.launch {
                snackbarHostState.showSnackbar("Location permission denied")
            }
        }
    }

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    ) { permissionsResult ->
        val granted = permissionsResult[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissionsResult[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchCurrentLocation()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Location permission denied. Tap the map to set location manually."
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionsState.launchMultiplePermissionRequest()
    }

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted && markerPosition == null) {
            fetchCurrentLocation()
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LeadPostingUiState.Success -> {
                navController.navigate("leads/success/${state.leadId}") {
                    popUpTo("post_lead/step1") { inclusive = true }
                }
            }
            is LeadPostingUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }

    val isPostEnabled = formState.latitude != 0.0 && uiState !is LeadPostingUiState.Loading

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    var progressTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        progressTarget = 1f
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
                                text = "Step 3 of 3",
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
                                text = "Step 3",
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
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightBackground)
                    .padding(paddingValues)
            ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(animationSpec = tween(400)) { it / 3 } + fadeIn(animationSpec = tween(400)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            shape = MaterialTheme.shapes.large,
                            border = BorderStroke(1.dp, LightBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(MaterialTheme.shapes.large)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState,
                                    onMapClick = { latLng ->
                                        markerPosition = latLng
                                        scope.launch {
                                            val resolvedAddress = reverseGeocode(latLng)
                                            address = resolvedAddress
                                            viewModel.updateLocation(
                                                resolvedAddress,
                                                latLng.latitude,
                                                latLng.longitude
                                            )
                                        }
                                    }
                                ) {
                                    markerPosition?.let { pos ->
                                        val markerState = rememberMarkerState(position = pos)

                                        LaunchedEffect(markerState.position) {
                                            if (markerState.position != pos) {
                                                val newPos = markerState.position
                                                markerPosition = newPos
                                                val resolvedAddress = reverseGeocode(newPos)
                                                address = resolvedAddress
                                                viewModel.updateLocation(
                                                    resolvedAddress,
                                                    newPos.latitude,
                                                    newPos.longitude
                                                )
                                            }
                                        }

                                        Marker(
                                            state = markerState,
                                            title = "Lead Location",
                                            draggable = true
                                        )
                                    }
                                }

                                FloatingActionButton(
                                    onClick = {
                                        if (locationPermissionsState.allPermissionsGranted) {
                                            fetchCurrentLocation()
                                        } else {
                                            locationPermissionsState.launchMultiplePermissionRequest()
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(12.dp),
                                    containerColor = LightSurface,
                                    contentColor = OrangePrimary
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "My Location"
                                    )
                                }
                            }
                        }

                        Column {
                            Text(
                                text = "Street Address, City, or ZIP Code",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextDark,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                placeholder = { Text("e.g. 123 Main St, New York, NY") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = OrangePrimary
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

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = TextSubtle,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "We'll use this to find the nearest certified repair shop.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSubtle
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        PrimaryButton(
                            text = "Submit Lead",
                            onClick = { viewModel.submitLead() },
                            enabled = isPostEnabled,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (uiState is LeadPostingUiState.Loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OrangePrimary)
                    }
                }
            }
        }
    }
}
