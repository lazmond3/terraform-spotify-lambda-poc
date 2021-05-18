package terraform.spotify.lambda.poc.handler

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
}