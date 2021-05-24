package terraform.spotify.lambda.poc.service

import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.model.PushMessage
import com.linecorp.bot.model.ReplyMessage
import com.linecorp.bot.model.message.TextMessage

class LineBotService(
    val token: String
) {
    val client = LineMessagingClient.builder(token).build();

    fun sendMessage(mid: String, text: String) = client.pushMessage(
        PushMessage(
            mid,
            TextMessage(text)
        )
    ).get()

    fun replyToMessage(replyToken: String, text: String) = client.replyMessage(
        ReplyMessage(
            replyToken,
            TextMessage(text)
        )
    ).get()
}