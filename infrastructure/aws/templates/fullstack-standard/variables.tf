variable "org_id" {
  description = "Tenant organization identifier; use a UUID or stable, non-sensitive tenant key."
  type        = string
}

variable "project_id" {
  description = "Project identifier used for resource ownership and tags."
  type        = string
}

variable "deployment_id" {
  description = "Unique deployment identifier."
  type        = string
}

variable "environment" {
  description = "Deployment environment."
  type        = string

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "environment must be dev, staging, or prod."
  }
}

variable "app_name" {
  description = "Lowercase application name; used as an AWS resource name prefix."
  type        = string

  validation {
    condition     = can(regex("^[a-z][a-z0-9-]{1,19}$", var.app_name))
    error_message = "app_name must be 2-20 lowercase letters, digits, or hyphens and start with a letter."
  }
}

variable "region" {
  type    = string
  default = "ap-south-1"
}

variable "vpc_cidr" {
  type    = string
  default = "10.40.0.0/16"
}

variable "instance_type" {
  type    = string
  default = "t3.micro"
}

variable "db_instance_class" {
  type    = string
  default = "db.t3.micro"
}

variable "db_engine_version" {
  type    = string
  default = "15.10"
}

variable "app_port" {
  type    = number
  default = 8080
}

variable "app_image_tag" {
  description = "Immutable image tag in the ECR repository created by this template. Leave empty for the initial infrastructure apply."
  type        = string
  default     = ""

  validation {
    condition     = var.app_image_tag == "" || (var.app_image_tag != "latest" && can(regex("^[A-Za-z0-9_][A-Za-z0-9_.-]{0,127}$", var.app_image_tag)))
    error_message = "app_image_tag must be empty or a valid immutable tag other than latest."
  }
}

variable "certificate_arn" {
  description = "Optional ACM certificate ARN for HTTPS; HTTP redirects to HTTPS when provided."
  type        = string
  default     = ""
}

variable "enable_nat_gateway" {
  description = "One NAT Gateway is shared by private subnets; disabling it prevents private subnet internet egress."
  type        = bool
  default     = true
}
