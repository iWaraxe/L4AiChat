# 📄 Module S3: Context Management Guide

## Overview

This module demonstrates how to manage conversation context and memory in Spring AI applications. You'll learn when and how to implement different memory strategies, from simple in-memory storage to persistent database solutions.

## Learning Objectives

By completing this module, you will:
- ✅ Understand why conversation memory matters
- ✅ Implement in-memory vs persistent memory strategies
- ✅ Configure JDBC-based conversation storage
- ✅ Learn memory cleanup and optimization patterns
- ✅ Design APIs for conversational applications

## Why Context Management Is Critical

Without memory, every AI conversation starts from scratch:

```
User: "My name is Alice"
AI: "Nice to meet you, Alice!"
User: "What's my name?"
AI: "I don't know your name." ❌
```

With proper context management:
```
User: "My name is Alice"
AI: "Nice to meet you, Alice!"
User: "What's my name?"
AI: "Your name is Alice." ✅
```

## Module Structure

```
s3context/
├── ContextApplication.java          # Main application
├── JdbcConfig.java                 # Database configuration
├── controller/
│   └── ChatController.java         # REST endpoints
├── dto/
│   ├── ChatRequest.java           # Request structure
│   └── ChatResponse.java          # Response structure
└── service/
    ├── ChatService.java            # Business logic
    ├── InMemoryChatService.java    # Memory-based impl
    └── JdbcChatService.java        # Database-based impl
```

## Core Concepts

### 1. Memory Strategies Comparison

#### In-Memory Strategy
```java
@Service
@Profile("!jdbc")
public class InMemoryChatService implements ChatService {
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    
    public InMemoryChatService(ChatClient.Builder builder) {
        // Memory stays in application RAM
        this.chatMemory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(new InMemoryChatMemoryRepository())
            .maxMessages(20)
            .build();
            
        this.chatClient = builder
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build();
    }
}
```

**When to Use In-Memory:**
- ✅ Development and testing
- ✅ Single-instance deployments
- ✅ Short-lived conversations
- ✅ Privacy-sensitive applications

**Advantages:**
- **Fast** - No database round trips
- **Simple** - No setup required
- **Private** - Data never leaves memory
- **Cost Effective** - No storage costs

**Disadvantages:**
- **Volatile** - Lost on restart
- **Single Instance** - Can't scale horizontally
- **Limited** - Memory constraints
- **No Persistence** - Can't resume conversations

#### JDBC/Database Strategy
```java
@Service
@Profile("jdbc")
public class JdbcChatService implements ChatService {
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    
    public JdbcChatService(ChatClient.Builder builder, ChatMemoryRepository repository) {
        // Memory persisted to database
        this.chatMemory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(repository)  // Auto-configured JDBC repository
            .maxMessages(50)  // Can afford larger windows
            .build();
            
        this.chatClient = builder
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build();
    }
}
```

**When to Use Database:**
- ✅ Production environments
- ✅ Multi-instance deployments
- ✅ Long-term conversations
- ✅ Analytics and reporting needs

**Advantages:**
- **Persistent** - Survives restarts
- **Scalable** - Share across instances
- **Searchable** - Query conversation history
- **Auditable** - Complete conversation logs

**Disadvantages:**
- **Complexity** - Database setup required
- **Latency** - Network overhead
- **Cost** - Storage and compute costs
- **Privacy** - Data must be secured

### 2. Profile-Based Configuration

Spring AI 1.0.0 simplifies configuration through auto-configuration:

#### Default (In-Memory) Profile
```properties
# application.properties
# No specific configuration needed
# InMemoryChatMemoryRepository auto-configured
```

