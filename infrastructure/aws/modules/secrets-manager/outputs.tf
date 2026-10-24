output "secret_id" {
  description = "ID of the Secrets Manager secret"
  value       = aws_secretsmanager_secret.this.id
}

output "secret_arn" {
  description = "ARN of the Secrets Manager secret"
  value       = aws_secretsmanager_secret.this.arn
}
