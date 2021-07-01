package terraform.spotify.lambda.poc.client

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import terraform.spotify.lambda.poc.response.spotify.AcquireRefreshTokenResponse
import terraform.spotify.lambda.poc.response.spotify.RefreshTokenResponse

interface SpotifyApiAuthClient {
    @FormUrlEncoded
    @POST("api/token")
    fun refreshToken(
        @Header("Authorization") authorizationString: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String
    ): Call<RefreshTokenResponse>

    @FormUrlEncoded
    @POST("api/token")
    fun acquireRefreshToken(
        @Header("Authorization") authorizationString: String,
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String
    ): Call<AcquireRefreshTokenResponse>

}
