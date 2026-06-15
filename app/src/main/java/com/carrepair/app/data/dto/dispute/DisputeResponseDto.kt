package com.carrepair.app.data.dto.dispute

data class DisputeResponseDto(
    val id: Long,
    val leadId: Long,
    val status: String,
    val resolution: String?,
    val adminNote: String?,
    val imageUrls: String?,
    val createdAt: String?,
    val resolvedAt: String?
)