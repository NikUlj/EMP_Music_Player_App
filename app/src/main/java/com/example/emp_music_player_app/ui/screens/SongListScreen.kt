package com.example.emp_music_player_app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.emp_music_player_app.ui.MusicPlayerViewModel

@Composable
fun SongListScreen(
    viewModel: MusicPlayerViewModel,
    albumId: Long,
    onSongSelected: (Long, Int) -> Unit
) {
    val songsState = viewModel.getSongsByAlbum(albumId).collectAsState(initial = emptyList())
    val songs = songsState.value
    val context = LocalContext.current

    val albumState = viewModel.getAlbumById(albumId).collectAsState(initial = null)
    val album = albumState.value

    LaunchedEffect(songs) {
        viewModel.setCurrentPlaylist(songs)
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {
            items(songs) { song ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val index = songs.indexOf(song)
                            viewModel.setCurrentPlaylist(songs, index)
                            viewModel.playMusic(
                                context,
                                song.uri,
                                song.title,
                                song.duration,
                                album?.imagePath
                            )
                            onSongSelected(albumId, index)
                        }
                        .padding(16.dp)
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = formatDuration(song.duration),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                HorizontalDivider()
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}