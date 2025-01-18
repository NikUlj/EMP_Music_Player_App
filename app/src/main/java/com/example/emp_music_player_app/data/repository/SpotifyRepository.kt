package com.example.emp_music_player_app.data.repository

import android.util.Base64
import android.util.Log
import com.example.emp_music_player_app.data.api.SpotifyApi
import com.example.emp_music_player_app.data.api.SpotifyTokenResponse

class SpotifyRepository {
    private val authApi = SpotifyApi.createAuthApi()
    private val api = SpotifyApi.createApi()

    private var accessToken: String? = null
    private var tokenExpirationTime: Long = 0

    private suspend fun getAuthHeader(): String {
        if (accessToken == null || System.currentTimeMillis() >= tokenExpirationTime) {
            val credentials = "$CLIENT_ID:$CLIENT_SECRET".toByteArray()
            val encodedCredentials = Base64.encodeToString(credentials, Base64.NO_WRAP)
            val authHeader = "Basic $encodedCredentials"

            val response = authApi.getAccessToken(authorization = authHeader)
            if (response.isSuccessful) {
                val tokenResponse: SpotifyTokenResponse? = response.body()
                tokenResponse?.let { token ->
                    accessToken = token.accessToken
                    tokenExpirationTime = System.currentTimeMillis() + (token.expiresIn * 1000)
                }
            }
        }
        return "Bearer $accessToken"
    }

    suspend fun getArtistImage(artistName: String): String? {
        try {
            val authHeader = getAuthHeader()
            val response = api.searchArtist(artistName, authorization = authHeader)

            if (response.isSuccessful) {
                val artists = response.body()?.artists?.items
                val artist = artists?.firstOrNull()
                val largestImage = artist?.images?.maxByOrNull { image -> image.width }
                return largestImage?.url
            }
        } catch (e: Exception) {
            Log.e("SpotifyRepository", "Error fetching artist image: ${e.message}")
        }
        return null
    }

    suspend fun getAlbumImage(albumName: String, artistName: String): String? {
        try {
            val authHeader = getAuthHeader()
            val query = "$albumName artist:$artistName"
            Log.d("SpotifyRepository", "Searching for album with query: $query")

            val response = api.searchAlbum(query, authorization = authHeader)
            Log.d("SpotifyRepository", "Album search response successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val albums = response.body()?.albums?.items
                Log.d("SpotifyRepository", "Found ${albums?.size ?: 0} albums in search results")

                // Try to find an exact match first
                val album = albums?.find { album ->
                    val nameMatch = album.name.equals(albumName, ignoreCase = true)
                    val artistMatch = album.artists.any { it.name.equals(artistName, ignoreCase = true) }
                    Log.d("SpotifyRepository", """
                    Checking album: ${album.name} by ${album.artists.map { it.name }}
                    Name match: $nameMatch
                    Artist match: $artistMatch
                """.trimIndent())
                    nameMatch && artistMatch
                } ?: albums?.firstOrNull()

                Log.d("SpotifyRepository", "Selected album: ${album?.name}")
                Log.d("SpotifyRepository", "Available images: ${album?.images?.map { "${it.width}x${it.height}: ${it.url}" }}")

                val largestImage = album?.images?.maxByOrNull { image -> image.width }
                return largestImage?.url.also {
                    Log.d("SpotifyRepository", "Selected image URL: $it")
                }
            } else {
                Log.e("SpotifyRepository", "Error response: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("SpotifyRepository", "Error fetching album image: ${e.message}")
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private const val CLIENT_ID = "87fef7a583744cfca8e671a70f4838d5"
        private const val CLIENT_SECRET = "cb108356587a44d89378515cb6132a74"
    }
}