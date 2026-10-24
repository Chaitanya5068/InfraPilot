output "endpoint" {
  value = aws_db_instance.this.address
}

output "port" {
  value = aws_db_instance.this.port
}

output "master_user_secret_arn" {
  description = "ARN of the RDS-managed master credential secret; secret value is never output."
  value       = aws_db_instance.this.master_user_secret[0].secret_arn
}
