variable "name" {
  type        = string
  description = "Base name for CloudWatch resources"
}

variable "retention_in_days" {
  type        = number
  description = "Specifies the number of days you want to retain log events"
  default     = 30
}

variable "tags" {
  type        = map(string)
  description = "Resource tags"
  default     = {}
}
