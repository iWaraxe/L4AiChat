# Branch Migration Guide

## Overview
This guide provides comprehensive instructions for migrating between branches in the L4AiChat project's ultra-comprehensive development plan. The project follows a structured 4-phase, 22-branch approach to demonstrate progressive AI integration patterns.

## Branch Structure

### Phase 1: Foundation Restructuring (Completed)
- **s7-custom-advisors**: Advanced advisor patterns and configurations
- **s8-multi-model-ai**: Multiple OpenAI model support with comparison
- **s9-prompt-engineering**: Advanced prompt templating and dynamic generation

### Phase 2: Advanced Features (Planned)
- **security-jwt-auth**: Spring Security integration with JWT
- **monitoring-observability**: OpenTelemetry and metrics
- **caching-redis**: Redis integration for performance
- **rag-vector-db**: ChromaDB vector store for RAG
- **streaming-sse**: Server-Sent Events for real-time updates
- **batch-processing**: Spring Batch for bulk operations

### Phase 3: Production Infrastructure (Planned)
- **containerization-docker**: Docker support and containerization
- **cicd-github-actions**: Automated CI/CD workflows
- **cloud-deployment**: AWS/GCP deployment configurations
- **frontend-react**: React UI components and integration
- **api-gateway**: Spring Cloud Gateway
- **load-balancing**: Nginx configuration for scaling

### Phase 4: Enterprise Features (Planned)
- **multi-tenancy**: Tenant isolation and management
- **analytics-dashboard**: Usage metrics and dashboards
- **voice-integration**: Speech-to-text capabilities
- **function-calling**: OpenAI Functions integration
- **enterprise-security**: Advanced authentication and authorization
- **performance-optimization**: Caching and profiling

## Migration Instructions

### Basic Branch Operations

#### Switching Between Branches
```bash
# List all branches
git branch -a

# Switch to a specific branch
git checkout <branch-name>

# Create and switch to a new branch
git checkout -b <new-branch-name>
```

#### Merging Changes
```bash
# Merge changes from source branch to current branch
git merge <source-branch>

# Merge with no-fast-forward (preserves branch history)
git merge --no-ff <source-branch>
```

### Phase 1 Migrations

#### Working with Foundation Modules

**S7 Custom Advisors**
```bash
# Switch to s7 branch
git checkout s7-custom-advisors

# Verify module functionality
./mvnw test -Dtest=*S7*

# Test advisor patterns
curl -X POST http://localhost:8080/api/s7/advisors/basic \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello from S7"}'
```

**S8 Multi-Model AI**
```bash
# Switch to s8 branch
git checkout s8-multi-model-ai

# Test model comparison
curl -X POST http://localhost:8080/api/s8/models/compare \
  -H "Content-Type: application/json" \
  -d '{"message": "Compare AI models", "models": ["gpt-4", "gpt-3.5-turbo"]}'
```

**S9 Prompt Engineering**
```bash
# Switch to s9 branch
git checkout s9-prompt-engineering

# Test template processing
curl -X POST http://localhost:8080/api/s9/templates/process \
  -H "Content-Type: application/json" \
  -d '{"templateName": "code_generation", "variables": {"language": "Java"}, "userMessage": "Create a REST controller"}'
```

### Branch-Specific Configurations

#### Environment Variables
Each branch may require specific environment variables:

```bash
# Core requirement for all branches
export OPENAI_API_KEY=your_openai_api_key

# Phase 2 additional requirements
export REDIS_URL=redis://localhost:6379        # caching-redis
export CHROMA_DB_URL=http://localhost:8000     # rag-vector-db
export JWT_SECRET=your_jwt_secret              # security-jwt-auth

# Phase 3 requirements
export DOCKER_REGISTRY=your.registry.com      # containerization-docker
export AWS_ACCESS_KEY_ID=your_aws_key         # cloud-deployment
export GCP_PROJECT_ID=your_gcp_project        # cloud-deployment

# Phase 4 requirements
export TENANT_DB_URL=your_tenant_db_url       # multi-tenancy
export ANALYTICS_DB_URL=your_analytics_db     # analytics-dashboard
```

#### Database Migrations
Some branches require database schema changes:

```bash
# For branches with database changes
./mvnw liquibase:update

# Or for JDBC profile
./mvnw spring-boot:run -Dspring.profiles.active=jdbc
```

### Testing Migrations

#### Pre-Migration Checklist
- [ ] Backup current work: `git stash` or commit changes
- [ ] Verify branch exists: `git branch -a`
- [ ] Check environment variables
- [ ] Ensure dependencies are installed

