package terraform.spotify.lambda.poc.request

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import terraform.spotify.lambda.poc.annotation.NoArgsConstructor
import terraform.spotify.lambda.poc.entity.Event

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class PostLineUserDataWithCodeRequest(
    val iss: String,
    val sub: String, // mid
    val aud: String,
    val exp: Long,
    val iat: Long,
    val code: String,
    val name: String,
    val picture: String
)
