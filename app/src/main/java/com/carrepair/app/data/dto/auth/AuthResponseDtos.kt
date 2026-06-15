package com.carrepair.app.data.dto.auth

data class MessageResponseDto(val message: String)

data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val role: String
)

data class AccessTokenResponseDto(
    val accessToken: String,
    val refreshToken: String)
data class ApiErrorResponse(
    // "message" maps to the "message" key in backend error JSON
    val message: String,

    // "status" maps to the "status" key — the HTTP status code
    val status: Int
)