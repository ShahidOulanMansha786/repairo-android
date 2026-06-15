package com.carrepair.app.data.apis


import com.carrepair.app.data.dto.PurchaseCreditsRequestDto
import com.carrepair.app.data.dto.ShopCreditsResponseDto
import com.carrepair.app.data.dto.lead.NearbyLeadResponseDto
import com.carrepair.app.data.dto.quote.QuoteResponseDto
import com.carrepair.app.data.dto.quote.ShopQuoteResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface RepairShopApi {

    @GET("shops/leads/nearby")
    suspend fun getNearbyLeads(
        @Header("Authorization") token: String
    ): Response<List<NearbyLeadResponseDto>>

    @GET("shops/credits")
    suspend fun getShopCredits(
        @Header("Authorization") token: String
    ): Response<ShopCreditsResponseDto>

    @POST("shops/credits/purchase")
    suspend fun purchaseCredits(
        @Header("Authorization") token: String,
        @Body body: PurchaseCreditsRequestDto
    ): Response<ShopCreditsResponseDto>

    @POST("quotes")
    suspend fun submitQuote(
        @Header("Authorization") token: String,
        @Body body: SubmitQuoteRequestDto
    ): Response<QuoteResponseDto>

    @GET("quotes/my")
    suspend fun getMyQuotes(
        @Query("status") status: String? = null
    ): List<ShopQuoteResponse>
}

data class SubmitQuoteRequestDto(
    val leadId: Long,
    val price: Double,
    val message: String?
)