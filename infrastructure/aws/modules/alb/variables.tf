variable "name" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "public_subnet_ids" {
  type = list(string)
}

variable "target_port" {
  type    = number
  default = 8080
}

variable "health_check_path" {
  type    = string
  default = "/actuator/health"
}

variable "certificate_arn" {
  description = "Optional ACM certificate ARN; when provided, HTTP redirects to HTTPS."
  type        = string
  default     = ""
}

variable "tags" {
  type = map(string)
}
