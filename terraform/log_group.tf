// https://github.com/prameshbhattarai/java-lambda-terraform/blob/master/terraform/log-group.tf
// Create a log group for the lambda
resource "aws_cloudwatch_log_group" "log_group" {
  name = "/aws/lambda/spotify_lambda"
}

# allow lambda to log to cloudwatch
data "aws_iam_policy_document" "cloudwatch_log_group_access_document" {
  statement {
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]

    resources = [
      "arn:aws:logs:::*",
    ]
  }
}
