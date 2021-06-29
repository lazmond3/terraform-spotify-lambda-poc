package terraform.spotify.lambda.poc.entity

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import terraform.spotify.lambda.poc.annotation.NoArgsConstructor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@NoArgsConstructor
data class PlaylistTrack(
    val userId: String? = null,
    val playlistId: String? = null,
    val trackId: String? = null,
    val addedAt: LocalDateTime? = null,
) {
    constructor(result: Map<String, AttributeValue>) : this(
        // 本当は小文字にすべきだった？
        userId = result.get("UserId")?.s,
        playlistId = result.get("PlaylistId")?.s,
        trackId = result.get("TrackId")?.s,
        addedAt = result.get("UpdatedAt")?.s?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    )
}
