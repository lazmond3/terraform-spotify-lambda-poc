package terraform.spotify.lambda.poc.controller

import java.io.File
import mu.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import terraform.spotify.lambda.poc.`interface`.LoggerInterface
import terraform.spotify.lambda.poc.construction.ObjectConstructor
import terraform.spotify.lambda.poc.exception.SystemException
import terraform.spotify.lambda.poc.request.PostLineUserDataWithCodeRequest

private val ktlogger = KotlinLogging.logger {}

@RestController
class SpringBootController(
    val objectConstructor: ObjectConstructor
) {
    val objectMapper = objectConstructor.objectMapper

    @GetMapping(value = ["/", "/index.html"])
    fun index(): String {
        return readFile("index.html")
    }

    @GetMapping(value = [ "/index.js"])
    fun indexJs(): String {
        return readFile("index.js")
    }

    @PostMapping("/post")
    fun postDataCode(
        @RequestBody
        postRequest: PostLineUserDataWithCodeRequest
    ) {
        val logger = object : LoggerInterface {
            override fun log(message: String) {
                ktlogger.info(message)
            }
        }
        logger.log("codeがPOSTされた！")
        val headers = mapOf(
            "Cache-Control" to "no-store, no-chache"
        )
        val userId = postRequest.sub
        val code = postRequest.code

        // 1. code から refresh token を取得する
        val response = objectConstructor.spotifyService.acquireRefreshToken(code = code)
        val refreshToken = response.refreshToken

        // 2. refresh token を userId に対して、更新する。
        val userToken = objectConstructor.userTokenDynamoDBMapper.readRowOrNull(userId, logger)
        if (userToken != null) {
            objectConstructor.userTokenDynamoDBMapper.update(
                userToken.copy(
                    refreshToken = refreshToken
                )
            )
            objectConstructor.lineBotService.sendMessage(userId, "refreshToken を更新しました！")
            logger.log("refreshToken を更新しました")
        } else {
            objectConstructor.userTokenDynamoDBMapper.registerRefreshToken(
                userId = userId,
                refreshToken = refreshToken,
                logger = logger
            )
            objectConstructor.lineBotService.sendMessage(userId, "Spotifyと連携しました！")
            logger.log("Spotifyと連携しました")
        }


        logger.log("[post の結果] index.html を返却する return 直前")
    }

    private fun readFile(resourcePath: String): String {
        val fullPath = javaClass.classLoader.getResource(resourcePath)?.path
            ?: throw SystemException("Invalid resource path: $resourcePath")

        val reader = File(fullPath).bufferedReader()
        val text: String = reader.use { it.readText() }
        return text
    }
}
