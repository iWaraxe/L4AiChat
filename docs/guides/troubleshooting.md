# 🔧 Troubleshooting Guide: Spring AI Applications

## Overview

This comprehensive troubleshooting guide covers common issues encountered when developing and deploying Spring AI applications, along with their solutions and prevention strategies.

## Table of Contents
1. [Common Startup Issues](#common-startup-issues)
2. [API Integration Problems](#api-integration-problems)
3. [Memory and Performance Issues](#memory-and-performance-issues)
4. [Conversation Context Problems](#conversation-context-problems)
5. [Database and Persistence Issues](#database-and-persistence-issues)
6. [Streaming and WebSocket Issues](#streaming-and-websocket-issues)
7. [Security and Authentication Problems](#security-and-authentication-problems)
8. [Production Environment Issues](#production-environment-issues)

## Common Startup Issues

### 1. Application Fails to Start - Missing API Key

**Symptoms:**
```
***************************
APPLICATION FAILED TO START
***************************

Description:
Parameter 0 of constructor in org.springframework.ai.openai.OpenAiChatClient required a bean of type 'java.lang.String' that could not be found.

Action:
Consider defining a bean of type 'java.lang.String' in your configuration.
```

**Root Cause:** OpenAI API key not configured or incorrectly set.

**Solutions:**

#### Option 1: Environment Variable
```bash
export OPENAI_API_KEY=sk-proj-your-actual-api-key-here
```

#### Option 2: Application Properties
```properties
# application.properties
spring.ai.openai.api-key=sk-proj-your-actual-api-key-here
```

#### Option 3: Programmatic Configuration
```java
@Configuration
public class OpenAIConfig {
    
    @Bean
    @ConditionalOnMissingBean
    public ChatClient chatClient(@Value("${spring.ai.openai.api-key:}") String apiKey) {
        if (apiKey.isEmpty() || apiKey.startsWith("${")) {
            throw new IllegalStateException(
                "OpenAI API key must be configured. Set OPENAI_API_KEY environment variable " +
                "or spring.ai.openai.api-key property"
            );
        }
        
        return ChatClient.builder()
            .defaultOptions(OpenAiChatOptions.builder()
                .withModel("gpt-4-turbo")
                .build())
            .build();
    }
}
```

**Prevention:**
```java
@Component
public class ApiKeyValidator implements ApplicationListener<ApplicationReadyEvent> {
    
    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (apiKey.isEmpty() || apiKey.startsWith("${") || !apiKey.startsWith("sk-")) {
            logger.error("Invalid OpenAI API key configuration");
            System.exit(1);
        }
        logger.info("OpenAI API key validation successful");
    }
}
```

### 2. Bean Creation Error - Circular Dependencies

**Symptoms:**
```
The dependencies of some of the beans in the application context form a cycle:
┌─────┐
|  chatService defined in file [.../ChatService.class]
↑     ↓
|  chatClient defined in com.example.config.ChatConfig
└─────┘
```

**Root Cause:** Circular dependency between ChatClient and services that depend on it.

**Solution:**
```java
// ❌ Problematic configuration
@Configuration
public class ProblematicConfig {
    
    @Bean
    public ChatClient chatClient(ChatService chatService) { // Creates circular dependency
        return ChatClient.builder()
            .defaultAdvisors(chatService.getAdvisors()) // ChatService needs ChatClient
            .build();
    }
}

// ✅ Correct configuration
@Configuration
public class CorrectConfig {
    
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
            .chatMemoryRepository(new InMemoryChatMemoryRepository())
            .maxMessages(20)
            .build();
    }
    
    @Bean
    public ChatClient chatClient(ChatMemory chatMemory) {
        return ChatClient.builder()
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build();
    }
    
    @Bean
    public ChatService chatService(ChatClient chatClient) {
        return new ChatService(chatClient);
    }
}
```

### 3. Class Not Found - Version Compatibility

**Symptoms:**
```
java.lang.NoClassDefFoundError: org/springframework/ai/chat/ChatClient
```

**Root Cause:** Version mismatch between Spring AI and Spring Boot.

**Solution:**
```xml
<!-- Correct version alignment -->
<properties>
    <spring-boot.version>3.4.0</spring-boot.version>
    <spring-ai.version>1.0.0</spring-ai.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
        <version>${spring-boot.version}</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-openai</artifactId>
        <version>${spring-ai.version}</version>
    </dependency>
</dependencies>
```

**Version Compatibility Matrix:**
| Spring Boot | Spring AI | Java | Notes |
|-------------|-----------|------|-------|
| 3.4.0+ | 1.0.0 | 21+ | Recommended |
| 3.3.x | 1.0.0-M7 | 17+ | Legacy |
| 3.2.x | 0.8.x | 17+ | Not recommended |

## API Integration Problems

### 1. Connection Timeout Errors

**Symptoms:**
```
org.springframework.web.client.ResourceAccessException: I/O error on POST request for "https://api.openai.com/v1/chat/completions": Read timed out
```

**Root Cause:** Default timeout values too low for AI API responses.

**Solution:**
```java
@Configuration
public class ChatClientTimeoutConfig {
    
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
            .defaultOptions(OpenAiChatOptions.builder()
                .withModel("gpt-4-turbo")
                .withTimeout(Duration.ofMinutes(2)) // Increase timeout
                .build())
            .build();
    }
}

// For custom HTTP client configuration
@Bean
public OpenAiApi openAiApi(@Value("${spring.ai.openai.api-key}") String apiKey) {
    OkHttpClient httpClient = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build();
    
    return new OpenAiApi(
        "https://api.openai.com",
        apiKey,
        RestClient.builder()
            .requestFactory(new OkHttp3ClientHttpRequestFactory(httpClient))
            .build()
    );
}
```

### 2. Rate Limiting Errors

**Symptoms:**
```
OpenAiApi$OpenAiApiException: Too Many Requests
Response: {"error":{"message":"Rate limit reached","type":"requests","code":"rate_limit_exceeded"}}
```

**Root Cause:** Exceeding OpenAI API rate limits.

**Solutions:**

#### Implement Exponential Backoff
```java
@Component
public class ResilientChatService {
    private final ChatClient chatClient;
    private final RetryTemplate retryTemplate;
    
    public ResilientChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
        this.retryTemplate = RetryTemplate.builder()
            .maxAttempts(3)
            .exponentialBackoff(1000, 2, 10000)
            .retryOn(OpenAiApiException.class)
            .build();
    }
    
    public String processWithRetry(String message) {
        return retryTemplate.execute(context -> {
            logger.info("Attempt {} for message", context.getRetryCount() + 1);
            
            return chatClient.prompt()
                .user(message)
                .call()
                .content();
        });
    }
}
```

#### Implement Rate Limiting
```java
@Component
public class RateLimitedChatService {
    private final RateLimiter rateLimiter;
    private final ChatClient chatClient;
    
    public RateLimitedChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
        // 10 requests per minute
        this.rateLimiter = RateLimiter.create(10.0 / 60.0);
    }
    
    public String processMessage(String message) {
        if (!rateLimiter.tryAcquire(Duration.ofSeconds(5))) {
            throw new RateLimitExceededException("Rate limit exceeded, please try again later");
        }
        
        return chatClient.prompt()
            .user(message)
            .call()
            .content();
    }
}
```

### 3. Invalid Model Name Errors

**Symptoms:**
```
OpenAiApi$OpenAiApiException: The model 'gpt-5' does not exist
```

**Root Cause:** Using non-existent or deprecated model names.

**Solution:**
```java
@Component
public class ModelValidator {
    private static final Set<String> VALID_MODELS = Set.of(
        "gpt-4",
        "gpt-4-turbo",
        "gpt-4-turbo-preview",
        "gpt-3.5-turbo",
        "gpt-3.5-turbo-16k"
    );
    
    public void validateModel(String model) {
        if (!VALID_MODELS.contains(model)) {
            throw new IllegalArgumentException(
                "Invalid model: " + model + ". Valid models: " + VALID_MODELS
            );
        }
    }
}

@Configuration
public class ValidatedChatConfig {
    
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, 
                               @Value("${ai.model:gpt-4-turbo}") String model,
                               ModelValidator validator) {
        validator.validateModel(model);
        
        return builder
            .defaultOptions(OpenAiChatOptions.builder()
                .withModel(model)
                .build())
            .build();
    }
}
```

## Memory and Performance Issues

### 1. Memory Leaks in Long Conversations

**Symptoms:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Root Cause:** Unbounded conversation memory growth.

**Diagnostic Steps:**
```java
@Component
public class MemoryDiagnostics {
    
    @Scheduled(fixedDelay = 60000) // Every minute
    public void logMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        logger.info("Memory usage: {}MB used, {}MB free, {}MB total",
            usedMemory / 1024 / 1024,
            freeMemory / 1024 / 1024,
            totalMemory / 1024 / 1024);
        
        if (usedMemory > totalMemory * 0.9) {
            logger.warn("High memory usage detected!");
            // Trigger cleanup
            triggerCleanup();
        }
    }
    
    private void triggerCleanup() {
        // Force garbage collection
        System.gc();
        
        // Clear old conversations
        conversationCleanupService.cleanupOldConversations();
    }
}
```

**Solution:**
```java
@Service
public class BoundedMemoryService {
    private final Map<String, ConversationData> conversations = new ConcurrentHashMap<>();
    private static final int MAX_CONVERSATIONS = 1000;
    private static final int MAX_MESSAGES_PER_CONVERSATION = 50;
    
    public void addMessage(String conversationId, Message message) {
        conversations.compute(conversationId, (id, data) -> {
            if (data == null) {
                // Check if we're at capacity
                if (conversations.size() >= MAX_CONVERSATIONS) {
                    evictOldestConversation();
                }
                data = new ConversationData();
            }
            
            data.addMessage(message);
            
            // Limit messages per conversation
            if (data.getMessages().size() > MAX_MESSAGES_PER_CONVERSATION) {
                data.removeOldestMessages(10); // Remove 10 oldest
            }
            
            data.setLastAccessed(System.currentTimeMillis());
            return data;
        });
    }
    
    private void evictOldestConversation() {
        conversations.entrySet().stream()
            .min(Comparator.comparing(entry -> entry.getValue().getLastAccessed()))
            .ifPresent(entry -> {
                conversations.remove(entry.getKey());
                logger.info("Evicted conversation: {}", entry.getKey());
            });
    }
    
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void cleanupInactiveConversations() {
        long cutoff = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1);
        
        conversations.entrySet().removeIf(entry -> {
            if (entry.getValue().getLastAccessed() < cutoff) {
                logger.info("Removing inactive conversation: {}", entry.getKey());
                return true;
            }
            return false;
        });
    }
}
```

### 2. Slow Response Times

**Symptoms:**
- API responses taking longer than expected
- Timeouts in production

**Diagnostic Tools:**
```java
@Component
public class PerformanceDiagnostics {
    
    @EventListener
    public void onChatRequest(ChatRequestEvent event) {
        String conversationId = event.getConversationId();
        long startTime = System.currentTimeMillis();
        
        // Store start time
        requestTimes.put(conversationId, startTime);
    }
    
    @EventListener
    public void onChatResponse(ChatResponseEvent event) {
        String conversationId = event.getConversationId();
        Long startTime = requestTimes.remove(conversationId);
        
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            
            if (duration > 10000) { // Log slow requests (>10s)
                logger.warn("Slow chat response: {}ms for conversation {}", 
                           duration, conversationId);
                
                // Additional diagnostics
                analyzeSlowResponse(event, duration);
            }
        }
    }
    
    private void analyzeSlowResponse(ChatResponseEvent event, long duration) {
        // Check token count
        if (event.getTokenCount() > 3000) {
            logger.warn("High token count may cause slow response: {} tokens", 
                       event.getTokenCount());
        }
        
        // Check model used
        if ("gpt-4".equals(event.getModel())) {
            logger.info("GPT-4 typically slower than GPT-3.5-turbo");
        }
        
        // Check conversation length
        int messageCount = getMessageCount(event.getConversationId());
        if (messageCount > 20) {
            logger.warn("Long conversation may cause slow response: {} messages", 
                       messageCount);
        }
    }
}
```

**Solutions:**

#### Optimize Token Usage
```java
@Service
public class OptimizedChatService {
    
    public String processMessage(String conversationId, String message) {
        // Analyze message complexity
        MessageComplexity complexity = analyzeComplexity(message);
        
        // Choose appropriate model and settings
        OpenAiChatOptions options = switch (complexity) {
            case SIMPLE -> OpenAiChatOptions.builder()
                .withModel("gpt-3.5-turbo")
                .withMaxTokens(150)
                .withTemperature(0.3)
                .build();
            case MEDIUM -> OpenAiChatOptions.builder()
                .withModel("gpt-4-turbo")
                .withMaxTokens(500)
                .withTemperature(0.5)
                .build();
            case COMPLEX -> OpenAiChatOptions.builder()
                .withModel("gpt-4")
                .withMaxTokens(1000)
                .withTemperature(0.7)
                .build();
        };
        
        return chatClient.prompt()
            .user(message)
            .options(options)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .content();
    }
    
    private MessageComplexity analyzeComplexity(String message) {
        if (message.length() < 50) return MessageComplexity.SIMPLE;
        if (message.length() < 200) return MessageComplexity.MEDIUM;
        return MessageComplexity.COMPLEX;
    }
}
```

## Conversation Context Problems

### 1. Context Not Preserved Between Requests

**Symptoms:**
- AI doesn't remember previous conversation
- Context appears to reset unexpectedly

**Diagnostic:**
```java
@Component
public class ContextDiagnostics {
    
    public void debugConversationMemory(String conversationId) {
        List<Message> messages = chatMemory.get(conversationId);
        
        logger.info("Conversation {} has {} messages:", conversationId, messages.size());
        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            logger.info("  [{}] {}: {}", i, msg.getType(), 
                       msg.getContent().substring(0, Math.min(50, msg.getContent().length())));
        }
        
        // Check memory configuration
        if (chatMemory instanceof MessageWindowChatMemory windowMemory) {
            logger.info("Window size: {}", windowMemory.getMaxMessages());
        }
    }
}
```

**Common Causes and Solutions:**

#### Issue: Memory Not Configured
```java
// ❌ Missing memory configuration
@Bean
public ChatClient chatClient(ChatClient.Builder builder) {
    return builder.build(); // No memory advisor
}

// ✅ Correct configuration
@Bean
public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
    return builder
        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
        .build();
}
```

#### Issue: Wrong Conversation ID
```java
// ❌ Different conversation IDs
public String chat1(String message) {
    return chatClient.prompt()
        .user(message)
        .advisors(a -> a.param(CONVERSATION_ID, "conv-1")) // Fixed ID
        .call().content();
}

public String chat2(String message) {
    return chatClient.prompt()
        .user(message)
        .advisors(a -> a.param(CONVERSATION_ID, "conv-2")) // Different ID
        .call().content();
}

// ✅ Consistent conversation ID
@Service
public class ConversationService {
    public String chat(String conversationId, String message) {
        return chatClient.prompt()
            .user(message)
            .advisors(a -> a.param(CONVERSATION_ID, conversationId)) // Use same ID
            .call().content();
    }
}
```

### 2. Memory Window Too Small

**Symptoms:**
- AI forgets information from earlier in conversation
- Context seems limited

**Solution:**
```java
@Configuration
public class MemoryConfiguration {
    
    @Bean
    public ChatMemory adaptiveMemory() {
        return MessageWindowChatMemory.builder()
            .chatMemoryRepository(new InMemoryChatMemoryRepository())
            .maxMessages(calculateOptimalWindowSize())
            .build();
    }
    
    private int calculateOptimalWindowSize() {
        // Consider available memory
        long availableMemory = Runtime.getRuntime().freeMemory();
        
        // Consider average message size (rough estimate)
        int avgMessageSize = 200; // characters
        int avgTokensPerMessage = 50;
        
        // GPT-4 context limit is ~8k tokens, leave room for response
        int maxTokens = 6000;
        int maxMessages = maxTokens / avgTokensPerMessage;
        
        // Consider memory constraints
        long memoryForMessages = availableMemory / 10; // Use 10% of free memory
        int memoryLimitedMessages = (int) (memoryForMessages / avgMessageSize);
        
        return Math.min(maxMessages, memoryLimitedMessages);
    }
}
```

## Database and Persistence Issues

### 1. Database Connection Failures

**Symptoms:**
```
java.sql.SQLException: Connection is not available, request timed out after 30000ms.
```

**Diagnostic:**
```java
@Component
public class DatabaseDiagnostics {
    
    @Autowired
    private HikariDataSource dataSource;
    
    @EventListener
    @Async
    public void onDatabaseError(DatabaseErrorEvent event) {
        HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();
        
        logger.error("Database error occurred: {}", event.getException().getMessage());
        logger.info("Pool stats - Active: {}, Idle: {}, Total: {}, Waiting: {}",
            poolBean.getActiveConnections(),
            poolBean.getIdleConnections(),
            poolBean.getTotalConnections(),
            poolBean.getThreadsAwaitingConnection());
    }
}
```

**Solutions:**

#### Optimize Connection Pool
```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
      pool-name: ChatAppPool
```

#### Implement Health Checks
```java
@Component
public class DatabaseHealthCheck implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                return Health.up()
                    .withDetail("database", "Available")
                    .withDetail("validationQuery", "SELECT 1")
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", "Invalid connection")
                    .build();
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("database", "Connection failed")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

### 2. Schema Mismatch Issues

**Symptoms:**
```
org.springframework.dao.InvalidDataAccessResourceUsageException: could not prepare statement; SQL [select chat_memory_id, conversation_id, create_time from chat_memory where conversation_id = ?]; nested exception is org.hibernate.exception.SQLGrammarException: could not prepare statement
```

**Solution:**
```java
@Configuration
public class SchemaValidation {
    
    @PostConstruct
    public void validateSchema() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Check if required tables exist
            validateTableExists(metaData, "chat_memory");
            validateTableExists(metaData, "chat_memory_message");
            
            logger.info("Database schema validation passed");
            
        } catch (SQLException e) {
            logger.error("Database schema validation failed", e);
            throw new IllegalStateException("Database schema is invalid", e);
        }
    }
    
    private void validateTableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet tables = metaData.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
            if (!tables.next()) {
                throw new IllegalStateException("Required table not found: " + tableName);
            }
        }
    }
}
```

## Streaming and WebSocket Issues

### 1. Streaming Connections Drop

**Symptoms:**
- Streaming responses stop mid-way
- WebSocket connections close unexpectedly

**Solution:**
```java
@Component
public class ResilientStreamingService {
    
    public Flux<String> streamWithRetry(String message) {
        return chatClient.prompt()
            .user(message)
            .stream()
            .content()
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                .filter(throwable -> throwable instanceof ConnectException ||
                                   throwable instanceof SocketTimeoutException))
            .onErrorResume(throwable -> {
                logger.error("Streaming failed, falling back to regular response", throwable);
                
                // Fallback to non-streaming
                String response = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();
                
                return Flux.just(response);
            })
            .doOnError(error -> logger.error("Stream error", error))
            .doOnComplete(() -> logger.debug("Stream completed"));
    }
}
```

### 2. Browser Compatibility Issues

**Symptoms:**
- Streaming works in some browsers but not others
- EventSource connections fail

**Solution:**
```java
@RestController
public class CompatibleStreamingController {
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<ServerSentEvent<String>>> streamWithHeaders(
            @RequestParam String message,
            HttpServletRequest request) {
        
        // Set CORS headers for browser compatibility
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache");
        headers.add("Connection", "keep-alive");
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Headers", "Cache-Control");
        
        Flux<ServerSentEvent<String>> stream = chatService.streamMessage(message)
            .map(content -> ServerSentEvent.<String>builder()
                .id(UUID.randomUUID().toString())
                .event("message")
                .data(content)
                .retry(Duration.ofSeconds(10))
                .build())
            .doOnSubscribe(s -> logger.info("Client subscribed to stream"))
            .doOnCancel(() -> logger.info("Client cancelled stream"))
            .onErrorResume(error -> {
                logger.error("Stream error", error);
                return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data("Stream error occurred")
                    .build());
            });
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(stream);
    }
}
```

## Security and Authentication Problems

### 1. CORS Issues

**Symptoms:**
```
Access to fetch at 'http://localhost:8080/api/chat' from origin 'http://localhost:3000' has been blocked by CORS policy
```

**Solution:**
```java
@Configuration
public class CorsConfiguration {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins (don't use * in production)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",
            "https://*.yourdomain.com"
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }
}
```

### 2. JWT Token Issues

**Symptoms:**
- Authentication fails with valid tokens
- Token validation errors

**Diagnostic:**
```java
@Component
public class JwtDiagnostics {
    
    public void debugJwtToken(String token) {
        try {
            // Parse without verification for debugging
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                logger.error("Invalid JWT format: expected 3 parts, got {}", parts.length);
                return;
            }
            
            // Decode header
            String header = new String(Base64.getUrlDecoder().decode(parts[0]));
            logger.info("JWT Header: {}", header);
            
            // Decode payload
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            logger.info("JWT Payload: {}", payload);
            
            // Check expiration
            ObjectMapper mapper = new ObjectMapper();
            JsonNode claims = mapper.readTree(payload);
            long exp = claims.get("exp").asLong();
            long now = System.currentTimeMillis() / 1000;
            
            if (exp < now) {
                logger.error("Token expired. Exp: {}, Now: {}", exp, now);
            } else {
                logger.info("Token valid for {} more seconds", exp - now);
            }
            
        } catch (Exception e) {
            logger.error("Error debugging JWT token", e);
        }
    }
}
```

## Production Environment Issues

### 1. High CPU Usage

**Symptoms:**
- Application becomes unresponsive
- High CPU usage in monitoring

**Diagnostic:**
```bash
# Check Java process CPU usage
top -p $(pgrep -f java)

# Get thread dump for analysis
jstack $(pgrep -f java) > threaddump.txt

# Check GC activity
jstat -gc $(pgrep -f java) 5s
```

**Solutions:**

#### Optimize Garbage Collection
```bash
# JVM flags for better GC performance
JAVA_OPTS="-XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/heapdumps/ \
  -Xms2g -Xmx4g"
```

#### Implement Circuit Breakers
```java
@Component
public class CircuitBreakerChatService {
    private final CircuitBreaker circuitBreaker;
    
    public CircuitBreakerChatService() {
        this.circuitBreaker = CircuitBreaker.ofDefaults("chatService");
        circuitBreaker.getEventPublisher()
            .onStateTransition(event -> 
                logger.info("Circuit breaker state transition: {}", event));
    }
    
    public String processWithCircuitBreaker(String message) {
        return circuitBreaker.executeSupplier(() -> {
            return chatClient.prompt()
                .user(message)
                .call()
                .content();
        });
    }
}
```

### 2. Container Resource Issues

**Symptoms:**
- Pods getting OOMKilled
- Container restarts frequently

**Diagnostic:**
```bash
# Check container resource usage
kubectl top pods -n your-namespace

# Check pod events
kubectl describe pod your-pod-name -n your-namespace

# Check container logs
kubectl logs your-pod-name -n your-namespace --previous
```

**Solution:**
```yaml
# k8s/deployment.yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1000m"

# Add JVM memory configuration
env:
- name: JAVA_OPTS
  value: "-Xms1g -Xmx1500m -XX:+UseContainerSupport"
```

## Debugging Tools and Techniques

### 1. Comprehensive Health Checks

```java
@Component
public class ComprehensiveHealthCheck {
    
    @Autowired
    private ChatClient chatClient;
    
    @Autowired
    private DataSource dataSource;
    
    public HealthReport performFullHealthCheck() {
        HealthReport report = new HealthReport();
        
        // Test AI connectivity
        try {
            String testResponse = chatClient.prompt()
                .user("test")
                .options(OpenAiChatOptions.builder()
                    .withMaxTokens(5)
                    .withTimeout(Duration.ofSeconds(10))
                    .build())
                .call()
                .content();
            
            report.addCheck("ai_service", true, "Response: " + testResponse);
        } catch (Exception e) {
            report.addCheck("ai_service", false, e.getMessage());
        }
        
        // Test database connectivity
        try (Connection conn = dataSource.getConnection()) {
            boolean valid = conn.isValid(5);
            report.addCheck("database", valid, valid ? "Connected" : "Invalid connection");
        } catch (Exception e) {
            report.addCheck("database", false, e.getMessage());
        }
        
        // Test memory usage
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsage = (double) usedMemory / maxMemory;
        
        report.addCheck("memory", memoryUsage < 0.9, 
            String.format("%.1f%% used", memoryUsage * 100));
        
        return report;
    }
}
```

### 2. Request Tracing

```java
@Component
public class RequestTracer {
    
    @EventListener
    public void onChatRequest(ChatRequestEvent event) {
        MDC.put("conversationId", event.getConversationId());
        MDC.put("userId", event.getUserId());
        MDC.put("requestId", UUID.randomUUID().toString());
        
        logger.info("Chat request started: {}", event.getMessage().substring(0, 
            Math.min(50, event.getMessage().length())));
    }
    
    @EventListener
    public void onChatResponse(ChatResponseEvent event) {
        logger.info("Chat response completed: duration={}ms, tokens={}", 
            event.getDuration(), event.getTokenCount());
        
        MDC.clear();
    }
    
    @EventListener
    public void onChatError(ChatErrorEvent event) {
        logger.error("Chat request failed", event.getException());
        MDC.clear();
    }
}
```

## Key Takeaways

1. **Check API key configuration first** - Most startup issues are related to missing or invalid API keys
2. **Monitor memory usage** - Implement bounded conversation memory to prevent leaks
3. **Implement proper error handling** - Use retry mechanisms and circuit breakers
4. **Configure timeouts appropriately** - AI APIs can take longer than typical web services
5. **Use structured logging** - Include conversation IDs and request IDs for tracing
6. **Implement comprehensive health checks** - Monitor all dependencies, not just the application
7. **Plan for rate limiting** - Implement backoff strategies and consider API quotas

## Additional Resources

- 📄 [Performance Guide](../architecture/performance.md) - Performance optimization
- 📄 [Production Guide](production.md) - Deployment best practices
- 📄 [Testing Guide](testing.md) - Testing strategies
- 🏗️ [Architecture Patterns](../architecture/patterns.md) - Design decisions

---

[← Production Guide](production.md) | [Back to Guides](../README.md#practical-guides) | [Main README →](../../README.md)