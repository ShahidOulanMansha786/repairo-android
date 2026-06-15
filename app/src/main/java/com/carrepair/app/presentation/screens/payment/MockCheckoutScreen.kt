package com.carrepair.app.presentation.screens.payment

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
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
import com.carrepair.app.data.apis.PaymentApi
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.presentation.components.*
import com.carrepair.app.presentation.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Card brand detection ──────────────────────────────────────────────────────

private fun detectCardBrand(number: String): String {
    val clean = number.replace(" ", "")
    return when {
        clean.startsWith("4")           -> "VISA"
        clean.startsWith("5") ||
        clean.startsWith("2")           -> "MASTERCARD"
        clean.startsWith("3")           -> "AMEX"
        else                            -> "CARD"
    }
}

// ── Card number formatter ────────────────────────────────────────────────────

private fun formatCardNumber(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(16)
    return digits.chunked(4).joinToString(" ")
}

// ── Expiry formatter ─────────────────────────────────────────────────────────

private fun formatExpiry(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(4)
    return if (digits.length >= 2) "${digits.take(2)}/${digits.drop(2)}" else digits
}

// ── Validation helpers ────────────────────────────────────────────────────────

private fun isCardNumberValid(formatted: String) =
    formatted.replace(" ", "").length == 16

private fun isExpiryValid(formatted: String): Boolean {
    val parts = formatted.split("/")
    if (parts.size != 2) return false
    val month = parts[0].toIntOrNull() ?: return false
    val year = parts[1].toIntOrNull() ?: return false
    return month in 1..12 && year >= 24
}

private fun isCvvValid(cvv: String) = cvv.length in 3..4

private fun isNameValid(name: String) = name.trim().length >= 2

// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MockCheckoutScreen(
    navController: NavController,
    paymentId: Long,
    leadId: Long,
    amount: Double,
    shopName: String,
    paymentApi: PaymentApi,
    tokenManager: TokenManager
) {
    // ── Form state ────────────────────────────────────────────────────────────
    var cardNumber   by remember { mutableStateOf("") }
    var expiry       by remember { mutableStateOf("") }
    var cvv          by remember { mutableStateOf("") }
    var cardName     by remember { mutableStateOf("") }
    var cvvVisible   by remember { mutableStateOf(false) }

    // ── Validation touched state ──────────────────────────────────────────────
    var cardTouched  by remember { mutableStateOf(false) }
    var expiryTouched by remember { mutableStateOf(false) }
    var cvvTouched   by remember { mutableStateOf(false) }
    var nameTouched  by remember { mutableStateOf(false) }

    // ── Processing dialog state ───────────────────────────────────────────────
    var showProcessing by remember { mutableStateOf(false) }
    var processingStep by remember { mutableStateOf(0) }
    var processingDone by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val cardBrand = detectCardBrand(cardNumber)
    val isFormValid = isCardNumberValid(cardNumber) &&
                      isExpiryValid(expiry) &&
                      isCvvValid(cvv) &&
                      isNameValid(cardName)

    // ── Processing steps ──────────────────────────────────────────────────────
    val processingSteps = listOf(
        "Encrypting card details...",
        "Contacting payment network...",
        "Authorizing transaction...",
        "Securing funds in escrow...",
        "Payment confirmed!"
    )

    // ── Entrance animation ────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // ── Processing dialog ─────────────────────────────────────────────────────
    if (showProcessing) {
        PaymentProcessingDialog(
            steps = processingSteps,
            currentStep = processingStep,
            isDone = processingDone
        )
    }

    RepaiiroTheme(useDarkTheme = false) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                DarkNavHeader(
                    title = "Secure Payment",
                    subtitle = "256-bit SSL encrypted",
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
                // ── Amount summary card ───────────────────────────────────────
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
                                Text(
                                    "Paying to",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                                Text(
                                    shopName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "PKR",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                                Text(
                                    "%,.0f".format(amount),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = OrangePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // ── Visual card preview ───────────────────────────────────────
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(500)) { 20 }
                ) {
                    CardPreview(
                        cardNumber = cardNumber,
                        cardName = cardName,
                        expiry = expiry,
                        cardBrand = cardBrand
                    )
                }

                // ── Card form ─────────────────────────────────────────────────
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

                            // Card number
                            CardInputField(
                                value = cardNumber,
                                onValueChange = {
                                    cardNumber = formatCardNumber(it)
                                    cardTouched = true
                                },
                                label = "Card Number",
                                placeholder = "4242 4242 4242 4242",
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                isError = cardTouched && !isCardNumberValid(cardNumber),
                                errorMessage = "Enter a valid 16-digit card number",
                                trailingIcon = {
                                    CardBrandBadge(cardBrand)
                                }
                            )

                            // Expiry + CVV row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CardInputField(
                                    value = expiry,
                                    onValueChange = {
                                        expiry = formatExpiry(it)
                                        expiryTouched = true
                                    },
                                    label = "Expiry",
                                    placeholder = "MM/YY",
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                    isError = expiryTouched && !isExpiryValid(expiry),
                                    errorMessage = "Invalid",
                                    modifier = Modifier.weight(1f)
                                )

                                CardInputField(
                                    value = cvv,
                                    onValueChange = {
                                        if (it.length <= 4) {
                                            cvv = it.filter { c -> c.isDigit() }
                                            cvvTouched = true
                                        }
                                    },
                                    label = "CVV",
                                    placeholder = "•••",
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                    isError = cvvTouched && !isCvvValid(cvv),
                                    errorMessage = "Invalid",
                                    visualTransformation = if (cvvVisible)
                                        VisualTransformation.None
                                    else
                                        PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { cvvVisible = !cvvVisible },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                if (cvvVisible) Icons.Default.VisibilityOff
                                                else Icons.Default.Visibility,
                                                contentDescription = null,
                                                tint = TextSubtle,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Cardholder name
                            CardInputField(
                                value = cardName,
                                onValueChange = {
                                    cardName = it
                                    nameTouched = true
                                },
                                label = "Cardholder Name",
                                placeholder = "As printed on card",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done,
                                onNext = { focusManager.clearFocus() },
                                isError = nameTouched && !isNameValid(cardName),
                                errorMessage = "Enter cardholder name"
                            )
                        }
                    }
                }

                // ── Security badges ───────────────────────────────────────────
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600))
                ) {
                    SecureBadgeRow()
                }

                Spacer(Modifier.height(8.dp))

                // ── Pay button ────────────────────────────────────────────────
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { 40 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                // Mark all fields touched to show validation
                                cardTouched = true
                                expiryTouched = true
                                cvvTouched = true
                                nameTouched = true

                                if (!isFormValid) return@Button

                                focusManager.clearFocus()
                                showProcessing = true

                                scope.launch {
                                    // Animate through steps
                                    for (i in processingSteps.indices) {
                                        processingStep = i
                                        delay(700)
                                    }
                                    processingDone = true
                                    delay(600)

                                    // Call mock-pay endpoint via HTTP (no browser)
                                    try {
                                        val token = "Bearer ${tokenManager.getAccessToken()}"
                                        // We call the mock-pay endpoint
                                        // This is a GET that returns a 302 redirect
                                        // We don't follow the redirect — we just
                                        // navigate directly to success after it fires
                                        paymentApi.mockPay(paymentId)
                                    } catch (e: Exception) {
                                        // mock-pay returns 302 which Retrofit may throw
                                        // as an exception — that's fine, it still fired
                                    }

                                    showProcessing = false
                                    // Navigate to success screen
                                    navController.navigate(
                                        "payment/success/$paymentId/$leadId"
                                    ) {
                                        popUpTo("payment/confirm/{leadId}/{quoteId}/{quotedPrice}/{shopName}") {
                                            inclusive = true
                                        }
                                    }
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
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Pay PKR %,.0f Securely".format(amount),
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
private fun CardPreview(
    cardNumber: String,
    cardName: String,
    expiry: String,
    cardBrand: String
) {
    val displayNumber = cardNumber.padEnd(19, ' ')
        .chunked(1)
        .mapIndexed { i, c ->
            if (c == " " && i < 14) "•" else c
        }.joinToString("")
        .let {
            // Re-format with spaces at positions 4,9,14
            val digits = cardNumber.replace(" ", "").padEnd(16, '•')
            "${digits.take(4)} ${digits.drop(4).take(4)} ${digits.drop(8).take(4)} ${digits.drop(12).take(4)}"
        }

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
                    Brush.linearGradient(
                        listOf(
                            NavyDark,
                            Color(0xFF1A3A5C),
                            Color(0xFF0B2545)
                        )
                    ),
                    MaterialTheme.shapes.large
                )
                .padding(24.dp)
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = (-30).dp, y = (-30).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = 20.dp)
                    .clip(CircleShape)
                    .background(OrangePrimary.copy(alpha = 0.15f))
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: chip + brand
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chip
                    Box(
                        modifier = Modifier
                            .size(width = 40.dp, height = 30.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFD4A843), Color(0xFFF0C060))
                                )
                            )
                    )
                    // Brand
                    Text(
                        text = cardBrand,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Card number
                Text(
                    text = displayNumber,
                    style = TextStyle(
                        fontFamily = Inter,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                )

                // Bottom: name + expiry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "CARD HOLDER",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp
                        )
                        Text(
                            cardName.uppercase().ifEmpty { "YOUR NAME" },
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "EXPIRES",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp
                        )
                        Text(
                            expiry.ifEmpty { "MM/YY" },
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ── Card Input Field ──────────────────────────────────────────────────────────

@Composable
private fun CardInputField(
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
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isError) StatusRed else TextSubtle
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext() },
                onDone = { onNext() }
            ),
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            isError = isError,
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = LightBorder,
                errorBorderColor = StatusRed,
                focusedLabelColor = OrangePrimary,
                cursorColor = OrangePrimary,
                focusedContainerColor = LightSurface,
                unfocusedContainerColor = LightSurface
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextDark)
        )
        AnimatedVisibility(visible = isError) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = StatusRed
            )
        }
    }
}

