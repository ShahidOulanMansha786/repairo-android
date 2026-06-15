package com.carrepair.app.data.dto.auth

data class PresignedUrlRequestDto(
    val folder: String,
    val fileName: String,
    val contentType: String
)