package com.carrepair.app.data.dto.auth

data class PresignedUrlResponseDto(
    val uploadUrl: String,
    val objectKey: String
)