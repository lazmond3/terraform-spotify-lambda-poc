package terraform.spotify.lambda.poc.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import terraform.spotify.lambda.poc.construction.ObjectConstructor

// Lambda で ハンドラがこちらに渡される
class LambdaHandler : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    val objectConstructor = ObjectConstructor(isForReal = true)
    val innerHandler = InnerHandler(objectConstructor)

    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent =
        innerHandler.handleRequest(input, context)
}
