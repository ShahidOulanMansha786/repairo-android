package com.carrepair.app.data

import com.carrepair.app.data.apis.AuthApi
import com.carrepair.app.data.apis.DisputeApi
import com.carrepair.app.data.apis.LeadApi
import com.carrepair.app.data.apis.PaymentApi
import com.carrepair.app.data.apis.RepairShopApi
import com.carrepair.app.data.apis.ReviewApi
import com.carrepair.app.domain.security.BlockCheckInterceptor
import com.carrepair.app.domain.security.TokenAuthenticator
import com.carrepair.app.domain.security.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    const val BASE_URL = "http://192.168.0.100:8080/"

    lateinit var tokenManager: TokenManager

    private val authInterceptor = Interceptor { chain ->
        val token = tokenManager.getAccessToken()
        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(BlockCheckInterceptor(tokenManager))
            .addInterceptor(authInterceptor)
            .authenticator(TokenAuthenticator(tokenManager))
            .build()
    }


    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }



    val leadApi: LeadApi by lazy {
        retrofit.create(LeadApi::class.java)
    }

    val repairShopApi: RepairShopApi by lazy {
        retrofit.create(RepairShopApi::class.java)
    }
    val paymentApi: PaymentApi by lazy {
        retrofit.create(PaymentApi::class.java)
    }

    val disputeApi: DisputeApi by lazy {
        retrofit.create(DisputeApi::class.java)
    }

    val reviewApi: ReviewApi by lazy {
        retrofit.create(ReviewApi::class.java)
    }
}