resource "aws_security_group" "this" {
  name        = "${var.name}-database"
  description = "PostgreSQL access from the application security group only."
  vpc_id      = var.vpc_id

  ingress {
    description     = "PostgreSQL from application instances"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [var.application_security_group_id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, { Name = "${var.name}-database" })
}

resource "aws_db_subnet_group" "this" {
  name       = "${var.name}-database"
  subnet_ids = var.database_subnet_ids
  tags       = merge(var.tags, { Name = "${var.name}-database" })
}

resource "aws_db_instance" "this" {
  identifier                      = "${var.name}-postgres"
  engine                          = "postgres"
  engine_version                  = var.engine_version
  instance_class                  = var.instance_class
  allocated_storage               = 20
  max_allocated_storage           = 100
  storage_type                    = "gp3"
  storage_encrypted               = true
  db_name                         = "infrapilot"
  username                        = "infrapilot_admin"
  manage_master_user_password     = true
  port                            = 5432
  db_subnet_group_name            = aws_db_subnet_group.this.name
  vpc_security_group_ids          = [aws_security_group.this.id]
  publicly_accessible             = false
  multi_az                        = var.environment == "prod"
  backup_retention_period         = var.environment == "prod" ? 14 : 7
  auto_minor_version_upgrade      = true
  deletion_protection             = var.environment == "prod"
  skip_final_snapshot             = var.environment != "prod"
  final_snapshot_identifier       = var.environment == "prod" ? "${var.name}-final" : null
  copy_tags_to_snapshot           = true
  performance_insights_enabled    = true
  enabled_cloudwatch_logs_exports = ["postgresql", "upgrade"]

  tags = merge(var.tags, { Name = "${var.name}-postgres" })
}
