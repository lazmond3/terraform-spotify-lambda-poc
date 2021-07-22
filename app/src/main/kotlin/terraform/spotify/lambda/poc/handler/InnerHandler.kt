package terraform.spotify.lambda.poc.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import java.io.File
import terraform.spotify.lambda.poc.`interface`.LoggerInterface
import terraform.spotify.lambda.poc.construction.ObjectConstructor
import terraform.spotify.lambda.poc.entity.AwsInputEvent
import terraform.spotify.lambda.poc.exception.SystemException
import terraform.spotify.lambda.poc.request.PostLineUserDataWithCodeRequest

// Lambda のみから利用される
class InnerHandler(
        val objectConstructor: ObjectConstructor
) : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        val logger = context.logger.let {
            object : LoggerInterface {
                override fun log(message: String) {
                    it.log(message)
                }
            }
        }
        val objectMapper = objectConstructor.objectMapper

        logger.log("------------------------------------------------------------------")
        logger.log("[debug 1st] input: $input")
        logger.log("[debug 2nd] objectMapper: ")
        logger.log(objectMapper.writeValueAsString(input))
        logger.log("------------------------------------------------------------------")
//        logger.log("[debug 3rd] context: [[")
//        logger.log("[debug 3rd] awsRequestId: ${context.awsRequestId}")
//        logger.log("[debug 3rd] logGroupName: ${context.logGroupName}")
//        logger.log("[debug 3rd] logStreamName: ${context.logStreamName}")
//        logger.log("[debug 3rd] functionName: ${context.functionName}")
//        logger.log("[debug 3rd] functionVersion: ${context.functionVersion}")
//        logger.log("[debug 3rd] invokedFunctionArn: ${context.invokedFunctionArn}")
//        logger.log("[debug 3rd] invokedFunctionArn: ${context.identity}")
//        logger.log("[debug 3rd] remainingTimeInMillis: ${context.remainingTimeInMillis}")
//        logger.log("[debug 3rd] memoryLimitInMB: ${context.memoryLimitInMB}")

        // ここの部分は null になる。
//        logger.log("[debug 3rd] identity.identityId: ${context.identity.identityId}")
//        logger.log("[debug 3rd] identity.identityPoolId: ${context.identity.identityPoolId}")
//        logger.log("[debug 3rd] clientContext.client: ${context.clientContext.client}")
//        logger.log("[debug 3rd] clientContext.custom: ${context.clientContext.custom}")
//        logger.log("[debug 3rd] clientContext.environment: ${context.clientContext.environment}")
//        logger.log("[debug 3rd] ]]")
        logger.log("------------------------------------------------------------------")


        return if (input.path == "/callback") {
            // web アクセス (GET) の場合、この部分はない。
            val inputBody = input.body
            val inputObject = objectMapper.readValue(input.body, AwsInputEvent::class.java)
            logger.log("------")
            logger.log("[debug] body: $inputBody")
            logger.log("[debug] events: ")
            inputObject.events.forEach {
                logger.log("\t[debug] $it")
            }
            // LINE BOT のハンドラ
            objectConstructor.lineBotHookController.handle(inputObject, context)
        } else if (input.path == "/post") {
            when (input.httpMethod) {
                "OPTIONS" -> { // 今回追加した preflight
                    logger.log("[debug option] post に 届いた")
                    val headers = mapOf(
                            "Cache-Control" to "no-store, no-chache",
                            "Access-Control-Allow-Origin" to "*",
                            "Access-Control-Allow-Methods" to "POST, GET, OPTIONS, DELETE",
                            "Access-Control-Max-Age" to "86400"
                    )
                    logger.log("[debug option] return 直前")

                    APIGatewayProxyResponseEvent().apply {
                        isBase64Encoded = false
                        statusCode = 204
                        setHeaders(headers)
                    }
                }
                "POST" -> {
                    val headers = mapOf<String, String>(
                            "Cache-Control" to "no-store, no-chache"
                    )
                    val postRequest = objectMapper.readValue(input.body, PostLineUserDataWithCodeRequest::class.java)
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
                    } else {
                        objectConstructor.userTokenDynamoDBMapper.registerRefreshToken(
                                userId = userId,
                                refreshToken = refreshToken,
                                logger = logger
                        )
                    }

                    logger.log("[post の結果] index.html を返却する return 直前")
                    APIGatewayProxyResponseEvent().apply {
                        isBase64Encoded = false
                        statusCode = 200
                        setHeaders(headers)
                    }
                }
                else -> {
                    val headers = emptyMap<String, String>()
                    logger.log("[option ではない post の結果] index.html を返却する return 直前")
                    APIGatewayProxyResponseEvent().apply {
                        isBase64Encoded = false
                        statusCode = 200
                        setHeaders(headers)
                    }
                }
            }
        } else if (input.path == "/index.html") {
            val headers = mapOf(
                    "Cache-Control" to "no-store, no-chache",
                    "Content-Type" to "text/html"
            )
            APIGatewayProxyResponseEvent().apply {
                isBase64Encoded = false
                statusCode = 200
                setHeaders(headers)

                body = readFileAsString("index.html")
            }
        } else if (input.path == "/index.js") {
            val headers = mapOf(
                    "Cache-Control" to "no-store, no-chache",
                    "Content-Type" to "text/javascript"
            )
            APIGatewayProxyResponseEvent().apply {
                isBase64Encoded = false
                statusCode = 200
                setHeaders(headers)

                body = readFileAsString("index.js")
            }
        } else if (input.path == "/") {
            val headers = mapOf(
                    "Cache-Control" to "no-store, no-chache",
                    "Content-Type" to "text/html"
            )
            APIGatewayProxyResponseEvent().apply {
                isBase64Encoded = false
                statusCode = 200
                setHeaders(headers)
                body = readFileAsString("index.html")
            }
        } else if (input.path == "/test") {
            val headers = mapOf(
                    "Content-Type" to "text/html"
            )
            APIGatewayProxyResponseEvent().apply {
                isBase64Encoded = false
                statusCode = 200
                setHeaders(headers)
                body = "<!DOCTYPE html><html><head><title>AWS Lambda sample</title></head><body>" +
                        "<h1>Welcome</h1><p>Page generated by a Lambda function.</p>" +
                        "</body></html>"
            }
        } else {
            val headers = mapOf(
                    "Content-Type" to "text/html"
            )
            APIGatewayProxyResponseEvent().apply {
                isBase64Encoded = false
                statusCode = 404
                setHeaders(headers)
                body = "<!DOCTYPE html><html><head><title>Error</title></head><body>" +
                        "<h1>No such endpoint.</h1>" +
                        "</body></html>"
            }
        }
    }

    private fun readFileAsString(resourcePath: String): String {
        val fullPath = javaClass.classLoader.getResource(resourcePath)?.path
                ?: throw SystemException("Invalid resource path: $resourcePath")

        val reader = File(fullPath).bufferedReader()
        val text: String = reader.use { it.readText() }
        return text
    }
}
