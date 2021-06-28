package terraform.spotify.lambda.poc.entity

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import terraform.spotify.lambda.poc.annotation.NoArgsConstructor

@NoArgsConstructor
data class UserToken(
    val userId: String? = null,
    val refreshToken: String? = null,
    val playlistId: String? = null,
    val accessToken: String? = null,
    val expiresAt: Int? = null,
    val updatedAt: String? = null,
) {
    constructor(result: Map<String, AttributeValue>) : this(
        // 本当は小文字にすべきだった？
        userId = result.get("UserId")?.s,
        refreshToken = result.get("RefreshToken")?.s,
        playlistId = result.get("PlayListId")?.s,
        accessToken = result.get("AccessToken")?.s,
        expiresAt = result.get("ExpiresAt")?.n?.toInt(),
        updatedAt = result.get("UpdatedAt")?.s
    )
}
