package terraform.spotify.lambda.poc.entity

import terraform.spotify.lambda.poc.annotation.NoArgsConstructor

@NoArgsConstructor
data class UserToken(
//    val userId: String,
//    val refreshToken: String,
    val accessToken: String,
    val expiresIn: Int,
//    val scope: String
)
