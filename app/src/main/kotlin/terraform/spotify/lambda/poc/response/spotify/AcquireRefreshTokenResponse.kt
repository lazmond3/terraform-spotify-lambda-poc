package terraform.spotify.lambda.poc.response.spotify

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import terraform.spotify.lambda.poc.annotation.NoArgsConstructor

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class AcquireRefreshTokenResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val refreshToken: String,
    val scope: String
)
