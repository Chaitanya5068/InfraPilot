resource "aws_secretsmanager_secret" "this" {
  name                    = var.name
  description             = var.description
  recovery_window_in_days = 0 # allows immediate recreation if needed in dev/sandbox
  tags                    = var.tags
}
