package terraform.spotify.lambda.poc.controller

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import terraform.spotify.lambda.poc.service.LineBotService

class LineBotHookController(val token: String) {
    val lineBotService = LineBotService(token)

    fun handle(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {

    }

}