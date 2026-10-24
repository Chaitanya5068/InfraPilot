output "deployment_requests_url" {
  value = aws_sqs_queue.deployment_requests.url
}

output "pipeline_events_url" {
  value = aws_sqs_queue.pipeline_events.url
}

output "dead_letter_queue_arn" {
  value = aws_sqs_queue.dead_letter.arn
}
