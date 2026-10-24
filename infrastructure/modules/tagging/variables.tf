variable "org_id" {
  type        = string
  description = "Tenant Organization UUID"
}

variable "project_id" {
  type        = string
  description = "Project UUID"
}

variable "environment" {
  type        = string
  description = "Target environment (dev, staging, prod)"
}

variable "deployment_id" {
  type        = string
  description = "Deployment run UUID"
  default     = ""
}

variable "extra_tags" {
  type        = map(string)
  description = "Optional additional custom tags"
  default     = {}
}
