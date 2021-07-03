package terraform.spotify.lambda.poc.controller

import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import terraform.spotify.lambda.poc.`interface`.LoggerInterface
import terraform.spotify.lambda.poc.construction.ObjectConstructor


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
                        println("[pseudo-logback] $message")
                    }
                }

        )
        println("event: $event")
        return TextMessage(event.message.text)
    }
}
