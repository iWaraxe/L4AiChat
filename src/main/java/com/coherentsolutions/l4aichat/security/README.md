# Security JWT Auth Module

## Overview
This module implements comprehensive JWT-based authentication and authorization using Spring Security. It provides user registration, login, token refresh, and role-based access control (RBAC) for securing AI chat endpoints and administrative functions.

## Key Features

### 1. JWT Authentication
- **Token Generation**: Secure JWT tokens with configurable expiration
- **Token Validation**: Comprehensive token validation and parsing
- **Refresh Tokens**: Long-lived refresh tokens for seamless user experience
- **Token Blacklisting**: Security mechanism to invalidate compromised tokens

### 2. User Management
- **User Registration**: Secure user registration with validation
- **Password Encryption**: BCrypt password hashing
- **Role-Based Access**: USER, ADMIN, MODERATOR roles
- **Account Management**: Account status controls (enabled, locked, expired)

### 3. Security Configuration
- **CORS Support**: Configured for frontend integration
- **CSRF Protection**: Disabled for stateless JWT authentication
- **Method Security**: Annotation-based security with @PreAuthorize
- **Public Endpoints**: Health checks and authentication endpoints

## API Endpoints

### Authentication Endpoints

#### User Registration
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securepassword123",
  "roles": ["USER"]
}

Response:
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 86400000,
  "token_type": "Bearer",
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["USER"]
}
```

#### User Login
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "securepassword123"
}

Response: Same as registration response
```

#### Token Refresh
```
POST /api/auth/refresh
Authorization: Bearer <refresh_token>

Response: New access and refresh tokens
```

#### Token Validation
```
GET /api/auth/validate
Authorization: Bearer <access_token>

Response: "Token is valid" or error
```

### Protected Endpoints

#### Security Health Check
```
GET /api/security/health
Authorization: Bearer <access_token>

Response:
{
  "status": "UP",
  "module": "Security-JWT-Auth",
  "timestamp": "2024-01-01T12:00:00",
  "details": {
    "features": "JWT Authentication and Authorization",
    "endpoints": "/api/auth/*, /api/security/*",
    "security": "Spring Security with JWT",
    "authenticated": true,
    "principal": "johndoe",
    "authorities": "[ROLE_USER]"
  }
}
```

#### User Profile
```
GET /api/security/profile
Authorization: Bearer <access_token>

Response:
{
  "username": "johndoe",
  "authorities": ["ROLE_USER"],
  "authenticated": true,
  "details": {...}
}
```

#### User-Only Endpoint
```
GET /api/security/user
Authorization: Bearer <access_token>
Requires: ROLE_USER

Response:
{
  "message": "Hello User!",
  "user": "johndoe",
  "authorities": ["ROLE_USER"],
  "timestamp": 1704110400000
}
```

#### Admin-Only Endpoint
```
GET /api/security/admin
Authorization: Bearer <access_token>
Requires: ROLE_ADMIN

Response:
{
  "message": "Hello Admin!",
  "user": "admin",
  "authorities": ["ROLE_ADMIN"],
  "timestamp": 1704110400000,
  "adminFeatures": "User management, System configuration, Security settings"
}
```

## Data Models

### User Entity
```java
@Entity
@Table(name = "users")
public class User implements UserDetails {
    private Long id;
    private String username;
    private String email;
    private String password;
    private Boolean enabled;
    private Boolean accountNonExpired;
    private Boolean accountNonLocked;
    private Boolean credentialsNonExpired;
    private Set<Role> roles;
    
    public enum Role {
        USER, ADMIN, MODERATOR
    }
}
```

### Authentication DTOs
```java
// Registration Request
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private Set<User.Role> roles;
}

// Authentication Request
public class AuthenticationRequest {
    private String username;
    private String password;
}

// Authentication Response
public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType;
    private String username;
    private String email;
    private Set<User.Role> roles;
}
```

## Security Configuration

### JWT Configuration
```properties
# JWT Settings
jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000          # 24 hours
jwt.refresh-token.expiration=604800000  # 7 days
```

