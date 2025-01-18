package com.example.emp_music_player_app.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity
data class Artist(
    @PrimaryKey(autoGenerate = true) val artistId: Long = 0,
    val name: String,
    val imagePath: String? = null
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
    val artistId: Long,
    val imagePath: String? = null
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
    @PrimaryKey(autoGenerate = true) val songId: Long = 0,
    val title: String,
    val duration: Long,
    val uri: String,
    val albumId: Long
)


//data class Song(
//    val id: Long,
//    val title: String,
//    val artist: String,
//    val album: String,
//    val duration: Long,
//    val uri: Uri
//)