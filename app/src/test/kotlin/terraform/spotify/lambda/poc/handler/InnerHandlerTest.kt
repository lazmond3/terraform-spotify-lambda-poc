package terraform.spotify.lambda.poc.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import io.mockk.mockk
import org.junit.jupiter.api.Test
import terraform.spotify.lambda.poc.construction.ObjectConstructor
import terraform.spotify.lambda.poc.exception.SystemException
import java.io.File

internal class InnerHandlerTest {
    val objectConstructor = ObjectConstructor(isForReal = false)

    @Test
    fun preflightForPostTest() {
        val mockObjectConstructor = mockk<ObjectConstructor>(relaxed = true)
        val innerHandler = InnerHandler(mockObjectConstructor)

        val inputAsString = readFile("input/post_preflight.json")
        val inputAsObj =
            objectConstructor.objectMapper.readValue(inputAsString, APIGatewayProxyRequestEvent::class.java)
        val context = mockk<Context>(relaxed = true)
        val result = innerHandler.handleRequest(inputAsObj, context)
        assert(result.statusCode == 204)
        assert(result.headers.get("Access-Control-Allow-Origin") == "*")
        assert(result.headers.get("Access-Control-Allow-Methods") == "POST, GET, OPTIONS, DELETE")
        assert(result.headers.get("Access-Control-Max-Age") == "86400")
    }

    private fun readFile(resourcePath: String): String {
        val fullPath = javaClass.classLoader.getResource(resourcePath)?.path
            ?: throw SystemException("Invalid resource path: $resourcePath")

        val reader = File(fullPath).bufferedReader()
        val text: String = reader.use { it.readText() }
        return text
    }


}