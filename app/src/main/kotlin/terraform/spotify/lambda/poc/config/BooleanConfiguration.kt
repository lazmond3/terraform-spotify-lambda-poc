package terraform.spotify.lambda.poc.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BooleanConfiguration {
    @Bean
    fun isForReal() = true
}
