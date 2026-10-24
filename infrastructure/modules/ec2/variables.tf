variable "project_name" { type = string; default = "infrapilot" }
variable "vpc_id" { type = string }
variable "subnet_id" { type = string }
variable "instance_type" { type = string; default = "t3.small" }
variable "allowed_admin_cidr" { type = string; default = "0.0.0.0/0" }
variable "iam_instance_profile" { type = string; default = null }
variable "tags" { type = map(string); default = {} }
