package terraform.spotify.lambda.poc.handler

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.RequestHandler
import terraform.spotify.lambda.poc.variables.EnvironmentVariables
import kotlin.system.exitProcess


class LambdaHandler : RequestHandler<Map<String, Any>, Any> {
    val ddb = AmazonDynamoDBClientBuilder.defaultClient()
    val variables = EnvironmentVariables()

    override fun handleRequest(input: Map<String, Any>, context: Context): Any {
        val logger = context.logger

        println("hello world lambda!")
        logger.log("hello world! lambda 2 in logging")

        logger.log("--- start put item")
        dynamoCrudTest("hello world", logger)
        logger.log("--- end put item")

        logger.log("--- start read")
        dynamoRead(logger)
        logger.log("--- end read")
        return 1
    }

    fun refreshToken(logger: LambdaLogger) {

    }

    fun dynamoRead(logger: LambdaLogger) {

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
