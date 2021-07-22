package terraform.spotify.lambda.poc.controller

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.linecorp.bot.model.event.PostbackEvent
import mu.KotlinLogging
import terraform.spotify.lambda.poc.`interface`.LoggerInterface
import terraform.spotify.lambda.poc.entity.AwsInputEvent
import terraform.spotify.lambda.poc.exception.SystemException
import terraform.spotify.lambda.poc.mapper.dynamo.SpotifyTrackDynamoDbMapper
import terraform.spotify.lambda.poc.mapper.dynamo.UserTokenDynamoDbMapper
import terraform.spotify.lambda.poc.model.PostbackEventData
import terraform.spotify.lambda.poc.model.QuickPostbackData
import terraform.spotify.lambda.poc.service.LineBotService
import terraform.spotify.lambda.poc.service.SpotifyService

private val logger = KotlinLogging.logger {}

class LineBotHookController(
    val lineBotService: LineBotService,
    val spotifyDbMapper: SpotifyTrackDynamoDbMapper,
    val userTokenDynamoDBMapper: UserTokenDynamoDbMapper,
    val spotifyService: SpotifyService,
    val objectMapper: ObjectMapper
) {
    // これは Lambda のみから呼ばれる
    fun handle(inputEvent: AwsInputEvent, context: Context): APIGatewayProxyResponseEvent {
        inputEvent.events.forEach {
            val message = "[received] ${it.message.text}"
            lineBotService.replyToMessage(it.replyToken, message)
        }
        val text = inputEvent.events[0].message.text
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

    fun handlePostBack(event: PostbackEvent) {
        val data = objectMapper.readValue(event.postbackContent.data, PostbackEventData::class.java)
        when (data.cmd) {
            "プレイリスト追加" -> {
                val playlistId = data.playlistId
                    ?: throw SystemException("playlist Id is null: $data")
                val loggerAsInterface = object : LoggerInterface {
                    override fun log(message: String) {
                        logger.info { message }
                    }
                }
                    spotifyService.registerNewPlaylistId(data.userId, playlistId, loggerAsInterface)
            }
        }
    }

    // Lambda と Spring どちらからも呼ばれる
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
            "a3", "a",
            "現在の曲を追加",
            "add-current" -> {
                spotifyService.addCurrentTrackToPlaylist(userId, logger)
            }
            "d3", "d",
            "現在の曲を削除",
            "delete-current" -> {
                spotifyService.deleteCurrentTrackFromPlaylist(userId, logger)
            }
            "プレイリスト登録",
            "プレイリスト一覧",
            "register-playlist" -> {
                // プレイリストの取得
                // TODO: ここをpostback イベントで行う。
                logger.log("プレイリスト一覧に到達！")
                if (bodyValue != "") {
                    spotifyService.registerNewPlaylistId(userId, bodyValue, logger)
                } else {
                    val response = spotifyService.getPlaylists(userId, logger)
                    logger.log("[controller : response register playlist] ${response}")
                    lineBotService.sendQuickReplyPostbackAction(
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
                            QuickPostbackData(
                                imageUrl = imageUrl,
                                label = it.name,
                                data = objectMapper.writeValueAsString(
                                    PostbackEventData(
                                        userId = userId,
                                        cmd = "プレイリスト追加",
                                        playlistId = uri,
                                        playlistName = ""
                                    )
                                ),
//                                data = "プレイリスト追加 $uri",
                                displayMessage = it.name
                            )
                        }
                    )
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
