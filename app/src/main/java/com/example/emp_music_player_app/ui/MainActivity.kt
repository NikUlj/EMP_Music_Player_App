package com.example.emp_music_player_app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.emp_music_player_app.MusicPlayerApplication
import com.example.emp_music_player_app.data.repository.MusicRepository
import com.example.emp_music_player_app.utility.PermissionUtility
import com.example.emp_music_player_app.ui.screens.AlbumSelectScreen
import com.example.emp_music_player_app.ui.screens.ArtistSelectScreen
import com.example.emp_music_player_app.ui.screens.MusicPlayerUI
import com.example.emp_music_player_app.ui.screens.SongListScreen
import com.example.emp_music_player_app.ui.theme.EMP_Music_Player_AppTheme

class MainActivity : ComponentActivity() {
    private val permissionUtility = PermissionUtility(this)
    private val viewModel: MusicPlayerViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val musicDao = (application as MusicPlayerApplication).getMusicDao()
                val repository = MusicRepository(
                    context = applicationContext,
                    musicDao = musicDao,
                    coroutineScope = lifecycleScope
                )
                @Suppress("UNCHECKED_CAST")
                return MusicPlayerViewModel(repository) as T
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.all { it.value }) {
            loadMusic()
        }
    }

    private fun checkAndRequestPermissions() {
        if (permissionUtility.hasPermissions()) {
            loadMusic()
        } else {
            permissionLauncher.launch(permissionUtility.requiredPermissions)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        lifecycleScope.launch {
//            viewModel.getMusicFiles()
//        }

        checkAndRequestPermissions()
    }


    private fun loadMusic() {
        Log.d("MusicPlayer", "Loading music...")
        viewModel.getMusicFiles()
        viewModel.bindMediaService(this)
        setContent {
            EMP_Music_Player_AppTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "artists") {
                    composable("artists") {
                        ArtistSelectScreen(
                            viewModel = viewModel,
                            onArtistSelected = { artistId ->
                                navController.navigate("albums/$artistId")
                            }
                        )
                    }
                    composable(
                        "albums/{artistId}",
                        arguments = listOf(navArgument("artistId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val artistId = backStackEntry.arguments?.getLong("artistId") ?: return@composable
                        AlbumSelectScreen(
                            viewModel = viewModel,
                            artistId = artistId,
                            onAlbumSelected = { albumId ->
                                navController.navigate("songs/$albumId")
                            }
                        )
                    }
                    composable(
                        "songs/{albumId}",
                        arguments = listOf(navArgument("albumId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val albumId = backStackEntry.arguments?.getLong("albumId") ?: return@composable
                        SongListScreen(
                            viewModel = viewModel,
                            albumId = albumId,
                            onSongSelected = { albumId, _ ->
                                navController.navigate("player/$albumId")
                            }
                        )
                    }
                    composable(
                        "player/{albumId}",
                        arguments = listOf(navArgument("albumId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val albumId = backStackEntry.arguments?.getLong("albumId") ?: return@composable
                        MusicPlayerUI(
                            viewModel = viewModel,
                            albumId = albumId,
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }
                }
            }
        }
    }


}