### Database Configuration
```properties
# For development with H2
spring.datasource.url=jdbc:h2:mem:securitydb
spring.datasource.username=sa
spring.datasource.password=

# For production with PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/l4aichat
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

## Security Architecture

### JWT Filter Chain
1. **JwtAuthenticationFilter**: Extracts and validates JWT tokens
2. **Authentication Provider**: Validates user credentials
3. **User Details Service**: Loads user information from database
4. **Security Context**: Maintains authentication state

### Password Security
- **BCrypt Hashing**: Industry-standard password hashing
- **Salt Generation**: Automatic salt generation for each password
- **Strength Configuration**: Configurable BCrypt rounds

### Token Security
- **HMAC SHA-256**: Secure token signing algorithm
- **Secret Key**: Base64-encoded secret key for token signing
- **Expiration**: Configurable token expiration times
- **Claims**: Custom claims for user roles and authorities

## Integration with AI Modules

### Secured Chat Endpoints
All existing chat endpoints can be secured by adding authentication:

```java
@RestController
@RequestMapping("/api/s1/chat")
@PreAuthorize("hasRole('USER')")
public class SecuredChatController {
    // Existing chat functionality with security
}
```

### User Context in AI Responses
```java
@Service
public class SecuredChatService {
    
    public String processSecuredChat(String message) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // Include user context in AI prompts
        String contextualPrompt = String.format(
            "User %s asks: %s", username, message
        );
        
        return chatClient.prompt()
            .user(contextualPrompt)
            .call()
            .content();
    }
}
```

## Testing

### Unit Tests
```java
@SpringBootTest
@AutoConfigureTestDatabase
class AuthenticationServiceTest {
    
    @Test
    void shouldRegisterUserSuccessfully() {
        // Test user registration
    }
    
    @Test
    void shouldAuthenticateUserSuccessfully() {
        // Test user login
    }
    
    @Test
    void shouldRefreshTokenSuccessfully() {
        // Test token refresh
    }
}
```

### Integration Tests
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {
    
    @Test
    void shouldRegisterAndLoginUser() {
        // Test complete registration and login flow
    }
    
    @Test
    void shouldProtectSecuredEndpoints() {
        // Test endpoint protection
    }
}
```

## Security Best Practices

### 1. Token Management
- **Short-lived Access Tokens**: 24-hour expiration by default
- **Long-lived Refresh Tokens**: 7-day expiration for convenience
- **Secure Storage**: Tokens should be stored securely on client side
- **Token Rotation**: Refresh tokens are rotated on each refresh

### 2. Password Policy
- **Minimum Length**: 6 characters minimum
- **Complexity**: Consider implementing complexity requirements
- **Hashing**: BCrypt with appropriate cost factor
- **Validation**: Server-side validation for all inputs

### 3. CORS Configuration
- **Origin Control**: Configure allowed origins in production
- **Credential Support**: Enable credentials for authenticated requests
- **Method Restrictions**: Limit allowed HTTP methods
- **Header Validation**: Control allowed headers

### 4. Rate Limiting
Consider implementing rate limiting for:
- Authentication endpoints
- Password reset endpoints
- Token refresh endpoints
- Failed login attempts

## Deployment Considerations

### Environment Variables
```bash
# Required for production
export JWT_SECRET_KEY=your_secure_random_key_here
export DB_USERNAME=your_db_username
export DB_PASSWORD=your_db_password
export CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

### Database Migration
```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    account_non_expired BOOLEAN DEFAULT true,
    account_non_locked BOOLEAN DEFAULT true,
    credentials_non_expired BOOLEAN DEFAULT true
);

-- User roles table
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id),
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role)
);
```

## Monitoring and Observability

### Security Events
- **Login Attempts**: Log successful and failed login attempts
- **Token Usage**: Monitor token generation and validation
- **Access Patterns**: Track endpoint access patterns
- **Security Violations**: Alert on security violations

### Metrics
- **Authentication Rate**: Successful/failed authentication ratio
- **Token Refresh Rate**: Frequency of token refresh requests
- **Endpoint Usage**: Usage patterns by role
- **Security Alerts**: Failed authentication attempts

## Learning Objectives

1. **JWT Implementation**: Understanding JWT token structure and validation
2. **Spring Security**: Configuring Spring Security for REST APIs
3. **Password Security**: Implementing secure password handling
4. **Role-Based Access**: Designing and implementing RBAC
5. **Security Testing**: Writing comprehensive security tests

## Next Steps

This security module prepares for:
- **Multi-tenancy**: Tenant-based access control
- **OAuth 2.0**: External identity provider integration
- **API Gateway**: Centralized authentication and authorization
- **Audit Logging**: Comprehensive security audit trails

## Common Issues

### JWT Token Issues
- **Clock Skew**: Ensure server time synchronization
- **Secret Key**: Use a strong, randomly generated secret key
- **Token Size**: Monitor token size for performance impact

### Database Issues
- **Connection Pooling**: Configure appropriate connection pools
- **Transaction Management**: Ensure proper transaction boundaries
- **Index Performance**: Create indexes on username and email columns

### CORS Issues
- **Preflight Requests**: Handle OPTIONS requests properly
- **Credential Headers**: Ensure credentials are included in requests
- **Origin Validation**: Validate origins in production environments