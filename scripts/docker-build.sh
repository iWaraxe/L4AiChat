#!/bin/bash

# Docker build script for L4AiChat
# This script builds the Docker image for the application

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 Building L4AiChat Docker Image${NC}"

# Get the version from pom.xml
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
echo -e "${YELLOW}📦 Version: ${VERSION}${NC}"

# Build the Docker image
echo -e "${YELLOW}🔨 Building Docker image...${NC}"
docker build -t l4aichat:${VERSION} -t l4aichat:latest .

# Check if build was successful
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Docker image built successfully!${NC}"
    echo -e "${YELLOW}📋 Available images:${NC}"
    docker images | grep l4aichat
else
    echo -e "${RED}❌ Docker build failed!${NC}"
    exit 1
fi

# Optional: Push to registry
if [ "$1" = "--push" ]; then
    echo -e "${YELLOW}📤 Pushing to registry...${NC}"
    # Add your registry push commands here
    # docker tag l4aichat:${VERSION} your-registry.com/l4aichat:${VERSION}
    # docker push your-registry.com/l4aichat:${VERSION}
fi

echo -e "${GREEN}🎉 Build complete!${NC}"