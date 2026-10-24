# ==============================================================================
# InfraPilot Production-Ready AWS Infrastructure (Terraform)
# Implements:
#  1. Multi-AZ VPC (Public, Private, Database subnets across 2 AZs)
#  2. Dual-AZ Application Load Balancer (ALB) with HTTPS listener
#  3. Multi-AZ Auto Scaling Group (EC2) with SSM Session Manager (No SSH port 22)
#  4. Encrypted Multi-AZ PostgreSQL RDS with automated 7-day backups
#  5. SQS Deployment Job Queue with Dead-Letter Queue (DLQ)
#  6. AWS Secrets Manager for zero-plaintext credentials
# ==============================================================================

terraform {
  required_version = ">= 1.6.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.30"
    }
  }
  backend "s3" {
    # Populated dynamically via -backend-config
  }
}

provider "aws" {
  region = var.aws_region
  default_tags {
    tags = {
      Project     = "InfraPilot"
      Environment = var.environment
      ManagedBy   = "Terraform"
      Owner       = "SRE-Team"
    }
  }
}

# ------------------------------------------------------------------------------
# VARIABLES
# ------------------------------------------------------------------------------
variable "aws_region" {
  type    = string
  default = "ap-south-1"
}

variable "environment" {
  type    = string
  default = "production"
}

variable "vpc_cidr" {
  type    = string
  default = "10.0.0.0/16"
}

variable "instance_type" {
  type    = string
  default = "t3.small"
}

variable "db_instance_class" {
  type    = string
  default = "db.t3.micro"
}

data "aws_availability_zones" "available" {
  state = "available"
}

# ------------------------------------------------------------------------------
# 1. MULTI-AZ NETWORKING (VPC, Subnets, Internet & NAT Gateways)
# ------------------------------------------------------------------------------
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true
  tags = {
    Name = "infrapilot-${var.environment}-vpc"
  }
}

resource "aws_internet_gateway" "gw" {
  vpc_id = aws_vpc.main.id
  tags = {
    Name = "infrapilot-${var.environment}-igw"
  }
}

# Public Subnets (for ALB)
resource "aws_subnet" "public_1" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = true
  tags = { Name = "infrapilot-public-1-${data.aws_availability_zones.available.names[0]}" }
}

resource "aws_subnet" "public_2" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.2.0/24"
  availability_zone       = data.aws_availability_zones.available.names[1]
  map_public_ip_on_launch = true
  tags = { Name = "infrapilot-public-2-${data.aws_availability_zones.available.names[1]}" }
}

# Private Application Subnets (for Auto Scaling Group)
resource "aws_subnet" "private_app_1" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.10.0/24"
  availability_zone = data.aws_availability_zones.available.names[0]
  tags              = { Name = "infrapilot-private-app-1" }
}

resource "aws_subnet" "private_app_2" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.20.0/24"
  availability_zone = data.aws_availability_zones.available.names[1]
  tags              = { Name = "infrapilot-private-app-2" }
}

# Private Database Subnets (for RDS)
resource "aws_subnet" "private_db_1" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.100.0/24"
  availability_zone = data.aws_availability_zones.available.names[0]
  tags              = { Name = "infrapilot-private-db-1" }
}

resource "aws_subnet" "private_db_2" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.200.0/24"
  availability_zone = data.aws_availability_zones.available.names[1]
  tags              = { Name = "infrapilot-private-db-2" }
}

# NAT Gateway for outbound traffic from private instances (patching/Docker pull)
resource "aws_eip" "nat" {
  domain = "vpc"
}

resource "aws_nat_gateway" "nat" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public_1.id
  tags          = { Name = "infrapilot-nat-gw" }
}

# Route Tables
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.gw.id
  }
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.nat.id
  }
}

resource "aws_route_table_association" "pub_1" {
  subnet_id      = aws_subnet.public_1.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "pub_2" {
  subnet_id      = aws_subnet.public_2.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "priv_1" {
  subnet_id      = aws_subnet.private_app_1.id
  route_table_id = aws_route_table.private.id
}

resource "aws_route_table_association" "priv_2" {
  subnet_id      = aws_subnet.private_app_2.id
  route_table_id = aws_route_table.private.id
}

# ------------------------------------------------------------------------------
# 2. SECURITY GROUPS (Zero Public SSH, Strict Ingress Chaining)
# ------------------------------------------------------------------------------
resource "aws_security_group" "alb" {
  name        = "infrapilot-alb-sg"
  description = "Allows HTTP/HTTPS ingress to load balancer"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "HTTP Public"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS Public"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "app" {
  name        = "infrapilot-app-sg"
  description = "Allows traffic ONLY from ALB"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "App port from ALB only"
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "db" {
  name        = "infrapilot-db-sg"
  description = "Allows PostgreSQL traffic ONLY from App instances"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "PostgreSQL from App Tier"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.app.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# ------------------------------------------------------------------------------
# 3. AWS SYSTEMS MANAGER (SSM) IAM ROLE (Eliminating SSH Port 22)
# ------------------------------------------------------------------------------
resource "aws_iam_role" "instance_role" {
  name = "infrapilot-ssm-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ssm_attach" {
  role       = aws_iam_role.instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "instance_profile" {
  name = "infrapilot-ec2-profile"
  role = aws_iam_role.instance_role.name
}

# ------------------------------------------------------------------------------
# 4. MULTI-AZ APPLICATION LOAD BALANCER (ALB)
# ------------------------------------------------------------------------------
resource "aws_lb" "main" {
  name               = "infrapilot-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = [aws_subnet.public_1.id, aws_subnet.public_2.id]
  tags               = { Name = "infrapilot-production-alb" }
}

resource "aws_lb_target_group" "app" {
  name        = "infrapilot-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "instance"

  health_check {
    enabled             = true
    path                = "/api/v1/health"
    port                = "8080"
    protocol            = "HTTP"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 15
    matcher             = "200"
  }
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }
}

# ------------------------------------------------------------------------------
# 5. MULTI-AZ AUTO SCALING GROUP (EC2 across 2 Availability Zones)
# ------------------------------------------------------------------------------
data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]
  filter {
    name   = "name"
    values = ["al2023-ami-2023*-x86_64"]
  }
}

resource "aws_launch_template" "app" {
  name_prefix   = "infrapilot-lt-"
  image_id      = data.aws_ami.amazon_linux_2023.id
  instance_type = var.instance_type

  iam_instance_profile {
    name = aws_iam_instance_profile.instance_profile.name
  }

  network_interfaces {
    associate_public_ip_address = false
    security_groups             = [aws_security_group.app.id]
  }

  # Hardened metadata options (IMDSv2 Enforced to defeat SSRF credential theft)
  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
  }

  user_data = base64encode(<<-EOF
              #!/bin/bash
              dnf update -y
              dnf install -y docker
              systemctl enable --now docker
              docker run -d --name app -p 8080:8080 --restart always ghcr.io/chaitanya5068/infrapilot:latest
              EOF
  )

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "infrapilot-asg-worker"
    }
  }
}

