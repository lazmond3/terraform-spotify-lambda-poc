package terraform.spotify.lambda.poc.construction

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import terraform.spotify.lambda.poc.client.SpotifyApiClient
import terraform.spotify.lambda.poc.controller.LineBotHookController
import terraform.spotify.lambda.poc.mapper.dynamo.UserTokenDynamoDbMapper
import terraform.spotify.lambda.poc.service.SpotifyService
import terraform.spotify.lambda.poc.variables.EnvironmentVariables

class ObjectConstructor {
//    val objectMapper = ObjectMapper()
    val tableName = "spotify-poc"
    val ddb = AmazonDynamoDBClientBuilder.defaultClient()
    val variables = EnvironmentVariables()
    val baseUrl = "https://accounts.spotify.com"
    val lineBotHookController = LineBotHookController(variables.lineBotChannelAccessToken)
    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(JacksonConverterFactory.create())
        .build()
    val spotifyApiClient = retrofit.create(SpotifyApiClient::class.java)

    val userTokenDynamoDBMapper = UserTokenDynamoDbMapper(
        tableName = tableName,
        dbClient = ddb
    )
//    val spotifyService = SpotifyService(
//        spotifyApiClient = spotifyApiClient,
//        variables = variables,
//        userTokenDynamoDbMapper = userTokenDynamoDBMapper
//    )
}