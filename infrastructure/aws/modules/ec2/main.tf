data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

resource "aws_security_group" "this" {
  name        = "${var.name}-app"
  description = "Application ingress from the ALB only."
  vpc_id      = var.vpc_id

  ingress {
    description     = "Application traffic from ALB"
    from_port       = var.app_port
    to_port         = var.app_port
    protocol        = "tcp"
    security_groups = [var.alb_security_group_id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, { Name = "${var.name}-app" })
}

resource "aws_iam_role" "instance" {
  name = "${var.name}-ec2"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Action    = "sts:AssumeRole"
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })

  tags = merge(var.tags, { Name = "${var.name}-ec2" })
}

resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.instance.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy" "ecr_pull" {
  name = "${var.name}-ecr-pull"
  role = aws_iam_role.instance.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["ecr:GetAuthorizationToken"]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage"
        ]
        Resource = var.ecr_repository_arn
      }
    ]
  })
}

resource "aws_cloudwatch_log_group" "application" {
  name              = var.cloudwatch_log_group_name
  retention_in_days = 30
  tags              = merge(var.tags, { Name = "${var.name}-application" })
}

resource "aws_iam_role_policy" "cloudwatch_logs" {
  name = "${var.name}-application-logs"
  role = aws_iam_role.instance.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ]
      Resource = "${aws_cloudwatch_log_group.application.arn}:*"
    }]
  })
}

resource "aws_iam_instance_profile" "this" {
  name = "${var.name}-ec2"
  role = aws_iam_role.instance.name
  tags = merge(var.tags, { Name = "${var.name}-ec2" })
}

resource "aws_launch_template" "this" {
  name_prefix            = "${var.name}-"
  image_id               = data.aws_ami.amazon_linux.id
  instance_type          = var.instance_type
  update_default_version = true
  vpc_security_group_ids = [aws_security_group.this.id]
  user_data = var.app_image == "" ? null : base64encode(templatefile("${path.module}/user_data.sh.tftpl", {
    aws_region           = var.aws_region
    app_image            = var.app_image
    app_port             = var.app_port
    cloudwatch_log_group = var.cloudwatch_log_group_name
  }))

  iam_instance_profile {
    name = aws_iam_instance_profile.this.name
  }

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
  }

  monitoring {
    enabled = true
  }

  tag_specifications {
    resource_type = "instance"
    tags          = merge(var.tags, { Name = "${var.name}-app" })
  }

  tag_specifications {
    resource_type = "volume"
    tags          = merge(var.tags, { Name = "${var.name}-app" })
  }

  tags = merge(var.tags, { Name = "${var.name}-launch-template" })
}

resource "aws_autoscaling_group" "this" {
  name                      = "${var.name}-app"
  min_size                  = var.app_image == "" ? 0 : 1
  desired_capacity          = var.app_image == "" ? 0 : 1
  max_size                  = 2
  vpc_zone_identifier       = var.private_subnet_ids
  target_group_arns         = [var.target_group_arn]
  health_check_type         = "ELB"
  health_check_grace_period = 600

  launch_template {
    id      = aws_launch_template.this.id
    version = aws_launch_template.this.latest_version
  }

  tag {
    key                 = "Name"
    value               = "${var.name}-app"
    propagate_at_launch = true
  }

  dynamic "tag" {
    for_each = var.tags
    content {
      key                 = tag.key
      value               = tag.value
      propagate_at_launch = true
    }
  }

  depends_on = [
    aws_iam_role_policy_attachment.ssm,
    aws_iam_role_policy.ecr_pull,
    aws_iam_role_policy.cloudwatch_logs
  ]
}
