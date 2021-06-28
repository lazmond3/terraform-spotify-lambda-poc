# user id, playlist id, music_id
resource "aws_dynamodb_table" "spotify-dynamo-music" {
  name           = "spotify-dynamo-music"
  billing_mode   = "PROVISIONED"
  read_capacity  = 1
  write_capacity = 1
  hash_key       = "UserId"
  range_key      = "PlaylistId"

  attribute {
    name = "UserId"
    type = "S"
  }

  attribute {
    name = "PlaylistId"
    type = "S"
  }

  attribute {
    name = "TrackId"
    type = "S"
  }

  global_secondary_index {
    name               = "SpotifyDynamoMusicPlaylistIndex"
    hash_key           = "PlaylistId"
    range_key          = "TrackId"
    write_capacity     = 1
    read_capacity      = 1
    projection_type    = "INCLUDE"
    non_key_attributes = ["UserId"]
  }


  tags = {
    Name        = "spotify-dynamo-music"
    Environment = "production"
  }
}

# lambda policy
resource "aws_iam_policy" "lambda_dynamo_music" {
  name = "lambda_dynamo_music-policy"
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
            "Resource": "${aws_dynamodb_table.spotify-dynamo-music.arn}*"
        }
    ]
}
EOF
}

# Attach the policy to the role
resource "aws_iam_role_policy_attachment" "lambda_dynamo_music" {
  role       = aws_iam_role.iam_role_for_lambda.name
  policy_arn = aws_iam_policy.lambda_dynamo_music.arn
}
