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
