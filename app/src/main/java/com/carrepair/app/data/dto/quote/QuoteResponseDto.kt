package com.carrepair.app.data.dto.quote


data class QuoteResponseDto(
    val id: Long,
    val leadId: Long,
    val repairShopId: Long,
    val shopName: String,
    val shopLogoUrl: String?,
    val price: Double,
    val message: String?,
    val status: String,
    val createdAt: String,
    val channelId: String,
    val averageRating: Double?,
    val reviewCount: Int?
)