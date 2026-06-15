package com.carrepair.app.data.dto

import com.google.gson.annotations.SerializedName

data class SubmitReviewRequestDto(
    @SerializedName("leadId") val leadId: Long,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null
)

data class ReviewResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("leadId") val leadId: Long,
    @SerializedName("repairShopId") val repairShopId: Long,
    @SerializedName("carOwnerId") val carOwnerId: Long,
    @SerializedName("carOwnerName") val carOwnerName: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("createdAt") val createdAt: String
)
