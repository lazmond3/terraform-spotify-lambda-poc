resource "aws_dynamodb_table" "spotify-poc" {
  name           = "spotify-poc"
  billing_mode   = "PROVISIONED"
  read_capacity  = 1
  write_capacity = 1
  hash_key       = "UserId"
#   range_key      = "GameTitle"

  attribute {
    name = "UserId"
    type = "S"
  }

  # ttl {
  #   attribute_name = "TimeToExist"
  #   enabled        = false
  # }
  tags = {
    Name        = "spotify-poc"
    Environment = "production"
  }
}


# role


# lambda policy
resource "aws_iam_policy" "lambda_dynamo" {
  name = "lambda_dynamo-policy"
  path = "/"

  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "ReadWriteTable",
            "Effect": "Allow",
            "Action": [
                "dynamodb:BatchGetItem",
                "dynamodb:GetItem",
                "dynamodb:Query",
                "dynamodb:Scan",
                "dynamodb:BatchWriteItem",
                "dynamodb:PutItem",
                "dynamodb:UpdateItem"
            ],
            "Resource": "${aws_dynamodb_table.spotify-poc.arn}"
        }
    ]
}
EOF
}

# Attach the policy to the role
resource "aws_iam_role_policy_attachment" "lambda_dynamo" {
  role       = aws_iam_role.iam_role_for_lambda.name
  policy_arn = aws_iam_policy.lambda_dynamo.arn
}
