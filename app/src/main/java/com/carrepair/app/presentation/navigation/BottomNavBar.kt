package com.carrepair.app.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.carrepair.app.data.repository.ChatChannel
import com.carrepair.app.presentation.ui.theme.*

data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

val ownerBottomNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, "home_tab"),
    BottomNavItem("Messages", Icons.Default.Chat, "messages"),
    BottomNavItem("History", Icons.Default.History, "history"),
    BottomNavItem("Profile", Icons.Default.Person, "profile")
)

val shopBottomNavItems = listOf(
    BottomNavItem("Browse", Icons.Default.Search, "shop_graph"),
    BottomNavItem("My Quotes", Icons.Default.Description, "shop/my-quotes"),
    BottomNavItem("Messages", Icons.Default.Chat, "messages"),
    BottomNavItem("Profile", Icons.Default.Person, "profile")
)

val bottomNavRoutes = setOf(
    "home",
    "home_tab",
    "shop/home",
    "shop_graph",
    "shop/my-quotes",
    "messages",
    "history",
    "profile"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavBar(
    navController: NavController,
    homeRoute: String,
    channels: List<ChatChannel>,
    currentUserId: String?
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val unreadCount = remember(channels, currentUserId) {
        channels.count { channel ->
            channel.lastMessage != null &&
                    channel.lastMessageSenderId != currentUserId
        }
    }

    val isShop = homeRoute == "shop_graph"
    val displayedItems = if (isShop) shopBottomNavItems else ownerBottomNavItems

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightSurface)
    ) {
        HorizontalDivider(color = LightBorder, thickness = 1.dp)
        NavigationBar(
            containerColor = LightSurface,
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
        ) {
            displayedItems.forEach { item ->
                val resolvedRoute = when (item.route) {
                    "home_tab" -> homeRoute
                    else -> item.route
                }

                val isSelected = when (item.route) {
                "home_tab" -> currentRoute == "home"
                "shop_graph" -> currentRoute?.startsWith("shop/") == true
                        && currentRoute != "shop/my-quotes"
                else -> currentRoute == item.route
            }

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1.0f,
                    animationSpec = tween(200),
                    label = "IconScale"
                )

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) OrangePrimary else NavInactive,
                    animationSpec = tween(200),
                    label = "IconColor"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) OrangePrimary else NavInactive,
                    animationSpec = tween(200),
                    label = "TextColor"
                )

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            val popUpRoute = if (isShop) "shop_graph" else "home"
                            navController.navigate(resolvedRoute) {
                                popUpTo(popUpRoute) {
                                    saveState = true
                                    inclusive = false
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangePrimary,
                        unselectedIconColor = NavInactive,
                        selectedTextColor = OrangePrimary,
                        unselectedTextColor = NavInactive,
                        indicatorColor = Color.Transparent
                    ),
                    icon = {
                        if (item.route == "messages" && unreadCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = OrangePrimary,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = unreadCount.toString(),
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = iconColor,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .scale(scale)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = iconColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .scale(scale)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = item.label,
                            color = textColor,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        )
                    }
                )
            }
        }
    }
}
