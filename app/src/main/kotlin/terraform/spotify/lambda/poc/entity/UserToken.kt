package terraform.spotify.lambda.poc.entity

import terraform.spotify.lambda.poc.annotation.NoArgsConstructor
import java.time.LocalDateTime

@NoArgsConstructor
data class UserToken(
    val userId: String? = null,
    val refreshToken: String? = null,
    val accessToken: String? = null,
    val expiresIn: Int? = null,
    val updatedAt: String? = null,
)
