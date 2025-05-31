# 📄 Module S5: Production REST API Chatbot Guide

## Overview

This module demonstrates how to build a production-ready chatbot API with Spring AI. It showcases enterprise patterns including proper error handling, validation, streaming responses, and API design best practices.

## Learning Objectives

By completing this module, you will:
- ✅ Design production-ready REST APIs for AI chat
- ✅ Implement proper error handling and validation
- ✅ Add streaming responses for better UX
- ✅ Apply enterprise patterns (DTOs, services, exception handling)
- ✅ Understand scaling considerations

## Why This Architecture?

Moving from demo to production requires:
1. **Proper Layering** - Separation of concerns
2. **Error Handling** - Graceful failure modes
3. **Validation** - Input sanitization
4. **Monitoring** - Observability and debugging
5. **Performance** - Streaming and async patterns

## Module Structure

```
s5chatbot/
├── SpringAiChatbotApplication.java    # Main application
├── controller/
│   └── ChatbotController.java         # REST endpoints
├── service/
│   └── ChatbotService.java           # Business logic
├── dto/
│   ├── ChatRequest.java              # Input validation
│   ├── ChatResponse.java             # Structured responses
│   └── ErrorResponse.java            # Error structure
├── exception/
│   ├── ChatbotException.java         # Custom exceptions
│   └── GlobalExceptionHandler.java   # Centralized handling
└── WebConfig.java                    # CORS configuration
```

## Architectural Decisions

### 1. Layered Architecture

```
┌─────────────────────────────────────────┐
│          REST Controller                │
│    (HTTP concerns, validation)          │
├─────────────────────────────────────────┤
│          Service Layer                  │
│    (Business logic, orchestration)      │
├─────────────────────────────────────────┤
│        Spring AI ChatClient             │
│    (AI integration, advisors)           │
├─────────────────────────────────────────┤
│          Memory/Storage                 │
│    (Conversation persistence)           │
└─────────────────────────────────────────┘
```

**Why This Layering?**
- **Testability** - Mock each layer independently
- **Flexibility** - Change storage without touching controllers
- **Reusability** - Services usable from different endpoints
- **Clarity** - Each layer has clear responsibilities

### 2. DTO Pattern

#### Request DTO with Validation
```java
public class ChatRequest {
    @NotBlank(message = "Message cannot be empty")
    @Size(min = 1, max = 500, message = "Message must be between 1 and 500 characters")
    private String message;
    
    // Constructors, getters, setters...
}
```

**Why DTOs?**
- **Validation** - Centralized input checking
- **Documentation** - Clear API contracts
- **Evolution** - Version APIs without breaking clients
- **Security** - Control what data enters system

#### Response DTO with Metadata
```java
public class ChatResponse {
    private String message;
    private String conversationId;
    private LocalDateTime timestamp;
    
    // Rich responses with context
}
```

**Why Include Metadata?**
- **Debugging** - Track when responses were generated
- **Correlation** - Link requests and responses
- **Analytics** - Measure performance
- **Features** - Enable conversation export, search

### 3. Service Layer Design

```java
@Service
public class ChatbotService {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    
    public String processMessage(String conversationId, String userMessage) {
        try {
            // Input validation
            if (conversationId == null || conversationId.isEmpty()) {
                conversationId = createNewConversation();
            }
            
            // Update activity tracking
            lastInteractionTimes.put(conversationId, System.currentTimeMillis());
            
            // Process with proper error handling
            return this.chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a.param(CONVERSATION_ID_PARAM, conversationId))
                .call()
                .content();
                
        } catch (Exception e) {
            logger.error("Error processing message", e);
            throw new ChatbotException("Failed to process message", e);
        }
    }
}
```

**Why Service Layer?**
- **Business Logic** - Centralized rules and workflow
- **Transaction Boundaries** - Manage consistency
- **Error Translation** - Convert technical to business errors
- **Reusability** - Multiple controllers can use same service

### 4. Exception Handling Strategy

#### Custom Exception
```java
public class ChatbotException extends RuntimeException {
    public ChatbotException(String message) {
        super(message);
    }
    
    public ChatbotException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

#### Global Exception Handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        List<String> errors = ex.getBindingResult()
            .getAllErrors()
            .stream()
            .map(error -> error.getDefaultMessage())
            .collect(Collectors.toList());
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            errors
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
```

**Why This Pattern?**
- **Consistency** - All errors follow same format
- **Security** - Don't leak internal details
- **Client Experience** - Clear, actionable error messages
- **Monitoring** - Centralized error logging

### 5. Streaming Implementation

#### Traditional Blocking Response
```java
@PostMapping("/{conversationId}")
public ResponseEntity<ChatResponse> chat(
        @PathVariable String conversationId,
        @Valid @RequestBody ChatRequest request) {
    
    String response = chatbotService.processMessage(
        conversationId, request.getMessage());
    
    return ResponseEntity.ok(new ChatResponse(response, conversationId));
}
```

#### Streaming Response
```java
@PostMapping(value = "/stream/{conversationId}", 
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> streamChat(
        @PathVariable String conversationId,
        @Valid @RequestBody ChatRequest request) {
    
    return chatbotService.streamMessage(conversationId, request.getMessage());
}
```

**Service Implementation:**
```java
public Flux<String> streamMessage(String conversationId, String userMessage) {
    return this.chatClient.prompt()
        .user(userMessage)
        .advisors(a -> a.param(CONVERSATION_ID_PARAM, conversationId))
        .stream()
        .content();
}
```

**Why Streaming?**
- **User Experience** - Immediate feedback
- **Perceived Performance** - Feels faster
- **Memory Efficiency** - No buffering large responses
- **Cancellation** - Users can stop mid-generation

