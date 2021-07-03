package terraform.spotify.lambda.poc

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.linecorp.bot.model.event.Event
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import java.io.File
import java.time.LocalDate
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import terraform.spotify.lambda.poc.`interface`.LoggerInterface
import terraform.spotify.lambda.poc.annotation.NoArgsConstructor
import terraform.spotify.lambda.poc.construction.ObjectConstructor
import terraform.spotify.lambda.poc.exception.SystemException

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class Data(
        val localDate: LocalDate
)

@Configuration
class Config {
    @Bean
    fun isForReal() = true
}

//private val logger = KotlinLogging.logger {}

@SpringBootApplication
@LineMessageHandler
class Application(
        val objectConstructor: ObjectConstructor
) {
    @EventMapping
    fun handleTextMessageEvent(event: MessageEvent<TextMessageContent>): TextMessage {
        val message = event.message.text
        val userId = event.source.userId
//        println("source: ${}")
        objectConstructor.lineBotHookController.handleMessage(
                isForSpring = true,
                text = message,
                userId = userId,
                logger =
                object : LoggerInterface {
                    override fun log(message: String) {
                        println("[pseudo-logback] $message")
//                        it.info { message }
                    }
                }

        )
        println("event: $event")
        return TextMessage(event.message.text)
    }

    @EventMapping
    fun handleDefaultMessageEvent(event: Event) {
        println("event: $event")
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

    val objectConstructor = ObjectConstructor(isForReal = true)
    val objectMapper = objectConstructor.objectMapper

//    val mid = "U6339db851f0dd06878589cb0e7008294"
//    val mid = "U6acca7c692051138e7a5e4d8e769582c"
//    val response = objectConstructor.lineBotService.client.pushMessage(
//            PushMessage(
//                    mid,
//                    TextMessage(
//                            "hello world", QuickReply.items(
//                            listOf(
//                                    QuickReplyItem.builder()
//                                            .imageUrl(URI("https://img.icons8.com/material-outlined/24/000000/edit--v3.png"))
//                                            .action(MessageAction("プレイリスト1", "テキスト"))
//                                            .build(),
//                                    QuickReplyItem.builder()
//                                            .imageUrl(URI("https://img.icons8.com/material-rounded/24/000000/puzzle.png"))
//                                            .action(MessageAction("プレイリスト2", "テキスト"))
//                                            .build(),
//                                    QuickReplyItem.builder()
////                            .imageUrl(URI("https://img.icons8.com/material-two-tone/24/000000/link--v3.png"))
//                                            .imageUrl(URI("https://mosaic.scdn.co/640/ab67616d0000b27338aae75dc37fb42457866ffdab67616d0000b2733a0bdcd36420a021e2b5dcf8ab67616d0000b273af185c81ec1ba8608c780ca1ab67616d0000b273f6075bd5a7d1ae7d28ad8ab3"))
//                                            .action(MessageAction("プレイリスト3", "テキスト"))
//                                            .build(),
//                                    QuickReplyItem.builder()
//                                            .imageUrl(URI("https://img.icons8.com/material-sharp/24/000000/menu--v3.png"))
//                                            .action(MessageAction("新規作成", "テキスト"))
//                                            .build(),
//                            )
//                    )
//                    )
//            )
//    ).get()

//    println("response: ${objectMapper.writeValueAsString(response)}")

//    val objectMapper = ObjectMapper().apply {
////        registerModule(JavaTimeModule())
//        registerModule(KotlinModule())
//    }
//    val st = "{\"local_date\": \"2005-01-01\"}"
//    val result = objectMapper.readValue<Data>(st, Data::class.java)
//    println(result)
//
//    val dateTimeformatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")
//    val localDate = LocalDateTime.of(2020,1,1,1,1)
//    println(localDate.format(dateTimeformatter))

//    println("index html: ${Fun.readFile("index.html")}")
//    println("index js: ${Fun.readFile("index.js")}")
//    val objectConstructor = ObjectConstructor(isForReal = true)
//    val bear = "${objectConstructor.variables.spotifyClientId}:${objectConstructor.variables.spotifyClientSecret}"
//    val base64ed = Base64.getEncoder().encodeToString(bear.toByteArray())
//    val code =
//        "AQCQmwbF6yNrY92150OrO5yrbKO-lO5vJ6RXZH5j0prjp2S6Ou2UqmJwjMU6L2Ht_ZJPduS-IKuvgNIVJae8D5UcJQo24P3lFEdNNk0h-Jwj4rQmEqklfeOGQMqy4H0kbjwWPybacQk8OfeQXdV_ohuLyaU-NOfwNWGzyDbadCFG9eXWpgdpZxBYP2a8glYGNm9gvXCKejhlO1SOk0cBe_hodif0YLj-ZjQjTxhKHIPuA2Qa1FsctB79gA4fKZE5nW3YLs04Sd-7oPxNaVMWzzMJsh-nDc2s8sbB0QeJyIxLA0Dcyu0MPLaXPCxi-4OZrU4DsCPRXiLxmvbe6Tui9pnduLfn0XrtNmhGDPe1rQ9M9evwV3DtqZ49Z06qSKzsTNjtoUi5N4H6JuRnpxibfQ2uXHoDYqkdjjUoG7VnNUQ1RgqTOricylytfIAHn1pRhhn_bezwO5qHqhYv2q-BY_PTrXUmJOIBqiJKj70MlIV7x-lJlpmkFoL-PsxatkNbEMfSdR5niHFlA2MQGXtaChFJM9mV4C1oZ6_0fA"
////    val response = objectConstructor.spotifyService.acquireRefreshToken(code)
//    val response = objectConstructor.spotifyApiAuthClient.acquireRefreshToken(
//        authorizationString = "Basic $base64ed",
//        redirectUri = objectConstructor.redirectUrl,
//        code = code
//    ).execute()
//    println("errorbody: ${response.errorBody()?.string()}")
//    println("response: $response")
}

