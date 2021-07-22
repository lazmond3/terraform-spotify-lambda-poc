package terraform.spotify.lambda.poc.client

import retrofit2.Call
import retrofit2.http.*
import terraform.spotify.lambda.poc.request.AddToPlaylistRequest
import terraform.spotify.lambda.poc.request.DeleteFromPlaylistRequest
import terraform.spotify.lambda.poc.response.spotify.*

interface SpotifyApiClient {
    @GET("/v1/me/player/currently-playing")
    fun currentTrack(
        @Header("Authorization") authorizationString: String,
        @Header("Accept-Language") acceptLanguage: String = "ja;q=1",
        @Query("marget") marget: String = "JP"
    ): Call<SpotifyCurrentTrackResponse>

    @POST("/v1/playlists/{playlist_id}/tracks")
    fun addToPlaylist(
        @Header("Authorization") authorizationString: String,
        @Header("Accept-Language") acceptLanguage: String = "ja;q=1",
        @Header("Content-Type") contentType: String = "application/json",
        @Path("playlist_id") playlistId: String,
        @Body body: AddToPlaylistRequest
    ): Call<AddToPlaylistResponse>

    //    @DELETE("/v1/playlists/{playlist_id}/tracks")
    @HTTP(method = "DELETE", path = "/v1/playlists/{playlist_id}/tracks", hasBody = true)
    fun deleteFromPlaylist(
        @Header("Authorization") authorizationString: String,
        @Header("Accept-Language") acceptLanguage: String = "ja;q=1",
        @Header("Content-Type") contentType: String = "application/json",
        @Path("playlist_id") playlistId: String,
        @Body body: DeleteFromPlaylistRequest
    ): Call<DeleteFromPlaylistResponse>

    @GET("/v1/me/playlists")
    fun getPlaylists(
        @Header("Authorization") authorizationString: String,
        @Header("Accept-Language") acceptLanguage: String = "ja;q=1",
        @Query("limit") limit: Int,
        @Query("offset") offset: Int = 0
    ): Call<PlaylistResponse>

    @GET("/v1/playlists/{playlist_id}")
    fun getSinglePlaylist(
        @Header("Authorization") authorizationString: String,
        @Header("Accept-Language") acceptLanguage: String = "ja;q=1",
        @Path("playlist_id") playlistId: String,
    ): Call<ReadPlaylistItemResponse>

    @PUT("/v1/me/tracks")
    fun addToFavorite(
        @Header("Authorization") authorizationString: String,
        @Header("Accept-Language") acceptLanguage: String = "ja;q=1",
        @Query("ids") ids: String
    ): Call<Void>

    @DELETE("/v1/me/tracks")
    fun deleteFromFavorite(
        @Header("Authorization") authorizationString: String,
        @Header("Accept-Language") acceptLanguage: String = "ja;q=1",
        @Query("ids") ids: String
    ): Call<Void>
}
