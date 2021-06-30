package terraform.spotify.lambda.poc.entity

import terraform.spotify.lambda.poc.annotation.NoArgsConstructor

@NoArgsConstructor
data class AwsInputEvent(
    val destination: String,
    val events: List<Event>
)

@NoArgsConstructor
data class Event(
    val type: String,
    val message: Message,
    val timestamp: Long,
    val source: Source,
    val replyToken: String,
    val mode: String

)

@NoArgsConstructor
data class Message(
    val type: String,
    val id: String,
    val text: String
)

@NoArgsConstructor
data class Source(
    val type: String,
    val userId: String
)