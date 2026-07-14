package com.example.sonexa.data.remote

import com.google.gson.annotations.SerializedName

// 1. Our Clean UI-Facing DTO (The one that went missing!)
data class OnlineSongDto(
    val trackId: Long,
    val trackName: String?,
    val artistName: String?,
    val artworkUrl: String?,
    val previewUrl: String?
)

// 2. The JioSaavn Root Response
data class JioSaavnResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: JioSaavnDataPayload?
)

// 3. The Data Payload (contains the list of results)
data class JioSaavnDataPayload(
    @SerializedName("results") val results: List<JioSaavnSongDto>?
)

// 4. The Raw JioSaavn Song Object
data class JioSaavnSongDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val title: String?,
    @SerializedName("primaryArtists") val primaryArtists: String?,
    @SerializedName("image") val image: List<JioSaavnImageDto>?,
    @SerializedName("downloadUrl") val downloadUrl: List<JioSaavnDownloadDto>?
)

// 5. Helper to get the highest quality image
// 5. Helper to get the highest quality image
data class JioSaavnImageDto(
    @SerializedName("quality") val quality: String?,
    @SerializedName("url") val link: String?
)

// 6. Helper to get the highest quality audio stream
data class JioSaavnDownloadDto(
    @SerializedName("quality") val quality: String?,
    @SerializedName("url") val link: String?
)