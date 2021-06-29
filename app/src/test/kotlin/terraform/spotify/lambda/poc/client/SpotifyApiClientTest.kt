package terraform.spotify.lambda.poc.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.Ignore
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import terraform.spotify.lambda.poc.request.AddToPlaylistRequest
import terraform.spotify.lambda.poc.request.DeleteFromPlaylistRequest
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
        val bearerToken = "BQAm8FCfV_q6nS9CWSdTi-4B-nitCj9DFIGTYPp-gFdTP1G88JmaShG7apDd4JBnAH9HVKoO1kb1TY2FVLisIbUg3kKjOh2fqJzQZll2avWNPbmiEtWqlVtk3KU7G6w54BtLKXzpsQBe1GnOH2FRgU3ZGGK1LQCZ8wHcrIDNEUqM5xfBEzAUmTniKm0vjru7Fvm4FXWr62jvi9bYldlrwsw7EcJiDTXkRP3AUNaaeJIZB87ZEvhS8S762INII-E"
        val response = spotifyApiRealClient.currentTrack(
            authorizationString = "Bearer $bearerToken",
        )
        response.execute().let {
            println(it)
            println("code: ${it.code()}")
            println("body: ${it.body()}")
        }
    }

    @Ignore // bearer token を使えないため
    @Test
    fun realApiTestForAddToPlaylist() {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com")
            .client(OkHttpClient()
                .newBuilder()
                .addInterceptor(HttpLoggingInterceptor())
                .addInterceptor(
                    Interceptor { chain ->
                        val request = chain.request()
                        val reqString = Buffer().let{
                            val nReq = request.newBuilder().build()
                            nReq.body?.writeTo(it)
                            it.readUtf8()
                        }
                        println("[debug body] : $reqString")
//                        println("[debug ok intercept] ${request.toString()}")
                        chain.proceed((request))
                    }
                ).build()
            )
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build()

        val spotifyApiRealClient = retrofit.create(SpotifyApiClient::class.java)
        val bearerToken =
            "BQCZ4c29xcPpF0ODtumPJ1ej4QChuPTD4Hgg5JbAd38apKxj5XHtgzwX6-IaSgFMs4GWUa_h8bDSmTbqm8fgp8ESdntW9YQZtanIWpgdqACm_d7wc0bAjOV5-rdw3P_lAw_ZI6jqPvV6d0V4_K6uMA9rru1nZQaWZtrKkISl7K3HjluDlefR4mmZYBR85FjyUuxvh9sDPqUrZCmEq7YegJt28eXe0v_4db4xbsPFlT8o1HKnAB-jiMNEXxXzWSI"
        val response = spotifyApiRealClient.addToPlaylist(
            authorizationString = "Bearer $bearerToken",
            playlistId = "2eR0YK4jBO4ol03Z0FzsaU",
            body = AddToPlaylistRequest(
                uris = listOf("spotify:track:4QXpGTmnWwftsbcSVhBeAm")
            )

        )
        println("request: ${response.request()}")
        response.execute().let {
            println(it)
            println("code: ${it.code()}")
            println("body: ${it.body()}")
            assert(it.code() < 300)
        }
    }

    @Ignore // bearer token を使えないため
    @Test
    fun realApiTestForDeleteFromPlaylist() {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com")
            .client(OkHttpClient()
                .newBuilder()
                .addInterceptor(HttpLoggingInterceptor())
                .addInterceptor(
                    Interceptor { chain ->
                        val request = chain.request()
                        val reqString = Buffer().let{
                            val nReq = request.newBuilder().build()
                            nReq.body?.writeTo(it)
                            it.readUtf8()
                        }
                        println("[debug body] : $reqString")
                        println("[debug ok intercept] ${request.toString()}")
                        val result = chain.proceed((request))
                        result
                    }
                ).build()
            )
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build()

        val spotifyApiRealClient = retrofit.create(SpotifyApiClient::class.java)
        val bearerToken =
            "BQCZ4c29xcPpF0ODtumPJ1ej4QChuPTD4Hgg5JbAd38apKxj5XHtgzwX6-IaSgFMs4GWUa_h8bDSmTbqm8fgp8ESdntW9YQZtanIWpgdqACm_d7wc0bAjOV5-rdw3P_lAw_ZI6jqPvV6d0V4_K6uMA9rru1nZQaWZtrKkISl7K3HjluDlefR4mmZYBR85FjyUuxvh9sDPqUrZCmEq7YegJt28eXe0v_4db4xbsPFlT8o1HKnAB-jiMNEXxXzWSI"
        val response = spotifyApiRealClient.deleteFromPlaylist(
            authorizationString = "Bearer $bearerToken",
            playlistId = "2eR0YK4jBO4ol03Z0FzsaU",
            body = DeleteFromPlaylistRequest(
                uris = listOf("spotify:track:4QXpGTmnWwftsbcSVhBeAm")
            )

        )
        println("request: ${response.request()}")
        response.execute().let {
            println(it)
            println("code: ${it.code()}")
            println("body: ${it.body()}")
            assert(it.code() < 300)
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