package terraform.spotify.lambda.poc.service

import com.amazonaws.services.lambda.runtime.LambdaLogger
import terraform.spotify.lambda.poc.client.SpotifyApiAuthClient
import terraform.spotify.lambda.poc.client.SpotifyApiClient
import terraform.spotify.lambda.poc.entity.Token
import terraform.spotify.lambda.poc.exception.SystemException
import terraform.spotify.lambda.poc.mapper.dynamo.SpotifyTrackDynamoDbMapper
import terraform.spotify.lambda.poc.mapper.dynamo.UserTokenDynamoDbMapper
import terraform.spotify.lambda.poc.request.AddToPlaylistRequest
import terraform.spotify.lambda.poc.request.DeleteFromPlaylistRequest
import terraform.spotify.lambda.poc.response.spotify.SpotifyCurrentTrackResponse
import terraform.spotify.lambda.poc.variables.EnvironmentVariables
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class SpotifyService(
    val variables: EnvironmentVariables,
    val spotifyApiAuthClient: SpotifyApiAuthClient,
    val spotifyApiClient: SpotifyApiClient,
    val userTokenDynamoDbMapper: UserTokenDynamoDbMapper,
    val spotifyTrackDynamoDbMapper: SpotifyTrackDynamoDbMapper,
    val lineBotService: LineBotService
) {
    fun registerNewPlaylistId(userId: String, playlistId: String, logger: LambdaLogger) {
        val userToken = userTokenDynamoDbMapper.readRowOrNull(userId, logger)
        val newUserToken = userToken?.copy(
            playlistId = playlistId
        ) ?: throw SystemException("userToken is null: userId: $userId")
        userTokenDynamoDbMapper.update(newUserToken)
    }

    fun addCurrentTrackToPlaylist(userId: String, logger: LambdaLogger) {
        // userId
        // dynamo から取得する
        // [x] access トークンを取得する 実装済み
        val token: String = readAccessTokenOrUpdated(userId, logger)
        val playlistId: String? = readPlaylistId(userId, logger)
        if (playlistId == null) {
            return
        }
        // spotify API で trackId を取得
        val body = currentTrackInfo(token)
        val trackId = body.item.uri

        // dynamo にすでに追加してないか検証
        if (isAlreadyAdded(playlistId, trackId)) {
            val addedAt = getWhenAdded(playlistId, trackId)
            // LINE でメッセージ返したい
            lineBotService.sendMessage(userId, text = "この曲は、$addedAt にすでに追加されています。")
        } else {
            // もし追加してなかったら、 playlist に追加して、 dynamo にも追加する。
            val response = spotifyApiClient.addToPlaylist(
                authorizationString = "Bearer $token",
                playlistId = playlistId,
                body = AddToPlaylistRequest(
                    uris = listOf(trackId)
                )
            ).execute()
            if (response.code() >= 300) {
                lineBotService.sendMessage(
                    userId, text = "追加に失敗しました(okhttp). response code = ${response.code()}"
                )
                return
            }
            spotifyTrackDynamoDbMapper.create(
                userId, playlistId, trackId, logger
            )
            lineBotService.sendMessage(
                userId, text = "追加が完了しました。 \n" +
                        "by     : ${body.item.artists[0].name}\n" +
                        "タイトル: ${body.item.name}\n" +
                        "url   : ${body.item.externalUrls.spotify}"
            )
        }
    }

    fun deleteCurrentTrackFromPlaylist(userId: String, logger: LambdaLogger) {
        // userId
        // dynamo から取得する
        // [x] access トークンを取得する 実装済み
        val token: String = readAccessTokenOrUpdated(userId, logger)
        val playlistId: String? = readPlaylistId(userId, logger)
        if (playlistId == null) {
            return
        }
        // spotify API で trackId を取得
        val body = currentTrackInfo(token)
        val trackId = body.item.uri

        // dynamo にすでに追加してないか検証
        if (isAlreadyAdded(playlistId, trackId)) {
            val addedAt = getWhenAdded(playlistId, trackId)
            // LINE でメッセージ返したい
            lineBotService.sendMessage(userId, text = "この曲は、$addedAt にすでに追加されています。削除します")

            val response = spotifyApiClient.deleteFromPlaylist(
                authorizationString = "Bearer $token",
                playlistId = playlistId,
                body = DeleteFromPlaylistRequest(
                    uris = listOf(trackId)
                )
            ).execute()
            if (response.code() >= 300) {
                lineBotService.sendMessage(
                    userId, text = "追加に失敗しました(okhttp). response code = ${response.code()}"
                )
                return
            }
            spotifyTrackDynamoDbMapper.delete(
                playlistId, trackId, logger
            )
            lineBotService.sendMessage(
                userId, text = "削除が完了しました。 \n" +
                        "by     : ${body.item.artists[0].name}\n" +
                        "タイトル: ${body.item.name}\n" +
                        "url   : ${body.item.externalUrls.spotify}"
            )
        } else {
            lineBotService.sendMessage(
                userId, text = "この曲は登録されていません。 \n" +
                        "by     : ${body.item.artists[0].name}\n" +
                        "タイトル: ${body.item.name}\n" +
                        "url   : ${body.item.externalUrls.spotify}"
            )
        }
    }

    private fun currentTrackInfo(token: String): SpotifyCurrentTrackResponse {
        val response = spotifyApiClient.currentTrack(
            authorizationString = "Bearer $token"
        ).execute()
        val body = response.body()

        if (body != null) {
            return body
        } else throw SystemException("[spotifyService] currentTrack failed: code = ${response.code()}")

    }

    private fun isAlreadyAdded(playlistId: String, trackId: String): Boolean {
        val result = spotifyTrackDynamoDbMapper.readRow(playlistId, trackId)
        return result != null
    }

    private fun getWhenAdded(playlistId: String, trackId: String): LocalDateTime? {
        val result = spotifyTrackDynamoDbMapper.readRow(playlistId, trackId)
        return result?.addedAt
    }

    private fun readPlaylistId(userId: String, logger: LambdaLogger): String? {

        // もし playlist が設定されてなかったら、
        // ここで SystemException を出す。
        // このハンドラ欲しいけど?
        val token = userTokenDynamoDbMapper.readTokenRowOrNull(userId, logger)
            ?: throw SystemException("No entry for UserId=$userId")
        return token.playlistId
    }

    private fun readAccessTokenOrUpdated(userId: String, logger: LambdaLogger): String {
        val token = userTokenDynamoDbMapper.readTokenRowOrNull(userId, logger)
            ?: throw SystemException("No entry for UserId=$userId")

        if (token.refreshToken != null &&
            (token.expiresAt == null ||
                    checkExpired(token.expiresAt))
        ) {
            // refresh Token する
            val refreshedAccessToken = refreshToken(token.refreshToken, logger)
            val newUserToken = token.copy(
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
    // 必ず毎回 Expire するようにしている。
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

        spotifyApiAuthClient.refreshToken(
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
