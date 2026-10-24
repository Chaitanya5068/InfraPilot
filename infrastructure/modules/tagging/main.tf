locals {
  required_tags = {
    org_id        = var.org_id
    project_id    = var.project_id
    environment   = var.environment
    deployment_id = var.deployment_id
    managed_by    = "infrapilot"
  }

  all_tags = merge(local.required_tags, var.extra_tags)
}

output "tags" {
  description = "Merged map of mandatory and optional resource tags"
  value       = local.all_tags
}
