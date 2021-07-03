package terraform.spotify.lambda.poc

import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import java.io.File
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import terraform.spotify.lambda.poc.`interface`.LoggerInterface
import terraform.spotify.lambda.poc.construction.ObjectConstructor
import terraform.spotify.lambda.poc.exception.SystemException


@Configuration
class Config {
    @Bean
    fun isForReal() = true
}

@SpringBootApplication
@LineMessageHandler
class Application(
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

class Fun {
    companion object {
        fun readFile(resourcePath: String): String {
            val fullPath = javaClass.classLoader.getResource(resourcePath)?.path
                    ?: throw SystemException("Invalid resource path: $resourcePath")

            val reader = File(fullPath).bufferedReader()
            val text: String = reader.use { it.readText() }
            return text
        }
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

