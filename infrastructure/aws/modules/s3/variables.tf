variable "bucket_name" {
  type        = string
  description = "Name of the S3 bucket"
}

variable "tags" {
  type        = map(string)
  description = "Resource tags"
  default     = {}
}

variable "force_destroy" {
  type        = bool
  description = "Whether all objects should be deleted from bucket so it can be destroyed"
  default     = false
}
