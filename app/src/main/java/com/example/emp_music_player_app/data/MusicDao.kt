package com.example.emp_music_player_app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: Artist): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: Album): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song): Long

    @Query("SELECT * FROM Artist")
    fun getAllArtists(): Flow<List<Artist>>

    @Query("SELECT * FROM Album WHERE artistId = :artistId")
    fun getAlbumsByArtist(artistId: Long): Flow<List<Album>>

    @Query("SELECT * FROM Song WHERE albumId = :albumId")
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>>
}