package terraform.spotify.lambda.poc.handler

import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.client.LineMessagingClientBuilder
import com.linecorp.bot.model.PushMessage
import com.linecorp.bot.model.message.Message
import com.linecorp.bot.model.message.TemplateMessage
import com.linecorp.bot.model.message.TextMessage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

internal class LambdaHandlerTest {

    @Test
    fun time() {
        val jpCal: Calendar = Calendar.getInstance()
        val now = LocalDateTime.ofInstant(
            jpCal.toInstant(), ZoneId.of("Asia/Tokyo")
        )
        println(now.format(DateTimeFormatter.ISO_DATE_TIME))
    }

    val channelToken =
        System.getenv("LINE_BOT_CHANNEL_ACCESS_TOKEN")

    @Test
    fun linebotTest() {
        // secret
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
}