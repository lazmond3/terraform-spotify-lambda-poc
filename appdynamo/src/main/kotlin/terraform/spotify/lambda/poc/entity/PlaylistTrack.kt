package terraform.spotify.lambda.poc.entity

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import terraform.spotify.lambda.poc.annotation.NoArgsConstructor

@NoArgsConstructor
data class PlaylistTrack(
    val userId: String? = null,
    val playlistId: String? = null,
    val trackId: String? = null,
    val updatedAt: String? = null,
) {
    constructor(result: Map<String, AttributeValue>) : this(
        // 本当は小文字にすべきだった？
        userId = result.get("UserId")?.s,
        playlistId = result.get("PlayListId")?.s,
        trackId = result.get("TrackId")?.s,
        updatedAt = result.get("UpdatedAt")?.s
    )
}
