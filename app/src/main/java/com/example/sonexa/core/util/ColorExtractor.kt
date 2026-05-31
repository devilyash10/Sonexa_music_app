package com.example.sonexa.core.util

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ColorExtractor {

    suspend fun getDominantColor(context: Context, imageUrl: String): Color? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Request the image via Coil
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .size(128) // 🚨 Scale down heavily for blazing fast pixel extraction!
                    .allowHardware(false) // Required for Palette API to read pixels
                    .build()

                val result = context.imageLoader.execute(request)

                if (result is SuccessResult) {
                    val bitmap = result.drawable.toBitmap()

                    // 2. Generate the Palette
                    val palette = Palette.from(bitmap).generate()

                    // 3. Try to get the Vibrant color first. If it fails, get the Dominant color.
                    val rgb = palette.vibrantSwatch?.rgb ?: palette.dominantSwatch?.rgb

                    if (rgb != null) {
                        return@withContext Color(rgb)
                    }
                }
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}