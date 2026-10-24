#!/bin/bash
# ==============================================================================
# InfraPilot Local Environment Quickstart Script
# ==============================================================================
set -e

echo "🚀 Setting up InfraPilot v2.1 Local Environment..."

# 1. Ensure .env exists
if [ ! -f ".env" ]; then
    echo "📋 Copying .env.example to .env..."
    cp .env.example .env
fi

# 2. Start Core Infrastructure Services via Docker Compose
echo "🐳 Starting PostgreSQL, Prometheus, Grafana, Node-Exporter, cAdvisor, and Jenkins..."
docker compose up -d postgres prometheus grafana node-exporter cadvisor

echo "⏳ Waiting for PostgreSQL to be healthy..."
until docker compose exec -T postgres pg_isready -U infrapilot > /dev/null 2>&1; do
    sleep 2
done

echo "✅ Database is ready."
echo "🌐 Services online:"
echo "   - InfraPilot Dashboard: http://localhost:8080"
echo "   - InfraPilot API:       http://localhost:8085"
echo "   - Demo Task Tracker:    http://localhost:8081"
echo "   - Grafana Metrics:      http://localhost:3000"
echo "   - Prometheus Scrapes:   http://localhost:9090"
echo "   - Jenkins Orchestrator: http://localhost:8090"
echo "🎉 InfraPilot environment initialization complete!"
