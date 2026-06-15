package com.carrepair.app.presentation.screens.shop

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carrepair.app.domain.viewmodels.DocumentUploadUiState
import com.carrepair.app.domain.viewmodels.ShopRegistrationViewModel
import com.carrepair.app.presentation.components.BannerType
import com.carrepair.app.presentation.components.MessageBanner

@Composable
fun ShopDocumentsScreen(
    viewModel: ShopRegistrationViewModel,
    navController: NavController
) {
    val formState     by viewModel.formState.collectAsState()
    val documentUiState by viewModel.documentUiState.collectAsState()

    var bannerMessage by remember { mutableStateOf("") }
    var bannerVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isLoading = documentUiState is DocumentUploadUiState.Loading

    fun getFileName(uri: Uri): String {
        return context.contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(
                cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
            ) else null
        } ?: "Unknown file"
    }

    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.updateDocumentUri("logo", it)
        }
    }

    val cnicPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.updateDocumentUri("cnic", it)
        }
    }

    val businessDocPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.updateDocumentUri("business", it)
        }
    }

    LaunchedEffect(documentUiState) {
        when (val state = documentUiState) {
            is DocumentUploadUiState.Success -> {
                navController.navigate("pending_approval") {
                    popUpTo("shop_registration/step1") { inclusive = true }
                }
            }
            is DocumentUploadUiState.Error -> {
                bannerMessage = state.message
                bannerVisible = true
            }
            else -> Unit
        }
    }

    val allPicked = formState.logoUri != null &&
            formState.cnicUri != null &&
            formState.businessDocUri != null

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Step 3 of 3",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LinearProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Upload Documents",
                    style = MaterialTheme.typography.headlineMedium
                )

                // Logo picker
                DocumentPickerRow(
                    label = "Shop Logo",
                    fileName = formState.logoUri?.let { getFileName(it) },
                    onPick = { logoPicker.launch(arrayOf("image/*")) }
                )

                // CNIC picker
                DocumentPickerRow(
                    label = "CNIC",
                    fileName = formState.cnicUri?.let { getFileName(it) },
                    onPick = { cnicPicker.launch(arrayOf("image/*")) }
                )

                // Business Document picker
                DocumentPickerRow(
                    label = "Business Document",
                    fileName = formState.businessDocUri?.let { getFileName(it) },
                    onPick = { businessDocPicker.launch(arrayOf("image/*", "application/pdf")) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.uploadDocuments() },
                    enabled = allPicked && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Submit Documents")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                MessageBanner(
                    message = bannerMessage,
                    bannerType = BannerType.Error,
                    visible = bannerVisible,
                    onDismiss = {
                        bannerVisible = false
                        viewModel.resetDocumentState()
                    }
                )
            }
        }
    }
}

@Composable
private fun DocumentPickerRow(
    label: String,
    fileName: String?,
    onPick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge
        )

        OutlinedButton(
            onClick = onPick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = if (fileName != null) "Change File" else "Choose File")
        }

        if (fileName != null) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}