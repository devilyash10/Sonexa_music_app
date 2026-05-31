package com.example.sonexa.feature.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance // 🚨 IMPORTED FOR BRIGHTNESS DETECTION
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sonexa.core.util.ColorExtractor
import com.example.sonexa.model.Song

@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    currentPosition: Long,
    totalDuration: Long,
    onNavigateToPlayer: () -> Unit,
    onPauseClick: () -> Unit = {},
    onResumeClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onPreviousClick: () -> Unit = {}
) {
    val context = LocalContext.current

    var extractedColor by remember { mutableStateOf<Color?>(null) }
    val defaultColor = Color(0xFF1E1E1E)

    LaunchedEffect(song.artworkUri) {
        if (song.artworkUri.isNotBlank()) {
            extractedColor = ColorExtractor.getDominantColor(context, song.artworkUri)
        }
    }

    val primaryColor by animateColorAsState(
        targetValue = extractedColor ?: defaultColor,
        animationSpec = tween(1000),
        label = "PrimaryColor"
    )
    val secondaryColor by animateColorAsState(
        targetValue = extractedColor?.copy(alpha = 0.3f) ?: Color.Black,
        animationSpec = tween(1000),
        label = "SecondaryColor"
    )

    // 🚨 THE FIX: Dynamic Luminance Detection
    // If the background is too bright, make the text/bars Black. If dark, make them White.
    val isLightBackground = primaryColor.luminance() > 0.5f
    val contentColor by animateColorAsState(
        targetValue = if (isLightBackground) Color.Black else Color.White,
        animationSpec = tween(1000),
        label = "ContentColor"
    )

    val targetProgress = if (totalDuration > 0) currentPosition.toFloat() / totalDuration.toFloat() else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        label = "MiniPlayerProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.8f),
                        secondaryColor.copy(alpha = 0.6f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onNavigateToPlayer() }
    ) {
        // BACKGROUND LAYER: The Dancing Spectrum
        if (isPlaying) {
            AudioscapeSpectrum(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 16.dp),
                // 🚨 THE FIX: Cranked opacity way up for high visibility!
                activeColor = contentColor.copy(alpha = 0.85f),
                inactiveColor = contentColor.copy(alpha = 0.25f)
            )
        } else {
            // Static line when paused
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .height(3.dp), // Slightly thicker for visibility
                color = contentColor.copy(alpha = 0.85f),
                trackColor = contentColor.copy(alpha = 0.25f),
                drawStopIndicator = {}
            )
        }

        // FOREGROUND LAYER: The actual UI
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ALBUM ART
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.5f)
                )
                AsyncImage(
                    model = song.artworkUri,
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // SONG INFO & TIMER
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = " • ${formatTime(currentPosition)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
            // CONTROLS
            IconButton(onClick = onPreviousClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.SkipPrevious, contentDescription = "Skip Previous", tint = contentColor)
            }
            IconButton(onClick = { if (isPlaying) onPauseClick() else onResumeClick() }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = contentColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = onNextClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "Skip Next", tint = contentColor)
            }
        }
    }
}

// THE CUSTOM SPECTRUM ENGINE
@Composable
fun AudioscapeSpectrum(
    progress: Float,
    modifier: Modifier = Modifier,
    activeColor: Color,
    inactiveColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SpectrumEngine")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpectrumTime"
    )

    Canvas(modifier = modifier) {
        val barWidth = 2.5.dp.toPx()
        val gap = 1.5.dp.toPx()
        val totalBarWidth = barWidth + gap
        val barCount = (size.width / totalBarWidth).toInt()

        val bottomY = size.height

        for (i in 0 until barCount) {
            val x = i * totalBarWidth

            val rawHeight = (kotlin.math.sin(i * 0.5f + time * 0.1f) +
                    kotlin.math.sin(i * 0.2f - time * 0.15f) +
                    kotlin.math.cos(i * 0.8f + time * 0.05f)) / 3f

            val normalizedHeight = ((rawHeight + 1f) / 2f).coerceIn(0.1f, 1.0f)

            val barHeight = normalizedHeight * size.height * 0.8f

            val isPlayed = (x / size.width) <= progress
            val color = if (isPlayed) activeColor else inactiveColor

            drawLine(
                color = color,
                start = Offset(x, bottomY),
                end = Offset(x, bottomY - barHeight),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return "$minutes:${remainingSeconds.toString().padStart(2, '0')}"
}