resource "aws_lambda_function" "java_lambda_function" {
  runtime          = var.lambda_runtime
  filename         = var.lambda_payload_filename
  source_code_hash = filebase64sha256(var.lambda_payload_filename)
  function_name    = "java_lambda_function"

  handler     = var.lambda_function_handler
  timeout     = 60
  memory_size = 256
  role        = aws_iam_role.iam_role_for_lambda.arn
  depends_on  = ["aws_cloudwatch_log_group.log_group"]
}
