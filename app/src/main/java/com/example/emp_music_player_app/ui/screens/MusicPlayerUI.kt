package com.example.emp_music_player_app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.emp_music_player_app.ui.MusicPlayerViewModel
import com.example.emp_music_player_app.R

@Composable
fun MusicPlayerUI(
    viewModel: MusicPlayerViewModel,
    albumId: Long,
    onNavigateBack: () -> Unit
) {
    val currentSong = viewModel.currentSong.collectAsState(initial = null)
    val isPlaying = viewModel.isPlaying.value
    val currentPosition = viewModel.currentPosition.collectAsState(initial = 0f)
    val context = LocalContext.current
    val currentAlbumImagePath = viewModel.getCurrentAlbumImagePath()

    Scaffold(
        topBar = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (currentAlbumImagePath != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentAlbumImagePath)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Album art",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_album_placeholder),
                    contentDescription = "Album art",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp)
                )
            }

            currentSong.value?.let { song ->
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Slider(
                        value = currentPosition.value,
                        onValueChange = { viewModel.seekTo(it) },
                        valueRange = 0f..song.duration.toFloat(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = formatTime(currentPosition.value.toLong()))
                        Text(text = formatTime(song.duration))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = {
                    viewModel.previousSong(context)
                }) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous song",
                        Modifier.size(50.dp)
                    )
                }

                IconButton(onClick = {
                    if (isPlaying) {
                        viewModel.pauseMusic(context)
                    } else {
                        currentSong.value?.let {
                            viewModel.playMusic(context, it.uri, it.title, it.duration, currentAlbumImagePath)
                        }
                    }
                }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        Modifier.size(50.dp)
                    )
                }

                IconButton(onClick = {
                    viewModel.nextSong(context)
                }) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next song",
                        Modifier.size(50.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}