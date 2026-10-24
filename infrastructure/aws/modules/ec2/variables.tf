variable "name" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "private_subnet_ids" {
  type = list(string)
}

variable "alb_security_group_id" {
  type = string
}

variable "target_group_arn" {
  type = string
}

variable "ecr_repository_arn" {
  type = string
}

variable "app_image" {
  description = "Immutable image URI in this stack's ECR repository; empty creates infrastructure without starting instances."
  type        = string
  default     = ""

  validation {
    condition     = var.app_image == "" || can(regex("^[0-9]{12}\\.dkr\\.ecr\\.[a-z0-9-]+\\.amazonaws\\.com/.+(:[^:]+|@sha256:[a-f0-9]{64})$", var.app_image))
    error_message = "app_image must be empty or an immutable ECR image URI with a tag or sha256 digest."
  }
}

variable "aws_region" {
  type = string
}

variable "cloudwatch_log_group_name" {
  type = string
}

variable "instance_type" {
  type    = string
  default = "t3.micro"
}

variable "app_port" {
  type    = number
  default = 8080
}

variable "tags" {
  type = map(string)
}
