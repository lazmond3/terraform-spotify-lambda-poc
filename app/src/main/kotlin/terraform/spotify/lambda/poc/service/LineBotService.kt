package terraform.spotify.lambda.poc.service

import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.model.PushMessage
import com.linecorp.bot.model.ReplyMessage
import com.linecorp.bot.model.action.MessageAction
import com.linecorp.bot.model.action.PostbackAction
import com.linecorp.bot.model.message.TemplateMessage
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.model.message.quickreply.QuickReply
import com.linecorp.bot.model.message.quickreply.QuickReplyItem
import com.linecorp.bot.model.message.template.CarouselColumn
import com.linecorp.bot.model.message.template.CarouselTemplate
import java.net.URI
import terraform.spotify.lambda.poc.`interface`.LoggerInterface
import terraform.spotify.lambda.poc.model.QuickPostbackData
import terraform.spotify.lambda.poc.model.QuickReplyData

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

    fun sendQuickReplyPostbackAction(mid: String, text: String, quickReplyData: List<QuickPostbackData>, logger: LoggerInterface) {
        val future = client.pushMessage(
            PushMessage(
                mid,
                TextMessage(
                    text,
                    QuickReply.items(
                        quickReplyData.map {
                            QuickReplyItem.builder()
                                .imageUrl(URI(it.imageUrl))
                                .action(PostbackAction(
                                    if (it.label.length >= 20) {
                                        it.label.substring(0, 20)
                                    } else it.label, it.data))
                                .build()
                        }
                    )
                )
            )
        )
        logger.log("details: ${future.get().details}, message:${future.get().message}")
    }

    fun sendMultipleCarouselMessage(mid: String, carouselList: List<CarouselColumn>, logger: LoggerInterface) {

        val future = client.pushMessage(
            PushMessage(
                mid,
                TemplateMessage(
                    "プレイリスト",
                    CarouselTemplate(carouselList)
                )
            )
        )
        logger.log("details: ${future.get().details}, message:${future.get().message}")

    }
}