#### JDBC Profile
```properties
# application-jdbc.properties
spring.datasource.url=jdbc:h2:mem:chatdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Enable H2 console for development
spring.h2.console.enabled=true

# JPA settings
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

**Why Profile-Based?**
- **Environment Flexibility** - Easy switching
- **Testing** - Different configs for different tests
- **Deployment** - Choose strategy at runtime
- **Migration** - Gradual transition between strategies

### 3. Auto-Configuration Magic

Spring AI 1.0.0 provides automatic configuration:

```java
@Configuration
public class JdbcConfig {
    // No manual configuration needed!
    // Spring AI auto-configures:
    // - JdbcChatMemoryRepository
    // - Database schema
    // - Connection pooling
    
    @Bean
    @ConditionalOnProfile("jdbc")
    public ChatMemory chatMemory(ChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
            .chatMemoryRepository(repository)  // Auto-injected
            .maxMessages(50)
            .build();
    }
}
```

**What Gets Auto-Configured:**
- **Database Schema** - Tables created automatically
- **Repository Implementation** - JDBC operations handled
- **Connection Management** - Pooling and transactions
- **Error Handling** - Database-specific error mapping

### 4. Window Size Strategy

#### Small Windows (10-20 messages)
```java
MessageWindowChatMemory.builder()
    .maxMessages(15)  // ~750-1500 tokens
    .build()
```

**When to Use:**
- Customer service bots
- Simple Q&A applications
- Cost-sensitive deployments
- Fast response requirements

#### Medium Windows (20-50 messages)
```java
MessageWindowChatMemory.builder()
    .maxMessages(35)  // ~1750-3500 tokens
    .build()
```

**When to Use:**
- Personal assistants
- Educational applications
- Technical support
- General conversational AI

#### Large Windows (50+ messages)
```java
MessageWindowChatMemory.builder()
    .maxMessages(100)  // ~5000-10000 tokens
    .build()
```

**When to Use:**
- Complex problem solving
- Creative writing assistance
- Long-term coaching/therapy
- Research and analysis

**Token Calculation:**
```java
// Rough estimate: 1 message = 50-100 tokens
// 20 messages × 75 tokens average = 1,500 tokens
// Plus system prompt (100-500 tokens)
// Total context: ~2,000 tokens per request
```

## Service Layer Patterns

### 1. Interface-Based Design
```java
public interface ChatService {
    ChatResponse processMessage(String conversationId, String message);
    List<Message> getConversationHistory(String conversationId);
    void clearConversation(String conversationId);
}
```

**Why Interface?**
- **Strategy Pattern** - Switch implementations easily
- **Testing** - Mock the interface
- **Extensibility** - Add new strategies without changing clients
- **Clarity** - Clear contract definition

### 2. Implementation Strategy Pattern
```java
@Component
public class ChatServiceFactory {
    private final InMemoryChatService inMemoryService;
    private final JdbcChatService jdbcService;
    
    public ChatService getChatService(@Value("${chat.storage.type:memory}") String type) {
        return switch (type.toLowerCase()) {
            case "jdbc", "database" -> jdbcService;
            case "memory", "inmemory" -> inMemoryService;
            default -> throw new IllegalArgumentException("Unknown storage type: " + type);
        };
    }
}
```

**Benefits:**
- **Runtime Switching** - Change strategy without restart
- **A/B Testing** - Compare strategies
- **Graceful Degradation** - Fallback to in-memory if DB fails

### 3. Error Handling and Resilience
```java
@Service
public class ResilientChatService implements ChatService {
    private final ChatService primaryService;
    private final ChatService fallbackService;
    
