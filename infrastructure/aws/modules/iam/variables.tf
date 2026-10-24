variable "name" {
  type        = string
  description = "Base name for IAM resources"
}

variable "tags" {
  type        = map(string)
  description = "Resource tags"
  default     = {}
}
