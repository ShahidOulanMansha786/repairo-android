package com.carrepair.app

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


data class UserRequest(
    val name: String,
    val email: String
)

interface ApiService {

    @GET("hello")
    suspend fun getHello(): String

    @POST("user")
    suspend fun createUser(@Body user: UserRequest): String
}