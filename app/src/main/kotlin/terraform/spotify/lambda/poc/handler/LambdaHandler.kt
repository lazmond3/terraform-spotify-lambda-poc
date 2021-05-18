package terraform.spotify.lambda.poc.handler

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.RequestHandler
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import terraform.spotify.lambda.poc.client.SpotifyApiClient
import terraform.spotify.lambda.poc.exception.SystemException
import terraform.spotify.lambda.poc.variables.EnvironmentVariables
import java.util.*
import kotlin.system.exitProcess


class LambdaHandler : RequestHandler<Map<String, Any>, Any> {
    val ddb = AmazonDynamoDBClientBuilder.defaultClient()
    val variables = EnvironmentVariables()
    val baseUrl = "https://accounts.spotify.com"

    //    val objectMapper =
    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(JacksonConverterFactory.create())
        .build()

    val spotifyService = retrofit.create(SpotifyApiClient::class.java)


    override fun handleRequest(input: Map<String, Any>, context: Context): Any {
        val logger = context.logger

        println("hello world lambda!")
        logger.log("hello world! lambda 2 in logging")

        logger.log("--- start put item")
        dynamoCrudTest("hello world", logger)
        logger.log("--- end put item")

        logger.log("--- start read")
        val refreshToken = dynamoRead(logger)
        logger.log("--- end read")

        logger.log("--- start -refreshToken read")
        refreshToken(refreshToken, logger)
        logger.log("--- end -refreshToken read")
        return 1
    }

    fun refreshToken(refreshToken: String, logger: LambdaLogger) {
        val bear = "${variables.spotifyClientId}:${variables.spotifyClientSecret}"
        val base64ed = Base64.getEncoder().encodeToString(bear.toByteArray())
        logger.log("base64: $base64ed")
        logger.log("refreshToken: $refreshToken")

        val result = spotifyService.refreshToken(
            authorizationString = "Basic $base64ed",
            grantType = "refresh_token",
            refreshToken = refreshToken,
            clientId = variables.spotifyClientId
        ).execute().let {
            logger.log("request response...isSuccessful: ${it.isSuccessful}")
            logger.log("request response...code: ${it.code()}")
            val body = it.body()
            if (it.isSuccessful && body != null) {
                logger.log("request response...token: ${body.accessToken}")
                logger.log("request response...expires: ${body.expiresIn}")
                logger.log("request response...scope: ${body.scope}")
            } else {
                logger.log("error: ${it.errorBody().toString()}")
            }
        }
    }

    fun dynamoRead(logger: LambdaLogger): String {

        val tableName = "spotify-poc"
        try {
            val result = ddb.getItem(
                GetItemRequest()
                    .withTableName(tableName)
                    .withKey(
                        mapOf(
                            "UserId" to AttributeValue()
                                .withS("moikilo00")
                        )
                    )

            )
            result.item.keys.forEach { key ->
                logger.log(
                    "$key : ${result.item.get(key)}"
                )
            }
            logger.log("-----------")
            return result.item["RefreshToken"]?.s
                ?: throw SystemException("RefreshToken is not in the got item")
        } catch (e: ResourceNotFoundException) {
            logger.log("Error: The table \"$tableName\" can't be found.\n");
            logger.log("Be sure that it exists and that you've typed its name correctly!");
            exitProcess(1);
        } catch (e: AmazonServiceException) {
            logger.log("[amazon service exception] ${e.message}")
            exitProcess(1);
        }
    }

    fun dynamoCrudTest(text: String, logger: LambdaLogger) {

        val tableName = "spotify-poc"
        val item: Map<String, AttributeValue> = mapOf(
            "UserId" to AttributeValue("moikilo00"),
            "RefreshToken" to AttributeValue(variables.spotifyRefreshToken),
            "DebugMessage" to AttributeValue(text)
        )

        try {
            ddb.putItem(tableName, item);
        } catch (e: ResourceNotFoundException) {
            logger.log("Error: The table \"$tableName\" can't be found.\n");
            logger.log("Be sure that it exists and that you've typed its name correctly!");
            exitProcess(1);
        } catch (e: AmazonServiceException) {
            logger.log("[amazon service exception] ${e.message}")
            exitProcess(1);
        }
    }
}
