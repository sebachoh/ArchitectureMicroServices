#!/bin/bash

# Quick Start Script for E-Commerce Microservices
echo "================================================"
echo "E-Commerce Microservices - Quick Start"
echo "================================================"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
  echo "❌ Error: Docker is not running. Please start Docker Desktop first."
  exit 1
fi

echo "✅ Docker is running"
echo ""

# Build backend services
echo "📦 Building backend services..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
  echo "❌ Error: Maven build failed"
  exit 1
fi

echo "✅ Backend services built successfully"
echo ""

# Start services with Docker Compose
echo "🚀 Starting all services with Docker Compose..."
docker-compose up -d

if [ $? -ne 0 ]; then
  echo "❌ Error: Docker Compose failed to start services"
  exit 1
fi

echo ""
echo "✅ All services started successfully!"
echo ""
echo "================================================"
echo "Service URLs:"
echo "================================================"
echo "Frontend:         http://localhost"
echo "API Gateway:      http://localhost:8080"
echo "Catalogue:        http://localhost:8081"
echo "Panier:           http://localhost:8082"
echo "Paiment:          http://localhost:8083"
echo "Tracking:         http://localhost:8084"
echo ""
echo "================================================"
echo "Useful Commands:"
echo "================================================"
echo "View logs:        docker-compose logs -f"
echo "Stop services:    docker-compose down"
echo "Restart:          docker-compose restart"
echo ""
