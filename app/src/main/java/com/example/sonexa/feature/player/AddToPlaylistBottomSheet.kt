package com.example.sonexa.feature.player


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sonexa.data.local.PlaylistEntity
import com.example.sonexa.model.Song
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistBottomSheet(
    song: Song,
    playlists: List<PlaylistEntity>,
    onDismiss: () -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onAddToPlaylist: (Long, Long) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "Add to Playlist",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 1. Create New Playlist Button
            OutlinedButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Playlist")
            }

            // 2. List of Existing Playlists
            if (playlists.isEmpty()) {
                Text("No playlists yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn {

                    items(playlists) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAddToPlaylist(playlist.playlistId, song.id)
                                    Toast.makeText(
                                        context,
                                        "${song.title} added to ${playlist.name}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onDismiss() // Close the sheet!
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.QueueMusic, contentDescription = null)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(playlist.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

    // 3. The Text Input Dialog for a New Playlist
    if (showCreateDialog) {
        var newPlaylistName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    singleLine = true,
                    placeholder = { Text("e.g. Synthwave Mix") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCreatePlaylist(newPlaylistName)
                        showCreateDialog = false
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }
}