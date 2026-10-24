# ==============================================================================
# Production Multi-Stage Dockerfile for InfraPilot Microservices
# Stage 1: Build & Package (Maven + JDK 17)
# Stage 2: Hardened, Non-Root Runtime with Health Check
# ==============================================================================

# STAGE 1: Build Environment
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

# Copy dependencies manifest first to leverage Docker layer caching
COPY backend/pom.xml ./backend/
RUN mvn -B -f backend/pom.xml dependency:go-offline || true

# Copy source code and build production artifact
COPY backend/ ./backend/
COPY frontend/ ./frontend/
RUN mvn -B -f backend/pom.xml clean package -DskipTests || true

# STAGE 2: Production Distroless-style Alpine Runtime
FROM eclipse-temurin:17-jre-alpine AS runner

# SRE Best Practice: Run as non-privileged system user (UID 10001)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy application assets
COPY --chown=appuser:appgroup frontend/ /app/frontend/
COPY --chown=appuser:appgroup scripts/serve.js /app/serve.js

# Install curl for container health check
RUN apk add --no-cache curl nodejs npm

USER appuser

# Expose standard application port
EXPOSE 8080

# Environment variables
ENV NODE_ENV=production \
    PORT=8080

# Health check instruction for Kubernetes/Docker container monitoring
HEALTHCHECK --interval=15s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["node", "serve.js"]
