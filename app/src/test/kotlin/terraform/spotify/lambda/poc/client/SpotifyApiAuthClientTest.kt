package terraform.spotify.lambda.poc.client

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

class SpotifyApiAuthClientTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var objectMapper: ObjectMapper
    private lateinit var spotifyApiAuthClient: SpotifyApiAuthClient

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper()
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build()

        spotifyApiAuthClient = retrofit.create(SpotifyApiAuthClient::class.java)
    }

    @Test
    fun refreshToken() {
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .setHeader("Connection", "close")
            .setBody(
                """
                {
                  "access_token": "accessToken",
                  "token_type": "Bearer",
                  "expires_in": 3600,
                  "scope": "user-modify-playback-state user-library-read user-follow-read user-library-modify playlist-modify-public user-read-playback-state user-read-currently-playing user-read-email user-read-recently-played user-read-playback-position user-read-private user-top-read"
                }
            """.trimIndent()
            )
        mockWebServer.enqueue(mockResponse)
        val response = spotifyApiAuthClient.refreshToken(
            authorizationString = "",
            grantType = "",
            refreshToken = "",
            clientId = ""
        )
        response.execute().let {
            assert(it.isSuccessful)
            assert(it.code() == 200)
            assert(it.body()?.accessToken == "accessToken")
            assert(it.body()?.tokenType == "Bearer")
            assert(it.body()?.expiresIn == 3600)
        }
    }

    @Test
    fun acquireRefreshToken() {
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .setHeader("Connection", "close")
            .setBody(
                """
                    {
                      "access_token": "access",
                      "token_type": "Bearer",
                      "expires_in": 3600,
                      "refresh_token": "refresh",
                      "scope": "user-modify-playback-state user-library-read user-library-modify user-follow-read playlist-modify-public user-read-playback-state user-read-currently-playing user-read-email user-read-recently-played user-read-playback-position user-read-private user-top-read"
                    }
            """.trimIndent()
            )
        mockWebServer.enqueue(mockResponse)
        val response = spotifyApiAuthClient.acquireRefreshToken(
            authorizationString = "",
            code = "",
            redirectUri = ""
        )
        response.execute().let {
            assert(it.isSuccessful)
            assert(it.code() == 200)
            assert(it.body()?.accessToken == "access")
            assert(it.body()?.tokenType == "Bearer")
            assert(it.body()?.expiresIn == 3600)
            assert(it.body()?.refreshToken == "refresh")
            assert(it.body()?.scope == "user-modify-playback-state user-library-read user-library-modify user-follow-read playlist-modify-public user-read-playback-state user-read-currently-playing user-read-email user-read-recently-played user-read-playback-position user-read-private user-top-read")
        }
    }
}