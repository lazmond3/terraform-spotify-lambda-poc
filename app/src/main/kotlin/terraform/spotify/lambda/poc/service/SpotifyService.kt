package terraform.spotify.lambda.poc.service

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Calendar
import terraform.spotify.lambda.poc.`interface`.LoggerInterface
import terraform.spotify.lambda.poc.client.SpotifyApiAuthClient
import terraform.spotify.lambda.poc.client.SpotifyApiClient
import terraform.spotify.lambda.poc.construction.ObjectConstructor
import terraform.spotify.lambda.poc.entity.Token
import terraform.spotify.lambda.poc.entity.UserToken
import terraform.spotify.lambda.poc.exception.SystemException
import terraform.spotify.lambda.poc.mapper.dynamo.SpotifyTrackDynamoDbMapper
import terraform.spotify.lambda.poc.mapper.dynamo.UserTokenDynamoDbMapper
import terraform.spotify.lambda.poc.request.AddToPlaylistRequest
import terraform.spotify.lambda.poc.request.DeleteFromPlaylistRequest
import terraform.spotify.lambda.poc.response.spotify.AcquireRefreshTokenResponse
import terraform.spotify.lambda.poc.response.spotify.PlaylistResponse
import terraform.spotify.lambda.poc.response.spotify.ReadPlaylistItemResponse
import terraform.spotify.lambda.poc.response.spotify.SpotifyCurrentTrackResponse
import terraform.spotify.lambda.poc.variables.EnvironmentVariablesInterface

