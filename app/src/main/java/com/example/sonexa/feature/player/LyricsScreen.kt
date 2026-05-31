package com.example.sonexa.feature.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sonexa.core.util.LyricLine

@Composable
fun LyricsScreen(
    lyrics: List<LyricLine>,
    activeLyricIndex: Int,
    onBackClick: () -> Unit
) {
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current

    // 🚨 THE SCROLL ENGINE: Automatically tracks the singing position
    LaunchedEffect(activeLyricIndex) {
        if (activeLyricIndex >= 0 && activeLyricIndex < lyrics.size) {
            // We subtract 2 so the active line sits perfectly in the middle/upper-middle of the screen
            val targetIndex = maxOf(0, activeLyricIndex - 2)
            listState.animateScrollToItem(targetIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)) // Premium dark glass backdrop
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LYRICS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 2.sp
                )
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBackClick()
                    },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.15f), shape = CircleShape)
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close Lyrics", tint = Color.White)
                }
            }

            // --- THE LYRICS LIST ---
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = 32.dp, bottom = 200.dp), // Extra bottom padding so the last lyric can scroll up
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (lyrics.isEmpty()) {
                    item {
                        Text(
                            text = "Lyrics not available for this track.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    itemsIndexed(lyrics) { index, line ->
                        val isActive = index == activeLyricIndex

                        // 🚨 SMOOTH ANIMATIONS: The text physically grows and glows when active
                        val animatedColor by animateColorAsState(
                            targetValue = if (isActive) Color.White else Color.White.copy(alpha = 0.3f),
                            animationSpec = tween(500),
                            label = "LyricColor"
                        )

                        Text(
                            text = line.text,
                            color = animatedColor,
                            fontSize = if (isActive) 28.sp else 24.sp,
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                            lineHeight = 34.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}