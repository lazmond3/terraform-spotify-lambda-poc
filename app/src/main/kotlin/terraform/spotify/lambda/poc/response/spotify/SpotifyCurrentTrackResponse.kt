package terraform.spotify.lambda.poc.response.spotify

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import terraform.spotify.lambda.poc.annotation.NoArgsConstructor
import java.time.LocalDate

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown=true)
data class SpotifyCurrentTrackResponse(
    val timestamp: Long,
    val context: Any?,
    val progress_ms: Long,
    val item: SpotifyCurrentTrackItem,
    val currentlyPlayingType: String,
    val actions: SpotifyCurrentTrackActions,
    val isPlaying: Boolean
)

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown=true)
data class SpotifyCurrentTrackActions(
    val disallows: SpotifyCurrentTrackActionsDisallows
)

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown=true)
data class SpotifyCurrentTrackActionsDisallows(
    val resuming: Boolean,
    val skippingPrev: Boolean,
    val togglingRepeatContext: Boolean,
    val togglingRepeatTrack: Boolean,
    val togglingShuffle: Boolean
)

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown=true)
data class SpotifyCurrentTrackItem(
    val album: SpotifyCurrentTrackAlbum,
    val artists: List<SpotifyCurrentTrackArtist>,
    val discNumber: Int,
    val durationMs: Int,
    val explicit: Boolean,
    val externalIds: SpotifyCurrentTrackExternalId,
    val externalUrls: SpotifyCurrentTrackExternalUrl,
    val href: String,
    val id: String,
    val isLocal: Boolean,
    val isPlayable: Boolean,
    val name: String,
    val popularity: Int,
    val previewUrl: String,
    val trackNumber: Int,
    val type: String,
    val uri: String
)

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown=true)
data class SpotifyCurrentTrackExternalId(
    val isrc: String
)

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown=true)
data class SpotifyCurrentTrackAlbum(
    val albumType: String,
    val artists: List<SpotifyCurrentTrackArtist>,
    val externalUrls: SpotifyCurrentTrackExternalUrl,
    val href: String,
    val id: String,
    val images: List<SpotifyCurrentTrackImage>,
    val name: String,
    val releaseDate: String, // これちゃんと取得できるか
    val releaseDatePrecision: String, // "day"
    val totalTracks: Int,
    val type: String,
    val uri: String

)

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown=true)
data class SpotifyCurrentTrackArtist(
    val externalUrls: SpotifyCurrentTrackExternalUrl,
    val href: String,
    val id: String,
    val name: String,
    val type: String,
    val uri: String
)

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown=true)
data class SpotifyCurrentTrackExternalUrl(
    val spotify: String
)


@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown=true)
data class SpotifyCurrentTrackImage(
    val width: Int,
    val height: Int,
    val url: String
)

