locals {
  name = "${var.app_name}-${var.environment}"

  allowed_instance_types = {
    dev     = ["t3.micro", "t3.small"]
    staging = ["t3.small", "t3.medium"]
    prod    = ["t3.medium", "t3.large", "m5.large"]
  }

  common_tags = {
    org_id        = var.org_id
    project_id    = var.project_id
    environment   = var.environment
    deployment_id = var.deployment_id
    managed_by    = "infrapilot"
  }
}

resource "terraform_data" "validate_instance_size" {
  input = {
    environment   = var.environment
    instance_type = var.instance_type
  }

  lifecycle {
    precondition {
      condition     = contains(local.allowed_instance_types[var.environment], var.instance_type)
      error_message = "instance_type is outside the size allowlist for the selected environment."
    }
  }
}

module "vpc" {
  source             = "../../modules/vpc"
  name               = local.name
  vpc_cidr           = var.vpc_cidr
  enable_nat_gateway = var.enable_nat_gateway
  tags               = local.common_tags
}

module "ecr" {
  source = "../../modules/ecr"
  name   = "${local.name}-app"
  tags   = local.common_tags
}

module "alb" {
  source            = "../../modules/alb"
  name              = local.name
  vpc_id            = module.vpc.vpc_id
  public_subnet_ids = module.vpc.public_subnet_ids
  target_port       = var.app_port
  certificate_arn   = var.certificate_arn
  tags              = local.common_tags
}

module "ec2" {
  source                    = "../../modules/ec2"
  name                      = local.name
  vpc_id                    = module.vpc.vpc_id
  private_subnet_ids        = module.vpc.private_subnet_ids
  alb_security_group_id     = module.alb.security_group_id
  target_group_arn          = module.alb.target_group_arn
  ecr_repository_arn        = module.ecr.repository_arn
  app_image                 = var.app_image_tag == "" ? "" : "${module.ecr.repository_url}:${var.app_image_tag}"
  app_port                  = var.app_port
  aws_region                = var.region
  instance_type             = var.instance_type
  cloudwatch_log_group_name = "/${local.name}/application"
  tags                      = local.common_tags

  depends_on = [terraform_data.validate_instance_size]
}

module "rds" {
  source                        = "../../modules/rds"
  name                          = local.name
  vpc_id                        = module.vpc.vpc_id
  database_subnet_ids           = module.vpc.database_subnet_ids
  application_security_group_id = module.ec2.security_group_id
  environment                   = var.environment
  instance_class                = var.db_instance_class
  engine_version                = var.db_engine_version
  tags                          = local.common_tags
}

module "sqs" {
  source = "../../modules/sqs"
  name   = local.name
  tags   = local.common_tags
}
