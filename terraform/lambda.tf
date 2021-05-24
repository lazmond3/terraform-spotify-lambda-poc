resource "aws_lambda_function" "java_lambda_function" {
  runtime          = var.lambda_runtime
  filename         = var.lambda_payload_filename
  source_code_hash = filebase64sha256(var.lambda_payload_filename)
  function_name    = "java_lambda_function"

  handler = var.lambda_function_handler
  timeout = 60
  # memory_size = 256
  memory_size = 1536
  role        = aws_iam_role.iam_role_for_lambda.arn
  depends_on  = [aws_cloudwatch_log_group.log_group]
  environment {
    variables = {
      SPOTIFY_REFRESH_TOKEN         = var.spotify_refresh_token,
      SPOTIFY_CLIENT_ID             = var.spotify_client_id,
      SPOTIFY_CLIENT_SECRET         = var.spotify_client_secret,
      LINE_BOT_CHANNEL_ACCESS_TOKEN = var.line_bot_channel_access_token
    }
  }
}
