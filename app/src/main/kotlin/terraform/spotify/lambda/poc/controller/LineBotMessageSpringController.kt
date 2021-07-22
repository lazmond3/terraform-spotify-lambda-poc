package terraform.spotify.lambda.poc.controller

import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.PostbackEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import mu.KotlinLogging
import terraform.spotify.lambda.poc.`interface`.LoggerInterface
import terraform.spotify.lambda.poc.construction.ObjectConstructor

private val logger = KotlinLogging.logger {}

// Spring Boot で手元で実験するときのみ利用する。
// これを利用する場合は、 ngrok を利用して line bot callback URL を変更する。
@LineMessageHandler
class LineBotMessageSpringController(
    val objectConstructor: ObjectConstructor
) {
    @EventMapping
    fun handleTextMessageEvent(event: MessageEvent<TextMessageContent>): TextMessage {
        val message = event.message.text
        val userId = event.source.userId
        objectConstructor.lineBotHookController.handleMessage(
            isForSpring = true,
            text = message,
            userId = userId,
            logger =
            object : LoggerInterface {
                override fun log(message: String) {
                    logger.info { message }
                }
            }

        )
        logger.info { "event: $event" }
        return TextMessage(event.message.text)
    }

    @EventMapping
    fun handlePostbackEvent(event: PostbackEvent): TextMessage {
        objectConstructor.lineBotHookController.handlePostBackFromEventForSpringBoot(event)
        return TextMessage("ポストバックイベントを受け取りました")
    }
}
