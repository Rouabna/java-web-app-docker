#!/bin/bash

# Deployment script for Java Web App
# This script is executed on the deployment server

set -e

DOCKER_IMAGE="rouabna/java-web-app"
CONTAINER_NAME="java-web-app"

echo "=== Starting Deployment ==="

# Pull latest image
echo "Pulling latest image from DockerHub..."
docker pull ${DOCKER_IMAGE}:latest

# Stop and remove existing container
echo "Stopping existing container..."
docker stop ${CONTAINER_NAME} 2>/dev/null || true
docker rm ${CONTAINER_NAME} 2>/dev/null || true

# Remove old images (keep latest)
echo "Cleaning up old images..."
docker image prune -f

# Run new container
echo "Starting new container..."
docker run -d \
    --name ${CONTAINER_NAME} \
    --restart unless-stopped \
    -p 8080:8080 \
    ${DOCKER_IMAGE}:latest

# Verify container is running
echo "Verifying deployment..."
sleep 5
if docker ps | grep -q ${CONTAINER_NAME}; then
    echo "=== Deployment Successful ==="
    docker ps | grep ${CONTAINER_NAME}
else
    echo "=== Deployment Failed ==="
    exit 1
fi
