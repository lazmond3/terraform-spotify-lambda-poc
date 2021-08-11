terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 3.0"
    }
  }
  backend "s3" {
    bucket = "tfstate-bucket-spotify-lambda-poc"
    key    = "tfstate-bucket-spotify-lambda-poc/dev.tfstate"
    region = "ap-northeast-1"
  }
}

provider "aws" {
  region = "ap-northeast-1"
}

provider "aws" {
  alias  = "virginia"
  region = "us-east-1"
}


/* ALB の 設定 */
# module "alb-lambda" {
#   source                                   = "github.com/lazmond3/terraform-https-alb-lambda"
#   app                                      = "spotify-line-bot"
#   environment                              = "develop"
#   aws_lambda_function_lambda_arn           = aws_lambda_function.spotify_lambda.arn
#   aws_lambda_function_lambda_id            = aws_lambda_function.spotify_lambda.id
#   aws_lambda_function_lambda_function_name = aws_lambda_function.spotify_lambda.function_name
#   domain                                   = "moikilo00.net"
# }