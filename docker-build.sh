#!/bin/bash
# ===================================================================
# Petcare Docker Build Script
# ===================================================================
# Soluciona el problema de DNS en Docker daemon bridge network
# usando --network=host para el build
# ===================================================================

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
IMAGE_NAME="petcare-hackathonone--petcare"

echo "🔨 Building Petcare Docker image..."
echo "   Using --network=host to bypass Docker DNS issues..."

# Build con network host (evita problema de DNS)
docker build \
    --network=host \
    --tag "$IMAGE_NAME" \
    --file "$PROJECT_DIR/Dockerfile" \
    "$PROJECT_DIR"

echo ""
echo "✅ Build successful!"
echo ""
echo "📦 Starting containers with docker-compose..."
echo ""

# Levantar containers (sin rebuild porque ya está built)
docker compose up -d

echo ""
echo "🎉 Done! App should be running at http://localhost:8088"
echo "   Health check: http://localhost:8088/actuator/health"
echo "   Swagger UI: http://localhost:8088/swagger-ui/index.html"
