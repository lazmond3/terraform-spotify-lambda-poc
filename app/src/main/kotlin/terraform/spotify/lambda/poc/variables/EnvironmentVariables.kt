package terraform.spotify.lambda.poc.variables

import dagger.Module
import terraform.spotify.lambda.poc.exception.SystemException

@Module
class EnvironmentVariables {
    val spotifyClientId = System.getenv("SPOTIFY_CLIENT_ID")
        ?: throw SystemException("SPOTIFY_CLIENT_ID is not set")
    val spotifyClientSecret = System.getenv("SPOTIFY_CLIENT_SECRET")
        ?: throw SystemException("SPOTIFY_CLIENT_SECRET is not set")
    val lineBotChannelAccessToken = System.getenv("LINE_BOT_CHANNEL_ACCESS_TOKEN")
        ?: throw SystemException("LINE_BOT_CHANNEL_ACCESS_TOKEN is not set")
}