class SpotifyService(
    val variables: EnvironmentVariablesInterface,
    val spotifyApiAuthClient: SpotifyApiAuthClient,
    val spotifyApiClient: SpotifyApiClient,
    val userTokenDynamoDbMapper: UserTokenDynamoDbMapper,
    val spotifyTrackDynamoDbMapper: SpotifyTrackDynamoDbMapper,
    val lineBotService: LineBotService,
    val objectConstructor: ObjectConstructor
) {

    // 呼ばれるところ: プレイリスト追加
    private fun addToFavorite(userId: String, trackId: String, logger: LoggerInterface) {
        val token: String = readAccessTokenOrUpdated(userId, logger)
        logger.log("trackid: $trackId")
        val trackSimpleId = trackId.split(":")[2]
        val response = spotifyApiClient.addToFavorite(
            authorizationString = "Bearer $token",
            ids = trackSimpleId
        ).execute()
        logger.log("response: ${response.code()} error: ${response.errorBody()?.string()}")
        if (response.code() >= 300) {
            logger.log("[addToFavorite] service failed for userId:$userId, trackId: $trackId")
            throw SystemException("[addToFavorite] service failed for userId:$userId, trackId: $trackId")
        }
    }

    private fun deleteFromFavorite(userId: String, trackId: String, logger: LoggerInterface) {
        val token: String = readAccessTokenOrUpdated(userId, logger)
        val trackSimpleId = trackId.split(":")[2]
        val response = spotifyApiClient.deleteFromFavorite(
            authorizationString = "Bearer $token",
            ids = trackSimpleId
        ).execute()
        if (response.code() >= 300) {
            logger.log("[deleteFromFavorite] service failed for userId:$userId, trackId: $trackId")
            throw SystemException("[deleteFromFavorite] service failed for userId:$userId, trackId: $trackId")
        }
    }

    fun getSinglePlaylist(userId: String, playlistId: String, logger: LoggerInterface): ReadPlaylistItemResponse {
        val token: String = readAccessTokenOrUpdated(userId, logger)
        val response = spotifyApiClient.getSinglePlaylist(
            authorizationString = "Bearer $token",
            playlistId = playlistId
        ).execute()

        if (response == null || response.code() >= 300) {
            lineBotService.sendMessage(
                userId, text = "プレイリスト情報の取得に失敗しました(okhttp). response code = ${response.code()}"
            )
            throw SystemException("プレイリスト情報の取得に失敗しました(okhttp). response code = ${response.code()}")
        }

        return response.body()
            ?: throw SystemException("プレイリスト情報の取得に失敗しました(okhttp). response code = ${response.code()}")
    }

    fun registerNewPlaylistId(userId: String, playlistId: String, logger: LoggerInterface) {
        val userToken = userTokenDynamoDbMapper.readRowOrNull(userId, logger)
            ?: run {
                lineBotService.sendMessage(userId, "先に「Spotify連携」してね！")
                logger.log("[registerNewPlaylistId] userToken is null: userId: $userId")
                throw SystemException("[registerNewPlaylistId] userToken is null: userId: $userId")
            }

        val playlist = getSinglePlaylist(userId, playlistId, logger)

        val newUserToken = userToken.copy(
            playlistId = playlistId,
            playlistName = playlist.name
        )

        userTokenDynamoDbMapper.update(newUserToken)
        lineBotService.sendMessage(userId, "プレイリストを登録しました: ${playlist.name}")
    }

    // add-current のときに、PlaylistName がなければ、dynamoDB にアップデートをかける
    fun updatePlaylistName(userId: String, playlistId: String, logger: LoggerInterface): String {
        val userToken = userTokenDynamoDbMapper.readRowOrNull(userId, logger)

        val playlist = getSinglePlaylist(userId, playlistId, logger)

        val newUserToken = userToken?.copy(
            playlistName = playlist.name
        ) ?: throw SystemException("userToken is null: userId: $userId")

        userTokenDynamoDbMapper.update(newUserToken)
        return playlist.name
    }


    fun addCurrentTrackToPlaylist(userId: String, logger: LoggerInterface) {
        // userId
        // dynamo から取得する
        // [x] access トークンを取得する 実装済み
        val token: String = readAccessTokenOrUpdated(userId, logger)
        val playlistInfo = readPlaylistId(userId, logger)
        val playlistId = playlistInfo.playlistId
            ?: run {
                lineBotService.sendMessage(userId, "先にプレイリストIDを登録してね！")
                logger.log("[addCurrentTrackToPlaylist] playlist Id が null です")
                throw SystemException("プレイリストIDが設定されていません。")
            }
        val playlistName = playlistInfo.playlistName
            ?: updatePlaylistName(userId, playlistId = playlistId, logger)

        val body = currentTrackInfo(token)
        val trackId = body.item.uri

        addToFavorite(userId, trackId, logger)

        // dynamo にすでに追加してないか検証
        if (isAlreadyAdded(playlistId, trackId)) {
            val addedAt = getWhenAdded(playlistId, trackId)
            // LINE でメッセージ返したい
            lineBotService.sendMessage(
                userId,
                text = "この曲は、${addedAt} に ${playlistName} にすでに追加されています。\n" +
                    "${body.item.externalUrls.spotify}"
            )
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
                userId, text = "追加が完了しました ${playlistName.let { " ($it) " }}。  \n" +
                "タイトル: ${body.item.name}\n" +
                "by     : ${body.item.artists[0].name}\n" +
                "${body.item.externalUrls.spotify}"
            )
        }
    }

    fun deleteCurrentTrackFromPlaylist(userId: String, logger: LoggerInterface) {
        // userId
        // dynamo から取得する
        // [x] access トークンを取得する 実装済み
        val token: String = readAccessTokenOrUpdated(userId, logger)
        val playlistInfo = readPlaylistId(userId, logger)
        val playlistId = playlistInfo.playlistId
            ?: run {
                lineBotService.sendMessage(userId, "プレイリストIDが登録されていません。")
                logger.log("[deleteCurrentTrackFromPlaylist] playlist Id が null です")
                throw SystemException("プレイリストIDが設定されていません。")
            }
        val playlistName = playlistInfo.playlistName
            ?: updatePlaylistName(userId, playlistId = playlistId, logger)

        // spotify API で trackId を取得
        val body = currentTrackInfo(token)
        val trackId = body.item.uri

        // お気に入りから削除
        deleteFromFavorite(userId, trackId, logger)

        // dynamo にすでに追加してないか検証
        if (isAlreadyAdded(playlistId, trackId)) {
            val addedAt = getWhenAdded(playlistId, trackId)
            // LINE でメッセージ返したい
            if (addedAt != null) {
                val dateTimeformatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH時mm分")
                val addedAtText = addedAt.format(dateTimeformatter)
                lineBotService.sendMessage(userId, text = "この曲は、$addedAtText にすでに追加されています。削除します")
            } else {
                lineBotService.sendMessage(userId, text = "この曲はすでに追加されています。削除します")
            }

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
                userId, text = "削除が完了しました。 ( from $playlistName )\n" +
                "タイトル: ${body.item.name}\n" +
                "by     : ${body.item.artists[0].name}"
            )
        } else {
            lineBotService.sendMessage(
                userId, text = "この曲は登録されていません。 ( in $playlistName ) \n" +
                "タイトル: ${body.item.name}\n" +
                "by     : ${body.item.artists[0].name}"
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
        } else throw SystemException(
            "[spotifyService] currentTrack failed: code = ${response.code()} error = ${
                response.errorBody()?.string()
            }"
        )
    }

    fun getPlaylists(userId: String, logger: LoggerInterface): PlaylistResponse {
        val token: String = readAccessTokenOrUpdated(userId, logger)
        val response = spotifyApiClient.getPlaylists(
            authorizationString = "Bearer $token",
            limit = 7
        ).execute()
        val body = response.body()

        if (body != null) {
            return body
        } else throw SystemException(
            "[spotifyService] playlists failed: code = ${response.code()} error = ${
                response.errorBody()?.string()
            }"
        )
    }

    private fun isAlreadyAdded(playlistId: String, trackId: String): Boolean {
        val result = spotifyTrackDynamoDbMapper.readRow(playlistId, trackId)
        return result != null
    }

    private fun getWhenAdded(playlistId: String, trackId: String): LocalDateTime? {
        val result = spotifyTrackDynamoDbMapper.readRow(playlistId, trackId)
        return result?.addedAt
    }

    private fun readPlaylistId(userId: String, logger: LoggerInterface): UserToken {

        // もし playlist が設定されてなかったら、
        // ここで SystemException を出す。
        // このハンドラ欲しいけど?
        val token = userTokenDynamoDbMapper.readTokenRowOrNull(userId, logger)
            ?: throw SystemException("No entry for UserId=$userId")
        return token
    }

    private fun readAccessTokenOrUpdated(userId: String, logger: LoggerInterface): String {
        val token = userTokenDynamoDbMapper.readTokenRowOrNull(userId, logger)
            ?: run {
                lineBotService.sendMessage(userId, "先に「Spotify連携」してね！")
                logger.log("[readAccessTokenOrUpdated] userToken is null: userId: $userId")
                throw SystemException("[readAccessTokenOrUpdated] userToken is null: userId: $userId No entry for UserId=$userId")
            }

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

    fun acquireRefreshToken(code: String): AcquireRefreshTokenResponse {
        val bear = "${variables.spotifyClientId}:${variables.spotifyClientSecret}"
        val base64ed = Base64.getEncoder().encodeToString(bear.toByteArray())
        val response = spotifyApiAuthClient.acquireRefreshToken(
            authorizationString = "Basic $base64ed",
            redirectUri = objectConstructor.redirectUrl,
            code = code
        ).execute()
        val body = response.body()
        if (response.isSuccessful && body != null) {
            return body
        } else throw SystemException(
            "[spotify-service] acquireRefreshToken failed code: ${response.code()} body: ${
                response.errorBody()?.string()
            }"
        )
    }

    fun refreshToken(refreshToken: String, logger: LoggerInterface): Token {
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
