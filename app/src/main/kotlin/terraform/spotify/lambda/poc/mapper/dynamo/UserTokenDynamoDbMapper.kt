package terraform.spotify.lambda.poc.mapper.dynamo

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.*
import com.amazonaws.services.lambda.runtime.LambdaLogger
import terraform.spotify.lambda.poc.entity.UserToken
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.system.exitProcess

class UserTokenDynamoDbMapper(
    val tableName: String,
    val dbClient: AmazonDynamoDB,

    ) {
    // もし エントリがなかったら -> エラー？
    // もし expire してたら -> spotify で更新処理かけたい(依存大丈夫？) 新規登録して それを返す
    // これは spotify service でやること
    // もし expire してなかったら
    fun readTokenRowOrNull(userId: String, logger: LambdaLogger): UserToken? {
        val r = readRow(userId, logger)?.item
        return r?.let {
            UserToken(it)
        }
    }


    // トークン更新したらこちらを使う
    fun updateWithRefreshedToken(userId: String, newAccessToken: String, expiresIn: Int, logger: LambdaLogger) {
        val timeString = makeNowTimeString()

        val userToken = UserToken(
            userId = userId,
            accessToken = newAccessToken,
            expiresAt = expiresIn,
        )
        update(userToken)
    }

    fun update(userToken: UserToken) {
        dbClient.updateItem(
            UpdateItemRequest()
                .withTableName(tableName)
                .withKey(
                    mapOf(
                        "UserId" to AttributeValue()
                            .withS(userToken.userId)
                    )
                )
                .withAttributeUpdates(
                    userToken.let {
                        val map = mutableMapOf<String, AttributeValueUpdate>()
                        if (it.accessToken != null) {
                            map.put("AccessToken", withSAttributeUpdateValue(it.accessToken))
                        }
                        if (it.refreshToken != null) {
                            map.put("RefreshToken", withSAttributeUpdateValue(it.refreshToken))
                        }
                        if (it.expiresAt != null) {
                            map.put("ExpiresAt", withNAttributeUpdateValue(it.expiresAt.toString()))
                        }
                        if (it.updatedAt != null) {
                            map.put("UpdatedAt", withSAttributeUpdateValue(it.updatedAt))
                        }
                        map
                    }
                )
        )
    }


    // 中間。読み出すときはこれを使う。
    // この結果がなかったらregister する必要があるし（でもエラーメッセージを返す)
    // この結果があっても、expiresIn がだめだったら更新処理をかける必要がある。
    private fun readRow(userId: String, logger: LambdaLogger): GetItemResult? {
        val result = dbClient.getItem(
            GetItemRequest()
                .withTableName(tableName)
                .withKey(
                    mapOf(
                        "UserId" to AttributeValue()
                            .withS(userId)
                    )
                )

        )
        return result
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