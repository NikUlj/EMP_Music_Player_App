package com.example.emp_music_player_app.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity
data class Artist(
    @PrimaryKey(autoGenerate = true) val artist: Long = 0,
    val name: String
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Artist::class,
            parentColumns = ["artistId"],
            childColumns = ["artistId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Album(
    @PrimaryKey(autoGenerate = true) val albumId: Long = 0,
    val name: String,
    val artistId: Long
)


@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Album::class,
            parentColumns = ["albumId"],
            childColumns = ["albumId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: Uri
)