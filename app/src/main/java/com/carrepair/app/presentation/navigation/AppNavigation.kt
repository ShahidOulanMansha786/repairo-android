package com.carrepair.app.presentation.navigation

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.carrepair.app.data.RetrofitClient
import com.carrepair.app.data.repository.FirestoreChatRepository
import com.carrepair.app.domain.security.TokenManager
import com.carrepair.app.domain.service.S3UploadService
import com.carrepair.app.domain.viewmodels.ChannelListViewModel
import com.carrepair.app.domain.viewmodels.ChannelListViewModelFactory
import com.carrepair.app.domain.viewmodels.ChatViewModel
import com.carrepair.app.domain.viewmodels.ChatViewModelFactory
import com.carrepair.app.domain.viewmodels.LeadPostingViewModel
import com.carrepair.app.domain.viewmodels.LeadPostingViewModelFactory
import com.carrepair.app.domain.viewmodels.quotes.QuotesViewModel
import com.carrepair.app.domain.viewmodels.quotes.QuotesViewModelFactory
import com.carrepair.app.domain.viewmodels.ShopHomeViewModel
import com.carrepair.app.domain.viewmodels.ShopHomeViewModelFactory
import com.carrepair.app.domain.viewmodels.ShopLoginViewModel
import com.carrepair.app.domain.viewmodels.ShopLoginViewModelFactory
import com.carrepair.app.domain.viewmodels.ShopRegistrationViewModel
import com.carrepair.app.domain.viewmodels.ShopRegistrationViewModelFactory
import com.carrepair.app.presentation.screens.CarOwnerAuthScreen
import com.carrepair.app.presentation.screens.HomeScreen
import com.carrepair.app.presentation.screens.RoleSelectionScreen
import com.carrepair.app.presentation.screens.Screen
import com.carrepair.app.presentation.screens.SplashScreen
import com.carrepair.app.presentation.screens.VerifyOtpScreen
import com.carrepair.app.presentation.screens.lead.CarDetailsScreen
import com.carrepair.app.presentation.screens.lead.IssueDescriptionScreen
import com.carrepair.app.presentation.screens.lead.LeadDetailScreen
import com.carrepair.app.presentation.screens.lead.LeadLocationScreen
import com.carrepair.app.presentation.screens.lead.LeadSuccessScreen
import com.carrepair.app.presentation.screens.lead.MyLeadsScreen
import com.carrepair.app.presentation.screens.shop.PendingApprovalScreen
import com.carrepair.app.presentation.screens.shop.RejectedScreen
import com.carrepair.app.presentation.screens.shop.ShopDetailsScreen
import com.carrepair.app.presentation.screens.shop.ShopDocumentsScreen
import com.carrepair.app.presentation.screens.shop.ShopHomeScreen
import com.carrepair.app.presentation.screens.shop.ShopLoginEmailScreen
import com.carrepair.app.presentation.screens.shop.ShopLoginOtpScreen
import com.carrepair.app.presentation.screens.shop.ShopOtpScreen
import com.carrepair.app.presentation.screens.shop.ShopOwnerDetailsScreen
import okhttp3.OkHttpClient
import com.carrepair.app.domain.viewmodels.ShopLeadDetailViewModel
import com.carrepair.app.domain.viewmodels.ShopLeadDetailViewModelFactory
import com.carrepair.app.presentation.screens.chat.ChannelListScreen
import com.carrepair.app.presentation.screens.chat.ChatScreen
import com.carrepair.app.presentation.screens.lead.QuotesScreen
import com.carrepair.app.presentation.screens.ProfileScreen
import com.carrepair.app.presentation.screens.shop.SubmitQuoteScreen
import com.carrepair.app.presentation.screens.shop.BuyCreditsScreen
import com.carrepair.app.presentation.screens.shop.ShopLeadDetailScreen
import com.carrepair.app.stomp.StompClientManager
import com.carrepair.app.presentation.screens.shop.CreditsCheckoutScreen
import com.carrepair.app.presentation.screens.lead.LeaveReviewScreen
import com.carrepair.app.presentation.screens.shop.ShopProfileScreen
import com.carrepair.app.domain.viewmodels.ReviewViewModel
import com.carrepair.app.domain.viewmodels.ReviewViewModelFactory

