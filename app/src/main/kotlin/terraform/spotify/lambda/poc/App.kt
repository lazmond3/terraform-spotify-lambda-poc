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
        "AQBGZbRdWUA-Y4t2zlCjkm9xsMvWVYjeAQpnuQAaqXSGh8ueO8quPAkWZs1n5PGpReQkIx3tGhinNBaCeSgZpAKXXgTnllt3NqCmUrWTfs2v7pamrAhxf_JxjvdsPO846bvk-4DIOR3npcKjUVfhVpMp4tp2xSBXfw_kKLMBH50JLMrxfQ7iOiaMJ9HU2r6ZNHGmebGdzFjv1qwV-keNi7hX5jZEHIFRn86_YpQINlAG5wvYEW8NeNhVuAYNcG8EZUV3tnEjMCNmc9qiEaeU4p7dbmXuFOqT_qevbPBsWb2kFd3oJz_kTfsPMbzh7gCrDAY-Fe0wPXbX1wNYRHweqWSUzWre1cckIBF42WijSBLbR6yhv9CaEp4n6HoPdscE5gB_OM-xImWiReS9unaAux4uo_7Yl15LrPo0c52Z6usjIfdLj7CyQbLl7pG7SFemkwygiXZ497cF_qyxUESiN5WLbjCfFNex8J524wciTbfNPILCxGvsQ5zC14ut73qg6obm2_qj516Sq_7UFxw5sTxIdaohL9mo90zZwg"
//    val response = objectConstructor.spotifyService.acquireRefreshToken(code)
    val response = objectConstructor.spotifyApiAuthClient.acquireRefreshToken(
        authorizationString = "Basic $base64ed",
        redirectUri = objectConstructor.redirectUrl,
        code = code
    ).execute()
    println("errorbody: ${response.errorBody()?.string()}")
    println("response: $response")
}

