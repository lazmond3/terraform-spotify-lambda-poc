package terraform.spotify.lambda.poc

import terraform.spotify.lambda.poc.variables.EnvironmentVariables
import javax.inject.Inject

fun main() {
    val app = App()
    println("hello world")
    println("app: ${app.variables}")
}

class App {
    @Inject
    lateinit var variables: EnvironmentVariables
}