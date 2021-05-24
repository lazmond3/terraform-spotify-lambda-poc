package terraform.spotify.lambda.poc.entity

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import terraform.spotify.lambda.poc.annotation.NoArgsConstructor

@NoArgsConstructor
data class UserToken(
    val userId: String? = null,
    val refreshToken: String? = null,
    val accessToken: String? = null,
    val expiresAt: Int? = null,
    val updatedAt: String? = null,
) {
    constructor(result: Map<String, AttributeValue>) : this(
        userId = result.get("UserId")?.s,
        refreshToken = result.get("RefreshToken")?.s,
        accessToken = result.get("AccessToken")?.s,
        expiresAt = result.get("ExpiresAt")?.n?.toInt(),
        updatedAt = result.get("UpdatedAt")?.s
    )
}
