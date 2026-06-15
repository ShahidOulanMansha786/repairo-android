package com.carrepair.app.data.dto.lead

import com.google.gson.annotations.SerializedName

data class LeadResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("carMake") val carMake: String,
    @SerializedName("carModel") val carModel: String,
    @SerializedName("carYear") val carYear: Int,
    @SerializedName("address") val address: String,
    @SerializedName("status") val status: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("expiresAt") val expiresAt: String,
    @SerializedName("imageUrls") val imageUrls: List<String>
)