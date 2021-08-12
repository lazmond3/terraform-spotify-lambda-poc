package terraform.spotify.lambda.poc.model

import terraform.spotify.lambda.poc.annotation.NoArgsConstructor

@NoArgsConstructor
data class PostbackEventData(
    val userId: String,
    val cmd: String,
    val playlistId: String?,
    val playlistName: String?
)
