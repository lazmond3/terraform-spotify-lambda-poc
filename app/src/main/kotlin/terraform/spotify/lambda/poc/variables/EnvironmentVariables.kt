package terraform.spotify.lambda.poc.variables

import terraform.spotify.lambda.poc.exception.SystemException

interface EnvironmentVariablesInterface {
    val spotifyClientId: String
    val spotifyClientSecret: String
    val lineBotChannelAccessToken: String
}

class EnvironmentVariables : EnvironmentVariablesInterface {
    override val spotifyClientId = System.getenv("SPOTIFY_CLIENT_ID")
        ?: throw SystemException("SPOTIFY_CLIENT_ID is not set")
    override val spotifyClientSecret = System.getenv("SPOTIFY_CLIENT_SECRET")
        ?: throw SystemException("SPOTIFY_CLIENT_SECRET is not set")
    override val lineBotChannelAccessToken = System.getenv("LINE_BOT_CHANNEL_ACCESS_TOKEN")
        ?: throw SystemException("LINE_BOT_CHANNEL_ACCESS_TOKEN is not set")
}

class TestEnvironmentVariables : EnvironmentVariablesInterface {
    override val spotifyClientId = System.getenv("SPOTIFY_CLIENT_ID") ?: ""
    override val spotifyClientSecret = System.getenv("SPOTIFY_CLIENT_SECRET") ?: ""
    override val lineBotChannelAccessToken = System.getenv("LINE_BOT_CHANNEL_ACCESS_TOKEN") ?: ""
}