// ── Card Brand Badge ──────────────────────────────────────────────────────────

@Composable
private fun CardBrandBadge(brand: String) {
    val (bg, fg) = when (brand) {
        "VISA"       -> Color(0xFF1A1F71) to Color.White
        "MASTERCARD" -> Color(0xFFEB001B) to Color.White
        "AMEX"       -> Color(0xFF2E77BC) to Color.White
        else         -> LightBorder to TextSubtle
    }
    if (brand != "CARD") {
        Surface(color = bg, shape = MaterialTheme.shapes.extraSmall) {
            Text(
                text = brand,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall,
                color = fg,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
        }
    }
}

// ── Processing Dialog ─────────────────────────────────────────────────────────

@Composable
private fun PaymentProcessingDialog(
    steps: List<String>,
    currentStep: Int,
    isDone: Boolean
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = LightSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Icon
                AnimatedContent(
                    targetState = isDone,
                    transitionSpec = { scaleIn() togetherWith scaleOut() },
                    label = "doneIcon"
                ) { done ->
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (done) StatusGreenTint else OrangeSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        if (done) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = StatusGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = OrangePrimary,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }

                // Step text
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        fadeIn(tween(250)) + slideInVertically(tween(250)) { 10 } togetherWith
                        fadeOut(tween(200))
                    },
                    label = "stepText"
                ) { step ->
                    Text(
                        text = steps.getOrElse(step) { steps.last() },
                        style = MaterialTheme.typography.titleSmall,
                        color = TextDark,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Progress bar
                LinearProgressIndicator(
                    progress = { (currentStep + 1).toFloat() / steps.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(MaterialTheme.shapes.extraLarge),
                    color = OrangePrimary,
                    trackColor = LightBorder
                )

                Text(
                    text = if (isDone) "Redirecting..." else "Please wait...",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSubtle
                )
            }
        }
    }
}
