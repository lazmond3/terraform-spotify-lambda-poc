package terraform.spotify.lambda.poc.handler

import com.amazonaws.AmazonServiceException
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.RequestHandler
import kotlin.system.exitProcess


class LambdaHandler : RequestHandler<Map<String, Any>, Any> {
    override fun handleRequest(input: Map<String, Any>, context: Context): Any {
        val logger = context.logger

        println("hello world lambda!")
        logger.log("hello world! lambda 2 in logging")

        dynamoCrudTest("hello world", logger)
        return 1
    }

    fun dynamoCrudTest(text: String, logger: LambdaLogger) {
        val ddb = AmazonDynamoDBClientBuilder.defaultClient();
        val tableName = "spotify-poc"
        val item: Map<String, AttributeValue> = mapOf(
            "userId" to AttributeValue("moikilo00"),
            "refreshToken" to AttributeValue("AQCJz-WoKqGrO2vAVptjdpjcJrha1CbrVfr3gremuL46TxXbeRTvsAtSQz3PWhcPnmxHO3huXmbQXcOu_bIlyW7hgfhufolwPQETozI7xjrorPBd6PQhVgPqrYed30wzNow"),
            "debugMessage" to AttributeValue(text)
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
