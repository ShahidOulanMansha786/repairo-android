package com.carrepair.app.presentation.screens.shop

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.carrepair.app.domain.viewmodels.PurchaseCreditsUiState
import com.carrepair.app.domain.viewmodels.ShopHomeViewModel
import com.carrepair.app.presentation.components.DarkNavHeader
import com.carrepair.app.presentation.components.PrimaryButton
import com.carrepair.app.presentation.ui.theme.*
import kotlinx.coroutines.launch

data class CreditPackage(
    val id: Int,
    val credits: Int,
    val price: Double,
    val pricePerCredit: Double,
    val bonus: Int = 0,
    val isPopular: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyCreditsScreen(
    navController: NavController,
    viewModel: ShopHomeViewModel
) {
    val creditsBalance by viewModel.creditsBalance.collectAsState()
    val purchaseState by viewModel.purchaseCreditsState.collectAsState()
    val packages = remember {
        listOf(
            CreditPackage(1, 10, 49.99, 4.99),
            CreditPackage(2, 25, 99.99, 4.00, bonus = 5, isPopular = true),
            CreditPackage(3, 50, 179.99, 3.60, bonus = 12)
        )
    }

    var selectedPackageId by remember { mutableStateOf(2) }
    val selectedPackage = remember(selectedPackageId) {
        packages.find { it.id == selectedPackageId } ?: packages[1]
    }

    var visible by remember { mutableStateOf(false) }
    var showPurchaseSuccess by remember { mutableStateOf(false) }
    var purchasedCredits by remember { mutableStateOf(0) }

    val purchaseLoading = purchaseState is PurchaseCreditsUiState.Loading
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        visible = true
        viewModel.loadShopCredits()
        viewModel.resetPurchaseState()
    }

    LaunchedEffect(purchaseState) {
        when (val state = purchaseState) {
            is PurchaseCreditsUiState.Success -> {
                purchasedCredits = selectedPackage.credits + selectedPackage.bonus
                showPurchaseSuccess = true
                viewModel.resetPurchaseState()
            }
            is PurchaseCreditsUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetPurchaseState()
            }
            else -> Unit
        }
    }

    RepaiiroTheme(useDarkTheme = false) {
        Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = LightBackground,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                Surface(
                    color = LightSurface,
                    border = BorderStroke(1.dp, LightBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PrimaryButton(
                            text = "💳 Purchase ${selectedPackage.credits + selectedPackage.bonus} Credits",
                            isLoading = purchaseLoading,
                            onClick = {
                               val totalCredits = selectedPackage.credits + selectedPackage.bonus
                               val price = "%.2f".format(selectedPackage.price)
                               navController.navigate(
                                   "credits/checkout/$totalCredits/${selectedPackage.id}/$price"
                               )
                           },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "🔒 SECURE PAYMENT",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSubtle,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
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
                        .verticalScroll(rememberScrollState())
                ) {
                    // Top Bar
                    DarkNavHeader(
                        title = "Buy Credits",
                        onBack = { navController.popBackStack() }
                    )

                    // Balance Card
                    Surface(
                        color = NavyMedium,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "CURRENT BALANCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = creditsBalance?.toString() ?: "—",
                                style = MaterialTheme.typography.displaySmall,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "credits available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }

                    // How Credits Work Card
                    Surface(
                        color = LightSurface,
                        shape = MaterialTheme.shapes.large,
                        border = BorderStroke(1.dp, LightBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(OrangeSubtle)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "How Credits Work",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextDark,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Each credit unlocks one lead. View full lead details, submit quotes, and message car owners.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSubtle
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Package Title
                    Text(
                        text = "Select a Package",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    // Package Cards
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        packages.forEach { pack ->
                            val isSelected = pack.id == selectedPackageId
                            val borderWidth = if (isSelected) 2.dp else 1.dp
                            val borderColor by animateColorAsState(
                                targetValue = if (isSelected) OrangePrimary else LightBorder,
                                label = "BorderColor"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Surface(
                                    color = LightSurface,
                                    shape = MaterialTheme.shapes.large,
                                    border = BorderStroke(borderWidth, borderColor),
                                    onClick = { selectedPackageId = pack.id },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        if (pack.isPopular) {
                                            Surface(
                                                color = OrangePrimary,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "MOST POPULAR",
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = "${pack.credits} Credits",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        color = TextDark,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (pack.bonus > 0) {
                                                        Surface(
                                                            color = StatusGreenTint,
                                                            shape = MaterialTheme.shapes.extraSmall
                                                        ) {
                                                            Text(
                                                                text = "+${pack.bonus} BONUS",
                                                                color = StatusGreen,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "PKR ${pack.pricePerCredit} per credit",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSubtle
                                                )
                                            }

                                            Text(
                                                text = "PKR ${pack.price}",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = TextDark,
                                                fontWeight = FontWeight.Bold
                                            )

                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedPackageId = pack.id },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = OrangePrimary,
                                                    unselectedColor = LightBorder
                                                )
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

            AnimatedVisibility(
                visible = showPurchaseSuccess,
                enter = scaleIn(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    initialScale = 0.85f
                ) + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OverlayScrim),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = LightSurface,
                        modifier = Modifier
                            .padding(32.dp)
                            .width(280.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(StatusBlueTint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = StatusBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Credits Added!",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                if (purchasedCredits > 0) {
                                    "$purchasedCredits credits have been added to your account."
                                } else {
                                    "Your credits have been added to your account."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSubtle,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    showPurchaseSuccess = false
                                    navController.popBackStack()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Text(
                                    "Start Browsing",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