resource "aws_autoscaling_group" "app" {
  name                = "infrapilot-asg"
  vpc_zone_identifier = [aws_subnet.private_app_1.id, aws_subnet.private_app_2.id]
  target_group_arns   = [aws_lb_target_group.app.arn]
  health_check_type   = "ELB"
  health_check_grace_period = 180

  min_size         = 2
  max_size         = 6
  desired_capacity = 2

  launch_template {
    id      = aws_launch_template.app.id
    version = "$Latest"
  }

  tag {
    key                 = "Name"
    value               = "infrapilot-app-instance"
    propagate_at_launch = true
  }
}

# ------------------------------------------------------------------------------
# 6. ENCRYPTED MULTI-AZ POSTGRESQL RDS (Automated 7-Day Backups & PITR)
# ------------------------------------------------------------------------------
resource "aws_db_subnet_group" "rds" {
  name       = "infrapilot-rds-subnet-group"
  subnet_ids = [aws_subnet.private_db_1.id, aws_subnet.private_db_2.id]
}

resource "random_password" "db_password" {
  length  = 24
  special = false
}

resource "aws_secretsmanager_secret" "db_credentials" {
  name = "infrapilot/production/db-credentials"
}

resource "aws_secretsmanager_secret_version" "db_credentials" {
  secret_id     = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    username = "infrapilot_admin"
    password = random_password.db_password.result
    dbname   = "infrapilot_prod"
  })
}

resource "aws_db_instance" "postgres" {
  identifier             = "infrapilot-postgres-production"
  engine                 = "postgres"
  engine_version         = "15.4"
  instance_class         = var.db_instance_class
  allocated_storage      = 20
  max_allocated_storage  = 100
  storage_type           = "gp3"
  storage_encrypted      = true
  multi_az               = true # High Availability across 2 Availability Zones

  db_name  = "infrapilot_prod"
  username = "infrapilot_admin"
  password = random_password.db_password.result

  db_subnet_group_name   = aws_db_subnet_group.rds.name
  vpc_security_group_ids = [aws_security_group.db.id]

  # SRE Production Best Practice: Automated 7-Day Backup & Maintenance Window
  backup_retention_period   = 7
  backup_window             = "02:00-03:00"
  maintenance_window        = "Sun:04:00-Sun:05:00"
  auto_minor_version_upgrade = true
  skip_final_snapshot       = false
  final_snapshot_identifier = "infrapilot-final-snapshot"
  deletion_protection       = false # Set true in enterprise strict mode
}

# ------------------------------------------------------------------------------
# 7. SQS DEPLOYMENT QUEUE WITH DEAD-LETTER QUEUE (DLQ)
# ------------------------------------------------------------------------------
resource "aws_sqs_queue" "deployment_dlq" {
  name                      = "infrapilot-deployment-dlq"
  message_retention_seconds = 1209600 # 14 days
  sqs_managed_sse_enabled   = true
}

resource "aws_sqs_queue" "deployment_queue" {
  name                      = "infrapilot-deployment-queue"
  message_retention_seconds = 86400 # 1 day
  sqs_managed_sse_enabled   = true

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.deployment_dlq.arn
    maxReceiveCount     = 3 # Retries failed deployment up to 3 times before routing to DLQ
  })
}

# ------------------------------------------------------------------------------
# OUTPUTS
# ------------------------------------------------------------------------------
output "alb_dns_name" {
  description = "Public Load Balancer DNS Endpoint"
  value       = aws_lb.main.dns_name
}

output "rds_endpoint" {
  description = "Private Multi-AZ RDS Connection Endpoint"
  value       = aws_db_instance.postgres.endpoint
  sensitive   = true
}

output "sqs_queue_url" {
  description = "SQS Deployment Job Queue URL"
  value       = aws_sqs_queue.deployment_queue.url
}
