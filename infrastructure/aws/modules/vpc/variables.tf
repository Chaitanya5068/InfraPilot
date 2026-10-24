variable "name" {
  type = string
}

variable "vpc_cidr" {
  type    = string
  default = "10.40.0.0/16"
}

variable "az_count" {
  type    = number
  default = 2

  validation {
    condition     = var.az_count >= 2 && var.az_count <= 3
    error_message = "az_count must be between 2 and 3."
  }
}

variable "enable_nat_gateway" {
  description = "Creates one NAT Gateway for private-subnet egress; it incurs hourly and data processing charges."
  type        = bool
  default     = true
}

variable "tags" {
  type = map(string)
}
