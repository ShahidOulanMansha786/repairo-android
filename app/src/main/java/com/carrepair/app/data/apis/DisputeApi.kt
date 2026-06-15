package com.carrepair.app.data.apis

import com.carrepair.app.data.dto.dispute.RaiseDisputeRequestDto
import com.carrepair.app.data.dto.dispute.DisputeResponseDto
import retrofit2.Response
import retrofit2.http.*

interface DisputeApi {
    @POST("disputes/leads/{leadId}")
    suspend fun raiseDispute(
        @Header("Authorization") token: String,
        @Path("leadId") leadId: Long,
        @Body request: RaiseDisputeRequestDto
    ): Response<DisputeResponseDto>

    @GET("disputes/leads/{leadId}")
    suspend fun getDispute(
        @Header("Authorization") token: String,
        @Path("leadId") leadId: Long
    ): Response<DisputeResponseDto>
}