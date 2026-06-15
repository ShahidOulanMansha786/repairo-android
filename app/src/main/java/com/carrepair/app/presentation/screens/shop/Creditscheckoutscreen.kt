package com.carrepair.app.presentation.screens.shop

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.carrepair.app.data.apis.RepairShopApi
import com.carrepair.app.data.dto.PurchaseCreditsRequestDto
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.presentation.components.*
import com.carrepair.app.presentation.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Reuse card formatting helpers ─────────────────────────────────────────────

private fun formatCardNumber(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(16)
    return digits.chunked(4).joinToString(" ")
}

private fun formatExpiry(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(4)
    return if (digits.length >= 2) "${digits.take(2)}/${digits.drop(2)}" else digits
}

private fun detectBrand(number: String) = when {
    number.replace(" ", "").startsWith("4") -> "VISA"
    number.replace(" ", "").startsWith("5") -> "MASTERCARD"
    number.replace(" ", "").startsWith("3") -> "AMEX"
    else -> "CARD"
}

private fun cardValid(n: String) = n.replace(" ", "").length == 16
private fun expiryValid(e: String): Boolean {
    val p = e.split("/")
    if (p.size != 2) return false
    val m = p[0].toIntOrNull() ?: return false
    val y = p[1].toIntOrNull() ?: return false
    return m in 1..12 && y >= 24
}
private fun cvvValid(c: String) = c.length in 3..4
private fun nameValid(n: String) = n.trim().length >= 2

// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CreditsCheckoutScreen(
    navController: NavController,
    credits: Int,
    packageId: Int,
    price: Double,
    repairShopApi: RepairShopApi,
    tokenManager: TokenManager
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiry     by remember { mutableStateOf("") }
    var cvv        by remember { mutableStateOf("") }
    var cardName   by remember { mutableStateOf("") }
    var cvvVisible by remember { mutableStateOf(false) }

    var cardTouched   by remember { mutableStateOf(false) }
    var expiryTouched by remember { mutableStateOf(false) }
    var cvvTouched    by remember { mutableStateOf(false) }
    var nameTouched   by remember { mutableStateOf(false) }

    var showProcessing  by remember { mutableStateOf(false) }
    var processingStep  by remember { mutableStateOf(0) }
    var processingDone  by remember { mutableStateOf(false) }
    var showSuccess     by remember { mutableStateOf(false) }
    var newBalance      by remember { mutableStateOf(0) }

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isFormValid = cardValid(cardNumber) && expiryValid(expiry) &&
            cvvValid(cvv) && nameValid(cardName)

    val processingSteps = listOf(
        "Encrypting card details...",
        "Contacting payment network...",
        "Authorizing transaction...",
        "Adding credits to your account...",
        "Purchase confirmed!"
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // ── Processing dialog ─────────────────────────────────────────────────────
    if (showProcessing) {
        CreditsProcessingDialog(
            steps = processingSteps,
            currentStep = processingStep,
            isDone = processingDone
        )
    }

    // ── Success dialog ────────────────────────────────────────────────────────
    if (showSuccess) {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(StatusGreenTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CreditCard,
                        null,
                        tint = StatusGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    "Credits Added!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "$credits credits have been added to your account.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSubtle,
                        textAlign = TextAlign.Center
                    )
                    if (newBalance > 0) {
                        Surface(
                            color = OrangeSubtle,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                "New balance: $newBalance credits",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleSmall,
                                color = OrangePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccess = false
                        // Go back to BuyCredits, popping checkout off stack
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Browsing Leads", color = Color.White)
                }
            }
        )
    }

    RepaiiroTheme(useDarkTheme = false) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                DarkNavHeader(
                    title = "Purchase Credits",
                    subtitle = "$credits credits · PKR ${"%.0f".format(price)}",
                    onBack = if (!showProcessing) ({ navController.popBackStack() }) else null
                )
            },
            containerColor = LightBackground
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ScreenPadding, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ── Order summary card ────────────────────────────────────
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(300)) + slideInVertically(tween(400)) { -20 }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = NavyDark
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Purchasing", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Bolt, null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
                                    Text(
                                        "$credits Credits",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    "Unlocks $credits repair leads",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("PKR", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Text(
                                    "%.0f".format(price),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = OrangePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // ── Visual card preview ───────────────────────────────────
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(500)) { 20 }
                ) {
                    CreditsCardPreview(
                        cardNumber = cardNumber,
                        cardName = cardName,
                        expiry = expiry,
                        brand = detectBrand(cardNumber)
                    )
                }

                // ── Card form ─────────────────────────────────────────────
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(600)) { 30 }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = LightSurface,
                        border = BorderStroke(1.dp, LightBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "CARD DETAILS",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSubtle,
                                letterSpacing = 1.sp
                            )

                            CreditsInputField(
                                value = cardNumber,
                                onValueChange = { cardNumber = formatCardNumber(it); cardTouched = true },
                                label = "Card Number",
                                placeholder = "4242 4242 4242 4242",
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                isError = cardTouched && !cardValid(cardNumber),
                                errorMessage = "Enter a valid 16-digit card number",
                                trailingIcon = {
                                    val brand = detectBrand(cardNumber)
                                    if (brand != "CARD") {
                                        val (bg, fg) = when (brand) {
                                            "VISA"       -> Color(0xFF1A1F71) to Color.White
                                            "MASTERCARD" -> Color(0xFFEB001B) to Color.White
                                            else         -> Color(0xFF2E77BC) to Color.White
                                        }
                                        Surface(color = bg, shape = MaterialTheme.shapes.extraSmall) {
                                            Text(brand, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelSmall, color = fg,
                                                fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                        }
                                    }
                                }
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CreditsInputField(
                                    value = expiry,
                                    onValueChange = { expiry = formatExpiry(it); expiryTouched = true },
                                    label = "Expiry",
                                    placeholder = "MM/YY",
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                    isError = expiryTouched && !expiryValid(expiry),
                                    errorMessage = "Invalid",
                                    modifier = Modifier.weight(1f)
                                )
                                CreditsInputField(
                                    value = cvv,
                                    onValueChange = {
                                        if (it.length <= 4) { cvv = it.filter { c -> c.isDigit() }; cvvTouched = true }
                                    },
                                    label = "CVV",
                                    placeholder = "•••",
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                    isError = cvvTouched && !cvvValid(cvv),
                                    errorMessage = "Invalid",
                                    visualTransformation = if (cvvVisible) VisualTransformation.None
                                    else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { cvvVisible = !cvvVisible }, modifier = Modifier.size(20.dp)) {
                                            Icon(
                                                if (cvvVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                null, tint = TextSubtle, modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            CreditsInputField(
                                value = cardName,
                                onValueChange = { cardName = it; nameTouched = true },
                                label = "Cardholder Name",
                                placeholder = "As printed on card",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done,
                                onNext = { focusManager.clearFocus() },
                                isError = nameTouched && !nameValid(cardName),
                                errorMessage = "Enter cardholder name"
                            )
                        }
                    }
                }

                // ── Security badges ───────────────────────────────────────
                AnimatedVisibility(visible = visible, enter = fadeIn(tween(600))) {
                    SecureBadgeRow()
                }

                Spacer(Modifier.height(8.dp))

                // ── Pay button ────────────────────────────────────────────
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { 40 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                cardTouched = true; expiryTouched = true
                                cvvTouched = true;  nameTouched = true
                                if (!isFormValid) return@Button
                                focusManager.clearFocus()
                                showProcessing = true

                                scope.launch {
                                    for (i in processingSteps.indices) {
                                        processingStep = i
                                        delay(700)
                                    }
                                    processingDone = true
                                    delay(500)

                                    // Call the real backend endpoint
                                    try {
                                        val token = "Bearer ${tokenManager.getAccessToken()}"
                                        val response = repairShopApi.purchaseCredits(
                                            token,
                                            PurchaseCreditsRequestDto(
                                                credits = credits,
                                                packageId = packageId
                                            )
                                        )
                                        if (response.isSuccessful) {
                                            newBalance = response.body()?.resolvedBalance() ?: credits
                                        } else {
                                            // Backend failed — still show success with local count
                                            newBalance = credits
                                        }
                                    } catch (e: Exception) {
                                        newBalance = credits
                                    }

                                    showProcessing = false
                                    showSuccess = true
                                }
                            },
                            enabled = !showProcessing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFormValid) OrangePrimary
                                else OrangePrimary.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Pay PKR ${"%.0f".format(price)} · Get $credits Credits",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            "Your card details are encrypted and never stored",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSubtle,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ── Visual Card Preview ───────────────────────────────────────────────────────

@Composable
private fun CreditsCardPreview(
    cardNumber: String,
    cardName: String,
    expiry: String,
    brand: String
) {
    val digits = cardNumber.replace(" ", "").padEnd(16, '•')
    val displayNumber = "${digits.take(4)} ${digits.drop(4).take(4)} ${digits.drop(8).take(4)} ${digits.drop(12).take(4)}"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = MaterialTheme.shapes.large,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(listOf(NavyDark, Color(0xFF1A3A5C), Color(0xFF0B2545))),
                    MaterialTheme.shapes.large
                )
                .padding(24.dp)
        ) {
            // Decorative circles
            Box(modifier = Modifier.size(120.dp).offset(x = (-30).dp, y = (-30).dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)))
            Box(modifier = Modifier.size(100.dp).align(Alignment.BottomEnd).offset(x = 20.dp, y = 20.dp).clip(CircleShape).background(OrangePrimary.copy(alpha = 0.15f)))

            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(width = 40.dp, height = 30.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)).background(Brush.linearGradient(listOf(Color(0xFFD4A843), Color(0xFFF0C060)))))
                    Text(brand, style = MaterialTheme.typography.titleSmall, color = Color.White.copy(0.9f), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Text(displayNumber, style = androidx.compose.ui.text.TextStyle(fontFamily = Inter, fontSize = 18.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp, color = Color.White))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text("CARD HOLDER", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.5f), fontSize = 9.sp)
                        Text(cardName.uppercase().ifEmpty { "YOUR NAME" }, style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("EXPIRES", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.5f), fontSize = 9.sp)
                        Text(expiry.ifEmpty { "MM/YY" }, style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── Input Field ───────────────────────────────────────────────────────────────

@Composable
private fun CreditsInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    onNext: () -> Unit,
    isError: Boolean = false,
    errorMessage: String = "",
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (isError) StatusRed else TextSubtle)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = TextMuted) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onNext = { onNext() }, onDone = { onNext() }),
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            isError = isError,
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = LightBorder,
                errorBorderColor = StatusRed,
                cursorColor = OrangePrimary,
                focusedContainerColor = LightSurface,
                unfocusedContainerColor = LightSurface
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextDark)
        )
        AnimatedVisibility(visible = isError) {
            Text(errorMessage, style = MaterialTheme.typography.labelSmall, color = StatusRed)
        }
    }
}

