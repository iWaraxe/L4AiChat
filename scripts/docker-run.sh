#!/bin/bash

# Docker run script for L4AiChat
# This script provides various ways to run the application with Docker

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to display help
show_help() {
    echo -e "${GREEN}L4AiChat Docker Runner${NC}"
    echo ""
    echo "Usage: $0 [OPTION]"
    echo ""
    echo "Options:"
    echo "  dev         Run in development mode with hot reload"
    echo "  prod        Run in production mode"
    echo "  test        Run tests in Docker container"
    echo "  clean       Clean up Docker resources"
    echo "  logs        Show application logs"
    echo "  help        Show this help message"
    echo ""
}

# Function to check if .env file exists and create it if not
check_env() {
    if [ ! -f .env ]; then
        echo -e "${YELLOW}⚠️  .env file not found. Creating template...${NC}"
        cat > .env << EOF
# OpenAI API Key (required)
OPENAI_API_KEY=your_openai_api_key_here

# JWT Secret Key (optional, will use default if not set)
JWT_SECRET_KEY=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Database Configuration (optional for development)
DB_HOST=postgres
DB_PORT=5432
DB_NAME=l4aichat
DB_USER=l4aichat
DB_PASSWORD=l4aichat123

# Redis Configuration (optional)
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=redis123
EOF
        echo -e "${RED}⚠️  Please update .env file with your OpenAI API key before running!${NC}"
        return 1
    fi
    
    # Check if OpenAI API key is set
    if grep -q "your_openai_api_key_here" .env; then
        echo -e "${RED}⚠️  Please set your OpenAI API key in .env file!${NC}"
        return 1
    fi
    
    return 0
}

# Function to run in development mode
run_dev() {
    echo -e "${GREEN}🚀 Starting L4AiChat in Development Mode${NC}"
    
    if ! check_env; then
        exit 1
    fi
    
    # Build and run with override
    docker-compose -f docker-compose.yml -f docker-compose.override.yml up --build
}

# Function to run in production mode
run_prod() {
    echo -e "${GREEN}🚀 Starting L4AiChat in Production Mode${NC}"
    
    if ! check_env; then
        exit 1
    fi
    
    # Run production services
    docker-compose up -d
    
    echo -e "${GREEN}✅ Application started in production mode${NC}"
    echo -e "${BLUE}📊 Access points:${NC}"
    echo -e "  Application: http://localhost:8080"
    echo -e "  Grafana: http://localhost:3000 (admin/admin123)"
    echo -e "  Prometheus: http://localhost:9090"
    echo -e "${YELLOW}📝 Use 'docker-compose logs -f' to view logs${NC}"
}

# Function to run tests
run_test() {
    echo -e "${GREEN}🧪 Running Tests in Docker${NC}"
    
    # Build test image and run tests
    docker build --target builder -t l4aichat:test .
    docker run --rm l4aichat:test ./mvnw test
}

# Function to clean up Docker resources
clean_up() {
    echo -e "${YELLOW}🧹 Cleaning up Docker resources...${NC}"
    
    # Stop and remove containers
    docker-compose down -v
    
    # Remove images (optional)
    read -p "Remove L4AiChat Docker images? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker rmi $(docker images l4aichat -q) 2>/dev/null || true
        echo -e "${GREEN}✅ Images removed${NC}"
    fi
    
    echo -e "${GREEN}✅ Cleanup complete${NC}"
}

# Function to show logs
show_logs() {
    echo -e "${BLUE}📋 Showing application logs...${NC}"
    docker-compose logs -f l4aichat-app
}

# Main script logic
case "${1:-help}" in
    dev)
        run_dev
        ;;
    prod)
        run_prod
        ;;
    test)
        run_test
        ;;
    clean)
        clean_up
        ;;
    logs)
        show_logs
        ;;
    help|*)
        show_help
        ;;
esac