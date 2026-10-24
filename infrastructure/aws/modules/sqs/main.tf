resource "aws_sqs_queue" "dead_letter" {
  name                      = "${var.name}-dead-letter"
  message_retention_seconds = 1209600
  sqs_managed_sse_enabled   = true
  tags                      = merge(var.tags, { Name = "${var.name}-dead-letter" })
}

resource "aws_sqs_queue" "deployment_requests" {
  name                       = "${var.name}-deployment-requests"
  visibility_timeout_seconds = 300
  message_retention_seconds  = 345600
  sqs_managed_sse_enabled    = true
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dead_letter.arn
    maxReceiveCount     = 5
  })
  tags = merge(var.tags, { Name = "${var.name}-deployment-requests" })
}

resource "aws_sqs_queue" "pipeline_events" {
  name                       = "${var.name}-pipeline-events"
  visibility_timeout_seconds = 300
  message_retention_seconds  = 345600
  sqs_managed_sse_enabled    = true
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dead_letter.arn
    maxReceiveCount     = 5
  })
  tags = merge(var.tags, { Name = "${var.name}-pipeline-events" })
}
