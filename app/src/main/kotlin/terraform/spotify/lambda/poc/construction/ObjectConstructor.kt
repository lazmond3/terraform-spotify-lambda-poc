package terraform.spotify.lambda.poc.construction

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import terraform.spotify.lambda.poc.client.SpotifyApiAuthClient
import terraform.spotify.lambda.poc.client.SpotifyApiClient
import terraform.spotify.lambda.poc.controller.LineBotHookController
import terraform.spotify.lambda.poc.mapper.dynamo.SpotifyTrackDynamoDbMapper
import terraform.spotify.lambda.poc.mapper.dynamo.UserTokenDynamoDbMapper
import terraform.spotify.lambda.poc.service.LineBotService
import terraform.spotify.lambda.poc.service.SpotifyService
import terraform.spotify.lambda.poc.variables.EnvironmentVariables
import terraform.spotify.lambda.poc.variables.EnvironmentVariablesInterface
import terraform.spotify.lambda.poc.variables.TestEnvironmentVariables

class ObjectConstructor(
    val isForReal: Boolean
) {
    val objectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        registerModule(KotlinModule())
    }

    val tableName = "spotify-poc"
    val trackTableName = "spotify-dynamo-music"
    val ddb = AmazonDynamoDBClientBuilder.defaultClient()
    val variables: EnvironmentVariablesInterface =
        if (isForReal) {
            EnvironmentVariables()
        } else {
            TestEnvironmentVariables()
        }
    val baseUrl = "https://accounts.spotify.com"
    val apiBaseUrl = "https://api.spotify.com"

    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .build()
    val apiRetrofit = Retrofit.Builder()
        .baseUrl(apiBaseUrl)
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .build()
    val spotifyApiAuthClient = retrofit.create(SpotifyApiAuthClient::class.java)
    val spotifyApiClient = apiRetrofit.create(SpotifyApiClient::class.java)


    val userTokenDynamoDBMapper = UserTokenDynamoDbMapper(
        tableName = tableName,
        dbClient = ddb
    )
    val spotifyTrackDynamoDbMapper = SpotifyTrackDynamoDbMapper(
        tableName = trackTableName,
        gsiIndexName = "SpotifyDynamoMusicPlaylistIndex",
        dbClient = ddb
    )
    val lineBotService = LineBotService(variables.lineBotChannelAccessToken)
    val spotifyService = SpotifyService(
        spotifyApiAuthClient = spotifyApiAuthClient,
        spotifyApiClient = spotifyApiClient,
        variables = variables,
        userTokenDynamoDbMapper = userTokenDynamoDBMapper,
        spotifyTrackDynamoDbMapper = spotifyTrackDynamoDbMapper,
        lineBotService = lineBotService
    )

    val lineBotHookController = LineBotHookController(
        lineBotService = lineBotService,
        spotifyDbMapper = spotifyTrackDynamoDbMapper,
        userTokenDynamoDBMapper = userTokenDynamoDBMapper,
        spotifyService = spotifyService
    )
}