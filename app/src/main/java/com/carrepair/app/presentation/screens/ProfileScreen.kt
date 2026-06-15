package com.carrepair.app.presentation.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carrepair.app.data.RetrofitClient
import com.carrepair.app.data.apis.AuthApi
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.presentation.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    tokenManager: TokenManager,
    authApi: AuthApi,
    navController: NavController,
    onLogout: () -> Unit
) {
    val role = remember { tokenManager.getRole() ?: "" }
    // Inclusive role check to ensure shop owners see their menu
    val isShop = role.contains("SHOP", ignoreCase = true) || role.contains("REPAIR", ignoreCase = true)

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    
    var shopId by remember { mutableStateOf<Long?>(null) }
    var shopName by remember { mutableStateOf("My Shop") }

    // Screen Entrance Animation Trigger
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    // Avatar Bouncy Spring Animation
    var scaleTarget by remember { mutableStateOf(0f) }
    val avatarScale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "AvatarBouncyScale"
    )

    LaunchedEffect(Unit) {
        scaleTarget = 1f
    }

    // Fetch Profile Info
    LaunchedEffect(Unit) {
        isLoading = true
        val token = tokenManager.getAccessToken()
        val bearerToken = "Bearer $token"
        try {
            val response = authApi.getMe(bearerToken)
            if (response.isSuccessful) {
                val user = response.body()
                fullName = user?.fullName ?: ""
                email = user?.email ?: ""
            }

            if (isShop) {
                // Attempt to fetch shop-specific details for the "My Reviews" navigation
                val statusResponse = authApi.getMyShopStatus(bearerToken)
                if (statusResponse.isSuccessful) {
                    shopName = statusResponse.body()?.shopName ?: shopName
                }
                
                val creditsResponse = RetrofitClient.repairShopApi.getShopCredits(bearerToken)
                if (creditsResponse.isSuccessful) {
                    shopId = creditsResponse.body()?.shopId
                }
            }
        } catch (e: Exception) {
            Log.e("ProfileScreen", "Failed to load profile data", e)
        } finally {
            isLoading = false
        }
    }

    RepaiiroTheme(useDarkTheme = false) {
        Scaffold(
            containerColor = LightBackground
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
                ) {
                    // TOP NAV HEADER SECTION
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyDark)
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Text(
                                text = "Profile",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { /* Edit action */ }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(90.dp)
                                .graphicsLayer {
                                    scaleX = avatarScale
                                    scaleY = avatarScale
                                }
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = fullName.ifBlank { if (isShop) "Repair Shop Owner" else "Car Owner" },
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = OrangeSubtle,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = if (isShop) "REPAIR SHOP" else "CAR OWNER",
                                color = OrangePrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ACCOUNT INFO CARD
                        Surface(
                            color = LightSurface,
                            shape = MaterialTheme.shapes.large,
                            border = BorderStroke(1.dp, LightBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Account Information",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextDark,
                                    fontWeight = FontWeight.Bold
                                )
                                InfoRow(Icons.Default.Email, "Email Address", email.ifBlank { "Not provided" })
                                Divider(color = LightBorder, thickness = 1.dp)
                                InfoRow(Icons.Default.Phone, "Phone Number", phone.ifBlank { "Not provided" })
                            }
                        }

                        // MENU OPTIONS
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val items = mutableListOf<MenuOption>()
                            
                            // Always add My Reviews for shops first
                            if (isShop) {
                                items.add(MenuOption("My Reviews", Icons.Default.Star, false))
                            }
                            
                            items.add(MenuOption("Settings", Icons.Default.Settings, false))
                            
                            if (isShop) {
                                items.add(MenuOption("Notifications", Icons.Default.Notifications, false))
                                items.add(MenuOption("Billing History", Icons.Default.Receipt, false))
                            }
                            
                            items.add(MenuOption("Log Out", Icons.Default.Logout, true))

                            items.forEachIndexed { index, option ->
                                var rowVisible by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) {
                                    delay(index * 60L)
                                    rowVisible = true
                                }

                                AnimatedVisibility(
                                    visible = rowVisible,
                                    enter = slideInHorizontally(animationSpec = tween(300)) { -it / 5 } + fadeIn(animationSpec = tween(300))
                                ) {
                                    MenuRowItem(
                                        option = option,
                                        onClick = {
                                            when {
                                                option.isLogout -> {
                                                    tokenManager.clearTokens()
                                                    onLogout()
                                                }
                                                option.label == "My Reviews" -> {
                                                    // Navigate to the public shop profile to see reviews
                                                    navController.navigate(
                                                        Screen.ShopProfile.createRoute(
                                                            shopId = shopId ?: 0L,
                                                            shopName = shopName,
                                                            logoUrl = null
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class MenuOption(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isLogout: Boolean
)

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(20.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSubtle)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = TextDark, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun MenuRowItem(option: MenuOption, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = LightSurface,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, LightBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    tint = if (option.isLogout) StatusRed else NavInactive,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (option.isLogout) StatusRed else TextDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (!option.isLogout) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = NavInactive, modifier = Modifier.size(20.dp))
            }
        }
    }
}