### 6. API Design Best Practices

#### RESTful Endpoints
```java
POST   /api/chat/new                  // Create conversation
POST   /api/chat/{id}                 // Send message
GET    /api/chat/{id}/history         // Get history
DELETE /api/chat/{id}                 // End conversation
POST   /api/chat/stream/{id}          // Stream response
```

**Why This Design?**
- **Intuitive** - Follows REST conventions
- **Stateless** - Each request is independent
- **Cacheable** - GET requests can be cached
- **Scalable** - Load balance across instances

#### Versioning Strategy
```java
@RequestMapping("/api/v1/chat")  // Version in URL
// or
@RequestMapping(value = "/api/chat", 
                headers = "API-Version=1")  // Version in header
```

**Why Version APIs?**
- **Evolution** - Change without breaking clients
- **Migration** - Gradual client updates
- **Testing** - A/B test new versions
- **Support** - Maintain old versions temporarily

## Production Considerations

### 1. Rate Limiting

```java
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter = RateLimiter.create(10.0); // 10 req/sec
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        if (!rateLimiter.tryAcquire()) {
            response.setStatus(429); // Too Many Requests
            return false;
        }
        return true;
    }
}
```

**Why Rate Limit?**
- **Cost Control** - AI API calls are expensive
- **Fair Usage** - Prevent single user monopolizing
- **Protection** - Guard against abuse
- **SLA Compliance** - Stay within provider limits

### 2. Monitoring and Metrics

```java
@Component
public class ChatMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordChatRequest(String model, long duration, boolean success) {
        meterRegistry.timer("chat.request.duration",
            "model", model,
            "success", String.valueOf(success)
        ).record(duration, TimeUnit.MILLISECONDS);
        
        meterRegistry.counter("chat.request.total",
            "model", model,
            "success", String.valueOf(success)
        ).increment();
    }
}
```

**Key Metrics to Track:**
- Response times
- Token usage
- Error rates
- Active conversations
- Model performance

### 3. Security Considerations

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/chat/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()));
        
        return http.build();
    }
}
```

**Security Checklist:**
- ✅ Authentication required
- ✅ Input validation
- ✅ Rate limiting
- ✅ CORS configuration
- ✅ API key protection
- ✅ Audit logging

### 4. Scalability Patterns

#### Horizontal Scaling with Sticky Sessions
```yaml
# application.yml
spring:
  session:
    store-type: redis
    redis:
      namespace: chatbot:sessions
```

**Why Sticky Sessions?**
- In-memory conversations need same instance
- Alternatively, use distributed memory (Redis)

#### Async Processing
```java
@Async
public CompletableFuture<String> processMessageAsync(String conversationId, 
                                                    String message) {
    return CompletableFuture.completedFuture(
        processMessage(conversationId, message)
    );
}
```

**Why Async?**
- Handle more concurrent requests
- Better resource utilization
- Improved responsiveness

## Testing Strategies

### 1. Unit Testing Services
```java
@Test
void testProcessMessage() {
    // Given
    String conversationId = "test-123";
    String message = "Hello";
    when(chatClient.prompt()).thenReturn(mockPromptSpec);
    
    // When
    String response = chatbotService.processMessage(conversationId, message);
    
    // Then
    assertNotNull(response);
    verify(chatClient).prompt();
}
```

### 2. Integration Testing Controllers
```java
@Test
void testChatEndpoint() throws Exception {
    mockMvc.perform(post("/api/chat/test-123")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"message\": \"Hello\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.conversationId").value("test-123"));
}
```

### 3. Load Testing
```bash
# Using Apache Bench
ab -n 1000 -c 10 -p request.json -T application/json \
   http://localhost:8080/api/chat/test-123
```

## Common Pitfalls and Solutions

### ❌ Pitfall: Unbounded Conversation Growth
```java
// Bad: No cleanup
Map<String, List<Message>> conversations = new HashMap<>();
```

### ✅ Solution: Implement Cleanup
```java
@Scheduled(fixedDelay = 3600000)
public void cleanupInactiveConversations() {
    long cutoff = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24);
    lastInteractionTimes.entrySet().removeIf(
        entry -> entry.getValue() < cutoff
    );
}
```

### ❌ Pitfall: Blocking in Reactive Pipeline
```java
// Bad: Blocks event loop
webClient.get()
    .retrieve()
    .bodyToMono(String.class)
    .map(data -> chatService.processMessage("123", data)) // Blocks!
```

### ✅ Solution: Use Reactive Patterns
```java
// Good: Fully reactive
webClient.get()
    .retrieve()
    .bodyToMono(String.class)
    .flatMap(data -> Mono.fromCallable(() -> 
        chatService.processMessage("123", data))
        .subscribeOn(Schedulers.boundedElastic()))
```

## Key Takeaways

1. **Layer your architecture** for maintainability
2. **Use DTOs** for clear API contracts
3. **Handle errors gracefully** with global handlers
4. **Stream responses** for better UX
5. **Plan for production** from the start
6. **Monitor everything** to understand behavior
7. **Secure by default** with proper authentication

## What's Next?

Ready for advanced features? Explore:
- 📄 [S6: Advanced Features](s6-advanced-guide.md) - Structured output
- 📄 [S7: Advisors Guide](s7-advisors-guide.md) - Custom advisors
- 🏗️ [Performance Guide](../architecture/performance.md) - Optimization

---

[← S4: State Management](s4-state-guide.md) | [Back to Modules](../README.md#module-guides) | [S6: Advanced Features →](s6-advanced-guide.md)