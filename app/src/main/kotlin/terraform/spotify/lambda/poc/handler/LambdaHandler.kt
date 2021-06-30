package terraform.spotify.lambda.poc.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import terraform.spotify.lambda.poc.construction.ObjectConstructor
import terraform.spotify.lambda.poc.entity.AwsInputEvent
import terraform.spotify.lambda.poc.exception.SystemException
import java.io.File


class LambdaHandler : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    val objectConstructor = ObjectConstructor()
    val lineBotHookController = objectConstructor.lineBotHookController

    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        val logger = context.logger
        val objectMapper = objectConstructor.objectMapper

        logger.log("------------------------------------------------------------------")
        logger.log("[debug 1st] input: $input")
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
            lineBotHookController.handle(inputObject, context)
        } else if (input.path == "/index.html") {
            val headers = mapOf(
                "Content-Type" to "text/html"
            )
            APIGatewayProxyResponseEvent().apply {
                isBase64Encoded = false
                statusCode = 200
                setHeaders(headers)

                body = readFile("index.html")
            }
        } else if (input.path == "/index.js") {
            val headers = mapOf(
                "Content-Type" to "text/javascript"
            )
            APIGatewayProxyResponseEvent().apply {
                isBase64Encoded = false
                statusCode = 200
                setHeaders(headers)

                body = readFile("index.js")
            }
        } else if (input.path == "/") {
            val headers = mapOf(
                "Content-Type" to "text/html"
            )
            APIGatewayProxyResponseEvent().apply {
                isBase64Encoded = false
                statusCode = 200
                setHeaders(headers)
                body = readFile("index.html")
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

    private fun readFile(resourcePath: String): String {
        val fullPath = javaClass.classLoader.getResource(resourcePath)?.path
            ?: throw SystemException("Invalid resource path: $resourcePath")

        val reader = File(fullPath).bufferedReader()
        val text: String = reader.use { it.readText() }
        return text
    }
}
