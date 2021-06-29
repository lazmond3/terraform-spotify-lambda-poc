package terraform.spotify.lambda.poc.client

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import terraform.spotify.lambda.poc.response.spotify.SpotifyCurrentTrackResponse

interface SpotifyApiClient {
    @GET("/v1/me/player/currently-playing?market=JP")
    fun currentTrack(
        @Header("Authorization") authorizationString: String,
        @Header("Accept-Language") acceptLanguage: String = "ja;q=1"
    ): Call<SpotifyCurrentTrackResponse>
}
