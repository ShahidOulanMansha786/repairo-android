package com.carrepair.app.presentation.screens.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.carrepair.app.data.repository.ChatChannel
import com.carrepair.app.domain.viewmodels.ChannelListViewModel
import com.carrepair.app.presentation.components.DarkNavHeader
import com.carrepair.app.presentation.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelListScreen(
    navController: NavController,
    viewModel: ChannelListViewModel
) {
    val channels by viewModel.channels.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        viewModel.loadChannels()
    }

    val filteredChannels = remember(channels, searchQuery) {
        if (searchQuery.isBlank()) channels
        else channels.filter {
            it.repairShopName.contains(searchQuery, ignoreCase = true) ||
            it.carOwnerName.contains(searchQuery, ignoreCase = true) ||
            it.leadTitle.contains(searchQuery, ignoreCase = true) ||
            (it.lastMessage ?: "").contains(searchQuery, ignoreCase = true)
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
                ) {
                    // Header Section
                    DarkNavHeader(
                        title = "Messages",
                        trailingContent = {
                            IconButton(onClick = { /* menu click */ }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = TextWhite)
                            }
                        }
                    )

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search messages...", color = TextSubtle) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NavInactive) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.small,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedPlaceholderColor = TextSubtle,
                            unfocusedPlaceholderColor = TextSubtle,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = LightBorder,
                            focusedContainerColor = LightSurface,
                            unfocusedContainerColor = LightSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LightBackground)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    // List Content
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when {
                            isLoading -> {
                                CircularProgressIndicator(
                                    color = OrangePrimary,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            filteredChannels.isEmpty() -> {
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = null,
                                        tint = NavInactive,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Text(
                                        text = "No messages yet",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextSubtle,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Messages with repair shops will appear here.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSubtle
                                    )
                                }
                            }

                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    itemsIndexed(filteredChannels) { index, channel ->
                                        var itemVisible by remember { mutableStateOf(false) }
                                        LaunchedEffect(channel.channelId) {
                                            delay(index * 60L)
                                            itemVisible = true
                                        }

                                        val itemAlpha by animateFloatAsState(
                                            targetValue = if (itemVisible) 1f else 0f,
                                            animationSpec = tween(300),
                                            label = "ItemAlpha"
                                        )

                                        Box(modifier = Modifier.graphicsLayer { alpha = itemAlpha }) {
                                            ChannelRow(
                                                channel = channel,
                                                currentUserId = currentUserId ?: "",
                                                onClick = {
                                                    navController.navigate("chat/${channel.channelId}")
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
    }
}

@Composable
private fun ChannelRow(
    channel: ChatChannel,
    currentUserId: String,
    onClick: () -> Unit
) {
    val isCarOwner = currentUserId == channel.carOwnerId
    val otherPartyName = if (isCarOwner) channel.repairShopName else channel.carOwnerName

    val hasUnread = channel.lastMessage != null &&
            channel.lastMessageSenderId != currentUserId

    val timeText = remember(channel.lastMessageAt) {
        channel.lastMessageAt?.let { date ->
            val now = Calendar.getInstance()
            val msgCal = Calendar.getInstance().apply { time = date }
            val isToday = now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)
            if (isToday) {
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
            } else {
                SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
            }
        } ?: ""
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Surface(
        color = LightSurface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar circle 48dp StatusBlueTint
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(StatusBlueTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = StatusBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Details Content
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = otherPartyName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextDark,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSubtle
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (channel.leadTitle.isNotBlank()) {
                        Text(
                            text = channel.leadTitle.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangePrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Text(
                        text = channel.lastMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSubtle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Unread dot
                if (hasUnread) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .graphicsLayer { alpha = dotAlpha }
                            .clip(CircleShape)
                            .background(OrangePrimary)
                    )
                }
            }

            Divider(
                color = LightBorder,
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}