provider "aws" {
  region                      = "sa-east-1"
  access_key                  = "test"
  secret_key                  = "test"
  skip_credentials_validation = true
  skip_requesting_account_id  = true

  endpoints {
    dynamodb = "http://localhost:4566"
  }
}

resource "aws_dynamodb_table" "content" {
  name           = "flights"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "route"
  range_key      = "expiryTime"

  attribute {
    name = "route"
    type = "S"
  }
  attribute {
    name = "expiryTime"
    type = "N"
  }
}

