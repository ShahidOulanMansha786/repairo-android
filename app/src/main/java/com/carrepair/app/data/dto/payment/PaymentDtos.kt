package com.carrepair.app.data.dto.payment

import com.google.gson.annotations.SerializedName

data class PaymentInitiateRequestDto(
    @SerializedName("leadId") val leadId: Long,
    @SerializedName("quoteId") val quoteId: Long
)

data class PaymentSessionResponseDto(
    @SerializedName("paymentId") val paymentId: Long,
    @SerializedName("paymentUrl") val paymentUrl: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("platformFee") val platformFee: Double,
    @SerializedName("shopReceives") val shopReceives: Double,
    @SerializedName("currency") val currency: String,
    @SerializedName("escrowNote") val escrowNote: String
)

data class PaymentStatusResponseDto(
    @SerializedName("paymentId") val paymentId: Long,
    @SerializedName("leadId") val leadId: Long,
    @SerializedName("amountTotal") val amountTotal: Double,
    @SerializedName("platformFee") val platformFee: Double,
    @SerializedName("amountPayableToShop") val amountPayableToShop: Double,
    @SerializedName("escrowStatus") val escrowStatus: String,
    @SerializedName("paidAt") val paidAt: String?,
    @SerializedName("eligibleReleaseAt") val eligibleReleaseAt: String?,
    @SerializedName("releasedAt") val releasedAt: String?,
    @SerializedName("disputeWindowHours") val disputeWindowHours: Int,
    @SerializedName("currency") val currency: String
)
