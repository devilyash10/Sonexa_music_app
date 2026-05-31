package com.example.sonexa.feature.settings

import android.media.audiofx.Equalizer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomEqualizerScreen(
    audioSessionId: Int,
    onBackClick: () -> Unit
) {
    // 1. Initialize the Hardware Equalizer for THIS app only
    val equalizer = remember(audioSessionId) {
        if (audioSessionId > 0) {
            try {
                // Priority 0, attach cleanly to our specific Media3 player instance session
                Equalizer(0, audioSessionId).apply { enabled = true }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    // Colors to match your AMOLED Gold Theme
    val neonGold = Color(0xFFFFD700)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Equalizer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            if (equalizer == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Equalizer not supported on this device/emulator.", color = Color.Gray)
                }
            } else {
                val numBands = equalizer.numberOfBands
                val minEQLevel = equalizer.bandLevelRange[0]
                val maxEQLevel = equalizer.bandLevelRange[1]

                Text(
                    text = "CUSTOM PRESET",
                    style = MaterialTheme.typography.labelMedium,
                    color = neonGold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // 2. Generate Sliders for every frequency band
                for (i in 0 until numBands) {
                    val band = i.toShort()
                    // Get the frequency in Hertz (Hz)
                    val freq = equalizer.getCenterFreq(band) / 1000

                    // State to hold the current slider value
                    var sliderValue by remember {
                        mutableStateOf(equalizer.getBandLevel(band).toFloat())
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Frequency Label (e.g., 60Hz for Bass)
                            Text(
                                text = "${freq}Hz",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(60.dp)
                            )

                            // The Slider
                            Slider(
                                value = sliderValue,
                                onValueChange = { newValue ->
                                    sliderValue = newValue
                                    equalizer.setBandLevel(band, newValue.toInt().toShort())
                                },
                                valueRange = minEQLevel.toFloat()..maxEQLevel.toFloat(),
                                colors = SliderDefaults.colors(
                                    thumbColor = neonGold,
                                    activeTrackColor = neonGold,
                                    inactiveTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                            )

                            // Readout (e.g., +3dB)
                            val dbValue = (sliderValue / 100).toInt()
                            Text(
                                text = if (dbValue > 0) "+${dbValue}dB" else "${dbValue}dB",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(45.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}