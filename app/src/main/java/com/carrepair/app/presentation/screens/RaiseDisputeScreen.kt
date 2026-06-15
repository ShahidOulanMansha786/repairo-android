package com.carrepair.app.presentation.screens

import android.app.Application
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.carrepair.app.data.RetrofitClient
import com.carrepair.app.data.apis.DisputeApi
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.domain.service.S3UploadService
import com.carrepair.app.domain.viewmodels.dispute.RaiseDisputeViewModel
import com.carrepair.app.domain.viewmodels.dispute.RaiseDisputeViewModelFactory
import com.carrepair.app.presentation.components.DarkNavHeader
import com.carrepair.app.presentation.ui.theme.*
import okhttp3.OkHttpClient


@Composable
fun RaiseDisputeScreen(
    navController: NavController,
    leadId: Long,
    disputeApi: DisputeApi,
    tokenManager: TokenManager
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: RaiseDisputeViewModel = viewModel(
        factory = RaiseDisputeViewModelFactory(
            disputeApi, tokenManager,
            S3UploadService(RetrofitClient.authApi, OkHttpClient()),
            application
        )
    )
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { viewModel.addImage(it) } }

    LaunchedEffect(viewModel.success) {
        if (viewModel.success) {
            snackbarHostState.showSnackbar("Dispute submitted successfully")
            navController.popBackStack()
        }
    }

    LaunchedEffect(viewModel.error) {
        viewModel.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DarkNavHeader(title = "Raise a Dispute", onBack = { navController.popBackStack() })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Describe the issue", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = viewModel.reason,
                onValueChange = { if (it.length <= 500) viewModel.reason = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                placeholder = { Text("e.g. The shop did not complete the work as promised...") },
                supportingText = { Text("${viewModel.reason.length} / 500") },
                isError = viewModel.reason.isBlank() && viewModel.error != null
            )

            Text("Add Photos (${viewModel.imageUris.size}/5)", style = MaterialTheme.typography.titleSmall)

            Surface(
                onClick = { if (viewModel.imageUris.size < 5) imagePicker.launch(arrayOf("image/*")) },
                color = OrangeSubtle,
                border = BorderStroke(1.dp, OrangePrimary),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().height(100.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.PhotoCamera, null, tint = OrangePrimary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Tap to add photos", color = OrangePrimary, style = MaterialTheme.typography.labelMedium)
                }
            }

            if (viewModel.imageUris.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(viewModel.imageUris) { uri ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(LightSurface)
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { viewModel.removeImage(uri) },
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.submitDispute(leadId) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !viewModel.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Submit Dispute", fontWeight = FontWeight.Bold)
                }
            }

            Text(
                "This will pause the payment release",
                style = MaterialTheme.typography.bodySmall,
                color = TextSubtle,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}