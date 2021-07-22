package terraform.spotify.lambda.poc.model

data class QuickPostbackData(
    val imageUrl: String,
    val label: String,
    val data: String,
    val displayMessage: String
)