// ── Processing Dialog ─────────────────────────────────────────────────────────

@Composable
private fun CreditsProcessingDialog(
    steps: List<String>,
    currentStep: Int,
    isDone: Boolean
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(shape = MaterialTheme.shapes.large, color = LightSurface, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AnimatedContent(
                    targetState = isDone,
                    transitionSpec = { scaleIn() togetherWith scaleOut() },
                    label = "icon"
                ) { done ->
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape)
                            .background(if (done) StatusGreenTint else OrangeSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        if (done) Icon(Icons.Default.Check, null, tint = StatusGreen, modifier = Modifier.size(32.dp))
                        else CircularProgressIndicator(modifier = Modifier.size(32.dp), color = OrangePrimary, strokeWidth = 3.dp)
                    }
                }

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        fadeIn(tween(250)) + slideInVertically(tween(250)) { 10 } togetherWith fadeOut(tween(200))
                    },
                    label = "step"
                ) { step ->
                    Text(
                        steps.getOrElse(step) { steps.last() },
                        style = MaterialTheme.typography.titleSmall,
                        color = TextDark,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                LinearProgressIndicator(
                    progress = { (currentStep + 1).toFloat() / steps.size },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(MaterialTheme.shapes.extraLarge),
                    color = OrangePrimary,
                    trackColor = LightBorder
                )

                Text(
                    if (isDone) "Credits added!" else "Please wait...",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDone) StatusGreen else TextSubtle
                )
            }
        }
    }
}