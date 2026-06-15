package com.carrepair.app.data.apis

import com.carrepair.app.data.dto.jobtracking.AcceptedLeadDetailDto
import com.carrepair.app.data.dto.jobtracking.JobProgressDto
import com.carrepair.app.data.dto.lead.CreateLeadRequestDto
import com.carrepair.app.data.dto.lead.LeadResponseDto
import com.carrepair.app.data.dto.quote.QuoteResponseDto
import retrofit2.Response
import retrofit2.http.*

interface LeadApi {

    @POST("leads")
    suspend fun createLead(
        @Header("Authorization") token: String,
        @Body body: CreateLeadRequestDto
    ): Response<LeadResponseDto>

    @GET("leads/my")
    suspend fun getMyLeads(
        @Header("Authorization") token: String
    ): Response<List<LeadResponseDto>>

    @GET("leads/{leadId}")
    suspend fun getLeadById(
        @Header("Authorization") token: String,
        @Path("leadId") leadId: Long
    ): Response<LeadResponseDto>

    @PATCH("leads/{leadId}/cancel")
    suspend fun cancelLead(
        @Header("Authorization") token: String,
        @Path("leadId") leadId: Long
    ): Response<LeadResponseDto>

    @GET("leads/{leadId}/quotes")
    suspend fun getQuotesForLead(
        @Header("Authorization") token: String,
        @Path("leadId") leadId: Long
    ): Response<List<QuoteResponseDto>>

    @POST("leads/{leadId}/quotes/{quoteId}/accept")
    suspend fun acceptQuote(
        @Header("Authorization") token: String,
        @Path("leadId") leadId: Long,
        @Path("quoteId") quoteId: Long
    ): Response<QuoteResponseDto>

    @GET("leads/{leadId}/chat-channel")
    suspend fun getChatChannel(
        @Path("leadId") leadId: Long
    ): Response<ChatChannelResponseDto>

    data class ChatChannelResponseDto(
        val channelId: String
    )

    @GET("leads/{leadId}/progress")
    suspend fun getJobProgress(@Path("leadId") leadId: Long): Response<List<JobProgressDto>>

    @POST("leads/{leadId}/mark-work-done")
    suspend fun markWorkDone(@Path("leadId") leadId: Long): Response<Void>

    @GET("leads/{leadId}/accepted-detail")
    suspend fun getAcceptedLeadDetail(@Path("leadId") leadId: Long): Response<AcceptedLeadDetailDto>
}