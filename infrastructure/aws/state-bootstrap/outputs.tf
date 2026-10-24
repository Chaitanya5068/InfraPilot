output "state_bucket_name" {
  description = "S3 bucket name to pass to Terraform's S3 backend configuration."
  value       = aws_s3_bucket.terraform_state.bucket
}

output "lock_table_name" {
  description = "DynamoDB lock table name to pass to Terraform's S3 backend configuration."
  value       = aws_dynamodb_table.terraform_locks.name
}
