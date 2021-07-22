package terraform.spotify.lambda.poc.request

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import terraform.spotify.lambda.poc.annotation.NoArgsConstructor

@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
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
