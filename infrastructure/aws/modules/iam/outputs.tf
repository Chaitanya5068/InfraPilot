output "role_name" {
  description = "Name of the IAM role"
  value       = aws_iam_role.instance.name
}

output "role_arn" {
  description = "ARN of the IAM role"
  value       = aws_iam_role.instance.arn
}

output "instance_profile_name" {
  description = "Name of the IAM instance profile"
  value       = aws_iam_instance_profile.instance.name
}

output "instance_profile_arn" {
  description = "ARN of the IAM instance profile"
  value       = aws_iam_instance_profile.instance.arn
}
