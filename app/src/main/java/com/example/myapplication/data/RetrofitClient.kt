package com.example.myapplication.data

import com.example.myapplication.rescue.RescueService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton Retrofit Client.
 */
object RetrofitClient {
    private const val BASE_URL = "https://api.sensesafe.com/" // Placeholder URL

    val instance: RescueService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RescueService::class.java)
    }
}
