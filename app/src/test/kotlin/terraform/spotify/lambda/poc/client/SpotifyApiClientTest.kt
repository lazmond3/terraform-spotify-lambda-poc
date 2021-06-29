package terraform.spotify.lambda.poc.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Ignore
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File

class SpotifyApiClientTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var objectMapper: ObjectMapper
    private lateinit var spotifyApiAuthClient: SpotifyApiAuthClient
    private lateinit var spotifyApiClient: SpotifyApiClient

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.registerModule(KotlinModule())


        mockWebServer = MockWebServer()
        mockWebServer.start()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build()

        spotifyApiAuthClient = retrofit.create(SpotifyApiAuthClient::class.java)
        spotifyApiClient = retrofit.create(SpotifyApiClient::class.java)

    }

    @Ignore // bearer token を使えないため
    @Test
    fun realApiTest() {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com")
            .client(OkHttpClient())
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build()

        val spotifyApiRealClient = retrofit.create(SpotifyApiClient::class.java)
        val bearerToken = ""
        val response = spotifyApiRealClient.currentTrack(
            authorizationString = "Bearer $bearerToken",
        )
        response.execute().let{
            println(it)
            println("code: ${it.code()}")
            println("body: ${it.body()}")
        }
    }

    @Test
    fun currentTrack() {
        val mockResponse = mockResponseFromJson(200, "json/current_track.json")
        mockWebServer.enqueue(mockResponse)
        val response = spotifyApiClient.currentTrack(
            authorizationString = "",
        )
        response.execute().let {
            assert(it.isSuccessful)
            assert(it.code() == 200)
            val body = it.body()
            assert(body != null)
            if (body != null) {
                assert(body.isPlaying == true)
                assert(body.timestamp == 1624960525343L)
                println("[debug body] $body")
            }
        }
    }

    private fun mockResponseFromJson(code: Int, resourcePath: String): MockResponse {

        val fullPath = javaClass.classLoader.getResource(resourcePath)?.path
            ?: fail("Invalid resource path: $resourcePath")

        val reader = File(fullPath).bufferedReader()
        val text: String = reader.use { it.readText() }

        val response = MockResponse()
            .setResponseCode(code)
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .setHeader("Connection", "close")

        response.setBody(text)
        return response
    }

}