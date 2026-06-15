package com.carrepair.app.data.apis

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.carrepair.app.data.dto.auth.AccessTokenResponseDto
import com.carrepair.app.data.dto.auth.AuthResponseDto
import com.carrepair.app.data.dto.auth.LoginRequestDto
import com.carrepair.app.data.dto.auth.OtpRequestDto
import com.carrepair.app.data.dto.auth.PresignedUrlRequestDto
import com.carrepair.app.data.dto.auth.PresignedUrlResponseDto
import com.carrepair.app.data.dto.auth.RefreshTokenRequestDto
import com.carrepair.app.data.dto.auth.ResendOtpRequestDto
import com.carrepair.app.data.dto.ShopAuthResponseDto
import com.carrepair.app.data.dto.ShopDocumentUploadDto
import com.carrepair.app.data.dto.ShopLoginRequestDto
import com.carrepair.app.data.dto.ShopLoginResponseDto
import com.carrepair.app.data.dto.ShopOtpRequestDto
import com.carrepair.app.data.dto.ShopStatusResponseDto
import com.carrepair.app.data.dto.ShopVerifyOtpRequestDto
import com.carrepair.app.data.dto.auth.SignupRequestDto
import com.carrepair.app.data.dto.auth.VerifyOtpRequestDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface AuthApi {

    @POST("auth/request-otp")
    suspend fun requestOtp(@Body body: OtpRequestDto): Response<MessageResponseDto>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequestDto): Response<AuthResponseDto>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshTokenRequestDto): AccessTokenResponseDto

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequestDto): Response<MessageResponseDto>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<MessageResponseDto>

    @POST("auth/resend-otp")
    suspend fun resendOtp(@Body dto: ResendOtpRequestDto): Response<MessageResponseDto>

    @POST("media/presigned-url")
    suspend fun getPresignedUrl(
        @Header("Authorization") token: String,
        @Body body: PresignedUrlRequestDto
    ): Response<PresignedUrlResponseDto>

    @POST("auth/shop/request-otp")
    suspend fun requestShopOtp(
        @Body body: ShopOtpRequestDto
    ): Response<MessageResponseDto>

    @POST("auth/shop/verify-otp")
    suspend fun verifyShopOtp(
        @Body body: ShopVerifyOtpRequestDto
    ): Response<ShopAuthResponseDto>

    @POST("shops/documents")
    suspend fun uploadShopDocuments(
        @Header("Authorization") token: String,
        @Body body: ShopDocumentUploadDto
    ): Response<ShopStatusResponseDto>

    @GET("shops/my-status")
    suspend fun getMyShopStatus(
        @Header("Authorization") token: String
    ): Response<ShopStatusResponseDto>

    @POST("auth/shop/login/request-otp")
    suspend fun shopLoginRequestOtp(
        @Body body: ShopLoginRequestDto
    ): Response<MessageResponseDto>

    @POST("auth/shop/login/verify-otp")
    suspend fun shopLoginVerifyOtp(
        @Body body: ShopVerifyOtpRequestDto
    ): Response<ShopLoginResponseDto>

    @POST("users/fcm-token")
    suspend fun updateFcmToken(
        @Header("Authorization") token: String,
        @Body body: FcmTokenRequestDto
    ): Response<MessageResponseDto>

    data class FcmTokenRequestDto(val fcmToken: String)

    @GET("chat/token")
    suspend fun getChatToken(
        @Header("Authorization") token: String
    ): Response<StreamTokenResponseDto>

    @GET("leads/{leadId}/channel")
    suspend fun getChannelInfo(
        @Header("Authorization") token: String,
        @Path("leadId") leadId: Long
    ): Response<StreamTokenResponseDto>

    @GET("users/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): Response<UserMeResponseDto>


    data class StreamTokenResponseDto(
        val streamToken: String,
        val channelId: String?
    )

    data class UserMeResponseDto(
        val id: Long,
        val fullName: String,
        val email: String,
        val role: String
    )

    @POST("chat/notify")
    suspend fun sendChatNotification(
        @Header("Authorization") token: String,
        @Body body: ChatNotifyRequestDto
    ): Response<MessageResponseDto>

    data class ChatNotifyRequestDto(
        val channelId: String,
        val senderName: String,
        val messagePreview: String
    )
    data class MessageResponseDto(
        val message: String
    )


}