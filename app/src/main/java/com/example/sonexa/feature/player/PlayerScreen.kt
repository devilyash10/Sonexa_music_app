package com.example.sonexa.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onBackClick: () -> Unit = {}
) {
    // For now, we use local state.
    // Later, this will come from a ViewModel connected to ExoPlayer.
    var isPlaying by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0.3f) }

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
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Album Art
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder for actual image (Coil will be used later)
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // 2. Song Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Blinding Lights",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "The Weeknd",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Progress Bar (Slider)
            Column {
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
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
                    Text("1:24", style = MaterialTheme.typography.labelSmall)
                    Text("3:50", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Shuffle, contentDescription = "Shuffle")
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Skip Previous", modifier = Modifier.size(42.dp))
                }

                // Main Play/Pause Toggle
                Surface(
                    onClick = { isPlaying = !isPlaying },
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier
                            .padding(20.dp)
                            .size(40.dp)
                    )
                }

                IconButton(onClick = { }) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Skip Next", modifier = Modifier.size(42.dp))
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Repeat, contentDescription = "Repeat")
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}