package com.example.sonexa.data.remote

import android.util.Log

class MusicRepository {
    private val apiService = MusicApiService.create()

    suspend fun searchOnlineSongs(query: String): List<OnlineSongDto> {
        return try {
            val response = apiService.searchOnlineSongs(query)
            response.results
        } catch (e: Exception) {
            Log.e("MusicRepository", "Network Error: ${e.message}")
            emptyList() // Return an empty list if the internet fails
        }
    }
}