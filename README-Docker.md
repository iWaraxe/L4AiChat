# Docker Containerization Guide

## Overview
This guide provides comprehensive instructions for running L4AiChat using Docker and Docker Compose. The containerization setup includes the application, database, caching, reverse proxy, and monitoring stack.

## Quick Start

### Prerequisites
- Docker Desktop installed and running
- OpenAI API key

### 1. Clone and Setup
```bash
git clone https://github.com/your-repo/L4AiChat.git
cd L4AiChat
git checkout containerization-docker
```

### 2. Configure Environment
```bash
cp .env.example .env
# Edit .env file and add your OpenAI API key
```

### 3. Run the Application
```bash
# Development mode with hot reload
./scripts/docker-run.sh dev

# Or production mode
./scripts/docker-run.sh prod
```

### 4. Access the Application
- **Application**: http://localhost:8080
- **Grafana Dashboard**: http://localhost:3000 (admin/admin123)
- **Prometheus Metrics**: http://localhost:9090

## Architecture

### Services Overview
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Nginx       │    │   L4AiChat      │    │   PostgreSQL    │
│  Reverse Proxy  │────│   Application   │────│    Database     │
│   Port: 80/443  │    │   Port: 8080    │    │   Port: 5432    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │              ┌─────────────────┐              │
         │              │      Redis      │              │
         └──────────────│      Cache      │──────────────┘
                        │   Port: 6379    │
                        └─────────────────┘
                                 │
                        ┌─────────────────┐    ┌─────────────────┐
                        │   Prometheus    │    │     Grafana     │
                        │   Monitoring    │────│   Dashboards    │
                        │   Port: 9090    │    │   Port: 3000    │
                        └─────────────────┘    └─────────────────┘
```

### Container Details

#### L4AiChat Application
- **Image**: Built from Dockerfile (multi-stage build)
- **Port**: 8080
- **Features**: Spring Boot application with all AI modules
- **Health Check**: `/api/health`
- **Profiles**: `docker,jdbc`

#### PostgreSQL Database
- **Image**: postgres:15-alpine
- **Port**: 5432
- **Database**: l4aichat
- **User**: l4aichat
- **Volume**: Persistent data storage

#### Redis Cache
- **Image**: redis:7-alpine
- **Port**: 6379
- **Features**: Persistence enabled, password protected
- **Volume**: Persistent cache storage

#### Nginx Reverse Proxy
- **Image**: nginx:alpine
- **Ports**: 80 (HTTP), 443 (HTTPS)
- **Features**: Load balancing, SSL termination, static files
- **Configuration**: Custom nginx.conf

#### Prometheus Monitoring
- **Image**: prom/prometheus:latest
- **Port**: 9090
- **Features**: Metrics collection from application
- **Configuration**: Custom prometheus.yml

#### Grafana Dashboards
- **Image**: grafana/grafana:latest
- **Port**: 3000
- **Features**: Pre-configured dashboards
- **Credentials**: admin/admin123

## Docker Configuration Files

### Dockerfile
Multi-stage build optimized for Spring Boot:
```dockerfile
# Builder stage - compiles the application
FROM eclipse-temurin:21-jdk-alpine AS builder
# ... build process

# Production stage - runs the application
FROM eclipse-temurin:21-jre-alpine AS production
# ... runtime configuration
```

### docker-compose.yml
Production-ready orchestration:
- Service dependencies and health checks
- Volume management for persistence
- Network isolation
- Environment variable configuration

### docker-compose.override.yml
Development enhancements:
- Hot reload support
- Debug port exposure
- Volume mounting for live development
- Development database configuration

## Environment Variables

### Required Variables
```bash
# OpenAI API Key (required)
OPENAI_API_KEY=your_openai_api_key

# JWT Secret (optional, has default)
JWT_SECRET_KEY=your_jwt_secret_key
```

### Optional Variables
```bash
# Database Configuration
DB_HOST=postgres
DB_PORT=5432
DB_NAME=l4aichat
DB_USER=l4aichat
DB_PASSWORD=l4aichat123

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=redis123

# Application Configuration
SPRING_PROFILES_ACTIVE=docker,jdbc
JAVA_OPTS=-Xmx512m -Xms256m
SERVER_PORT=8080
```

## Scripts

### docker-build.sh
Builds the Docker image with version tagging:
```bash
./scripts/docker-build.sh
# Optionally push to registry
./scripts/docker-build.sh --push
```

### docker-run.sh
Comprehensive runner script:
```bash
# Development mode
./scripts/docker-run.sh dev

# Production mode
./scripts/docker-run.sh prod

# Run tests
./scripts/docker-run.sh test

# View logs
./scripts/docker-run.sh logs

