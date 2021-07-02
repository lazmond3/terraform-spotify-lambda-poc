package terraform.spotify.lambda.poc.model

data class QuickReplyData(
    val imageUrl: String,
    val label: String,
    val messageText: String
)