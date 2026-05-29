package com.example.sonexa.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApiService {

    // We hit the iTunes search endpoint, specifying we only want music tracks
    @GET("search?media=music&entity=song")
    suspend fun searchOnlineSongs(
        @Query("term") query: String,
        @Query("limit") limit: Int = 20
    ): ITunesResponse

    companion object {
        private const val BASE_URL = "https://itunes.apple.com/"

        fun create(): MusicApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MusicApiService::class.java)
        }
    }
}