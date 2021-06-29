package terraform.spotify.lambda.poc.mapper.dynamo

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.*
import com.amazonaws.services.lambda.runtime.LambdaLogger
import terraform.spotify.lambda.poc.entity.PlaylistTrack
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.system.exitProcess

class SpotifyTrackDynamoDbMapper(
    val tableName: String,
    val gsiIndexName: String,
    val dbClient: AmazonDynamoDB,
) {

    /* TODO
        - create
        - delete
        - read
     */

    fun readRowOrNull(playlistId: String, trackId: String, logger: LambdaLogger): PlaylistTrack? =
        readRow(playlistId, trackId)


    fun delete(playlistId: String, trackId: String, logger: LambdaLogger?) {
        val result = dbClient.deleteItem(
            DeleteItemRequest()
                .withTableName(tableName)
                .withKey(
                    mapOf(
                        "PlaylistId" to AttributeValue()
                            .withS(playlistId),
                        "TrackId" to AttributeValue()
                            .withS(trackId)
                    )
                )
        )
        logger?.log("[delete] result: $result")
    }

    fun create(userId: String, playlistId: String, trackId: String, logger: LambdaLogger) {
        val item: Map<String, AttributeValue> = mapOf(
            "UserId" to AttributeValue(userId),
            "PlaylistId" to AttributeValue(playlistId),
            "TrackId" to AttributeValue(trackId)
        )
        try {
            dbClient.putItem(tableName, item);
        } catch (e: ResourceNotFoundException) {
            logger.log("[playlist mapper] Error: The table \"$tableName\" can't be found.\n");
            logger.log("[playlist mapper] Be sure that it exists and that you've typed its name correctly!");
            exitProcess(1);
        } catch (e: AmazonServiceException) {
            logger.log("[playlist mapper] [amazon service exception] ${e.message}")
            exitProcess(1);
        }
    }

    fun readRow(playlistId: String, trackId: String): PlaylistTrack? {
//        val result = dbClient.query(
//            QueryRequest()
//                .withTableName(tableName)
//                .withKeyConditionExpression("PlaylistId = :playlistId and TrackId = :trackId")
//                .withExpressionAttributeValues(
//                    mapOf(
//                        ":playlistId" to AttributeValue(playlistId),
//                        ":trackId" to AttributeValue(trackId)
//                    )
//                )
//        )
        val result = dbClient.getItem(
            GetItemRequest()
                .withTableName(tableName)
                .withKey(
                    mapOf(
                        "PlaylistId" to AttributeValue()
                            .withS(playlistId),
                        "TrackId" to AttributeValue()
                            .withS(trackId),
                    )
                )

        )
        return result.item?.let { PlaylistTrack(it) }
    }


    // 検証 or LIFF で登録するエンドポイント
    fun registerRefreshToken(userId: String, refreshToken: String, logger: LambdaLogger) {

        val item: Map<String, AttributeValue> = mapOf(
            "UserId" to AttributeValue(userId),
            "RefreshToken" to AttributeValue(refreshToken),
        )

        try {
            dbClient.putItem(tableName, item);
        } catch (e: ResourceNotFoundException) {
            logger.log("Error: The table \"$tableName\" can't be found.\n");
            logger.log("Be sure that it exists and that you've typed its name correctly!");
            exitProcess(1);
        } catch (e: AmazonServiceException) {
            logger.log("[amazon service exception] ${e.message}")
            exitProcess(1);
        }
    }

    private fun withSAttributeUpdateValue(value: String) =
        AttributeValueUpdate()
            .withValue(
                AttributeValue()
                    .withS(value)
            )

    private fun withNAttributeUpdateValue(value: String) =
        AttributeValueUpdate()
            .withValue(
                AttributeValue()
                    .withN(value)
            )


    private fun makeNowTimeString(): String {
        val jpCal: Calendar = Calendar.getInstance()
        val now = LocalDateTime.ofInstant(
            jpCal.toInstant(), ZoneId.of("Asia/Tokyo")
        )
        val timeString = now.format(DateTimeFormatter.ISO_DATE_TIME)
        return timeString
    }

    private fun getUnixTime(): Long {
        return System.currentTimeMillis() / 1000
    }

}