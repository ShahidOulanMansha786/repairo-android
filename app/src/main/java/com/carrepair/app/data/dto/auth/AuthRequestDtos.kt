package com.carrepair.app.data.dto.auth

data class OtpRequestDto(val email: String)

data class VerifyOtpRequestDto(val email: String, val otp: String)

data class RefreshTokenRequestDto(val refreshToken: String)

data class SignupRequestDto(
    val fullName: String,
    val email: String,
    val phone: String,

    // Role is always CAR_OWNER from this screen
    // We hardcode it here, the ViewModel will pass it in
    val role: String
)

data class LoginRequestDto(
    val email: String
)