package terraform.spotify.lambda.poc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import terraform.spotify.lambda.poc.annotation.NoArgsConstructor
import terraform.spotify.lambda.poc.construction.ObjectConstructor
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class Data(
    val localDate: LocalDate
)

fun main() {
    val objectMapper = ObjectConstructor().objectMapper
//    val objectMapper = ObjectMapper().apply {
////        registerModule(JavaTimeModule())
//        registerModule(KotlinModule())
//    }
    val st = "{\"local_date\": \"2005-01-01\"}"
    val result = objectMapper.readValue<Data>(st, Data::class.java)
    println(result)

    val dateTimeformatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")
    val localDate = LocalDateTime.of(2020,1,1,1,1)
    println(localDate.format(dateTimeformatter))
}