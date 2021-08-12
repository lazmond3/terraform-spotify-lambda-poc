package terraform.spotify.lambda.poc.response.spotify

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import terraform.spotify.lambda.poc.annotation.NoArgsConstructor

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown=true)
data class PlaylistResponse(
    val href: String,
    val items: List<ReadPlaylistItemResponse>,
    val limit: Int,
    val next: String?,
    val offset: Int,
    val previous: String?,
    val total: Int
)


@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown=true)
data class ReadPlaylistItemResponse(
    val collaborative: Boolean,
    val description: String,
    val externalUrls: SpotifyCurrentTrackExternalUrl,
    val href: String,
    val id: String,
    val images: List<ReadPlaylistItemImage>,
    val name: String, // これ重要
    val owner: ReadPlaylistItemOwner,
    val primaryColor: String?,
    val public: Boolean,
    val snapshotId: String,
    val tracks: ReadPlaylistItemTrack,
    val type: String, // playlist
    val uri: String // これも重要
)

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown=true)
data class ReadPlaylistItemImage(
    val height: Int,
    val url: String,
    val width: Int
)

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown=true)
data class ReadPlaylistItemOwner(
    val displayName: String,
    val externalUrls: SpotifyCurrentTrackExternalUrl,
    val href: String,
    val id: String,
    val type: String,
    val uri: String
)

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown=true)
data class ReadPlaylistItemTrack(
    val href: String,
    val total: Int
)
