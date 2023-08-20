package com.example.attendance

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface QuotableApiService {
    @GET("random")
    suspend fun getRandomQuote(): Response<QuoteResponse>
}

data class QuoteResponse(val content: String)
