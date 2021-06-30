package terraform.spotify.lambda.poc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import terraform.spotify.lambda.poc.annotation.NoArgsConstructor
import terraform.spotify.lambda.poc.construction.ObjectConstructor
import terraform.spotify.lambda.poc.exception.SystemException
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class Data(
    val localDate: LocalDate
)

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

fun main() {
    val objectMapper = ObjectConstructor().objectMapper
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

    println("index html: ${Fun.readFile("index.html")}")
    println("index js: ${Fun.readFile("index.js")}")
}

