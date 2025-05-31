# 🚀 Production Deployment Guide

## Overview

This guide covers everything you need to deploy Spring AI chat applications to production environments, including security hardening, monitoring setup, scaling strategies, and operational best practices.

## Table of Contents
1. [Pre-Production Checklist](#pre-production-checklist)
2. [Security Hardening](#security-hardening)
3. [Infrastructure Setup](#infrastructure-setup)
4. [Monitoring and Observability](#monitoring-and-observability)
5. [Scaling and Performance](#scaling-and-performance)
6. [Operational Procedures](#operational-procedures)
7. [Disaster Recovery](#disaster-recovery)

## Pre-Production Checklist

### Essential Requirements

Before deploying to production, ensure you have completed:

```yaml
# deployment-checklist.yml
security:
  - api_keys_secured: true
  - authentication_enabled: true
  - rate_limiting_configured: true
  - input_validation_enabled: true
  - cors_properly_configured: true
  - secrets_management_setup: true

performance:
  - load_testing_completed: true
  - memory_optimization_done: true
  - connection_pooling_configured: true
  - caching_strategy_implemented: true
  - timeout_values_tuned: true

monitoring:
  - metrics_collection_enabled: true
  - logging_configured: true
  - health_checks_implemented: true
  - alerting_setup: true
  - distributed_tracing_enabled: true

infrastructure:
  - database_backup_configured: true
  - ssl_certificates_valid: true
  - load_balancer_configured: true
  - auto_scaling_rules_defined: true
  - disaster_recovery_plan_created: true
```

### Configuration Management

#### Environment-Specific Configuration
```yaml
# application-prod.yml
spring:
  profiles:
    active: prod
  datasource:
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000

  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: ${OPENAI_BASE_URL:https://api.openai.com}
      chat:
        options:
          model: ${AI_MODEL:gpt-4-turbo}
          temperature: ${AI_TEMPERATURE:0.7}
          max-tokens: ${AI_MAX_TOKENS:2000}
          timeout: ${AI_TIMEOUT:60s}

server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /api
  compression:
    enabled: true
    mime-types: application/json,text/plain,text/html
  tomcat:
    threads:
      max: ${TOMCAT_MAX_THREADS:200}
      min-spare: ${TOMCAT_MIN_THREADS:10}
    connection-timeout: ${TOMCAT_CONNECTION_TIMEOUT:20000}
    max-connections: ${TOMCAT_MAX_CONNECTIONS:8192}

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus,info
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5,0.95,0.99

logging:
  level:
    com.coherentsolutions.l4aichat: ${LOG_LEVEL:INFO}
    org.springframework.ai: ${AI_LOG_LEVEL:INFO}
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/chatbot/application.log
    max-size: 100MB
    max-history: 30
```

#### Secrets Management
```java
@Configuration
@EnableConfigurationProperties(AISecurityProperties.class)
public class ProductionSecurityConfig {
    
    @Bean
    @ConditionalOnProperty("security.vault.enabled")
    public VaultTemplate vaultTemplate() {
        VaultEndpoint endpoint = new VaultEndpoint();
        endpoint.setHost(vaultHost);
        endpoint.setPort(vaultPort);
        endpoint.setScheme("https");
        
        ClientAuthentication clientAuth = new TokenAuthentication(vaultToken);
        return new VaultTemplate(endpoint, clientAuth);
    }
    
    @Bean
    public ChatClient secureChatClient(ChatClient.Builder builder, 
                                     @Value("${spring.ai.openai.api-key}") String apiKey) {
        
        // Validate API key format
        if (!isValidApiKey(apiKey)) {
            throw new IllegalStateException("Invalid OpenAI API key format");
        }
        
        return builder
            .defaultOptions(OpenAiChatOptions.builder()
                .withModel("gpt-4-turbo")
                .withTemperature(0.7)
                .build())
            .build();
    }
    
    private boolean isValidApiKey(String apiKey) {
        return apiKey != null && 
               !apiKey.startsWith("${") && 
               apiKey.startsWith("sk-") && 
               apiKey.length() > 40;
    }
}

@ConfigurationProperties(prefix = "security")
@Data
public class AISecurityProperties {
    private Vault vault = new Vault();
    private RateLimit rateLimit = new RateLimit();
    private Cors cors = new Cors();
    
    @Data
    public static class Vault {
        private boolean enabled = false;
        private String host = "localhost";
        private int port = 8200;
        private String token;
    }
    
    @Data
    public static class RateLimit {
        private int requestsPerMinute = 60;
        private int requestsPerHour = 1000;
        private boolean enabled = true;
    }
    
    @Data
    public static class Cors {
        private List<String> allowedOrigins = List.of("https://yourdomain.com");
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE");
        private boolean allowCredentials = true;
    }
}
```

## Security Hardening

### 1. Authentication and Authorization

#### JWT-Based Security
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ProductionSecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/prometheus").hasRole("MONITOR")
                .requestMatchers("/api/chat/**").authenticated()
                .anyRequest().denyAll())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .xssProtection().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true)))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler()));
        
        return http.build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
            .withJwkSetUri("https://your-auth-server/.well-known/jwks.json")
            .build();
        
        decoder.setJwtValidator(jwtValidator());
        return decoder;
    }
    
    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<String> authorities = jwt.getClaimAsStringList("authorities");
            return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        });
        return converter;
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("https://*.yourdomain.com"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
```

### 2. Rate Limiting and Throttling

#### Redis-Based Rate Limiting
```java
@Component
public class AIRateLimitingFilter implements Filter {
    private final RedisTemplate<String, String> redisTemplate;
    private final AISecurityProperties securityProperties;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientId = extractClientId(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        // Check rate limits
        RateLimitResult result = checkRateLimit(clientId, userAgent);
        
        if (!result.isAllowed()) {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(result.getLimit()));
            httpResponse.setHeader("X-RateLimit-Remaining", "0");
            httpResponse.setHeader("X-RateLimit-Reset", String.valueOf(result.getResetTime()));
            
            writeErrorResponse(httpResponse, "Rate limit exceeded");
            return;
        }
        
        // Add rate limit headers
        httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(result.getLimit()));
        httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
        httpResponse.setHeader("X-RateLimit-Reset", String.valueOf(result.getResetTime()));
        
        chain.doFilter(request, response);
    }
    
    private RateLimitResult checkRateLimit(String clientId, String userAgent) {
        String minuteKey = "rate_limit:minute:" + clientId;
        String hourKey = "rate_limit:hour:" + clientId;
        
        long currentTime = System.currentTimeMillis();
        long minuteWindow = currentTime / 60000; // 1-minute windows
        long hourWindow = currentTime / 3600000; // 1-hour windows
        
        // Check minute limit
        String minuteCount = redisTemplate.opsForValue().get(minuteKey + ":" + minuteWindow);
        int currentMinuteCount = minuteCount != null ? Integer.parseInt(minuteCount) : 0;
        
        if (currentMinuteCount >= securityProperties.getRateLimit().getRequestsPerMinute()) {
            return RateLimitResult.denied(
                securityProperties.getRateLimit().getRequestsPerMinute(),
                (minuteWindow + 1) * 60000
            );
        }
        
        // Check hour limit
        String hourCount = redisTemplate.opsForValue().get(hourKey + ":" + hourWindow);
        int currentHourCount = hourCount != null ? Integer.parseInt(hourCount) : 0;
        
        if (currentHourCount >= securityProperties.getRateLimit().getRequestsPerHour()) {
            return RateLimitResult.denied(
                securityProperties.getRateLimit().getRequestsPerHour(),
                (hourWindow + 1) * 3600000
            );
        }
        
        // Increment counters
        redisTemplate.opsForValue().increment(minuteKey + ":" + minuteWindow);
        redisTemplate.expire(minuteKey + ":" + minuteWindow, Duration.ofMinutes(2));
        
        redisTemplate.opsForValue().increment(hourKey + ":" + hourWindow);
        redisTemplate.expire(hourKey + ":" + hourWindow, Duration.ofHours(2));
        
        return RateLimitResult.allowed(
            securityProperties.getRateLimit().getRequestsPerMinute(),
            securityProperties.getRateLimit().getRequestsPerMinute() - currentMinuteCount - 1,
            (minuteWindow + 1) * 60000
        );
    }
    
    private String extractClientId(HttpServletRequest request) {
        // Try JWT sub claim first
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getSubject();
        }
        
        // Fallback to IP address
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        return request.getRemoteAddr();
    }
    
    @Data
    @AllArgsConstructor
    private static class RateLimitResult {
        private boolean allowed;
        private int limit;
        private int remaining;
        private long resetTime;
        
        public static RateLimitResult allowed(int limit, int remaining, long resetTime) {
            return new RateLimitResult(true, limit, remaining, resetTime);
        }
        
        public static RateLimitResult denied(int limit, long resetTime) {
            return new RateLimitResult(false, limit, 0, resetTime);
        }
    }
}
```

### 3. Input Validation and Sanitization

#### Comprehensive Input Validation
```java
@Component
public class AIInputValidator {
    private static final int MAX_MESSAGE_LENGTH = 4000;
    private static final int MAX_CONVERSATION_ID_LENGTH = 100;
    private static final Pattern CONVERSATION_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-_]+$");
    
    public ValidationResult validateChatRequest(ChatRequest request) {
        List<String> errors = new ArrayList<>();
        
        // Message validation
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            errors.add("Message cannot be empty");
        } else if (request.getMessage().length() > MAX_MESSAGE_LENGTH) {
            errors.add("Message too long (max " + MAX_MESSAGE_LENGTH + " characters)");
        } else if (containsProhibitedContent(request.getMessage())) {
            errors.add("Message contains prohibited content");
        }
        
        // Conversation ID validation
        if (request.getConversationId() != null) {
            if (request.getConversationId().length() > MAX_CONVERSATION_ID_LENGTH) {
                errors.add("Conversation ID too long");
            } else if (!CONVERSATION_ID_PATTERN.matcher(request.getConversationId()).matches()) {
                errors.add("Invalid conversation ID format");
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    public String sanitizeMessage(String message) {
        if (message == null) {
            return null;
        }
        
        // Remove potentially dangerous characters
        String sanitized = message
            .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "") // Control characters
            .replaceAll("<script[^>]*>.*?</script>", "") // Script tags
            .replaceAll("javascript:", "") // JavaScript protocols
            .trim();
        
        // Normalize whitespace
        sanitized = sanitized.replaceAll("\\s+", " ");
        
        return sanitized;
    }
    
    private boolean containsProhibitedContent(String message) {
        String lower = message.toLowerCase();
        
        // Check for common injection patterns
        if (lower.contains("drop table") || 
            lower.contains("delete from") || 
            lower.contains("union select") ||
            lower.contains("<script") ||
            lower.contains("javascript:")) {
            return true;
        }
        
        // Check for excessive repetition (potential DoS)
        if (isExcessiveRepetition(message)) {
            return true;
        }
        
        return false;
    }
    
    private boolean isExcessiveRepetition(String message) {
        // Check for repeated characters
        Pattern repeatedChar = Pattern.compile("(.)\\1{50,}");
        if (repeatedChar.matcher(message).find()) {
            return true;
        }
        
        // Check for repeated words
        String[] words = message.split("\\s+");
        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : words) {
            wordCount.merge(word.toLowerCase(), 1, Integer::sum);
            if (wordCount.get(word.toLowerCase()) > 20) {
                return true;
            }
        }
        
        return false;
    }
    
    @Data
    @AllArgsConstructor
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;
    }
}

