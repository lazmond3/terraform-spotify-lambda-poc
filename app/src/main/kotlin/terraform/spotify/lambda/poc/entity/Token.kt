package terraform.spotify.lambda.poc.entity

import terraform.spotify.lambda.poc.annotation.NoArgsConstructor

@NoArgsConstructor
data class Token(
//    val userId: String,
//    val refreshToken: String,
    val accessToken: String,
    val expiresIn: Int,
//    val scope: String
)
