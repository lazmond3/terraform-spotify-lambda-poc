package terraform.spotify.lambda.poc.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler


class LambdaHandler: RequestHandler<Map<String, Any>, Any> {
    override fun handleRequest(input: Map<String, Any>, context: Context): Any {
        val logger = context.logger

        println("hello world lambda!")
        logger.log("hello world! lambda 2 in logging")

        return 1
    }
}
