package com.carrepair.app.data.dto
import com.google.gson.annotations.SerializedName

data class ShopOtpRequestDto(
    val fullName: String,
    val email: String,
    val phone: String,
    val shopName: String,
    val description: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

data class ShopVerifyOtpRequestDto(
    val email: String,
    val otp: String
)

data class ShopAuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val role: String
)

data class ShopDocumentUploadDto(
    val logoKey: String,
    val cnicKey: String,
    val businessDocKey: String
)

data class ShopStatusResponseDto(
    val approvalStatus: String,
    val rejectionReason: String?,
    val shopName: String,
    val credits: Int? = null,
    val creditBalance: Int? = null,
    val leadCredits: Int? = null,
    val balance: Int? = null
) {
    fun resolvedCredits(): Int? = credits ?: creditBalance ?: leadCredits ?: balance
}

data class ShopLoginRequestDto(
    val email: String
)

data class ShopLoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val role: String,
    val approvalStatus: String,
    val rejectionReason: String?
)
data class ShopCreditsResponseDto(
    @SerializedName("shopId")  val shopId: Long? = null,
    @SerializedName("credits") val credits: Int? = null,
    // Some backends return "balance" instead of "credits" — handle both
    @SerializedName("balance") val balance: Int? = null,
    // Some return nested object
    @SerializedName("creditsBalance") val creditsBalance: Int? = null
) {
    fun resolvedBalance(): Int? =
        credits ?: balance ?: creditsBalance
}


data class PurchaseCreditsRequestDto(
    @SerializedName("credits")   val credits: Int,
    @SerializedName("packageId") val packageId: Int
)