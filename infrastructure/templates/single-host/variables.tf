variable "run_id" {
  type        = string
  description = "Unique InfraPilot run identifier"
}

variable "environment" {
  type        = string
  description = "Target deployment environment (dev, staging, prod)"
}

variable "region" {
  type        = string
  default     = "ap-south-1"
  description = "AWS deployment region"
}

variable "instance_type" {
  type        = string
  default     = "t3.small"
  description = "EC2 instance type (t3.micro or t3.small for demo cost control)"
}

variable "app_port" {
  type        = number
  default     = 8080
  description = "Application port"
}

variable "my_ip_cidr" {
  type        = string
  default     = "0.0.0.0/0"
  description = "CIDR block permitted for administrative SSH and Grafana access"
}