@ControllerAdvice
public class ValidationExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());
        
        ErrorResponse response = new ErrorResponse(
            "VALIDATION_ERROR",
            "Input validation failed",
            errors
        );
        
        return ResponseEntity.badRequest().body(response);
    }
}
```

## Infrastructure Setup

### 1. Docker Configuration

#### Production Dockerfile
```dockerfile
# Multi-stage build for production
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Cache dependencies
RUN ./mvnw dependency:go-offline -B

# Build application
RUN ./mvnw clean package -DskipTests

# Production stage
FROM eclipse-temurin:21-jre-alpine

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Install security updates
RUN apk update && apk upgrade && \
    apk add --no-cache curl dumb-init && \
    rm -rf /var/cache/apk/*

WORKDIR /app

# Copy application
COPY --from=builder /app/target/l4aichat-*.jar app.jar
COPY --chown=appuser:appgroup docker/entrypoint.sh /entrypoint.sh

# Create log directory
RUN mkdir -p /var/log/chatbot && \
    chown -R appuser:appgroup /var/log/chatbot

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["/entrypoint.sh"]
```

#### Docker Compose for Production
```yaml
# docker-compose.prod.yml
version: '3.8'

services:
  chatbot:
    build: .
    image: l4aichat:latest
    container_name: l4aichat-app
    restart: unless-stopped
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_URL=jdbc:postgresql://postgres:5432/chatdb
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - REDIS_URL=redis://redis:6379
    depends_on:
      - postgres
      - redis
    ports:
      - "8080:8080"
    volumes:
      - logs:/var/log/chatbot
    networks:
      - app-network
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '0.5'
        reservations:
          memory: 512M
          cpus: '0.25'

  postgres:
    image: postgres:15-alpine
    container_name: l4aichat-db
    restart: unless-stopped
    environment:
      - POSTGRES_DB=chatdb
      - POSTGRES_USER=${DB_USERNAME}
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    networks:
      - app-network

  redis:
    image: redis:7-alpine
    container_name: l4aichat-redis
    restart: unless-stopped
    command: redis-server --appendonly yes --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    networks:
      - app-network

  nginx:
    image: nginx:alpine
    container_name: l4aichat-nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
      - logs:/var/log/nginx
    depends_on:
      - chatbot
    networks:
      - app-network

volumes:
  postgres_data:
  redis_data:
  logs:

networks:
  app-network:
    driver: bridge
```

### 2. Kubernetes Deployment

#### Kubernetes Manifests
```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: l4aichat
  labels:
    name: l4aichat

---
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: l4aichat-config
  namespace: l4aichat
data:
  application.yml: |
    spring:
      profiles:
        active: prod
      datasource:
        url: jdbc:postgresql://postgres-service:5432/chatdb
        hikari:
          maximum-pool-size: 20
          minimum-idle: 5
    server:
      port: 8080
    management:
      endpoints:
        web:
          exposure:
            include: health,metrics,prometheus,info

---
# k8s/secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: l4aichat-secrets
  namespace: l4aichat
type: Opaque
data:
  db-username: # base64 encoded
  db-password: # base64 encoded
  openai-api-key: # base64 encoded
  redis-password: # base64 encoded

---
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: l4aichat-app
  namespace: l4aichat
  labels:
    app: l4aichat
spec:
  replicas: 3
  selector:
    matchLabels:
      app: l4aichat
  template:
    metadata:
      labels:
        app: l4aichat
    spec:
      containers:
      - name: l4aichat
        image: l4aichat:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: l4aichat-secrets
              key: db-username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: l4aichat-secrets
              key: db-password
        - name: OPENAI_API_KEY
          valueFrom:
            secretKeyRef:
              name: l4aichat-secrets
              key: openai-api-key
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        - name: logs-volume
          mountPath: /var/log/chatbot
      volumes:
      - name: config-volume
        configMap:
          name: l4aichat-config
      - name: logs-volume
        persistentVolumeClaim:
          claimName: l4aichat-logs-pvc

---
# k8s/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: l4aichat-service
  namespace: l4aichat
spec:
  selector:
    app: l4aichat
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP

---
# k8s/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: l4aichat-ingress
  namespace: l4aichat
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/rate-limit: "100"
    nginx.ingress.kubernetes.io/rate-limit-burst: "150"
spec:
  tls:
  - hosts:
    - chat.yourdomain.com
    secretName: l4aichat-tls
  rules:
  - host: chat.yourdomain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: l4aichat-service
            port:
              number: 80

---
# k8s/hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: l4aichat-hpa
  namespace: l4aichat
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: l4aichat-app
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

## Monitoring and Observability

### 1. Comprehensive Logging

#### Structured Logging Configuration
```java
@Configuration
public class LoggingConfiguration {
    
    @Bean
    @ConditionalOnProperty("logging.structured.enabled")
    public Logger structuredLogger() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        // JSON encoder for structured logging
        JsonEncoder jsonEncoder = new JsonEncoder();
        jsonEncoder.setContext(context);
        jsonEncoder.start();
        
        // File appender
        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setFile("/var/log/chatbot/application.json");
        fileAppender.setEncoder(jsonEncoder);
        
        // Rolling policy
        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setFileNamePattern("/var/log/chatbot/application-%d{yyyy-MM-dd}.%i.json.gz");
        rollingPolicy.setMaxHistory(30);
        rollingPolicy.setParent(fileAppender);
        
        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<>();
        triggeringPolicy.setMaxFileSize(FileSize.valueOf("100MB"));
        
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(triggeringPolicy);
        rollingPolicy.start();
        
        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.start();
        
        // Add to root logger
        ch.qos.logback.classic.Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(fileAppender);
        rootLogger.setLevel(Level.INFO);
        
        return rootLogger;
    }
}

@Component
public class ChatAuditLogger {
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private final ObjectMapper objectMapper;
    
    public void logChatRequest(String userId, String conversationId, String message, String userAgent, String ipAddress) {
        AuditEvent event = AuditEvent.builder()
            .timestamp(Instant.now())
            .eventType("CHAT_REQUEST")
            .userId(userId)
            .conversationId(conversationId)
            .details(Map.of(
                "messageLength", message.length(),
                "userAgent", userAgent,
                "ipAddress", hashIpAddress(ipAddress)
            ))
            .build();
        
        try {
            auditLogger.info(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            logger.error("Failed to log audit event", e);
        }
    }
    
    public void logChatResponse(String userId, String conversationId, long responseTime, int tokenCount, double cost) {
        AuditEvent event = AuditEvent.builder()
            .timestamp(Instant.now())
            .eventType("CHAT_RESPONSE")
            .userId(userId)
            .conversationId(conversationId)
            .details(Map.of(
                "responseTimeMs", responseTime,
                "tokenCount", tokenCount,
                "estimatedCost", cost
            ))
            .build();
        
        try {
            auditLogger.info(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            logger.error("Failed to log audit event", e);
        }
    }
    
    private String hashIpAddress(String ipAddress) {
        // Hash IP for privacy compliance
        return DigestUtils.sha256Hex(ipAddress + "salt");
    }
    
    @Builder
    @Data
    private static class AuditEvent {
        private Instant timestamp;
        private String eventType;
        private String userId;
        private String conversationId;
        private Map<String, Object> details;
    }
}
```

### 2. Metrics and Monitoring

#### Prometheus Metrics
```java
@Component
public class ChatMetricsCollector {
    private final MeterRegistry meterRegistry;
    private final Counter chatRequestCounter;
    private final Timer chatResponseTimer;
    private final Gauge activeConversationsGauge;
    private final Counter tokenUsageCounter;
    private final Counter costCounter;
    
    public ChatMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        this.chatRequestCounter = Counter.builder("chat_requests_total")
            .description("Total number of chat requests")
            .tag("status", "success")
            .register(meterRegistry);
        
        this.chatResponseTimer = Timer.builder("chat_response_duration_seconds")
            .description("Chat response time")
            .register(meterRegistry);
        
        this.activeConversationsGauge = Gauge.builder("chat_conversations_active")
            .description("Number of active conversations")
            .register(meterRegistry, this, ChatMetricsCollector::getActiveConversationCount);
        
        this.tokenUsageCounter = Counter.builder("chat_tokens_total")
            .description("Total tokens used")
            .register(meterRegistry);
        
        this.costCounter = Counter.builder("chat_cost_total")
            .description("Total estimated cost")
            .register(meterRegistry);
    }
    
    public void recordChatRequest(String userId, String model, String status) {
        chatRequestCounter.increment(
            Tags.of(
                "user_id", hashUserId(userId),
                "model", model,
                "status", status
            )
        );
    }
    
    public Timer.Sample startResponseTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordChatResponse(Timer.Sample sample, String model, int tokens, double cost, boolean success) {
        sample.stop(Timer.builder("chat_response_duration_seconds")
            .tag("model", model)
            .tag("success", String.valueOf(success))
            .register(meterRegistry));
        
        tokenUsageCounter.increment(Tags.of("model", model), tokens);
        costCounter.increment(Tags.of("model", model), cost);
    }
    
    public void recordError(String errorType, String model) {
        meterRegistry.counter("chat_errors_total",
            "error_type", errorType,
            "model", model)
            .increment();
    }
    
    private double getActiveConversationCount() {
        // Implement logic to count active conversations
        return conversationService.getActiveConversationCount();
    }
    
    private String hashUserId(String userId) {
        return DigestUtils.sha256Hex(userId).substring(0, 8);
    }
}
```

#### Grafana Dashboard Configuration
```json
{
  "dashboard": {
    "title": "Spring AI Chat Application",
    "panels": [
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(chat_requests_total[5m])",
            "legendFormat": "{{status}}"
          }
        ]
      },
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(chat_response_duration_seconds_bucket[5m]))",
            "legendFormat": "95th percentile"
          },
          {
            "expr": "histogram_quantile(0.50, rate(chat_response_duration_seconds_bucket[5m]))",
            "legendFormat": "50th percentile"
          }
        ]
      },
      {
        "title": "Token Usage",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(chat_tokens_total[5m])",
            "legendFormat": "{{model}}"
          }
        ]
      },
      {
        "title": "Error Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(chat_errors_total[5m])",
            "legendFormat": "{{error_type}}"
          }
        ]
      },
      {
        "title": "Cost per Hour",
        "type": "singlestat",
        "targets": [
          {
            "expr": "rate(chat_cost_total[1h]) * 3600",
            "legendFormat": "Cost/hour"
          }
        ]
      }
    ]
  }
}
```

### 3. Health Checks

#### Custom Health Indicators
```java
@Component
public class ChatServiceHealthIndicator implements HealthIndicator {
    private final ChatClient chatClient;
    private final ChatMemoryRepository memoryRepository;
    
    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        
        try {
            // Test AI service connectivity
            testAIConnectivity(builder);
            
            // Test database connectivity
            testDatabaseConnectivity(builder);
            
            // Test memory usage
            testMemoryUsage(builder);
            
            // Test external dependencies
            testExternalDependencies(builder);
            
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
        
        return builder.build();
    }
    
    private void testAIConnectivity(Health.Builder builder) {
        try {
            long startTime = System.currentTimeMillis();
            
            String testResponse = chatClient.prompt()
                .user("test")
                .options(OpenAiChatOptions.builder()
                    .withMaxTokens(10)
                    .withTimeout(Duration.ofSeconds(5))
                    .build())
                .call()
                .content();
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            builder.withDetail("ai_service", Map.of(
                "status", "UP",
                "response_time_ms", responseTime,
                "test_response_length", testResponse.length()
            ));
            
        } catch (Exception e) {
            builder.withDetail("ai_service", Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            ));
        }
    }
    
    private void testDatabaseConnectivity(Health.Builder builder) {
        try {
            // Simple database query
            List<Message> testMessages = memoryRepository.get("health-check");
            
            builder.withDetail("database", Map.of(
                "status", "UP",
                "connection_pool_active", getActiveConnections(),
                "connection_pool_idle", getIdleConnections()
            ));
            
        } catch (Exception e) {
            builder.withDetail("database", Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            ));
        }
    }
    
    private void testMemoryUsage(Health.Builder builder) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsagePercent = (double) usedMemory / totalMemory * 100;
        
        builder.withDetail("memory", Map.of(
            "used_bytes", usedMemory,
            "free_bytes", freeMemory,
            "total_bytes", totalMemory,
            "usage_percent", memoryUsagePercent
        ));
        
        if (memoryUsagePercent > 90) {
            builder.status(Status.DOWN);
        } else if (memoryUsagePercent > 80) {
            builder.status("DEGRADED");
        }
    }
}
```

## Scaling and Performance

### 1. Auto-Scaling Configuration

#### Kubernetes HPA with Custom Metrics
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: l4aichat-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: l4aichat-app
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  - type: Pods
    pods:
      metric:
        name: chat_requests_per_second
      target:
        type: AverageValue
        averageValue: "50"
  behavior:
    scaleUp:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
    scaleDown:
      stabilizationWindowSeconds: 600
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
```

### 2. Load Balancing

#### Nginx Configuration
```nginx
# nginx.conf
upstream l4aichat_backend {
    least_conn;
    server app1:8080 max_fails=3 fail_timeout=30s weight=1;
    server app2:8080 max_fails=3 fail_timeout=30s weight=1;
    server app3:8080 max_fails=3 fail_timeout=30s weight=1;
    
    keepalive 32;
}

server {
    listen 80;
    server_name chat.yourdomain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name chat.yourdomain.com;
    
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    
    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    limit_req_zone $http_authorization zone=user:10m rate=100r/m;
    
    location /api/chat {
        limit_req zone=api burst=20 nodelay;
        limit_req zone=user burst=10 nodelay;
        
        proxy_pass http://l4aichat_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Long timeout for AI responses
        proxy_read_timeout 300s;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        
        # WebSocket support for streaming
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
    
    location /actuator/health {
        proxy_pass http://l4aichat_backend;
        access_log off;
    }
    
    location /actuator/prometheus {
        proxy_pass http://l4aichat_backend;
        allow 10.0.0.0/8;  # Only internal monitoring
        deny all;
    }
}
```

## Operational Procedures

### 1. Deployment Pipeline

#### CI/CD Pipeline Configuration
```yaml
# .github/workflows/deploy.yml
name: Deploy to Production

on:
  push:
    branches: [main]
  workflow_dispatch:

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Run tests
      run: ./mvnw test
    
    - name: Run security scan
      run: ./mvnw org.owasp:dependency-check-maven:check
    
    - name: Run code quality analysis
      run: ./mvnw sonar:sonar -Dsonar.token=${{ secrets.SONAR_TOKEN }}

  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Build Docker image
      run: |
        docker build -t l4aichat:${{ github.sha }} .
        docker tag l4aichat:${{ github.sha }} l4aichat:latest
    
    - name: Security scan image
      run: |
        docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
          aquasec/trivy image l4aichat:${{ github.sha }}
    
    - name: Push to registry
      run: |
        echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
        docker push l4aichat:${{ github.sha }}
        docker push l4aichat:latest

  deploy:
    needs: build
    runs-on: ubuntu-latest
    environment: production
    steps:
    - name: Deploy to Kubernetes
      run: |
        kubectl set image deployment/l4aichat-app l4aichat=l4aichat:${{ github.sha }}
        kubectl rollout status deployment/l4aichat-app
        kubectl get pods -l app=l4aichat
```

### 2. Zero-Downtime Deployment

#### Rolling Deployment Strategy
```java
@Component
public class GracefulShutdownHandler {
    
    @EventListener
    public void onShutdown(ContextClosedEvent event) {
        logger.info("Gracefully shutting down application...");
        
        // Stop accepting new requests
        stopAcceptingNewRequests();
        
        // Wait for ongoing requests to complete
        waitForOngoingRequests();
        
        // Clean up resources
        cleanupResources();
        
        logger.info("Application shutdown complete");
    }
    
    private void stopAcceptingNewRequests() {
        // Implementation to stop accepting new requests
    }
    
    private void waitForOngoingRequests() {
        int maxWaitTime = 30; // seconds
        int waited = 0;
        
        while (hasOngoingRequests() && waited < maxWaitTime) {
            try {
                Thread.sleep(1000);
                waited++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private boolean hasOngoingRequests() {
        // Check if there are ongoing chat requests
        return false;
    }
    
    private void cleanupResources() {
        // Close database connections, clear caches, etc.
    }
}
```

## Disaster Recovery

### 1. Backup Strategy

#### Database Backup Configuration
```bash
#!/bin/bash
# backup-script.sh

BACKUP_DIR="/var/backups/postgresql"
DB_NAME="chatdb"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/chatdb_backup_${TIMESTAMP}.sql.gz"

# Create backup directory
mkdir -p $BACKUP_DIR

# Perform backup
pg_dump -h $DB_HOST -U $DB_USER -d $DB_NAME | gzip > $BACKUP_FILE

# Upload to S3
aws s3 cp $BACKUP_FILE s3://your-backup-bucket/database/

# Clean up old local backups (keep 7 days)
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete

# Verify backup
if [ -f "$BACKUP_FILE" ]; then
    echo "Backup completed successfully: $BACKUP_FILE"
else
    echo "Backup failed!"
    exit 1
fi
```

### 2. Recovery Procedures

#### Disaster Recovery Playbook
```yaml
# disaster-recovery-playbook.yml
recovery_procedures:
  database_corruption:
    steps:
      - stop_application_pods
      - restore_from_backup:
          source: "s3://backup-bucket/database/latest"
          target_db: "chatdb"
      - verify_data_integrity
      - restart_application_pods
      - run_health_checks
    
  complete_infrastructure_failure:
    steps:
      - provision_new_infrastructure:
          region: "us-east-2"  # DR region
      - restore_database_from_backup
      - deploy_application:
          version: "latest_stable"
      - update_dns_records
      - verify_functionality
      - notify_stakeholders
    
  api_key_compromise:
    steps:
      - revoke_compromised_key
      - generate_new_api_key
      - update_application_config
      - restart_application
      - monitor_for_unauthorized_usage
      - audit_recent_requests
```

## Key Takeaways

1. **Security first** - Implement authentication, rate limiting, and input validation
2. **Monitor everything** - Comprehensive logging, metrics, and alerting
3. **Plan for scale** - Auto-scaling, load balancing, and performance optimization
4. **Automate deployment** - CI/CD pipelines with security scanning
5. **Prepare for disasters** - Backup strategies and recovery procedures
6. **Test production setup** - Load testing and chaos engineering
7. **Document procedures** - Runbooks for common operational tasks

## Next Steps

- 📄 [Troubleshooting Guide](troubleshooting.md) - Common issues and solutions
- 🏗️ [Performance Guide](../architecture/performance.md) - Optimization strategies
- 📄 [Testing Guide](testing.md) - Testing methodologies

---

[← Testing Guide](testing.md) | [Back to Guides](../README.md#practical-guides) | [Troubleshooting →](troubleshooting.md)