#### Post-Migration Verification
```bash
# 1. Build the project
./mvnw clean compile

# 2. Run tests
./mvnw test

# 3. Start application
./mvnw spring-boot:run

# 4. Test health endpoints
curl http://localhost:8080/api/health
curl http://localhost:8080/api/s{n}/health

# 5. Test module-specific endpoints
# (See individual branch READMEs for specific tests)
```

### Common Migration Issues

#### Merge Conflicts
```bash
# When conflicts occur during merge
git status                    # See conflicted files
git mergetool                # Use merge tool
git add .                    # Stage resolved files
git commit                   # Complete merge
```

#### Dependency Conflicts
```bash
# Clean and rebuild when dependencies change
./mvnw clean
./mvnw dependency:purge-local-repository
./mvnw compile
```

#### Configuration Issues
```bash
# Reset application properties if needed
git checkout HEAD -- src/main/resources/application.properties
```

### Branch-Specific Features

#### S7 Custom Advisors
- **Key Features**: MessageChatMemoryAdvisor, context-aware chat
- **Endpoints**: `/api/s7/advisors/*`
- **Testing**: Memory conversation flows
- **Configuration**: In-memory vs JDBC chat storage

#### S8 Multi-Model AI
- **Key Features**: Parallel model processing, performance comparison
- **Endpoints**: `/api/s8/models/*`
- **Testing**: Model comparison and fastest response
- **Configuration**: Model-specific temperature and token limits

#### S9 Prompt Engineering
- **Key Features**: Template-based prompting, dynamic generation
- **Endpoints**: `/api/s9/templates/*`
- **Testing**: Template processing and specialized content
- **Configuration**: Template library and variable substitution

### Development Workflow

#### Feature Development
```bash
# 1. Create feature branch from appropriate phase branch
git checkout <phase-branch>
git checkout -b feature/<feature-name>

# 2. Develop feature
# ... make changes ...

# 3. Test feature
./mvnw test

# 4. Commit changes
git add .
git commit -m "feat: implement <feature-name>"

# 5. Merge back to phase branch
git checkout <phase-branch>
git merge feature/<feature-name>
```

#### Integration Testing
```bash
# Test integration between modules
git checkout s6-advanced-interaction  # Main integration branch
git merge s7-custom-advisors
git merge s8-multi-model-ai
git merge s9-prompt-engineering

# Run comprehensive tests
./mvnw test
```

### Branch Maintenance

#### Keeping Branches Updated
```bash
# Update all branches with latest changes
git checkout main
git pull origin main

# Update each branch
for branch in s7-custom-advisors s8-multi-model-ai s9-prompt-engineering; do
  git checkout $branch
  git merge main
done
```

#### Cleaning Up
```bash
# Remove merged feature branches
git branch -d feature/<feature-name>

# Remove remote tracking branches
git remote prune origin
```

### Documentation Updates

#### Branch-Specific Documentation
Each branch maintains its own README with:
- Module overview and features
- API endpoints and examples
- Configuration requirements
- Testing instructions
- Learning objectives

#### Central Documentation
- **CLAUDE.md**: Project status and configuration
- **BRANCH_MIGRATION_GUIDE.md**: This guide
- **docs/**: Architecture decisions and guides

### Troubleshooting

#### Common Issues
1. **Build Failures**: Check Java version and dependencies
2. **Test Failures**: Verify environment variables and API keys
3. **Port Conflicts**: Ensure ports 8080, 8000, 6379 are available
4. **Memory Issues**: Increase JVM heap size: `-Xmx2g`

#### Getting Help
```bash
# Check branch status
git status

# View recent commits
git log --oneline -10

# Check application logs
./mvnw spring-boot:run | grep ERROR

# Test specific module
./mvnw test -Dtest=*S7* -Dspring.profiles.active=test
```

## Best Practices

### Version Control
- Commit frequently with descriptive messages
- Use feature branches for experimental work
- Tag stable versions for easy rollback
- Keep branch history clean with meaningful commits

### Testing
- Run tests before switching branches
- Test module integration after merges
- Verify health endpoints after deployment
- Use appropriate test profiles

### Configuration Management
- Use environment variables for sensitive data
- Maintain branch-specific configuration files
- Document configuration changes
- Use profiles for different environments

### Performance
- Monitor memory usage with multiple modules
- Use appropriate connection pooling
- Implement caching where beneficial
- Profile application performance

This guide ensures smooth transitions between branches while maintaining the educational value and architectural integrity of the Spring AI course project.