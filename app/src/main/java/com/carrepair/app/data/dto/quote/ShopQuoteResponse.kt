package com.carrepair.app.data.dto.quote

data class ShopQuoteResponse(
    val quoteId: Long,
    val leadId: Long,
    val leadTitle: String,
    val carMake: String,
    val carModel: String,
    val carYear: Int,
    val carOwnerName: String,
    val price: Double,
    val message: String,
    val status: String,
    val createdAt: String
)