# Clean up
./scripts/docker-run.sh clean
```

## Development Workflow

### Local Development
1. **Start Services**:
   ```bash
   ./scripts/docker-run.sh dev
   ```

2. **Code Changes**: 
   - Application auto-reloads on source changes
   - Database schema updates automatically

3. **Debugging**:
   - Debug port exposed on 5005
   - Connect IDE debugger to localhost:5005

4. **Testing**:
   ```bash
   # Run tests in container
   ./scripts/docker-run.sh test
   
   # Or run locally against containerized services
   ./mvnw test -Dspring.profiles.active=test
   ```

### Production Deployment
1. **Build and Deploy**:
   ```bash
   ./scripts/docker-build.sh
   ./scripts/docker-run.sh prod
   ```

2. **Monitor**:
   - Check application health: http://localhost:8080/api/health
   - View metrics: http://localhost:9090
   - Monitor dashboards: http://localhost:3000

3. **Scale** (if needed):
   ```bash
   docker-compose up -d --scale l4aichat-app=3
   ```

## Volumes and Persistence

### Persistent Data
- **postgres_data**: PostgreSQL database files
- **redis_data**: Redis cache files
- **prometheus_data**: Prometheus metrics history
- **grafana_data**: Grafana dashboards and settings

### Backup and Restore
```bash
# Backup database
docker-compose exec postgres pg_dump -U l4aichat l4aichat > backup.sql

# Restore database
docker-compose exec -T postgres psql -U l4aichat -d l4aichat < backup.sql

# Backup volumes
docker run --rm -v l4aichat_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres_backup.tar.gz -C /data .
```

## Monitoring and Observability

### Health Checks
All services include health checks:
- Application: HTTP health endpoint
- PostgreSQL: pg_isready check
- Redis: Redis ping command
- Nginx: Process check

### Metrics Collection
- **Application Metrics**: Exposed via Spring Boot Actuator
- **Database Metrics**: PostgreSQL exporter (optional)
- **Cache Metrics**: Redis exporter (optional)
- **Infrastructure Metrics**: Node exporter (optional)

### Log Management
```bash
# View all logs
docker-compose logs

# Follow application logs
docker-compose logs -f l4aichat-app

# View specific service logs
docker-compose logs postgres redis nginx
```

## Security Considerations

### Container Security
- **Non-root user**: Application runs as non-privileged user
- **Minimal base images**: Alpine Linux for smaller attack surface
- **Security scanning**: Regularly scan images for vulnerabilities

### Network Security
- **Internal networking**: Services communicate via Docker network
- **SSL/TLS**: HTTPS configuration available for production
- **Secrets management**: Use Docker secrets or external secret managers

### Data Security
- **Encrypted storage**: Consider encrypted volumes for production
- **Access control**: Database and Redis password protection
- **JWT secrets**: Use strong, randomly generated secrets

## Troubleshooting

### Common Issues

#### Container Won't Start
```bash
# Check logs
docker-compose logs l4aichat-app

# Check container status
docker-compose ps

# Verify environment variables
docker-compose config
```

#### Database Connection Issues
```bash
# Test database connectivity
docker-compose exec l4aichat-app nc -zv postgres 5432

# Check database logs
docker-compose logs postgres

# Reset database
docker-compose down -v
docker-compose up postgres
```

#### Memory Issues
```bash
# Increase memory limits in docker-compose.yml
services:
  l4aichat-app:
    environment:
      - JAVA_OPTS=-Xmx1g -Xms512m
```

#### Performance Issues
```bash
# Monitor resource usage
docker stats

# Check application metrics
curl http://localhost:8080/actuator/metrics

# View detailed monitoring
# Open Grafana at http://localhost:3000
```

### Debugging Tips
1. **Use docker-compose logs -f** for real-time log monitoring
2. **Check health endpoints** before assuming service issues
3. **Verify environment variables** with docker-compose config
4. **Test individual services** by bringing them up separately
5. **Use docker exec** to access container shells for debugging

## Production Deployment

### Preparation Checklist
- [ ] Set strong JWT secret key
- [ ] Configure proper database credentials
- [ ] Set up SSL certificates for HTTPS
- [ ] Configure backup strategy
- [ ] Set up monitoring alerts
- [ ] Review security settings
- [ ] Test disaster recovery procedures

### Scaling Considerations
- **Horizontal scaling**: Multiple application instances behind load balancer
- **Database**: Consider read replicas for high traffic
- **Caching**: Redis cluster for distributed caching
- **Storage**: External volumes for container orchestration platforms

### Integration with CI/CD
```yaml
# Example GitHub Actions workflow
- name: Build and Push Docker Image
  run: |
    docker build -t ${{ secrets.REGISTRY }}/l4aichat:${{ github.sha }} .
    docker push ${{ secrets.REGISTRY }}/l4aichat:${{ github.sha }}

- name: Deploy to Production
  run: |
    docker-compose -f docker-compose.prod.yml up -d
```

This Docker containerization provides a complete, production-ready deployment solution for the L4AiChat Spring AI educational platform!