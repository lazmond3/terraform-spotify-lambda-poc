package terraform.spotify.lambda.poc.clinet

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface SpotifyApiClient {
    @FormUrlEncoded
    @POST("api/token")
    fun refreshToken(
        @Header("Authorization") authorizationString: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String
    ): Call<Any>
}
