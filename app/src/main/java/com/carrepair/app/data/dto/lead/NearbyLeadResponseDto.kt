package com.carrepair.app.data.dto.lead

import com.google.gson.annotations.SerializedName

data class NearbyLeadResponseDto(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("carMake") val carMake: String = "",
    @SerializedName("carModel") val carModel: String = "",
    @SerializedName("carYear") val carYear: Int = 0,
    @SerializedName("address") val address: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("expiresAt") val expiresAt: String = "",
    @SerializedName("imageUrls") val imageUrls: List<String> = emptyList(),
    @SerializedName("distanceMeters") val distanceMeters: Double? = null,
    @SerializedName("hasQuoted") val hasQuoted: Boolean? = null,
    @SerializedName("unlocked") val unlocked: Boolean? = null,
    @SerializedName("locked") val locked: Boolean? = null
)

/** Supports common backend envelope shapes for nearby leads. */
data class NearbyLeadsEnvelopeDto(
    @SerializedName("content") val content: List<NearbyLeadResponseDto>? = null,
    @SerializedName("data") val data: List<NearbyLeadResponseDto>? = null,
    @SerializedName("leads") val leads: List<NearbyLeadResponseDto>? = null
)
