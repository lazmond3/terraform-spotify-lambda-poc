package terraform.spotify.lambda.poc.controller

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import terraform.spotify.lambda.poc.entity.AwsInputEvent
import terraform.spotify.lambda.poc.mapper.dynamo.SpotifyTrackDynamoDbMapper
import terraform.spotify.lambda.poc.mapper.dynamo.UserTokenDynamoDbMapper
import terraform.spotify.lambda.poc.model.QuickReplyData
import terraform.spotify.lambda.poc.service.LineBotService
import terraform.spotify.lambda.poc.service.SpotifyService

class LineBotHookController(
    val lineBotService: LineBotService,
    val spotifyDbMapper: SpotifyTrackDynamoDbMapper,
    val userTokenDynamoDBMapper: UserTokenDynamoDbMapper,
    val spotifyService: SpotifyService
) {


    fun handle(inputEvent: AwsInputEvent, context: Context): APIGatewayProxyResponseEvent {
        val headers = mapOf(
            "Content-Type" to "text/html"
        )

        inputEvent.events.forEach {
            val message = "[received] ${it.message.text}"
            lineBotService.replyToMessage(it.replyToken, message)
        }
        val replyToken = inputEvent.events[0].replyToken
        // assert 入れたい
        val text = inputEvent.events[0].message.text

        val head = text.split(" ")[0]
        val bodyValue = if (text.split(" ").size >= 2) {
            text.split(" ")[1]
        } else ""
        val userId = inputEvent.events[0].source.userId
        when (head) {
            "register-refresh" -> {
                userTokenDynamoDBMapper.registerRefreshToken(
                    userId = userId,
                    refreshToken = bodyValue,
                    logger = context.logger
                )
                lineBotService.replyToMessage(inputEvent.events[0].replyToken, "registered refresh token")
            }
            "add-current" -> {
                lineBotService.sendMessage(userId, text = "add-current の send message test")
                spotifyService.addCurrentTrackToPlaylist(userId, context.logger)
            }
            "delete-current" -> {
                lineBotService.sendMessage(userId, text = "delete-current の send message test")
                spotifyService.deleteCurrentTrackFromPlaylist(userId, context.logger)
            }
            "register-playlist" -> {
                // プレイリストの取得
                if (bodyValue != "") {
                    spotifyService.registerNewPlaylistId(userId, bodyValue, context.logger)
                } else {
                    val response = spotifyService.playlsits(userId, context.logger)
                    context.logger.log("[controller : response register playlist] ${response}")
                    lineBotService.sendQuickReplyMessage(
                        mid = userId,
                        text = "プレイリストを選んでください",
                        quickReplyData =
                        response.items.map {
                            val uri = it.uri.split(":")[2]
                            QuickReplyData(
                                imageUrl = it.images.sortedByDescending { it.height }[0].url,
                                label = it.name,
                                messageText = "register-playlist $uri"
                            )
                        }
                    )
                }
            }
            "add-to-playlist-fulldebug" -> {
                val userId = text.split(" ")[1]
                val playlistId = text.split(" ")[2]
                val trackId = text.split(" ")[3]
                registerTrackIdToPlaylist(userId, playlistId, trackId, replyToken, context.logger)
            }
            "add-to-playlist-debug" -> {
                val playlistId = text.split(" ")[1]
                val trackId = text.split(" ")[2]
                registerTrackIdToPlaylist(userId, playlistId, trackId, replyToken, context.logger)
            }
            "add-to-playlist" -> {
                // userId から playlistId を取得する
                val token = userTokenDynamoDBMapper.readRowOrNull(
                    userId = userId,
                    context.logger
                )
                if (token != null) {
                    val playlistId = token.playlistId
                    if (playlistId == null) {
                        lineBotService.replyToMessage(
                            replyToken,
                            "token is null for userId: $userId"
                        )
                    } else {
                        val trackId = text.split(" ")[1]
                        registerTrackIdToPlaylist(userId, playlistId, trackId, replyToken, context.logger)
                    }
                } else {
                    lineBotService.replyToMessage(
                        replyToken,
                        "token is null for userId: $userId"
                    )
                }
            }
            "delete-from-playlist-fulldebug" -> {
                val userId = text.split(" ")[1]
                val playlistId = text.split(" ")[2]
                val trackId = text.split(" ")[3]

                val result = spotifyDbMapper.readRowOrNull(playlistId, trackId, context.logger)
                if (result != null) {
                    context.logger.log("[delete debug log] the track $trackId is already registered: ")
//                    replyToken.let {
//                        lineBotService.replyToMessage(
//                            it,
//                            "[delete] the track is already registered. \n${trackId}"
//                        )
//                    }
                    spotifyDbMapper.delete(playlistId, trackId, context.logger)
                } else {
                    context.logger.log("[delete debug log] the track $trackId is not registered: ")
                }
            }
        }
        return APIGatewayProxyResponseEvent().apply {
            isBase64Encoded = false
            statusCode = 200
            setHeaders(headers)
            body = "<!DOCTYPE html><html><head><title>AWS Lambda sample</title></head><body>" +
                    "<h1>Welcome</h1><p>Page generated by a Lambda function.[LineBotHookController]</p>" +
                    "</body></html>"
        }
    }

    private fun registerTrackIdToPlaylist(
        userId: String,
        playlistId: String,
        trackId: String,
        replyToken: String?,
        logger: LambdaLogger
    ) {
        val result = spotifyDbMapper.readRowOrNull(playlistId, trackId, logger)
        if (result != null) {
            replyToken?.let {
                lineBotService.replyToMessage(
                    it,
                    "the track is already registered. \n${trackId}"
                )
            }
        }
        spotifyDbMapper.create(userId, playlistId, trackId, logger)
        if (result != null) {
            replyToken?.let {
                lineBotService.replyToMessage(
                    it,
                    "Add success! $userId, $playlistId, $trackId"
                )
            }
        }
    }
}