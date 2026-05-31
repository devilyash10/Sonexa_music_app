package com.example.sonexa.feature.splash // Change to match your package

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import com.example.sonexa.R // Make sure to import your project's R file!

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // 🚨 Delays for 2.5 seconds, then triggers the navigation to Home
    LaunchedEffect(key1 = true) {
        delay(2500L)
        onSplashFinished()
    }

    // 🚨 Displays your full-screen poster
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A14)) // Dark fallback background
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_bg), // Your cropped image!
            contentDescription = "Sonexa Splash Screen",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Ensures it fills edge-to-edge
        )
    }
}