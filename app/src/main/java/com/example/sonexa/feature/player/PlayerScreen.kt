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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sonexa.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    song: Song,

    // 1. Receive the REAL state from ExoPlayer
    isPlaying: Boolean,
    currentPosition: Long,
    totalDuration: Long,

    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onPreviousClick: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    onResumeClick: () -> Unit = {},
    onSeek: (Long) -> Unit = {} // 2. Receive the seek action
) {
    // Look! No fake states or coroutine timers! The UI is completely dumb and clean.
    val scrollState = rememberScrollState() // Added to prevent overlap

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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState), // This prevents the overlap bug!
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Album Art (Removed weight(1f) to stop it from crushing the text)
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
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // 2. Dynamic Song Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = song.title, // Dynamic Title
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist, // Dynamic Artist
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Progress Bar (Slider)
            Column {
                Slider(
                    // Calculate percentage (protect against divide by zero)
                    value = if (totalDuration > 0) currentPosition.toFloat() / totalDuration.toFloat() else 0f,
                    onValueChange = { newPercent ->
                        // Tell the ViewModel where the user dragged the slider
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
                    // 4. Use the new Millisecond formatter
                    Text(formatTime(currentPosition), style = MaterialTheme.typography.labelSmall)
                    Text(formatTime(totalDuration), style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = { }) { Icon(Icons.Default.Shuffle, contentDescription = "Shuffle") }
                IconButton(onClick = onPreviousClick) { Icon(Icons.Rounded.SkipPrevious, contentDescription = "Skip Previous", modifier = Modifier.size(42.dp)) }

                Surface(
                    onClick = {
                        if (isPlaying) onPauseClick() else onResumeClick()
                    },
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        // UI automatically flips based on the real ExoPlayer state!
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.padding(20.dp).size(40.dp)
                    )
                }

                IconButton(onClick = onNextClick) { Icon(Icons.Rounded.SkipNext, contentDescription = "Skip Next", modifier = Modifier.size(42.dp)) }
                IconButton(onClick = { }) { Icon(Icons.Default.Repeat, contentDescription = "Repeat") }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// 6. Helper function to format Milliseconds (Long) into MM:SS
private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return "$minutes:${remainingSeconds.toString().padStart(2, '0')}"
}