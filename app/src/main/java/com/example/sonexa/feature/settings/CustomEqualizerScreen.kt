package com.example.sonexa.feature.settings

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomEqualizerScreen(
    audioSessionId: Int,
    onBackClick: () -> Unit
) {
    // 1. Initialize EQ
    val equalizer = remember(audioSessionId) {
        if (audioSessionId > 0) try { Equalizer(0, audioSessionId).apply { enabled = true } } catch (e: Exception) { null } else null
    }

    // 2. Initialize Bass Boost Engine
    val bassBoost = remember(audioSessionId) {
        if (audioSessionId > 0) try { BassBoost(0, audioSessionId).apply { enabled = true } } catch (e: Exception) { null } else null
    }

    // 3. Initialize 3D Virtualizer (Clarity/Surround)
    val virtualizer = remember(audioSessionId) {
        if (audioSessionId > 0) try { Virtualizer(0, audioSessionId).apply { enabled = true } } catch (e: Exception) { null } else null
    }

    val haptic = LocalHapticFeedback.current
    val primaryThemeColor = MaterialTheme.colorScheme.primary
    var refreshTrigger by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio FX", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBackClick()
                    }) {
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
                    Text("Audio FX not supported on this device.", color = Color.Gray)
                }
            } else {

                // --- 🚨 NEW: BASS & 3D SURROUND DIALS ---
                Text(
                    text = "MASTER EFFECTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = primaryThemeColor,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // BASS BOOST SLIDER
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BASS", fontWeight = FontWeight.Bold)
                            var bassValue by remember { mutableFloatStateOf((bassBoost?.roundedStrength ?: 0).toFloat()) }
                            Slider(
                                value = bassValue,
                                onValueChange = {
                                    bassValue = it
                                    bassBoost?.setStrength(it.toInt().toShort())
                                },
                                onValueChangeFinished = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                                valueRange = 0f..1000f,
                                colors = SliderDefaults.colors(thumbColor = primaryThemeColor, activeTrackColor = primaryThemeColor)
                            )
                        }
                    }

                    // 3D VIRTUALIZER SLIDER
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("3D AUDIO", fontWeight = FontWeight.Bold)
                            var virtValue by remember { mutableFloatStateOf((virtualizer?.roundedStrength ?: 0).toFloat()) }
                            Slider(
                                value = virtValue,
                                onValueChange = {
                                    virtValue = it
                                    virtualizer?.setStrength(it.toInt().toShort())
                                },
                                onValueChangeFinished = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                                valueRange = 0f..1000f,
                                colors = SliderDefaults.colors(thumbColor = primaryThemeColor, activeTrackColor = primaryThemeColor)
                            )
                        }
                    }
                }

                // --- PRESETS ---
                Text(
                    text = "PRESETS",
                    style = MaterialTheme.typography.labelMedium,
                    color = primaryThemeColor,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                val numPresets = equalizer.numberOfPresets
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(numPresets.toInt()) { i ->
                        val presetId = i.toShort()
                        val presetName = equalizer.getPresetName(presetId)
                        val isActive = equalizer.currentPreset == presetId

                        FilterChip(
                            selected = isActive,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                equalizer.usePreset(presetId)
                                refreshTrigger++
                            },
                            label = { Text(presetName, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryThemeColor.copy(alpha = 0.2f),
                                selectedLabelColor = primaryThemeColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- EQ BANDS ---
                Text(
                    text = "CUSTOM BANDS",
                    style = MaterialTheme.typography.labelMedium,
                    color = primaryThemeColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val numBands = equalizer.numberOfBands
                val minEQLevel = equalizer.bandLevelRange[0]
                val maxEQLevel = equalizer.bandLevelRange[1]

                for (i in 0 until numBands) {
                    val band = i.toShort()
                    val freq = equalizer.getCenterFreq(band) / 1000

                    var sliderValue by remember(refreshTrigger) { mutableFloatStateOf(equalizer.getBandLevel(band).toFloat()) }
                    var lastDbValue by remember(refreshTrigger) { mutableIntStateOf((sliderValue / 100).toInt()) }

                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "${freq}Hz", fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))

                            Slider(
                                value = sliderValue,
                                onValueChange = { newValue ->
                                    sliderValue = newValue
                                    equalizer.setBandLevel(band, newValue.toInt().toShort())

                                    val currentDb = (newValue / 100).toInt()
                                    if (currentDb != lastDbValue) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        lastDbValue = currentDb
                                    }
                                },
                                valueRange = minEQLevel.toFloat()..maxEQLevel.toFloat(),
                                colors = SliderDefaults.colors(thumbColor = primaryThemeColor, activeTrackColor = primaryThemeColor, inactiveTrackColor = Color.DarkGray),
                                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                            )

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