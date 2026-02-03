@echo off
REM Quick Start Script for E-Commerce Microservices

echo ================================================
echo E-Commerce Microservices - Quick Start
echo ================================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo X Error: Docker is not running. Please start Docker Desktop first.
    exit /b 1
)

echo OK Docker is running
echo.

REM Build backend services
echo Building backend services...
call mvnw.cmd clean package -DskipTests

if errorlevel 1 (
    echo X Error: Maven build failed
    exit /b 1
)

echo OK Backend services built successfully
echo.

REM Start services with Docker Compose
echo Starting all services with Docker Compose...
docker-compose up -d

if errorlevel 1 (
    echo X Error: Docker Compose failed to start services
    exit /b 1
)

echo.
echo OK All services started successfully!
echo.
echo ================================================
echo Service URLs:
echo ================================================
echo Frontend:         http://localhost
echo API Gateway:      http://localhost:8080
echo Catalogue:        http://localhost:8081
echo Panier:           http://localhost:8082
echo Paiment:          http://localhost:8083
echo Tracking:         http://localhost:8084
echo.
echo ================================================
echo Useful Commands:
echo ================================================
echo View logs:        docker-compose logs -f
echo Stop services:    docker-compose down
echo Restart:          docker-compose restart
echo.
