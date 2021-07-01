package terraform.spotify.lambda.poc

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import terraform.spotify.lambda.poc.annotation.NoArgsConstructor
import terraform.spotify.lambda.poc.construction.ObjectConstructor
import terraform.spotify.lambda.poc.exception.SystemException
import java.io.File
import java.time.LocalDate
import java.util.*

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
    val objectMapper = ObjectConstructor(isForReal = false).objectMapper
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
    val objectConstructor = ObjectConstructor(isForReal = true)
    val bear = "${objectConstructor.variables.spotifyClientId}:${objectConstructor.variables.spotifyClientSecret}"
    val base64ed = Base64.getEncoder().encodeToString(bear.toByteArray())
    val code =
        "AQCQmwbF6yNrY92150OrO5yrbKO-lO5vJ6RXZH5j0prjp2S6Ou2UqmJwjMU6L2Ht_ZJPduS-IKuvgNIVJae8D5UcJQo24P3lFEdNNk0h-Jwj4rQmEqklfeOGQMqy4H0kbjwWPybacQk8OfeQXdV_ohuLyaU-NOfwNWGzyDbadCFG9eXWpgdpZxBYP2a8glYGNm9gvXCKejhlO1SOk0cBe_hodif0YLj-ZjQjTxhKHIPuA2Qa1FsctB79gA4fKZE5nW3YLs04Sd-7oPxNaVMWzzMJsh-nDc2s8sbB0QeJyIxLA0Dcyu0MPLaXPCxi-4OZrU4DsCPRXiLxmvbe6Tui9pnduLfn0XrtNmhGDPe1rQ9M9evwV3DtqZ49Z06qSKzsTNjtoUi5N4H6JuRnpxibfQ2uXHoDYqkdjjUoG7VnNUQ1RgqTOricylytfIAHn1pRhhn_bezwO5qHqhYv2q-BY_PTrXUmJOIBqiJKj70MlIV7x-lJlpmkFoL-PsxatkNbEMfSdR5niHFlA2MQGXtaChFJM9mV4C1oZ6_0fA"
//    val response = objectConstructor.spotifyService.acquireRefreshToken(code)
    val response = objectConstructor.spotifyApiAuthClient.acquireRefreshToken(
        authorizationString = "Basic $base64ed",
        redirectUri = objectConstructor.redirectUrl,
        code = code
    ).execute()
    println("errorbody: ${response.errorBody()?.string()}")
    println("response: $response")
}

