package com.example.emp_music_player_app.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface SpotifyApi {
    @POST("api/token")
    @FormUrlEncoded
    suspend fun getAccessToken(
        @Field("grant_type") grantType: String = "client_credentials",
        @Header("Authorization") authorization: String
    ): Response<SpotifyTokenResponse>

    @GET("v1/search")
    suspend fun searchArtist(
        @Query("q") query: String,
        @Query("type") type: String = "artist",
        @Header("Authorization") authorization: String
    ): Response<SpotifySearchResponse>

    @GET("v1/search")
    suspend fun searchAlbum(
        @Query("q") query: String,
        @Query("type") type: String = "album",
        @Header("Authorization") authorization: String
    ): Response<SpotifyAlbumSearchResponse>

    companion object {
        private const val AUTH_BASE_URL = "https://accounts.spotify.com/"
        private const val API_BASE_URL = "https://api.spotify.com/"

        fun createAuthApi(): SpotifyApi {
            return Retrofit.Builder()
                .baseUrl(AUTH_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SpotifyApi::class.java)
        }

        fun createApi(): SpotifyApi {
            return Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SpotifyApi::class.java)
        }
    }
}
data class SpotifyTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int
)

data class SpotifySearchResponse(
    val artists: ArtistsResponse
)

data class ArtistsResponse(
    val items: List<SpotifyArtist>
)

data class SpotifyArtist(
    val id: String,
    val name: String,
    val images: List<SpotifyImage>
)

data class SpotifyAlbumResponse(
    val id: String,
    val name: String,
    val images: List<SpotifyImage>
)

data class SpotifyImage(
    val url: String,
    val height: Int,
    val width: Int
)

data class SpotifyAlbumSearchResponse(
    val albums: AlbumsResponse
)

data class AlbumsResponse(
    val items: List<SpotifyAlbum>
)

data class SpotifyAlbum(
    val id: String,
    val name: String,
    val images: List<SpotifyImage>,
    val artists: List<SpotifyArtistSimple>
)

data class SpotifyArtistSimple(
    val id: String,
    val name: String
)

