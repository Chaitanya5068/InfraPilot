output "public_ip" {
  description = "Public IP address of the deployed host"
  value       = aws_instance.host.public_ip
}

output "app_url" {
  description = "Live Web Application URL"
  value       = "http://${aws_instance.host.public_ip}"
}

output "grafana_url" {
  description = "Direct Grafana Dashboard URL"
  value       = "http://${aws_instance.host.public_ip}:3000"
}
