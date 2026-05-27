package com.example.sonexa.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sonexa.model.Song

@Composable
fun MiniPlayer(
    song: Song, // Receive the dynamic song
    modifier: Modifier = Modifier,
    onNavigateToPlayer: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            // We use surface color to blend better, with a subtle divider effect
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onNavigateToPlayer() }
    ) {
        // A tiny divider line to separate it from the content above it cleanly
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp), // Tighter vertical padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp) // Slightly smaller for a sleeker look
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title, // Dynamic
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist, // Dynamic
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = { /* Play/Pause */ }) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = "Play")
            }
            IconButton(onClick = { /* Skip */ }) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "Next")
            }
        }

        LinearProgressIndicator(
            progress = { 0.3f },
            modifier = Modifier.fillMaxWidth().height(2.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}