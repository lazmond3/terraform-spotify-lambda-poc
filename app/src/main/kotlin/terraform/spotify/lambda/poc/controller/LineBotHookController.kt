package terraform.spotify.lambda.poc.controller

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import terraform.spotify.lambda.poc.`interface`.LoggerInterface
import terraform.spotify.lambda.poc.entity.AwsInputEvent
import terraform.spotify.lambda.poc.exception.SystemException
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
        // TODO: これ以降の部分を 別の関数に切り出したい
        val userId = inputEvent.events[0].source.userId

        return handleMessage(isForSpring = false,
                text = text,
                userId = userId,
                logger = context.logger.let {
                    object : LoggerInterface {
                        override fun log(message: String) {
                            it.log(message)
                        }
                    }
                }
        ) ?: throw SystemException("[handle] should return APIGatewayProxyResponseEvent")
    }

    fun handleMessage(isForSpring: Boolean, text: String, userId: String, logger: LoggerInterface): APIGatewayProxyResponseEvent? {
        val head = text.split(" ")[0]
        val bodyValue = if (text.split(" ").size >= 2) {
            text.split(" ")[1]
        } else ""
        when (head) {
            "register-refresh" -> {
                userTokenDynamoDBMapper.registerRefreshToken(
                        userId = userId,
                        refreshToken = bodyValue,
                        logger = logger
                )
            }
            "add-current" -> {
                lineBotService.sendMessage(userId, text = "add-current の send message test")
                spotifyService.addCurrentTrackToPlaylist(userId, logger)
            }
            "delete-current" -> {
                lineBotService.sendMessage(userId, text = "delete-current の send message test")
                spotifyService.deleteCurrentTrackFromPlaylist(userId, logger)
            }
            "register-playlist" -> {
                // プレイリストの取得
                if (bodyValue != "") {
                    spotifyService.registerNewPlaylistId(userId, bodyValue, logger)
                } else {
                    val response = spotifyService.playlsits(userId, logger)
                    logger.log("[controller : response register playlist] ${response}")
                    lineBotService.sendQuickReplyMessage(
                            mid = userId,
                            text = "プレイリストを選んでください",
                            quickReplyData =
                            response.items.map {
                                val uri = it.uri.split(":")[2]

                                val imageUrl = if (it.images.isNotEmpty()) {
                                    it.images.sortedByDescending { it.height }[0].url
                                } else {
                                    "https://img.icons8.com/material-outlined/24/000000/menu--v3.png"
                                }
                                QuickReplyData(
                                        imageUrl = imageUrl,
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
                registerTrackIdToPlaylist(userId, playlistId, trackId, logger)
            }
            "add-to-playlist-debug" -> {
                val playlistId = text.split(" ")[1]
                val trackId = text.split(" ")[2]
                registerTrackIdToPlaylist(userId, playlistId, trackId, logger)
            }
            "add-to-playlist" -> {
                // userId から playlistId を取得する
                val token = userTokenDynamoDBMapper.readRowOrNull(
                        userId = userId,
                        logger
                )
                if (token != null) {
                    val playlistId = token.playlistId
                    if (playlistId == null) {
                        lineBotService.sendMessage(userId,
                                "token is null for userId: $userId"
                        )
                    } else {
                        val trackId = text.split(" ")[1]
                        registerTrackIdToPlaylist(userId, playlistId, trackId, logger)
                    }
                } else {
                    lineBotService.sendMessage(
                            userId,
                            "token is null for userId: $userId"
                    )
                }
            }
            "delete-from-playlist-fulldebug" -> {
                val userId = text.split(" ")[1]
                val playlistId = text.split(" ")[2]
                val trackId = text.split(" ")[3]

                val result = spotifyDbMapper.readRowOrNull(playlistId, trackId, logger)
                if (result != null) {
                    logger.log("[delete debug log] the track $trackId is already registered: ")
                    spotifyDbMapper.delete(playlistId, trackId, logger)
                } else {
                    logger.log("[delete debug log] the track $trackId is not registered: ")
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
            logger: LoggerInterface
    ) {
        val result = spotifyDbMapper.readRowOrNull(playlistId, trackId, logger)
        if (result != null) {
            lineBotService.sendMessage(
                    userId,
                    "the track is already registered. \n${trackId}"
            )
        }
        spotifyDbMapper.create(userId, playlistId, trackId, logger)
        if (result != null) {
            lineBotService.sendMessage(
                    userId,
                    "Add success! $userId, $playlistId, $trackId"
            )
        }
    }
}
