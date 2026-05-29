package com.example.sonexa.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil.compose.AsyncImage
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
    onAddToPlaylist: (Long, Long) -> Unit,

    // NEW: Ready for the Lyrics screen from our UI Mockup!
    onNavigateToLyrics: () -> Unit = {}
) {
    // 1. Create a state to show/hide the bottom sheet
    var showBottomSheet by remember { mutableStateOf(false) }

    // 2. Trigger the Bottom Sheet
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Now Playing", style = MaterialTheme.typography.labelLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Close", modifier = Modifier.size(32.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.MoreVert, contentDescription = "Settings") }
                    // FIXED: Using standard Icons.Default to prevent AutoMirrored version crashes
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(Icons.Default.PlaylistAdd, contentDescription = "Add to Playlist")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. Real Album Art with Coil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                )

                AsyncImage(
                    model = song.artworkUri,
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // 2. Dynamic Song Info & Favorite Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        modifier = Modifier.size(32.dp),
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3. Progress Bar (Slider)
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Slider(
                    value = if (totalDuration > 0) currentPosition.toFloat() / totalDuration.toFloat() else 0f,
                    onValueChange = { newPercent ->
                        onSeek((newPercent * totalDuration).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(currentPosition), style = MaterialTheme.typography.labelSmall)
                    Text(formatTime(totalDuration), style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = onShuffleClick) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onPreviousClick) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Skip Previous", modifier = Modifier.size(42.dp))
                }

                Surface(
                    onClick = {
                        if (isPlaying) onPauseClick() else onResumeClick()
                    },
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.padding(20.dp).size(40.dp)
                    )
                }

                IconButton(onClick = onNextClick) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Skip Next", modifier = Modifier.size(42.dp))
                }

                IconButton(onClick = onRepeatClick) {
                    Icon(
                        imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// 5. Helper function to format Milliseconds (Long) into MM:SS
private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return "$minutes:${remainingSeconds.toString().padStart(2, '0')}"
}