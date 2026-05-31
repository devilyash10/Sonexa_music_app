package com.example.sonexa.feature.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.sonexa.core.util.ColorExtractor
import com.example.sonexa.core.util.LyricLine
import com.example.sonexa.data.local.PlaylistEntity
import com.example.sonexa.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    song: Song,
    isPlaying: Boolean,
    currentPosition: Long,
    totalDuration: Long,
    isShuffleEnabled: Boolean,
    repeatMode: Int,
    isFavorite: Boolean,
    playlists: List<PlaylistEntity>,
    currentLyrics: List<LyricLine>,
    activeLyricIndex: Int,
    onToggleFavorite: () -> Unit,
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onPreviousClick: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    onResumeClick: () -> Unit = {},
    onSeek: (Long) -> Unit = {},
    onShuffleClick: () -> Unit = {},
    onRepeatClick: () -> Unit = {},
    onCreatePlaylist: (String) -> Unit,
    onAddToPlaylist: (Long, Song) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var showBottomSheet by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }

    // 🚨 1. DYNAMIC COLOR STATE
    var extractedColor by remember { mutableStateOf<Color?>(null) }
    val defaultThemeColor = MaterialTheme.colorScheme.primary

    // 🚨 2. EXTRACT COLOR WHENEVER THE SONG CHANGES
    LaunchedEffect(song.artworkUri) {
        if (song.artworkUri.isNotBlank()) {
            extractedColor = ColorExtractor.getDominantColor(context, song.artworkUri)
        } else {
            extractedColor = null
        }
    }

    // 🚨 3. ANIMATE THE COLOR TRANSITION (1 second crossfade!)
    val animatedThemeColor by animateColorAsState(
        targetValue = extractedColor ?: defaultThemeColor,
        animationSpec = tween(durationMillis = 1000),
        label = "ThemeColorAnimation"
    )

    val onBgColor = Color.White
    val scrollState = rememberScrollState()

    if (showBottomSheet) {
        AddToPlaylistBottomSheet(
            song = song,
            playlists = playlists,
            onDismiss = { showBottomSheet = false },
            onCreatePlaylist = onCreatePlaylist,
            onAddToPlaylist = onAddToPlaylist
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Blurred Background Art
        AsyncImage(
            model = song.artworkUri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp) // Increased blur for a softer premium glow
                .align(Alignment.Center),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )

        // 🚨 4. DYNAMIC GRADIENT GLASS OVERLAY
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            animatedThemeColor.copy(alpha = 0.4f), // Soft dynamic color at top
                            Color.Black.copy(alpha = 0.8f),        // Fading to dark
                            Color.Black                          // Pitch black at the bottom controls
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Close", tint = onBgColor, modifier = Modifier.size(36.dp))
                }
                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = onBgColor.copy(alpha = 0.7f),
                    letterSpacing = 2.sp
                )
                Row {
                    IconButton(onClick = { showLyrics = true }) {
                        Icon(Icons.Rounded.Notes, contentDescription = "Lyrics", tint = onBgColor)
                    }
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(Icons.Default.PlaylistAdd, contentDescription = "Add", tint = onBgColor)
                    }
                }
            }

            // Main Album Art
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.DarkGray.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = onBgColor.copy(alpha = 0.3f)
                )
                AsyncImage(
                    model = song.artworkUri,
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Track Info & Favorite
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = onBgColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = onBgColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleFavorite()
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        modifier = Modifier.size(32.dp),
                        tint = if (isFavorite) animatedThemeColor else onBgColor // 🚨 Heart uses dynamic color
                    )
                }
            }

            // Scrubber Slider
            Column(modifier = Modifier.padding(top = 24.dp)) {
                Slider(
                    value = if (totalDuration > 0) currentPosition.toFloat() / totalDuration.toFloat() else 0f,
                    onValueChange = { onSeek((it * totalDuration).toLong()) },
                    onValueChangeFinished = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                    colors = SliderDefaults.colors(
                        thumbColor = animatedThemeColor, // 🚨 Scrubber uses dynamic color
                        activeTrackColor = animatedThemeColor,
                        inactiveTrackColor = onBgColor.copy(alpha = 0.2f)
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(currentPosition), style = MaterialTheme.typography.labelMedium, color = onBgColor.copy(alpha = 0.5f))
                    Text(formatTime(totalDuration), style = MaterialTheme.typography.labelMedium, color = onBgColor.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls Engine
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShuffleClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleEnabled) animatedThemeColor else onBgColor.copy(alpha = 0.5f) // 🚨
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPreviousClick()
                    }
                ) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = onBgColor, modifier = Modifier.size(48.dp))
                }

                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (isPlaying) onPauseClick() else onResumeClick()
                    },
                    shape = CircleShape,
                    color = animatedThemeColor, // 🚨 Play button uses dynamic color
                    contentColor = Color.Black // High contrast icon
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier
                            .padding(20.dp)
                            .size(48.dp)
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNextClick()
                    }
                ) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = onBgColor, modifier = Modifier.size(48.dp))
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRepeatClick()
                    }
                ) {
                    Icon(
                        imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode != Player.REPEAT_MODE_OFF) animatedThemeColor else onBgColor.copy(alpha = 0.5f) // 🚨
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        AnimatedVisibility(
            visible = showLyrics,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            LyricsScreen(
                lyrics = currentLyrics,
                activeLyricIndex = activeLyricIndex,
                onBackClick = { showLyrics = false }
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