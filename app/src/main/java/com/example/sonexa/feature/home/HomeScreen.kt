package com.example.sonexa.feature.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.sonexa.model.Song
import com.example.sonexa.feature.home.components.HorizontalSongSlider

@Composable
fun HomeScreen(
    songs: List<Song>,
    recentlyPlayed: List<Song>, // 🚨 NEW: Injected from ViewModel
    mostPlayed: List<Song>,     // 🚨 NEW: Injected from ViewModel
    favoriteSongIds: List<Long>, // 🚨 NEW: To tint the hearts
    onSongClick: (Song) -> Unit, // 🚨 Changed to pass the full Song object
    onSearchClick: () -> Unit,
    onPermissionGranted: () -> Unit,
    onShufflePlayClick: () -> Unit,
    onNavigateToOnline: () -> Unit,
    onToggleFavorite: (Song) -> Unit,       // 🚨 NEW: Quick Action
    onAddToPlaylistClick: (Song) -> Unit    // 🚨 NEW: Quick Action
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val hasPermission = ContextCompat.checkSelfPermission(context, permissionToRequest) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(hasPermission) {
        if (hasPermission && songs.isEmpty()) {
            onPermissionGranted()
        }
    }

    // --- SORTING STATE ---
    var sortOption by remember { mutableStateOf("A-Z") }
    var showSortMenu by remember { mutableStateOf(false) }

    // Sort the list instantly in the UI layer
    val sortedSongs = remember(songs, sortOption) {
        when (sortOption) {
            "A-Z" -> songs.sortedBy { it.title }
            "Z-A" -> songs.sortedByDescending { it.title }
            "Artist" -> songs.sortedBy { it.artist }
            else -> songs
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // 1. BRAND HEADER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SONEXA",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "By Yash Bhadoriya",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. CLOUD GRID
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CloudCard(
                    title = "Cloud-streaming",
                    gradientColors = listOf(Color(0xFF8A2BE2), Color(0xFF00F0FF)),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToOnline
                )
                CloudCard(
                    title = "Global Top 50",
                    gradientColors = listOf(Color(0xFFFF2A6D), Color(0xFFFF7E67)),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToOnline
                )
            }
        }

        // 3. 🚨 THE NEW DATABASE SLIDERS
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (recentlyPlayed.isNotEmpty()) {
                    HorizontalSongSlider(
                        title = "Recently Played",
                        songs = recentlyPlayed,
                        onSongClick = onSongClick
                    )
                }

                if (mostPlayed.isNotEmpty()) {
                    HorizontalSongSlider(
                        title = "Most Played",
                        songs = mostPlayed,
                        onSongClick = onSongClick
                    )
                }
            }
        }

        // 4. LOCAL TRACKS HEADER WITH SORTING
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved Local Tracks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sorting Dropdown
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Sort,
                                contentDescription = "Sort",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Title (A-Z)", color = MaterialTheme.colorScheme.onBackground) },
                                onClick = { sortOption = "A-Z"; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Title (Z-A)", color = MaterialTheme.colorScheme.onBackground) },
                                onClick = { sortOption = "Z-A"; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Artist", color = MaterialTheme.colorScheme.onBackground) },
                                onClick = { sortOption = "Artist"; showSortMenu = false }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Shuffle Play
                    IconButton(
                        onClick = onShufflePlayClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle Play",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 5. THE REAL LOCAL SONGS LIST
        if (sortedSongs.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!hasPermission) {
                        Text("Permission required to access media.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onPermissionGranted,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Scan Storage", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    } else {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        } else {
            items(sortedSongs) { song ->
                LocalSongItem(
                    song = song,
                    isFavorite = favoriteSongIds.contains(song.id), // 🚨 Check if it's liked!
                    onClick = { onSongClick(song) },
                    onFavoriteClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleFavorite(song)
                    },
                    onPlaylistClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAddToPlaylistClick(song)
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

// --- BEAUTIFUL UI COMPONENTS ---

@Composable
fun CloudCard(
    title: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1.5f)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(gradientColors))
            .clickable { onClick() },
        contentAlignment = Alignment.BottomStart
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun LocalSongItem(
    song: Song,
    isFavorite: Boolean, // 🚨 NEW
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit, // 🚨 NEW
    onPlaylistClick: () -> Unit  // 🚨 NEW
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            AsyncImage(model = song.artworkUri, contentDescription = "Album Art", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        // 🚨 NEW: Quick-Action Menus right on the card!
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onPlaylistClick) {
                Icon(
                    imageVector = Icons.Default.PlaylistAdd,
                    contentDescription = "Add to Playlist",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}