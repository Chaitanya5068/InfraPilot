output "app_endpoint" {
  description = "Public ALB endpoint; HTTPS is active when certificate_arn is set."
  value       = "http${var.certificate_arn != "" ? "s" : ""}://${module.alb.dns_name}"
}

output "ecr_repository_url" {
  value = module.ecr.repository_url
}

output "database_endpoint" {
  value = module.rds.endpoint
}

output "database_secret_arn" {
  description = "ARN only; retrieve access through an authorized Secrets Manager identity."
  value       = module.rds.master_user_secret_arn
}

output "deployment_requests_queue_url" {
  value = module.sqs.deployment_requests_url
}

output "pipeline_events_queue_url" {
  value = module.sqs.pipeline_events_url
}

output "vpc_id" {
  value = module.vpc.vpc_id
}
