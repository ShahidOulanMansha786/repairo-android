package com.carrepair.app.data.apis

import com.carrepair.app.data.dto.payment.PaymentInitiateRequestDto
import com.carrepair.app.data.dto.payment.PaymentSessionResponseDto
import com.carrepair.app.data.dto.payment.PaymentStatusResponseDto
import retrofit2.Response
import retrofit2.http.*

interface PaymentApi {

    @POST("api/payments/initiate")
    suspend fun initiatePayment(
        @Header("Authorization") token: String,
        @Body request: PaymentInitiateRequestDto
    ): Response<PaymentSessionResponseDto>

    @GET("api/payments/leads/{leadId}")
    suspend fun getPaymentStatus(
        @Header("Authorization") token: String,
        @Path("leadId") leadId: Long
    ): Response<PaymentStatusResponseDto>

    @POST("api/payments/{paymentId}/release-immediately")
    suspend fun releaseImmediately(
        @Header("Authorization") token: String,
        @Path("paymentId") paymentId: Long
    ): Response<PaymentStatusResponseDto>

    @GET("api/payments/{paymentId}/mock-pay")
    suspend fun mockPay(
        @Path("paymentId") paymentId: Long
    ): Response<Unit>
}
