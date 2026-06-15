package com.carrepair.app.data.dto.lead


import com.google.gson.annotations.SerializedName

data class CreateLeadRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("carMake") val carMake: String,
    @SerializedName("carModel") val carModel: String,
    @SerializedName("carYear") val carYear: Int,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("imageKeys") val imageKeys: List<String>
)