    @Override
    public ChatResponse processMessage(String conversationId, String message) {
        try {
            return primaryService.processMessage(conversationId, message);
        } catch (Exception e) {
            logger.warn("Primary service failed, using fallback", e);
            return fallbackService.processMessage(conversationId, message);
        }
    }
}
```

## API Design Patterns

### 1. RESTful Conversation Management
```java
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    
    // Start new conversation
    @PostMapping("/new")
    public ChatResponse newConversation(@Valid @RequestBody ChatRequest request) {
        String conversationId = UUID.randomUUID().toString();
        return chatService.processMessage(conversationId, request.getMessage());
    }
    
    // Continue existing conversation
    @PostMapping("/{conversationId}")
    public ChatResponse continueConversation(
            @PathVariable String conversationId,
            @Valid @RequestBody ChatRequest request) {
        return chatService.processMessage(conversationId, request.getMessage());
    }
    
    // Get conversation history
    @GetMapping("/{conversationId}/history")
    public List<MessageResponse> getHistory(@PathVariable String conversationId) {
        return chatService.getConversationHistory(conversationId)
            .stream()
            .map(this::toMessageResponse)
            .collect(Collectors.toList());
    }
    
    // Clear conversation
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> clearConversation(@PathVariable String conversationId) {
        chatService.clearConversation(conversationId);
        return ResponseEntity.ok().build();
    }
}
```

**Why This Design?**
- **Resource-Oriented** - Conversations are resources
- **Stateless** - Each request contains all needed info
- **Standard HTTP** - Familiar patterns for clients
- **Scalable** - Works with load balancers

### 2. DTO Design for Context
```java
public class ChatResponse {
    private String message;
    private String conversationId;
    private LocalDateTime timestamp;
    private int messageCount;        // For client-side optimization
    private boolean isNewConversation;
    
    // Constructors, getters, setters...
}
```

**Why Include Metadata?**
- **Client Optimization** - Avoid unnecessary requests
- **User Experience** - Show conversation stats
- **Debugging** - Track request flow
- **Analytics** - Measure engagement

## Advanced Patterns

### 1. Conversation Lifecycle Management
```java
@Component
public class ConversationManager {
    private final Map<String, Long> lastActivity = new ConcurrentHashMap<>();
    
    @EventListener
    public void onMessageProcessed(MessageProcessedEvent event) {
        lastActivity.put(event.getConversationId(), System.currentTimeMillis());
    }
    
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void cleanupInactiveConversations() {
        long cutoff = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24);
        
        lastActivity.entrySet().removeIf(entry -> {
            if (entry.getValue() < cutoff) {
                chatService.clearConversation(entry.getKey());
                logger.info("Cleaned up inactive conversation: {}", entry.getKey());
                return true;
            }
            return false;
        });
    }
}
```

**Why Lifecycle Management?**
- **Resource Control** - Prevent memory leaks
- **Cost Management** - Reduce storage costs
- **Privacy** - Auto-delete sensitive conversations
- **Performance** - Keep working set manageable

### 2. Memory Optimization Strategies

#### Conversation Summarization
```java
@Service
public class SummarizingChatService implements ChatService {
    private static final int TRIGGER_SIZE = 40;
    private static final int TARGET_SIZE = 20;
    
    @Override
    public ChatResponse processMessage(String conversationId, String message) {
        List<Message> history = chatMemory.get(conversationId);
        
        if (history.size() >= TRIGGER_SIZE) {
            summarizeAndTruncate(conversationId, history);
        }
        
        return processWithMemory(conversationId, message);
    }
    
    private void summarizeAndTruncate(String conversationId, List<Message> history) {
        // Get first half of conversation
        List<Message> toSummarize = history.subList(0, history.size() / 2);
        
        // Create summary
        String summary = chatClient.prompt()
            .system("Summarize this conversation concisely")
            .messages(toSummarize)
            .call()
            .content();
        
        // Replace with summary
        chatMemory.clear(conversationId);
        chatMemory.add(conversationId, new SystemMessage("Previous conversation: " + summary));
        
        // Add back recent messages
        List<Message> recentMessages = history.subList(history.size() / 2, history.size());
        recentMessages.forEach(msg -> chatMemory.add(conversationId, msg));
    }
}
```

**Why Summarization?**
- **Cost Control** - Reduce token usage
- **Context Preservation** - Keep important information
- **Performance** - Faster processing
- **Scalability** - Handle very long conversations

### 3. Context Enrichment Patterns
```java
@Service
public class EnrichedChatService implements ChatService {
    private final UserProfileService userProfileService;
    private final ChatService delegate;
    
