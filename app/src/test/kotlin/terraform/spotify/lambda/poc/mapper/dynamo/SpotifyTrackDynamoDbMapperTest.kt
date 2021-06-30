package terraform.spotify.lambda.poc.mapper.dynamo

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class SpotifyTrackDynamoDbMapperTest {
    val trackTableName = "spotify-dynamo-music"
    val ddb = AmazonDynamoDBClientBuilder.defaultClient()
    val spotifyTrackDynamoDbMapper = SpotifyTrackDynamoDbMapper(
        tableName = trackTableName,
        gsiIndexName = "SpotifyDynamoMusicPlaylistIndex",
        dbClient = ddb
    )

    @Test
    @Disabled
    fun get() {
        val result = spotifyTrackDynamoDbMapper.readRow(
            playlistId = "2eR0YK4jBO4ol03Z0FzsaU",
            trackId = "7CZKnz9mEt8Z638tyY5cKL"
        )
        println("result: $result")
    }

    @Test
    @Disabled
    fun delete() {
        val result = spotifyTrackDynamoDbMapper.delete(
            playlistId = "debugplaylistId",
            trackId = "7CZKnz9mEt8Z638tyY5cKL",
            logger = null
        )
        println("result: $result")
    }
}