package com.example.sonexa.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApiService {

    // Searching the JioSaavn wrapper
    @GET("api/search/songs")
    suspend fun searchOnlineSongs(
        @Query("query") query: String,
        @Query("limit") limit: Int = 20
    ): JioSaavnResponse

    companion object {
        // 🚨 THE FIX: The official, active production server for the JioSaavn open-source API
        private const val BASE_URL = "https://saavn.sumit.co/"

        // Backup Mirror (Keep this handy just in case!):
        // private const val BASE_URL = "https://jiosaavn-api-sigma-sandy.vercel.app/"

        fun create(): MusicApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MusicApiService::class.java)
        }
    }
}