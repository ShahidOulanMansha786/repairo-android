package com.carrepair.app.presentation.screens

sealed class Screen(val route: String) {

    // Splash is the first screen the app opens on
    object Splash : Screen("splash")

    // Role selection — Car Owner or Repair Shop
    object RoleSelection : Screen("role_selection")

    // Placeholder for repair shop flow
    object RepairShopPlaceholder : Screen("repair_shop_placeholder")

    // Car owner auth screen — has Sign Up and Log In tabs
    object CarOwnerAuth : Screen("car_owner_auth")

    // Verify OTP — receives email and flowType as arguments
    // Route pattern: verify_otp/{email}/{flowType}
    object VerifyOtp : Screen("verify_otp/{email}/{flowType}") {

        // This function builds the actual route string with real values
        // Example: verify_otp/user@gmail.com/signup
        fun createRoute(email: String, flowType: String): String {
            return "verify_otp/$email/$flowType"
        }
    }

    // Home screen placeholder
    object Home : Screen("home")
    object Messages : Screen("messages")
    object History : Screen("history")
    object Profile : Screen("profile")

    object LeaveReview : Screen("leave_review/{leadId}/{shopName}/{shopId}/{carInfo}") {
        fun createRoute(leadId: Long, shopName: String, shopId: Long, carInfo: String): String {
            val encodedShop = java.net.URLEncoder.encode(shopName, "UTF-8")
            val encodedCar = java.net.URLEncoder.encode(carInfo, "UTF-8")
            return "leave_review/$leadId/$encodedShop/$shopId/$encodedCar"
        }
    }

    object ShopProfile : Screen("shop_profile/{shopId}/{shopName}?logoUrl={logoUrl}") {
        fun createRoute(shopId: Long, shopName: String, logoUrl: String?): String {
            val encodedShop = java.net.URLEncoder.encode(shopName, "UTF-8")
            val encodedLogo = if (logoUrl != null) java.net.URLEncoder.encode(logoUrl, "UTF-8") else ""
            return "shop_profile/$shopId/$encodedShop?logoUrl=$encodedLogo"
        }
    }
}