import com.carrepair.app.domain.utils.AppState
import com.carrepair.app.domain.viewmodels.quotes.ShopQuotesViewModel
import com.carrepair.app.domain.viewmodels.quotes.ShopQuotesViewModelFactory
import com.carrepair.app.presentation.screens.RaiseDisputeScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import com.carrepair.app.presentation.screens.payment.*
import com.carrepair.app.presentation.screens.shop.quotes.MyQuotesScreen
import com.carrepair.app.presentation.screens.shop.quotes.QuoteDetailScreen
import com.carrepair.app.presentation.screens.shop.quotes.ShopAcceptedLeadDetailScreen

@Composable
fun AppNavigation(
    navController: NavController
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val application = context.applicationContext as Application

    val isBlocked by AppState.isBlocked.collectAsState()
    val shouldGoToRoleSelection by AppState.shouldNavigateToRoleSelection.collectAsState()

    LaunchedEffect(isBlocked) {
        if (isBlocked) {
            navController.navigate("blocked") {
                popUpTo(0) { inclusive = true }
            }
            AppState.isBlocked.value = false
        }
    }

    LaunchedEffect(shouldGoToRoleSelection) {
        if (shouldGoToRoleSelection) {
            navController.navigate(Screen.RoleSelection.route) {
                popUpTo(0) { inclusive = true }
            }
            AppState.shouldNavigateToRoleSelection.value = false
        }
    }

    val shopViewModelFactory = remember {
        ShopRegistrationViewModelFactory(
            application = application,
            authApi = RetrofitClient.authApi,
            s3UploadService = S3UploadService(
                authApi = RetrofitClient.authApi,
                okHttpClient = OkHttpClient()
            ),
            tokenManager = TokenManager(context)
        )
    }

    val shopLoginViewModelFactory = remember {
        ShopLoginViewModelFactory(
            authApi = RetrofitClient.authApi,
            tokenManager = TokenManager(context)
        )
    }

    val leadPostingViewModelFactory = remember {
        LeadPostingViewModelFactory(
            application = application,
            leadApi = RetrofitClient.leadApi,
            s3UploadService = S3UploadService(
                authApi = RetrofitClient.authApi,
                okHttpClient = OkHttpClient()
            ),
            tokenManager = TokenManager(context)
        )
    }

    val shopHomeViewModelFactory = remember {
        ShopHomeViewModelFactory(
            repairShopApi = RetrofitClient.repairShopApi,
            authApi = RetrofitClient.authApi,
            tokenManager = TokenManager(context)
        )
    }

    val shopLeadDetailViewModelFactory = remember {
        ShopLeadDetailViewModelFactory(
            repairShopApi = RetrofitClient.repairShopApi,
            tokenManager = TokenManager(context)
        )
    }

    val quotesViewModelFactory = remember {
        QuotesViewModelFactory(
            leadApi = RetrofitClient.leadApi,
            tokenManager = TokenManager(context),
            stompClientManager = StompClientManager
        )
    }

    val reviewViewModelFactory = remember {
        ReviewViewModelFactory(
            reviewApi = RetrofitClient.reviewApi,
            s3UploadService = S3UploadService(
                authApi = RetrofitClient.authApi,
                okHttpClient = okhttp3.OkHttpClient()
            ),
            tokenManager = TokenManager(context),
            application = application
        )
    }

    val chatViewModelFactory = remember {
        ChatViewModelFactory(
            FirestoreChatRepository(
                authApi = RetrofitClient.authApi,
                tokenManager = TokenManager(context)
            ),
            authApi = RetrofitClient.authApi,
            tokenManager = TokenManager(context),
            leadApi = RetrofitClient.leadApi
        )
    }

    val channelListViewModelFactory = remember {
        ChannelListViewModelFactory(
            repository = FirestoreChatRepository(
                authApi = RetrofitClient.authApi,
                tokenManager = TokenManager(context)
            ),
            authApi = RetrofitClient.authApi,
            tokenManager = TokenManager(context)
        )
    }

    val channelListViewModel: ChannelListViewModel = remember {
        channelListViewModelFactory.create(ChannelListViewModel::class.java)
    }

    val channels by channelListViewModel.channels.collectAsState()
    val currentUserId by channelListViewModel.currentUserId.collectAsState()

    LaunchedEffect(Unit) {
        channelListViewModel.loadChannels()
    }
//    fun getHomeRoute(context: android.content.Context): String {
//        val role = TokenManager(context).getRole()
//        return if (role == "REPAIR_SHOP") "shop_graph" else "home"
//    }

    var homeRoute by remember { mutableStateOf(
        if (tokenManager.getRole() == "SHOP_OWNER") "shop_graph" else "home"
    ) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    navController = navController,
                    homeRoute = homeRoute,
                    channels = channels,
                    currentUserId = currentUserId
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController as NavHostController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Screen.Splash.route) {
                SplashScreen(navController = navController)
            }

            composable(Screen.RoleSelection.route) {
                RoleSelectionScreen(navController = navController)
            }

            composable(Screen.CarOwnerAuth.route) {
                CarOwnerAuthScreen(navController = navController)
            }

            composable(
                route = Screen.VerifyOtp.route,
                arguments = listOf(
                    navArgument("email") { type = NavType.StringType },
                    navArgument("flowType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                val flowType = backStackEntry.arguments?.getString("flowType") ?: ""
                VerifyOtpScreen(
                    email = email,
                    flowType = flowType,
                    onVerified = {
                        homeRoute = if (tokenManager.getRole() == "REPAIR_SHOP") "shop_graph" else "home"
                        navController.navigate(homeRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                val tokenManager = TokenManager(LocalContext.current)
                HomeScreen(
                    tokenManager = TokenManager(LocalContext.current),
                    authApi = RetrofitClient.authApi,
                    navController = navController,
                    onLogout = {
                        tokenManager.clearTokens()
                        navController.navigate(Screen.RoleSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            navigation(
                startDestination = "shop/home",
                route = "shop_graph"
            ) {
                composable("shop/home") { entry ->
                    val backStackEntry = remember(entry) {
                        navController.getBackStackEntry("shop_graph")
                    }
                    val viewModel = viewModel<ShopHomeViewModel>(
                        viewModelStoreOwner = backStackEntry,
                        factory = shopHomeViewModelFactory
                    )
                    ShopHomeScreen(
                        viewModel = viewModel,
                        tokenManager = TokenManager(LocalContext.current),
                        onLeadClick = { leadId ->
                            navController.navigate("shop/leads/$leadId")
                        },
                        onBuyCreditsClick = {
                            navController.navigate("shop/buy_credits")
                        }
                    )
                }

                composable(
                    route = "shop/leads/{leadId}",
                    arguments = listOf(navArgument("leadId") { type = NavType.LongType })
                ) { entry ->
                    val leadId = entry.arguments?.getLong("leadId") ?: 0L
                    val backStackEntry = remember(entry) {
                        navController.getBackStackEntry("shop_graph")
                    }
                    val shopHomeViewModel = viewModel<ShopHomeViewModel>(
                        viewModelStoreOwner = backStackEntry,
                        factory = shopHomeViewModelFactory
                    )
                    val shopLeadDetailViewModel = viewModel<ShopLeadDetailViewModel>(
                        factory = shopLeadDetailViewModelFactory
                    )
                    val chatViewModel = viewModel<ChatViewModel>(
                        factory = chatViewModelFactory
                    )
                    ShopLeadDetailScreen(
                        leadId = leadId,
                        navController = navController,
                        shopHomeViewModel = shopHomeViewModel,
                        shopLeadDetailViewModel = shopLeadDetailViewModel,
                        chatViewModel = chatViewModel,
                        onSubmitQuote = {
                            navController.navigate("shop/leads/$leadId/submit_quote")
                        }
                    )
                }

                composable(
                    route = "shop/leads/{leadId}/submit_quote",
                    arguments = listOf(navArgument("leadId") { type = NavType.LongType })
                ) { entry ->
                    val leadId = entry.arguments?.getLong("leadId") ?: 0L
                    val backStackEntry = remember(entry) {
                        navController.getBackStackEntry("shop_graph")
                    }
                    val shopHomeViewModel = viewModel<ShopHomeViewModel>(
                        viewModelStoreOwner = backStackEntry,
                        factory = shopHomeViewModelFactory
                    )
                    val shopLeadDetailViewModel = viewModel<ShopLeadDetailViewModel>(
                        factory = shopLeadDetailViewModelFactory
                    )
                    SubmitQuoteScreen(
                        leadId = leadId,
                        navController = navController,
                        shopHomeViewModel = shopHomeViewModel,
                        shopLeadDetailViewModel = shopLeadDetailViewModel
                    )
                }

                composable("shop/buy_credits") { entry ->
                    val backStackEntry = remember(entry) {
                        navController.getBackStackEntry("shop_graph")
                    }
                    val shopHomeViewModel = viewModel<ShopHomeViewModel>(
                        viewModelStoreOwner = backStackEntry,
                        factory = shopHomeViewModelFactory
                    )
                    BuyCreditsScreen(
                        navController = navController,
                        viewModel = shopHomeViewModel
                    )
                }
                composable(
                    route = "credits/checkout/{credits}/{packageId}/{price}",
                    arguments = listOf(
                        navArgument("credits")   { type = NavType.IntType },
                        navArgument("packageId") { type = NavType.IntType },
                        navArgument("price")     { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    CreditsCheckoutScreen(
                        navController  = navController,
                        credits        = backStackEntry.arguments?.getInt("credits") ?: 0,
                        packageId      = backStackEntry.arguments?.getInt("packageId") ?: 0,
                        price          = backStackEntry.arguments?.getString("price")?.toDoubleOrNull() ?: 0.0,
                        repairShopApi  = RetrofitClient.repairShopApi,
                        tokenManager   = tokenManager
                    )
                }

                composable("shop/my-quotes") {
                    MyQuotesScreen(
                        onNavigateBack = { navController.popBackStack() },
                        navController = navController
                    )
                }

                composable("shop/quote-detail/{quoteId}/{leadId}") { backStack ->
                    val quoteId = backStack.arguments?.getString("quoteId")?.toLong() ?: return@composable
                    val leadId = backStack.arguments?.getString("leadId")?.toLong() ?: return@composable

                    val shopQuotesViewModel: ShopQuotesViewModel = viewModel(
                        factory = ShopQuotesViewModelFactory(RetrofitClient.repairShopApi)
                    )
                    val quotes by shopQuotesViewModel.quotes.collectAsState()
                    val isLoading by shopQuotesViewModel.isLoading.collectAsState()
                    val quote = quotes.find { it.quoteId == quoteId }

                    LaunchedEffect(Unit) { shopQuotesViewModel.loadQuotes() }

                    when {
                        isLoading -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFFE86A2E))
                            }
                        }
                        quote != null -> {
                            QuoteDetailScreen(
                                quote = quote,
                                onBack = { navController.popBackStack() },
                                onViewLeadDetails = { navController.navigate("shop/accepted-lead-detail/$it") },
                                navController = navController,
                                leadApi = RetrofitClient.leadApi
                            )
                        }
                        else -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Quote not found", color = Color.Gray)
                            }
                        }
                    }
                }

                composable("shop/accepted-lead-detail/{leadId}") { backStack ->
                    val leadId = backStack.arguments?.getString("leadId")?.toLong() ?: return@composable
                    ShopAcceptedLeadDetailScreen(
                        leadId = leadId,
                        onBack = { navController.popBackStack() },
                        leadApi = RetrofitClient.leadApi
                    )
                }
            }

            navigation(
                startDestination = "shop_registration/step1",
                route = "shop_reg_graph"
            ) {
                composable("shop_registration/step1") { entry ->
                    val backStackEntry = remember(entry) {
                        navController.getBackStackEntry("shop_reg_graph")
                    }
                    val viewModel = viewModel<ShopRegistrationViewModel>(
                        viewModelStoreOwner = backStackEntry,
                        factory = shopViewModelFactory
                    )
                    ShopOwnerDetailsScreen(
                        viewModel = viewModel,
                        navController = navController,
                        onNext = { navController.navigate("shop_registration/step2") }
                    )
                }

                composable("shop_registration/step2") { entry ->
                    val backStackEntry = remember(entry) {
                        navController.getBackStackEntry("shop_reg_graph")
                    }
                    val viewModel = viewModel<ShopRegistrationViewModel>(
                        viewModelStoreOwner = backStackEntry,
                        factory = shopViewModelFactory
                    )
                    ShopDetailsScreen(
                        viewModel = viewModel,
                        navController = navController,
                        onNext = { navController.navigate("shop_registration/otp") }
                    )
                }

                composable("shop_registration/otp") { entry ->
                    val backStackEntry = remember(entry) {
                        navController.getBackStackEntry("shop_reg_graph")
                    }
                    val viewModel = viewModel<ShopRegistrationViewModel>(
                        viewModelStoreOwner = backStackEntry,
                        factory = shopViewModelFactory
                    )
                    ShopOtpScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable("shop_registration/step3") { entry ->
                    val backStackEntry = remember(entry) {
                        navController.getBackStackEntry("shop_reg_graph")
                    }
                    val viewModel = viewModel<ShopRegistrationViewModel>(
                        viewModelStoreOwner = backStackEntry,
                        factory = shopViewModelFactory
                    )
                    ShopDocumentsScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }

            composable("pending_approval") {
                PendingApprovalScreen(
                    tokenManager = TokenManager(LocalContext.current),
                    authApi = RetrofitClient.authApi,
                    navController = navController
                )
            }

            navigation(
                startDestination = "shop_login/email",
                route = "shop_login_graph"
            ) {
                composable("shop_login/email") { entry ->
                    val backStackEntry = remember(entry) {
                        navController.getBackStackEntry("shop_login_graph")
                    }
                    val viewModel = viewModel<ShopLoginViewModel>(
                        viewModelStoreOwner = backStackEntry,
                        factory = shopLoginViewModelFactory
                    )
                    ShopLoginEmailScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable("shop_login/otp") { entry ->
                    val backStackEntry = remember(entry) {
                        navController.getBackStackEntry("shop_login_graph")
                    }
                    val viewModel = viewModel<ShopLoginViewModel>(
                        viewModelStoreOwner = backStackEntry,
                        factory = shopLoginViewModelFactory
                    )
                    ShopLoginOtpScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }

            composable("rejected_screen") {
                val viewModel = viewModel<ShopRegistrationViewModel>(
                    factory = shopViewModelFactory
                )
                RejectedScreen(
                    navController = navController,
                    authApi = RetrofitClient.authApi,
                    tokenManager = TokenManager(LocalContext.current),
                    viewModel = viewModel
                )
            }

            navigation(
                startDestination = "post_lead/step1",
                route = "lead_posting_graph"
            ) {
                composable("post_lead/step1") { entry ->
                    val backStackEntry = remember(entry) {
                        navController.getBackStackEntry("lead_posting_graph")
                    }
                    val viewModel = viewModel<LeadPostingViewModel>(
                        viewModelStoreOwner = backStackEntry,
                        factory = leadPostingViewModelFactory
                    )
                    CarDetailsScreen(
                        navController = navController,
                        viewModel = viewModel
                    )
                }

                composable("post_lead/step2") { entry ->
                    val backStackEntry = remember(entry) {
                        navController.getBackStackEntry("lead_posting_graph")
                    }
                    val viewModel = viewModel<LeadPostingViewModel>(
                        viewModelStoreOwner = backStackEntry,
                        factory = leadPostingViewModelFactory
                    )
                    IssueDescriptionScreen(
                        navController = navController,
                        viewModel = viewModel
                    )
                }

                composable("post_lead/step3") { entry ->
                    val backStackEntry = remember(entry) {
                        navController.getBackStackEntry("lead_posting_graph")
                    }
                    val viewModel = viewModel<LeadPostingViewModel>(
                        viewModelStoreOwner = backStackEntry,
                        factory = leadPostingViewModelFactory
                    )
                    LeadLocationScreen(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }

            composable(
                route = "leads/success/{leadId}",
                arguments = listOf(navArgument("leadId") { type = NavType.LongType })
            ) { backStackEntry ->
                val leadId = backStackEntry.arguments?.getLong("leadId") ?: 0L
                LeadSuccessScreen(
                    navController = navController,
                    leadId = leadId
                )
            }

            composable("leads/my") {
                MyLeadsScreen(
                    navController = navController,
                    leadApi = RetrofitClient.leadApi,
                    tokenManager = TokenManager(LocalContext.current)
                )
            }

            composable(
                route = "leads/{leadId}",
                arguments = listOf(navArgument("leadId") { type = NavType.LongType })
            ) { backStackEntry ->
                val leadId = backStackEntry.arguments?.getLong("leadId") ?: 0L
                val chatViewModel = viewModel<ChatViewModel>(factory = chatViewModelFactory)
                LeadDetailScreen(
                    navController = navController,
                    leadId = leadId,
                    leadApi = RetrofitClient.leadApi,
                    tokenManager = TokenManager(LocalContext.current),
                    chatViewModel = chatViewModel
                )
            }

            composable(
                route = "leads/{leadId}/quotes?leadStatus={leadStatus}",
                arguments = listOf(
                    navArgument("leadId") { type = NavType.LongType },
                    navArgument("leadStatus") {
                        type = NavType.StringType
                        defaultValue = "OPEN"
                    }
                )
            ) { backStackEntry ->
                val leadId = backStackEntry.arguments?.getLong("leadId") ?: 0L
                val leadStatus = backStackEntry.arguments?.getString("leadStatus") ?: "OPEN"
                val viewModel = viewModel<QuotesViewModel>(factory = quotesViewModelFactory)
                QuotesScreen(
                    leadId = leadId,
                    leadStatus = leadStatus,
                    navController = navController,
                    viewModel = viewModel
                )
            }

            composable(
                route = "chat/{channelId}",
                arguments = listOf(navArgument("channelId") { type = NavType.StringType })
            ) { backStackEntry ->
                val channelId = backStackEntry.arguments?.getString("channelId") ?: ""
                val chatViewModel = viewModel<ChatViewModel>(factory = chatViewModelFactory)
                LaunchedEffect(channelId) {
                    chatViewModel.init(channelId)
                }
                ChatScreen(
                    channelId = channelId,
                    navController = navController,
                    viewModel = chatViewModel
                )
            }

            composable(Screen.Messages.route) {
                ChannelListScreen(
                    navController = navController,
                    viewModel = channelListViewModel
                )
            }

            composable(Screen.History.route) {
                MyLeadsScreen(
                    navController = navController,
                    leadApi = RetrofitClient.leadApi,
                    tokenManager = TokenManager(LocalContext.current)
                )
            }

            composable(Screen.Profile.route) {
                val tokenManager = TokenManager(LocalContext.current)
                ProfileScreen(
                    tokenManager = TokenManager(LocalContext.current),
                    authApi = RetrofitClient.authApi,
                    navController = navController,
                    onLogout = {
                        tokenManager.clearTokens()
                        navController.navigate(Screen.RoleSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "payment/confirm/{leadId}/{quoteId}/{quotedPrice}/{shopName}",
                arguments = listOf(
                    navArgument("leadId")      { type = NavType.LongType },
                    navArgument("quoteId")     { type = NavType.LongType },
                    navArgument("quotedPrice") { type = NavType.StringType },
                    navArgument("shopName")    { type = NavType.StringType }
                )
            ) { backStackEntry ->
                PaymentConfirmScreen(
                    navController   = navController,
                    leadId          = backStackEntry.arguments?.getLong("leadId") ?: 0L,
                    quoteId         = backStackEntry.arguments?.getLong("quoteId") ?: 0L,
                    quotedPrice     = backStackEntry.arguments?.getString("quotedPrice")?.toDouble() ?: 0.0,
                    shopName        = URLDecoder.decode(
                        backStackEntry.arguments?.getString("shopName") ?: "",
                        StandardCharsets.UTF_8.toString()
                    ),
                    paymentApi      = RetrofitClient.paymentApi,
                    tokenManager    = tokenManager
                )
            }

            // ── Route 2: Payment Processing (cinematic animation + Chrome Custom Tab) ─
            composable(
                route = "payment/process/{paymentId}/{leadId}?url={paymentUrl}&amount={amount}&shop={shopName}",
                arguments = listOf(
                    navArgument("paymentId")  { type = NavType.LongType },
                    navArgument("leadId")     { type = NavType.LongType },
                    navArgument("paymentUrl") { type = NavType.StringType; defaultValue = "" },
                    navArgument("amount")     { type = NavType.StringType; defaultValue = "0" },
                    navArgument("shopName")   { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                PaymentProcessScreen(
                    navController = navController,
                    paymentId     = backStackEntry.arguments?.getLong("paymentId") ?: 0L,
                    leadId        = backStackEntry.arguments?.getLong("leadId") ?: 0L,
                    paymentUrl    = URLDecoder.decode(
                        backStackEntry.arguments?.getString("paymentUrl") ?: "",
                        StandardCharsets.UTF_8.toString()
                    ),
                    amount        = backStackEntry.arguments?.getString("amount")?.toDoubleOrNull() ?: 0.0,
                    shopName      = URLDecoder.decode(
                        backStackEntry.arguments?.getString("shopName") ?: "",
                        StandardCharsets.UTF_8.toString()
                    ),
                    paymentApi    = RetrofitClient.paymentApi,
                    tokenManager  = tokenManager
                )
            }

// new route after the process route:

            composable(
                route = "payment/checkout/{paymentId}/{leadId}/{amount}/{shopName}",
                arguments = listOf(
                    navArgument("paymentId") { type = NavType.LongType },
                    navArgument("leadId")    { type = NavType.LongType },
                    navArgument("amount")    { type = NavType.StringType },
                    navArgument("shopName")  { type = NavType.StringType }
                )
            ) { backStackEntry ->
                MockCheckoutScreen(
                    navController = navController,
                    paymentId     = backStackEntry.arguments?.getLong("paymentId") ?: 0L,
                    leadId        = backStackEntry.arguments?.getLong("leadId") ?: 0L,
                    amount        = backStackEntry.arguments?.getString("amount")?.toDoubleOrNull() ?: 0.0,
                    shopName      = URLDecoder.decode(
                        backStackEntry.arguments?.getString("shopName") ?: "",
                        StandardCharsets.UTF_8.toString()
                    ),
                    paymentApi    = RetrofitClient.paymentApi,
                    tokenManager  = tokenManager
                )
            }


            composable(
                route = "payment/success/{paymentId}/{leadId}",
                arguments = listOf(
                    navArgument("paymentId") { type = NavType.LongType },
                    navArgument("leadId")    { type = NavType.LongType }
                )
            ) { backStackEntry ->
                PaymentSuccessScreen(
                    navController = navController,
                    paymentId     = backStackEntry.arguments?.getLong("paymentId") ?: 0L,
                    leadId        = backStackEntry.arguments?.getLong("leadId") ?: 0L,
                    paymentApi    = RetrofitClient.paymentApi,
                    tokenManager  = tokenManager
                )
            }

            composable(
                route = "payment/escrow/{leadId}",
                arguments = listOf(
                    navArgument("leadId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                EscrowStatusScreen(
                    navController = navController,
                    leadId        = backStackEntry.arguments?.getLong("leadId") ?: 0L,
                    paymentApi    = RetrofitClient.paymentApi,
                    tokenManager  = tokenManager
                )
            }

            composable(
                route = "dispute/raise/{leadId}",
                arguments = listOf(navArgument("leadId") { type = NavType.LongType })
            ) { backStackEntry ->
                val leadId = backStackEntry.arguments?.getLong("leadId") ?: return@composable
                RaiseDisputeScreen(
                    navController = navController,
                    leadId = leadId,
                    disputeApi = RetrofitClient.disputeApi,
                    tokenManager = tokenManager
                )
            }

            composable(
                route = Screen.LeaveReview.route,
                arguments = listOf(
                    navArgument("leadId") { type = NavType.LongType },
                    navArgument("shopName") { type = NavType.StringType },
                    navArgument("shopId") { type = NavType.LongType },
                    navArgument("carInfo") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val leadId = backStackEntry.arguments?.getLong("leadId") ?: 0L
                val shopName = URLDecoder.decode(backStackEntry.arguments?.getString("shopName") ?: "", StandardCharsets.UTF_8.toString())
                val shopId = backStackEntry.arguments?.getLong("shopId") ?: 0L
                val carInfo = URLDecoder.decode(backStackEntry.arguments?.getString("carInfo") ?: "", StandardCharsets.UTF_8.toString())
                val reviewViewModel = viewModel<ReviewViewModel>(factory = reviewViewModelFactory)
                LeaveReviewScreen(
                    leadId = leadId,
                    shopName = shopName,
                    shopId = shopId,
                    carInfo = carInfo,
                    navController = navController,
                    viewModel = reviewViewModel
                )
            }

            composable(
                route = Screen.ShopProfile.route,
                arguments = listOf(
                    navArgument("shopId") { type = NavType.LongType },
                    navArgument("shopName") { type = NavType.StringType },
                    navArgument("logoUrl") { type = NavType.StringType; nullable = true; defaultValue = "" }
                )
            ) { backStackEntry ->
                val shopId = backStackEntry.arguments?.getLong("shopId") ?: 0L
                val shopName = URLDecoder.decode(backStackEntry.arguments?.getString("shopName") ?: "", StandardCharsets.UTF_8.toString())
                val logoUrlEncoded = backStackEntry.arguments?.getString("logoUrl") ?: ""
                val logoUrl = if (logoUrlEncoded.isNotEmpty()) URLDecoder.decode(logoUrlEncoded, StandardCharsets.UTF_8.toString()) else null
                val reviewViewModel = viewModel<ReviewViewModel>(factory = reviewViewModelFactory)
                ShopProfileScreen(
                    shopId = shopId,
                    shopName = shopName,
                    logoUrl = logoUrl,
                    navController = navController,
                    viewModel = reviewViewModel
                )
            }


        }
    }
}
