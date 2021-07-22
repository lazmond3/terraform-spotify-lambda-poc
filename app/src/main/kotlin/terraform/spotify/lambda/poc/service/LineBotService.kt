package terraform.spotify.lambda.poc.service

import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.model.PushMessage
import com.linecorp.bot.model.ReplyMessage
import com.linecorp.bot.model.action.MessageAction
import com.linecorp.bot.model.action.PostbackAction
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.model.message.quickreply.QuickReply
import com.linecorp.bot.model.message.quickreply.QuickReplyItem
import terraform.spotify.lambda.poc.model.QuickReplyData
import java.net.URI
import terraform.spotify.lambda.poc.model.QuickPostbackData

class LineBotService(
    val token: String
) {
    val client = LineMessagingClient.builder(token).build()

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

    fun sendQuickReplyMessage(mid: String, text: String, quickReplyData: List<QuickReplyData>) = client.pushMessage(
        PushMessage(
            mid,
            TextMessage(
                text,
                QuickReply.items(
                    quickReplyData.map {
                        QuickReplyItem.builder()
                            .imageUrl(URI(it.imageUrl))
                            .action(MessageAction(it.label, it.messageText))
                            .build()
                    }
                )
            )
        )
    )

    fun sendQuickReplyPostbackAction(mid: String, text: String, quickReplyData: List<QuickPostbackData>) = client.pushMessage(
        PushMessage(
            mid,
            TextMessage(
                text,
                QuickReply.items(
                    quickReplyData.map {
                        QuickReplyItem.builder()
                            .imageUrl(URI(it.imageUrl))
                            .action(PostbackAction(it.label, it.data))
                            .build()
                    }
                )
            )
        )
    )

}
