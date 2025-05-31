# 📙 Memory and State Management

## Table of Contents
1. [Why Memory Matters](#why-memory-matters)
2. [Spring AI Memory Architecture](#spring-ai-memory-architecture)
3. [Memory Implementation Strategies](#memory-implementation-strategies)
4. [Choosing the Right Approach](#choosing-the-right-approach)
5. [Production Considerations](#production-considerations)
6. [Advanced Patterns](#advanced-patterns)

## Why Memory Matters

### The Conversation Problem

AI models are **stateless** by design. Each API call is independent:

```
User: "My name is John"
AI: "Nice to meet you, John!"
User: "What's my name?"
AI: "I don't know your name." ❌
```

### The Solution: Conversation Memory

With memory management:

```
User: "My name is John"
AI: "Nice to meet you, John!"
User: "What's my name?"
AI: "Your name is John." ✅
```

### Why is Memory Critical?

1. **Context Preservation** - Multi-turn conversations require history
2. **User Experience** - Natural conversations feel continuous
3. **Task Completion** - Complex tasks span multiple interactions
4. **Personalization** - Remember user preferences and context

## Spring AI Memory Architecture

### Core Components

```
┌─────────────────────────────────────────┐
│         ChatMemory Interface            │
│    (Conversation-level operations)      │
├─────────────────────────────────────────┤
│     MessageWindowChatMemory             │
│    (Sliding window implementation)      │
├─────────────────────────────────────────┤
│    ChatMemoryRepository Interface       │
│      (Storage abstraction)              │
├─────────────────────────────────────────┤
│  InMemory │ JDBC │ Redis │ Custom      │
│  (Repository Implementations)           │
└─────────────────────────────────────────┘
```

### Key Concepts

#### 1. ChatMemory
The high-level interface for managing conversation history.

```java
public interface ChatMemory {
    void add(String conversationId, Message message);
    List<Message> get(String conversationId);
    void clear(String conversationId);
}
```

**Why This Design?**
- **Simple API** - Easy to understand and implement
- **Conversation Isolation** - Multi-tenant safe
- **Flexibility** - Support different storage strategies

#### 2. MessageWindowChatMemory
The default implementation using a sliding window approach.

```java
MessageWindowChatMemory.builder()
    .chatMemoryRepository(repository)
    .maxMessages(10)  // Keep last 10 messages
    .build()
```

**Why Windowing?**
- **Token Limits** - AI models have context size limits
- **Cost Control** - More context = higher API costs
- **Performance** - Smaller contexts process faster
- **Relevance** - Recent messages usually more important

#### 3. ChatMemoryRepository
The storage abstraction layer.

**Why Separate Storage?**
- **Deployment Flexibility** - In-memory for dev, database for prod
- **Scalability** - Choose storage based on scale needs
- **Testing** - Easy to mock for unit tests
- **Migration** - Switch storage without changing logic

## Memory Implementation Strategies

### 1. In-Memory Storage

```java
@Configuration
public class DevMemoryConfig {
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
            .chatMemoryRepository(new InMemoryChatMemoryRepository())
            .maxMessages(20)
            .build();
    }
}
```

**When to Use:**
- ✅ Development and testing
- ✅ Single-instance applications
- ✅ Short-lived conversations
- ✅ Demo applications

**Limitations:**
- ❌ Data lost on restart
- ❌ Not scalable across instances
- ❌ Memory constraints

### 2. JDBC Storage

```java
@Configuration
@Profile("production")
public class ProdMemoryConfig {
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
            .maxMessages(50)  // Can afford more with persistent storage
            .build();
    }
}
```

**When to Use:**
- ✅ Production applications
- ✅ Need persistence across restarts
- ✅ Multi-instance deployments
- ✅ Audit requirements

**Advantages:**
- 🏢 Enterprise-ready
- 🔄 Transactional support
- 📊 Query capabilities
- 🔒 Backup and recovery

### 3. Custom Implementations

```java
@Component
public class RedisChatMemoryRepository implements ChatMemoryRepository {
    private final RedisTemplate<String, Message> redisTemplate;
    
    @Override
    public void add(String conversationId, Message message) {
        String key = "chat:" + conversationId;
        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.expire(key, Duration.ofHours(24));
    }
}
```

**When to Use:**
- ✅ High-performance requirements
- ✅ Distributed caching needs
- ✅ TTL (Time-To-Live) requirements
- ✅ Pub/sub patterns

## Choosing the Right Approach

### Decision Matrix

| Requirement | In-Memory | JDBC | Redis | Custom |
|------------|-----------|------|-------|---------|
| Development Speed | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐ |
| Persistence | ❌ | ✅ | ⚠️ | Varies |
| Scalability | ⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | Varies |
| Performance | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ | Varies |
| Complexity | ⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Cost | $ | $$ | $$$ | Varies |

### Use Case Recommendations

#### 🎯 Chatbot for Internal Tools
```java
// Simple in-memory is fine
MessageWindowChatMemory.builder()
    .chatMemoryRepository(new InMemoryChatMemoryRepository())
    .maxMessages(10)
    .build()
```

#### 🎯 Customer Service Platform
```java
// JDBC for persistence and audit
MessageWindowChatMemory.builder()
    .maxMessages(100)  // Keep more history
    .build()  // Auto-configured JDBC repository
```

#### 🎯 High-Traffic Public API
```java
// Redis for performance
@Bean
public ChatMemoryRepository chatMemoryRepository() {
    return new RedisChatMemoryRepository(redisTemplate);
}
```

## Production Considerations

### 1. Memory Limits and Token Management

```java
@Service
public class ConversationService {
    private static final int MAX_TOKENS = 4000;
    private static final int AVG_TOKENS_PER_MESSAGE = 50;
    
    @Value("${chat.memory.max-messages:20}")
    private int maxMessages;
    
    @PostConstruct
    public void validateConfig() {
        int estimatedTokens = maxMessages * AVG_TOKENS_PER_MESSAGE;
        if (estimatedTokens > MAX_TOKENS) {
            log.warn("Max messages may exceed token limit");
        }
    }
}
```

**Why:** Prevent token limit errors and control costs

### 2. Conversation Lifecycle Management

```java
@Component
public class ConversationCleaner {
    @Scheduled(fixedDelay = 3600000) // Every hour
    public void cleanupStaleConversations() {
        Instant cutoff = Instant.now().minus(Duration.ofHours(24));
        conversationRepository.deleteByLastActivityBefore(cutoff);
    }
}
```

**Why:** Prevent unbounded growth and comply with data retention policies

### 3. Security and Privacy

```java
@Service
public class SecureConversationService {
    public void clearSensitiveConversation(String conversationId, String userId) {
        // Verify ownership
        if (!conversationBelongsToUser(conversationId, userId)) {
            throw new AccessDeniedException("Not authorized");
        }
        
        // Clear with audit trail
        auditService.log("CONVERSATION_CLEARED", userId, conversationId);
        chatMemory.clear(conversationId);
    }
}
```

**Why:** Protect user privacy and maintain compliance

### 4. Performance Optimization

```java
@Configuration
public class MemoryOptimizationConfig {
    @Bean
    @Profile("high-performance")
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
            .chatMemoryRepository(new CachedRepository(
                new JdbcChatMemoryRepository(),
                cacheManager
            ))
            .maxMessages(20)
            .build();
    }
}
```

**Why:** Balance performance with persistence needs

## Advanced Patterns

### 1. Contextual Memory Windows

```java
@Service
public class ContextualMemoryService {
    public ChatMemory getMemoryForTask(TaskType taskType) {
        return switch (taskType) {
            case QUICK_QUESTION -> buildMemory(5);   // Short context
            case ANALYSIS -> buildMemory(20);         // Medium context
            case CREATIVE_WRITING -> buildMemory(50); // Long context
        };
    }
    
    private ChatMemory buildMemory(int maxMessages) {
        return MessageWindowChatMemory.builder()
            .maxMessages(maxMessages)
            .build();
    }
}
```

**Why:** Optimize context size for different use cases

### 2. Hierarchical Memory

```java
@Service
public class HierarchicalMemoryService {
    private final ChatMemory shortTermMemory;  // Last 5 messages
    private final ChatMemory longTermMemory;   // Summary storage
    
    public String processWithMemory(String conversationId, String message) {
        // Get recent context
        List<Message> recent = shortTermMemory.get(conversationId);
        
        // Get summaries if needed
        if (needsLongTermContext(message)) {
            List<Message> summaries = longTermMemory.get(conversationId);
            // Combine contexts...
        }
        
        // Process and update both memories
    }
}
```

**Why:** Balance detail with broader context

### 3. Memory Compression

```java
@Component
public class MemoryCompressionAdvisor implements RequestAdvisor {
    @Override
    public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        List<Message> messages = chatMemory.get(conversationId);
        
        if (messages.size() > COMPRESSION_THRESHOLD) {
            String summary = summarizeMessages(messages.subList(0, messages.size() / 2));
            // Replace old messages with summary
        }
        
        return request;
    }
}
```

**Why:** Maintain context while reducing tokens

## Key Takeaways

1. **Memory is essential** for natural, context-aware conversations
2. **Choose storage based on requirements** - not all apps need JDBC
3. **Window size matters** - balance context with performance/cost
4. **Plan for production** - lifecycle, security, scalability
5. **Consider advanced patterns** for complex use cases

## Next Steps

Ready to see memory in action? Explore:
- 📄 [Module S3 Guide](modules/s3-context-guide.md) - Memory implementation examples
- 📄 [Module S4 Guide](modules/s4-state-guide.md) - Advanced state patterns
- 🏗️ [Performance Guide](architecture/performance.md) - Optimization strategies

---

[← ChatClient Deep Dive](02-chatclient-deep-dive.md) | [Back to Main README](../README.md) | [Module Guides →](modules/s1-multiturn-guide.md)