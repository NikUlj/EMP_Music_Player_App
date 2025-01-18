package com.example.emp_music_player_app.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.emp_music_player_app.ui.MusicPlayerViewModel
import com.example.emp_music_player_app.ui.components.ArtistCard

@Composable
fun ArtistSelectScreen(
    viewModel: MusicPlayerViewModel,
    onArtistSelected: (Long) -> Unit
) {
    val artistsState = viewModel.getAllArtists().collectAsState(initial = emptyList())
    val artists = artistsState.value

    Scaffold { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(artists) { artist ->
                ArtistCard(
                    artistName = artist.name,
                    imagePath = artist.imagePath,
                    onClick = { onArtistSelected(artist.artistId) }
                )
            }
        }
    }
}