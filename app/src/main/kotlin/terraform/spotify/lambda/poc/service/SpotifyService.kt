package terraform.spotify.lambda.poc.service

import com.amazonaws.services.lambda.runtime.LambdaLogger
import terraform.spotify.lambda.poc.client.SpotifyApiClient
import terraform.spotify.lambda.poc.entity.Token
import terraform.spotify.lambda.poc.entity.UserToken
import terraform.spotify.lambda.poc.exception.SystemException
import terraform.spotify.lambda.poc.mapper.dynamo.UserTokenDynamoDbMapper
import terraform.spotify.lambda.poc.variables.EnvironmentVariables
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class SpotifyService(
    val variables: EnvironmentVariables,
    val spotifyApiClient: SpotifyApiClient,
    val userTokenDynamoDbMapper: UserTokenDynamoDbMapper
) {
    fun readAccessTokenOrUpdated(userId: String, logger: LambdaLogger): String {
        val token = userTokenDynamoDbMapper.readTokenRowOrNull(userId, logger)
            ?: throw SystemException("No entry for UserId=$userId")

        if (token.refreshToken != null &&
            (token.expiresAt == null ||
                    checkExpired(token.expiresAt))
        ) {
            // refresh Token する
            val refreshedAccessToken = refreshToken(token.refreshToken, logger)
            val newUserToken = UserToken(
                userId = userId,
                refreshToken = token.refreshToken,
                accessToken = refreshedAccessToken.accessToken,
                expiresAt = refreshedAccessToken.expiresIn + getUnixTime().toInt(),
                updatedAt = makeNowTimeString()
            )
            userTokenDynamoDbMapper.update(newUserToken)
            return refreshedAccessToken.accessToken
        } else {
            return token.accessToken
                ?: throw SystemException("Fetched access token is null!! token: $token")
        }
    }


    // TODO: impl
    private fun checkExpired(expiresAt: Int): Boolean {
        return true
    }

    private fun getUnixTime(): Long {
        return System.currentTimeMillis() / 1000
    }

    private fun makeNowTimeString(): String {
        val jpCal: Calendar = Calendar.getInstance()
        val now = LocalDateTime.ofInstant(
            jpCal.toInstant(), ZoneId.of("Asia/Tokyo")
        )
        val timeString = now.format(DateTimeFormatter.ISO_DATE_TIME)
        return timeString
    }

    fun refreshToken(refreshToken: String, logger: LambdaLogger): Token {
        val bear = "${variables.spotifyClientId}:${variables.spotifyClientSecret}"
        val base64ed = Base64.getEncoder().encodeToString(bear.toByteArray())
        logger.log("base64: $base64ed")
        logger.log("refreshToken: $refreshToken")

        spotifyApiClient.refreshToken(
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
                val token = Token(
                    accessToken = body.accessToken,
                    expiresIn = body.expiresIn
                )
                return token
            } else {
                logger.log("error: ${it.errorBody().toString()}")
                throw SystemException("error: ${it.errorBody().toString()}")
            }
        }
    }
}
