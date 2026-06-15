package com.carrepair.app.presentation.screens.chat

import com.carrepair.app.domain.viewmodels.ChatViewModel


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.carrepair.app.data.repository.ChatMessage
import com.carrepair.app.domain.viewmodels.SendUiState
import java.text.SimpleDateFormat
import java.util.*
import com.carrepair.app.presentation.components.DarkNavHeader
import com.carrepair.app.presentation.ui.theme.*
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    channelId: String,
    viewModel: ChatViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val channelInfo by viewModel.channelInfo.collectAsState()
    val sendUiState by viewModel.sendUiState.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    var textState by remember { mutableStateOf("") }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.sendImage(channelId, it, context)
        }
    }

    val otherPartyName = remember(channelInfo, currentUserId) {
        channelInfo?.let {
            if (currentUserId == it.carOwnerId) it.repairShopName else it.carOwnerName
        } ?: ""
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (fullScreenImageUrl != null) {
        Dialog(
            onDismissRequest = { fullScreenImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = fullScreenImageUrl,
                    contentDescription = "Full screen image",
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { fullScreenImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            DarkNavHeader(
                title = otherPartyName,
                onBack = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages) { message ->
                    val isFromMe = message.senderId == currentUserId
                    MessageBubble(
                        message = message,
                        isFromMe = isFromMe,
                        onImageClick = { url -> fullScreenImageUrl = url }
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightBackground)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        imagePickerLauncher.launch(arrayOf("image/*"))
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach image",
                        tint = OrangePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...", color = TextSubtle) },
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (textState.isNotBlank()) {
                                viewModel.sendText(channelId, textState)
                                textState = ""
                            }
                        }
                    ),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = LightBorder,
                        focusedContainerColor = LightSurface,
                        unfocusedContainerColor = LightSurface
                    )
                )

                FilledIconButton(
                    onClick = {
                        if (textState.isNotBlank()) {
                            viewModel.sendText(channelId, textState)
                            textState = ""
                        }
                    },
                    enabled = textState.isNotBlank() && sendUiState != SendUiState.Sending,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = OrangePrimary,
                        contentColor = Color.White,
                        disabledContainerColor = OrangePrimary.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                ) {
                    if (sendUiState == SendUiState.Sending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isFromMe: Boolean,
    onImageClick: (String) -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val timeText = remember(message.createdAt) {
        message.createdAt?.let { timeFormat.format(it) } ?: ""
    }

    val bubbleShape = if (isFromMe) {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 4.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .clip(bubbleShape)
                .background(if (isFromMe) OrangePrimary else LightSurface)
                .then(
                    if (!isFromMe) Modifier.border(width = 1.dp, color = LightBorder, shape = bubbleShape)
                    else Modifier
                )
                .padding(12.dp)
        ) {
            Column {
                if (message.imageUrl != null) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = "Image message",
                        modifier = Modifier
                            .width(200.dp)
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onImageClick(message.imageUrl) },
                        contentScale = ContentScale.Crop
                    )
                    if (message.text != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
                if (message.text != null) {
                    Text(
                        text = message.text,
                        color = if (isFromMe) Color.White else TextDark,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.75f)
        ) {
            Text(
                text = timeText,
                fontSize = 11.sp,
                color = TextSubtle
            )
            if (isFromMe && message.isRead) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "✓✓",
                    fontSize = 11.sp,
                    color = OrangePrimary
                )
            }
        }
    }
}