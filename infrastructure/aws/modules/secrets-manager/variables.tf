variable "name" {
  type        = string
  description = "Name of the secret in AWS Secrets Manager"
}

variable "description" {
  type        = string
  description = "Description of the secret"
  default     = "Managed by InfraPilot"
}

variable "tags" {
  type        = map(string)
  description = "Resource tags"
  default     = {}
}
