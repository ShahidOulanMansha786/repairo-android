package com.carrepair.app.data.apis

import com.carrepair.app.data.dto.ReviewResponseDto
import com.carrepair.app.data.dto.SubmitReviewRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ReviewApi {

    @POST("reviews")
    suspend fun submitReview(
        @Header("Authorization") token: String,
        @Body body: SubmitReviewRequestDto
    ): Response<ReviewResponseDto>

    @GET("repair-shops/{shopId}/reviews")
    suspend fun getReviewsForShop(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: Long
    ): Response<List<ReviewResponseDto>>
}