    @Override
    public ChatResponse processMessage(String conversationId, String message) {
        // Enrich context with user profile
        String userId = extractUserId(conversationId);
        UserProfile profile = userProfileService.getProfile(userId);
        
        String enrichedMessage = String.format(
            "User context: %s\nUser message: %s",
            profile.getContextSummary(),
            message
        );
        
        return delegate.processMessage(conversationId, enrichedMessage);
    }
}
```

## Testing Strategies

### 1. Unit Testing Memory Strategies
```java
@Test
void testInMemoryConversation() {
    // Given
    ChatService service = new InMemoryChatService(chatClientBuilder);
    String conversationId = "test-123";
    
    // When
    ChatResponse response1 = service.processMessage(conversationId, "My name is Alice");
    ChatResponse response2 = service.processMessage(conversationId, "What's my name?");
    
    // Then
    assertThat(response2.getMessage()).containsIgnoringCase("Alice");
}

@Test
void testMemoryPersistence() {
    // Given
    ChatService jdbcService = new JdbcChatService(chatClientBuilder, jdbcRepository);
    String conversationId = "test-persist";
    
    // When - Add message and restart service
    jdbcService.processMessage(conversationId, "Remember this: important data");
    
    // Simulate service restart
    ChatService newService = new JdbcChatService(chatClientBuilder, jdbcRepository);
    ChatResponse response = newService.processMessage(conversationId, "What should I remember?");
    
    // Then
    assertThat(response.getMessage()).containsIgnoringCase("important data");
}
```

### 2. Integration Testing
```java
@SpringBootTest
@ActiveProfiles("jdbc")
class ChatControllerIntegrationTest {
    
    @Test
    void testConversationFlow() throws Exception {
        // Start conversation
        mockMvc.perform(post("/api/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\": \"Hello, I'm Bob\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").exists());
        
        // Continue conversation with memory
        String conversationId = extractConversationId(result);
        
        mockMvc.perform(post("/api/chat/" + conversationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\": \"What's my name?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("Bob")));
    }
}
```

## Common Pitfalls and Solutions

### ❌ Pitfall: Unbounded Memory Growth
```java
// Bad: No cleanup
Map<String, List<Message>> conversations = new HashMap<>();
// Memory usage grows forever
```

### ✅ Solution: Implement Cleanup
```java
// Good: Bounded memory with cleanup
@Scheduled(fixedDelay = 300000)
public void cleanupOldConversations() {
    conversationService.removeInactive(Duration.ofHours(24));
}
```

### ❌ Pitfall: Inconsistent Memory Strategies
```java
// Bad: Different strategies in same deployment
if (randomBoolean()) {
    return inMemoryService.processMessage(id, message);
} else {
    return jdbcService.processMessage(id, message);
}
```

### ✅ Solution: Consistent Strategy Selection
```java
// Good: Strategy chosen at startup
@Bean
public ChatService chatService() {
    return profileActive("jdbc") ? jdbcService : inMemoryService;
}
```

## Key Takeaways

1. **Choose memory strategy** based on persistence needs
2. **Use profiles** for flexible deployment configurations
3. **Implement cleanup** to prevent resource leaks
4. **Design for scalability** from the beginning
5. **Monitor memory usage** to optimize window sizes
6. **Test both strategies** to ensure consistency

## What's Next?

Ready for advanced state management? Continue to:
- 📄 [S4: State Management](s4-state-guide.md) - Advanced patterns
- 📄 [S5: Production Chatbot](s5-chatbot-guide.md) - Enterprise features
- 🏗️ [Performance Guide](../architecture/performance.md) - Optimization

---

[← S2: Components](s2-components-guide.md) | [Back to Modules](../README.md#module-guides) | [S4: State Management →](s4-state-guide.md)