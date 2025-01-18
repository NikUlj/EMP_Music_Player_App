package com.example.emp_music_player_app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArtist(artist: Artist): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlbum(album: Album): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song): Long

    @Query("SELECT * FROM Artist")
    fun getAllArtists(): Flow<List<Artist>>

    @Query("SELECT artistId FROM Artist WHERE name = :name LIMIT 1")
    fun getArtistIdByName(name: String): Long?

    @Query("SELECT * FROM Artist WHERE artistId = :artistId LIMIT 1")
    suspend fun getArtistById(artistId: Long): Artist?

    @Query("SELECT albumId FROM Album WHERE name = :name LIMIT 1")
    fun getAlbumIdByName(name: String): Long?

    @Query("SELECT * FROM Album WHERE artistId = :artistId")
    fun getAlbumsByArtist(artistId: Long): Flow<List<Album>>

    @Query("SELECT * FROM Album WHERE albumId = :albumId LIMIT 1")
    fun getAlbumById(albumId: Long): Flow<Album?>

    @Query("SELECT songId FROM Song WHERE title = :title LIMIT 1")
    fun getSongIdByTitle(title: String): Long?

    @Query("SELECT * FROM Song WHERE albumId = :albumId")
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>>
}