package terraform.spotify.lambda.poc.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.model.PushMessage
import com.linecorp.bot.model.message.TextMessage
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import terraform.spotify.lambda.poc.construction.ObjectConstructor
import terraform.spotify.lambda.poc.exception.SystemException
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

internal class LambdaHandlerTest {
    private lateinit var objectMapper: ObjectMapper
    val objectConstructor = ObjectConstructor()
    val lambdaHandler = LambdaHandler()

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerModule(KotlinModule())
        }
    }

    @Test
    fun time() {
        val jpCal: Calendar = Calendar.getInstance()
        val now = LocalDateTime.ofInstant(
            jpCal.toInstant(), ZoneId.of("Asia/Tokyo")
        )
        println(now.format(DateTimeFormatter.ISO_DATE_TIME))
    }

    @Test
    @Disabled
    fun linebotTest() {
        val channelToken =
            System.getenv("LINE_BOT_CHANNEL_ACCESS_TOKEN")
        val client = LineMessagingClient.builder(channelToken).build();
        val message = TextMessage("hello world!")
        val sentResult = client.pushMessage(
            PushMessage(
                "",
                message
            )
        ).get()

        println("hell world, result: $sentResult")
    }

    @Test
    fun postPreflightTest() {
        val inputAsText = readFile("input/post_preflight.json")
        val inputAsObj = objectMapper.readValue(inputAsText, APIGatewayProxyRequestEvent::class.java)
        val context = mockk<Context>(relaxed = true)

    }

    @Test
    fun postAccessTest() {

    }

    private fun readFile(resourcePath: String): String {
        val fullPath = javaClass.classLoader.getResource(resourcePath)?.path
            ?: throw SystemException("Invalid resource path: $resourcePath")

        val reader = File(fullPath).bufferedReader()
        val text: String = reader.use { it.readText() }
        return text
    }
}