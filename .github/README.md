# GitHub CI/CD Configuration

This directory contains GitHub Actions workflows and configuration for the L4AiChat Spring AI Course project.

## Workflows Overview

### 🔄 Continuous Integration (`ci.yml`)
Runs on every push and pull request to main branches.

**Jobs:**
- **Test**: Unit and integration tests with PostgreSQL and Redis
- **Code Quality**: SpotBugs, PMD, Checkstyle analysis
- **Security Scan**: OWASP dependency vulnerability check
- **Build**: Maven package creation
- **Docker Build**: Container image testing

**Triggers:**
- Push to `main`, `master`, `develop`, `s6-advanced-interaction`
- Pull requests to main branches

### 🚀 Continuous Deployment (`cd.yml`)
Handles deployment to staging and production environments.

**Jobs:**
- **Deploy Staging**: Automatic deployment on main branch
- **Deploy Production**: Deployment on tags/releases
- **Rollback**: Automatic rollback on deployment failure
- **Notifications**: Teams/Slack integration

**Triggers:**
- Push to `main`/`master` (staging)
- Tags starting with `v*` (production)
- Release publication

### 📦 Release Management (`release.yml`)
Manages version releases and artifact distribution.

**Jobs:**
- **Validate Release**: Version format validation
- **Build Release**: JAR, source, and distribution archives
- **GitHub Release**: Automated release creation
- **Docker Images**: Multi-platform container builds

**Triggers:**
- Tags matching `v*` pattern
- Manual workflow dispatch

### 🔍 Dependency Updates (`dependency-update.yml`)
Automated dependency monitoring and security auditing.

**Jobs:**
- **Dependency Update**: Weekly dependency reports
- **Renovate Config**: Automated dependency PRs
- **Security Audit**: OWASP vulnerability scanning

**Schedule:**
- Weekly on Mondays at 9 AM UTC
- Manual trigger available

## Issue Templates

### 🐛 Bug Report (`bug_report.yml`)
Structured bug reporting with:
- Version and module information
- Environment details
- Reproduction steps
- Log collection
- Configuration details

### ✨ Feature Request (`feature_request.yml`)
Comprehensive feature proposals including:
- Problem statement
- Proposed solution
- Use cases and examples
- Educational value assessment
- Implementation willingness

## Pull Request Template
Standardized PR format ensuring:
- Clear change description
- Type of change classification
- Testing verification
- Quality checklist
- Educational value assessment
- Security considerations

## Code Owners
Defines review requirements:
- Global owners for all changes
- Specialized teams for security/DevOps
- Module-specific ownership
- Documentation review assignments

## Configuration Files

### Environment Setup
```bash
# Required secrets in GitHub repository
OPENAI_API_KEY           # OpenAI API access
GITHUB_TOKEN            # Automatic (provided by GitHub)
DOCKERHUB_USERNAME      # Docker Hub publishing (optional)
DOCKERHUB_TOKEN         # Docker Hub publishing (optional)
SONAR_TOKEN            # SonarCloud integration (optional)
TEAMS_WEBHOOK          # Microsoft Teams notifications (optional)
SLACK_WEBHOOK          # Slack notifications (optional)
```

### Branch Protection Rules
Recommended branch protection for `main`:
- Require pull request reviews
- Require status checks (CI workflow)
- Require up-to-date branches
- Include administrators
- Restrict pushes to selected actors

### Deployment Environments
Configure GitHub environments:

**Staging:**
- Environment name: `staging`
- Required reviewers: 1
- Wait timer: 0 minutes
- Environment secrets: staging-specific configurations

**Production:**
- Environment name: `production`
- Required reviewers: 2
- Wait timer: 5 minutes
- Environment secrets: production configurations

## Usage Guide

### For Contributors
1. **Creating Issues**: Use appropriate templates for bugs or features
2. **Pull Requests**: Follow the PR template checklist
3. **Code Review**: Address all automated checks and reviewer feedback
4. **Testing**: Ensure all CI checks pass before merging

### For Maintainers
1. **Release Process**:
   ```bash
   # Create and push a new tag
   git tag v1.0.0
   git push origin v1.0.0
   
   # Or use GitHub UI to create release
   ```

2. **Managing Dependencies**:
   - Review weekly dependency update issues
   - Address security vulnerabilities promptly
   - Test dependency updates in feature branches

3. **Monitoring**:
   - Check CI/CD workflow health
   - Review security scan results
   - Monitor deployment success rates

### For Educators
1. **Student Workflow**:
   - Fork repository
   - Create feature branches
   - Submit PRs following templates
   - Learn from automated feedback

2. **Course Integration**:
   - Use workflows as DevOps examples
   - Demonstrate CI/CD best practices
   - Show security scanning importance
   - Explain release management

## Troubleshooting

### Common CI Issues
1. **Test Failures**: Check logs in Actions tab
2. **Security Vulnerabilities**: Review OWASP reports
3. **Build Failures**: Verify Maven configuration
4. **Docker Issues**: Check Dockerfile and compose files

### Workflow Debugging
```bash
# Local testing of workflows
act -j test  # Requires act CLI tool

# Debug specific jobs
act -j test --verbose
```

### Security Considerations
- Never commit secrets to repository
- Use GitHub secrets for sensitive data
- Regularly rotate API keys and tokens
- Monitor security alerts and advisories

## Best Practices

### Workflow Design
- Keep jobs focused and atomic
- Use appropriate caching strategies
- Implement proper error handling
- Include comprehensive logging

### Security
- Scan dependencies regularly
- Validate all inputs
- Use minimal required permissions
- Implement secret scanning

### Documentation
- Keep workflows well-documented
- Update templates as project evolves
- Provide clear usage instructions
- Include troubleshooting guides

This CI/CD setup provides a production-ready foundation for the L4AiChat educational project while demonstrating modern DevOps practices to students!