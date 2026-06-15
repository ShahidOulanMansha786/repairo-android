package com.carrepair.app.presentation.screens.shop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carrepair.app.domain.viewmodels.ShopRegistrationViewModel
import com.carrepair.app.presentation.components.AuthInputField
import com.carrepair.app.presentation.components.PrimaryButton
import com.carrepair.app.presentation.ui.theme.LightBackground
import com.carrepair.app.presentation.ui.theme.LightBorder
import com.carrepair.app.presentation.ui.theme.OrangePrimary
import com.carrepair.app.presentation.ui.theme.RepaiiroTheme
import com.carrepair.app.presentation.ui.theme.TextDark
import com.carrepair.app.presentation.ui.theme.TextSubtle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopOwnerDetailsScreen(
    viewModel: ShopRegistrationViewModel,
    navController: NavController,
    onNext: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()

    var fullName by remember { mutableStateOf(formState.fullName) }
    var email by remember { mutableStateOf(formState.email) }
    var phone by remember { mutableStateOf(formState.phone) }

    val isNextEnabled = fullName.isNotBlank() && email.isNotBlank() && phone.isNotBlank()

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
                                    text = "Step 1 of 3",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSubtle,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "33% Complete",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OrangePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            LinearProgressIndicator(
                                progress = { 0.33f },
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
                            text = "Owner Details",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom AuthInputFields
                        AuthInputField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = "Full Name",
                            leadingIcon = Icons.Default.Person,
                            modifier = Modifier.fillMaxWidth()
                        )

                        AuthInputField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email Address",
                            leadingIcon = Icons.Default.Email,
                            modifier = Modifier.fillMaxWidth()
                        )

                        AuthInputField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = "Phone Number",
                            leadingIcon = Icons.Default.Phone,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Next Action Button
                        PrimaryButton(
                            text = "Next",
                            onClick = {
                                viewModel.updateOwnerDetails(fullName, email, phone)
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