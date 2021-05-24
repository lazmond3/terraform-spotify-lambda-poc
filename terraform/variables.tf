variable "lambda_payload_filename" {
  default = "../app/build/libs/app-all.jar"
}

variable "lambda_function_handler" {
  default = "terraform.spotify.lambda.poc.handler.LambdaHandler"
}

variable "lambda_runtime" {
  default = "java11"
}

variable "api_path" {
  default = "{proxy+}"
}

variable "api_env_stage_name" {
  default = "terraform-lambda-java-stage"
}
variable "spotify_refresh_token" {
  type = string
  sensitive = true
}
variable "spotify_client_id" {
  type = string
  sensitive = true
}
variable "spotify_client_secret" {
  type = string
  sensitive = true
}
variable "line_bot_channel_access_token" {
  type = string
  sensitive = true
}
