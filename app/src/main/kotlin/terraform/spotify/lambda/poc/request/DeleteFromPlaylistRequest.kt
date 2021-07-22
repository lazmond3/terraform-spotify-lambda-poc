package terraform.spotify.lambda.poc.request

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import terraform.spotify.lambda.poc.annotation.NoArgsConstructor

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class DeleteFromPlaylistRequest(
    val uris: List<String>
)
