package com.example.sonexa.feature.player

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.sonexa.data.local.PlaylistEntity
import com.example.sonexa.model.Song
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import com.example.sonexa.core.util.LyricLine

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
    var showBottomSheet by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) } // 🚨 NEW STATE

    if (showBottomSheet) {
        AddToPlaylistBottomSheet(
            song = song,
            playlists = playlists,
            onDismiss = { showBottomSheet = false },
            onCreatePlaylist = onCreatePlaylist,
            onAddToPlaylist = onAddToPlaylist
        )
    }

    val scrollState = rememberScrollState()

    // 1. ROOT BOX: Stack everything instead of using Scaffold
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Fallback color
    ) {
        // 2. THE BACKGROUND: Blurred Edge-to-Edge Album Art
        AsyncImage(
            model = song.artworkUri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(50.dp)
                .align(Alignment.Center),
            contentScale = ContentScale.Crop,
            alpha = 0.6f // Dim it slightly
        )

        // 3. THE GLASS OVERLAY: Dark Gradient for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // 4. THE FOREGROUND: The actual UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding() // Keeps UI below the status bar/battery icon
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Custom Transparent Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 2.sp
                )
                Row {
                    IconButton(onClick = { showLyrics = true }) {
                        Icon(Icons.Rounded.Notes, contentDescription = "Lyrics", tint = Color.White)
                    }
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(
                            Icons.Default.PlaylistAdd,
                            contentDescription = "Add",
                            tint = Color.White
                        )
                    }
                }
            }

            // Crisp, Elevated Album Art
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
                    tint = Color.White.copy(alpha = 0.3f)
                )
                AsyncImage(
                    model = song.artworkUri,
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Song Info & Favorite Button
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
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(56.dp)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        modifier = Modifier.size(32.dp),
                        tint = if (isFavorite) Color(0xFFFF2A6D) else Color.White // Neon Pink Heart!
                    )
                }
            }

            // Retrowave Slider
            Column(modifier = Modifier.padding(top = 24.dp)) {
                Slider(
                    value = if (totalDuration > 0) currentPosition.toFloat() / totalDuration.toFloat() else 0f,
                    onValueChange = { onSeek((it * totalDuration).toLong()) },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(currentPosition), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.5f))
                    Text(formatTime(totalDuration), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Glowing Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onShuffleClick) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleEnabled) Color(0xFF00F0FF) else Color.White.copy(alpha = 0.5f) // Neon Cyan!
                    )
                }

                IconButton(onClick = onPreviousClick) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(48.dp))
                }

                // Main Play/Pause Button
                Surface(
                    onClick = { if (isPlaying) onPauseClick() else onResumeClick() },
                    shape = CircleShape,
                    color = Color.White, // High contrast
                    contentColor = Color.Black
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier
                            .padding(20.dp)
                            .size(48.dp)
                    )
                }

                IconButton(onClick = onNextClick) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(48.dp))
                }

                IconButton(onClick = onRepeatClick) {
                    Icon(
                        imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode != Player.REPEAT_MODE_OFF) Color(0xFF00F0FF) else Color.White.copy(alpha = 0.5f) // Neon Cyan!
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
        // 5. THE SLIDE-UP LYRICS OVERLAY
        AnimatedVisibility(
            visible = showLyrics,
            enter = slideInVertically(initialOffsetY = { it }), // Slides up from the absolute bottom!
            exit = slideOutVertically(targetOffsetY = { it })   // Slides perfectly back down!
        ) {
            LyricsScreen(
                lyrics = currentLyrics,
                activeLyricIndex = activeLyricIndex,
                onBackClick = { showLyrics = false } // Closes the overlay!
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
















