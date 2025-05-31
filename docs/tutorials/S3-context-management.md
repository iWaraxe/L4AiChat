# 🧠 Tutorial S3: Context & Memory Management

> **⏱️ Duration**: 45 minutes  
> **🎯 Difficulty**: 🟡 Intermediate  
> **📋 Prerequisites**: Complete [Tutorial S2](./S2-components-deep-dive.md)

## 🎯 Learning Objectives

By the end of this tutorial, you will:
- ✅ Understand the difference between in-memory and persistent storage
- ✅ Configure MessageChatMemoryAdvisor for conversation context
- ✅ Implement JDBC-based chat history storage
- ✅ Manage memory windows and conversation cleanup
- ✅ Design scalable memory strategies for production

## 🛠️ Hands-On Exercise: Build a Persistent Chat Memory System

### Step 1: Understand Memory Architecture

Let's explore the S3 memory management implementation:

```bash
# Navigate to the S3 module
cd src/main/java/com/coherentsolutions/l4aichat/s3context

# Check the service implementations
ls -la service/
```

**🤔 Question**: What are the two memory implementations in this module?

<details>
<summary>💡 Click to reveal the answer</summary>

**Two Memory Strategies**:

1. **InMemoryChatService**: 
   - Stores conversations in application memory (HashMap)
   - Fast access, but data lost on restart
   - Good for development and simple use cases

2. **JdbcChatService**:
   - Persists conversations in database
   - Survives application restarts
   - Production-ready with scalability

**Memory Components**:
```
ChatMemoryRepository (Interface)
├── InMemoryChatMemoryRepository (Development)
└── JdbcChatMemoryRepository (Production)
```
</details>

### Step 2: Test In-Memory Storage

Start with the default in-memory configuration:

```bash
# Start S3 application with in-memory storage
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s3context.ContextApplication
```

```bash
# Start a conversation
curl -X POST http://localhost:8080/api/s3/chat/new \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello! My name is Sarah and I am a Java developer."}'
```

**Expected Response**:
```json
{
  "response": "Hello Sarah! Nice to meet you. It's great to connect with a fellow Java developer...",
  "conversationId": "conv-12345",
  "timestamp": "2024-01-01T10:00:00Z"
}
```

### Step 3: Test Memory Persistence

Continue the conversation to verify memory works:

```bash
# Use the conversationId from previous response
curl -X POST http://localhost:8080/api/s3/chat/conv-12345 \
  -H "Content-Type: application/json" \
  -d '{"message": "What programming language do I work with?"}'
```

**🎯 Challenge**: The AI should remember you're a Java developer!

### Step 4: Switch to JDBC Storage

Now let's test persistent storage:

```bash
# Stop the application (Ctrl+C)
# Restart with JDBC profile
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s3context.ContextApplication -Dspring-boot.run.profiles=jdbc
```

**🔍 Observation**: Notice the H2 database initialization in the logs!

### Step 5: Explore the Database

Access the H2 console to see stored conversations:

1. Open browser: http://localhost:8080/h2-console
2. Use these settings:
   - **JDBC URL**: `jdbc:h2:mem:chatdb`
   - **Username**: `sa`
   - **Password**: (empty)

```sql
-- View the chat memory table structure
SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CHAT_MEMORY';

-- See stored conversations
SELECT * FROM CHAT_MEMORY;
```

### Step 6: Test Persistence Across Restarts

```bash
# Start a conversation with JDBC storage
curl -X POST http://localhost:8080/api/s3/chat/new \
  -H "Content-Type: application/json" \
  -d '{"message": "I am learning Spring AI. Please remember this for our conversation."}'

# Note the conversationId, then restart the application
# After restart, continue the conversation with the same ID
curl -X POST http://localhost:8080/api/s3/chat/your-conversation-id \
  -H "Content-Type: application/json" \
  -d '{"message": "What was I learning about?"}'
```

**🎯 Challenge**: The conversation should persist even after application restart!

## 💡 Concept Deep-Dive: Memory Management Strategies

### **1. Memory Repository Pattern**

Spring AI uses a repository pattern for memory management:

```java
// Interface for all memory implementations
public interface ChatMemoryRepository {
    List<Message> getMessages(String conversationId);
    void addMessage(String conversationId, Message message);
    void deleteMessages(String conversationId);
}

// In-memory implementation (development)
public class InMemoryChatMemoryRepository implements ChatMemoryRepository {
    private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();
    
    @Override
    public List<Message> getMessages(String conversationId) {
        return conversations.getOrDefault(conversationId, new ArrayList<>());
    }
}

// JDBC implementation (production)
public class JdbcChatMemoryRepository implements ChatMemoryRepository {
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public List<Message> getMessages(String conversationId) {
        return jdbcTemplate.query(
            "SELECT * FROM chat_memory WHERE conversation_id = ? ORDER BY created_at",
            new MessageRowMapper(),
            conversationId
        );
    }
}
```

### **2. MessageChatMemoryAdvisor Configuration**

The advisor manages how memory is applied to conversations:

```java
@Bean
public MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
    return MessageChatMemoryAdvisor.builder(chatMemory)
        .chatMemoryRetrieveSize(100)    // Max messages to retrieve
        .chatMemoryAddSize(10)          // Max messages to add per interaction
        .build();
}

// Using the advisor in ChatClient
@Bean
public ChatClient chatClient(ChatClient.Builder builder, 
                           MessageChatMemoryAdvisor memoryAdvisor) {
    return builder
        .defaultAdvisors(memoryAdvisor)
        .build();
}
```

### **3. Memory Window Management**

Control how much conversation history to maintain:

```java
// Configure memory window size
MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(chatMemoryRepository)
    .maxMessages(20)  // Keep last 20 messages only
    .build();

// Dynamic window sizing based on conversation type
public ChatMemory createChatMemory(ConversationType type) {
    return switch (type) {
        case QUICK_ANSWER -> MessageWindowChatMemory.builder()
            .maxMessages(4)     // Small window for simple Q&A
            .build();
        case DETAILED_ANALYSIS -> MessageWindowChatMemory.builder()
            .maxMessages(50)    // Large window for complex discussions
            .build();
        case CODING_SESSION -> MessageWindowChatMemory.builder()
            .maxMessages(30)    // Medium window for code reviews
            .build();
    };
}
```

## 🧪 Live Experiment: Memory Configuration

Let's experiment with different memory strategies:

### Experiment 1: Window Size Impact

1. Open `ChatService.java` or create a test configuration
2. Try different window sizes:

```java
// Small window - only recent context
MessageWindowChatMemory smallMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(repository)
    .maxMessages(3)  // Very small window
    .build();

// Large window - extensive context
MessageWindowChatMemory largeMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(repository)
    .maxMessages(100)  // Large window
    .build();
```

**Test Scenario**: Start a long conversation about multiple topics and see how window size affects the AI's ability to reference earlier topics.

### Experiment 2: Custom Memory Filtering

Create intelligent memory management:

```java
// Custom memory implementation that filters by importance
public class SmartChatMemory implements ChatMemory {
    private final ChatMemoryRepository repository;
    private final MessageImportanceAnalyzer analyzer;
    
    @Override
    public List<Message> getMessages(String conversationId) {
        List<Message> allMessages = repository.getMessages(conversationId);
        
        // Keep system messages and important user/assistant messages
        return allMessages.stream()
            .filter(msg -> msg instanceof SystemMessage || 
                          analyzer.isImportant(msg))
            .collect(toList());
    }
}

// Importance analyzer
@Component
public class MessageImportanceAnalyzer {
    public boolean isImportant(Message message) {
        String content = message.getContent();
        
        // Keep messages with questions
        if (content.contains("?")) return true;
        
        // Keep messages with specific keywords
        if (containsKeywords(content, "error", "problem", "issue")) return true;
        
        // Keep messages that define requirements
        if (containsKeywords(content, "need", "require", "must")) return true;
        
        return false;
    }
}
```

### Experiment 3: Performance Monitoring

Add monitoring to understand memory usage:

```java
@Component
public class MemoryPerformanceMonitor {
    private final MeterRegistry meterRegistry;
    
    public void recordMemoryRetrieval(String conversationId, int messageCount, long durationMs) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("chat.memory.retrieval")
            .tag("conversation_id", conversationId)
            .tag("message_count", String.valueOf(messageCount))
            .register(meterRegistry));
    }
    
    public void recordMemorySize(String conversationId, int totalMessages) {
        meterRegistry.gauge("chat.memory.size", 
                          Tags.of("conversation_id", conversationId), 
                          totalMessages);
    }
}
```

## 🗄️ Database Schema Deep Dive

Understanding the JDBC storage structure:

### H2 Schema (Development)

```sql
-- Chat memory table structure
CREATE TABLE chat_memory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(50) NOT NULL,     -- 'user', 'assistant', 'system'
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSON
);

-- Index for performance
CREATE INDEX idx_chat_memory_conversation ON chat_memory(conversation_id);
CREATE INDEX idx_chat_memory_created_at ON chat_memory(created_at);
```

### Production Considerations

```sql
-- PostgreSQL production schema
CREATE TABLE chat_memory (
    id BIGSERIAL PRIMARY KEY,
    conversation_id UUID NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    metadata JSONB,
    user_id UUID,  -- For multi-tenant applications
    
    -- Partitioning by date for large datasets
    CONSTRAINT chat_memory_created_at_check 
        CHECK (created_at >= DATE '2024-01-01')
) PARTITION BY RANGE (created_at);

-- Create monthly partitions
CREATE TABLE chat_memory_2024_01 PARTITION OF chat_memory
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
```

## ✅ Check Your Understanding

### Quick Quiz

1. **What happens to in-memory conversations when you restart the application?**
   - A) They are saved to disk automatically
   - B) They are lost completely
   - C) They are transferred to the database
   - D) They are cached in browser storage

<details>
<summary>Answer</summary>
**B) They are lost completely** - In-memory storage exists only in application memory and is cleared on restart.
</details>

2. **What is the purpose of maxMessages in MessageWindowChatMemory?**
   - A) To limit API costs
   - B) To prevent memory overflow and control context size
   - C) To improve response speed
   - D) To reduce database storage

<details>
<summary>Answer</summary>
**B) To prevent memory overflow and control context size** - It maintains a sliding window of recent messages to balance context richness with performance.
</details>

3. **Which memory strategy is best for production applications?**
   - A) InMemoryChatMemoryRepository
   - B) JdbcChatMemoryRepository  
   - C) File-based storage
   - D) Browser localStorage

<details>
<summary>Answer</summary>
**B) JdbcChatMemoryRepository** - JDBC provides persistence, scalability, and reliability needed for production systems.
</details>

### Coding Challenge 🏆

**Challenge**: Create a "Conversation Archive" feature that:
1. Moves old conversations to an archive table after 30 days
2. Provides a search API to find archived conversations
3. Allows restoring archived conversations to active memory

**Requirements**:
```java
@RestController
public class ConversationArchiveController {
    
    @PostMapping("/api/s3/archive/{conversationId}")
    public ResponseEntity<String> archiveConversation(@PathVariable String conversationId) {
        // Move conversation to archive
    }
    
    @GetMapping("/api/s3/archive/search")
    public ResponseEntity<List<ArchivedConversation>> searchArchive(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        // Search archived conversations
    }
    
    @PostMapping("/api/s3/archive/{conversationId}/restore")
    public ResponseEntity<String> restoreConversation(@PathVariable String conversationId) {
        // Restore from archive to active memory
    }
}
```

<details>
<summary>💡 Solution</summary>

```java
// Archive entity
@Entity
@Table(name = "conversation_archive")
public class ArchivedConversation {
    @Id
    private String conversationId;
    
    @Column(columnDefinition = "JSON")
    private String messagesJson;
    
    private LocalDateTime archivedAt;
    private LocalDateTime lastActivity;
    private String summary;  // AI-generated conversation summary
}

// Archive service
@Service
public class ConversationArchiveService {
    
    @Scheduled(cron = "0 0 2 * * ?")  // Daily at 2 AM
    public void archiveOldConversations() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        
        List<String> oldConversations = chatMemoryRepository
            .findConversationsOlderThan(cutoff);
            
        for (String convId : oldConversations) {
            archiveConversation(convId);
        }
    }
    
    public void archiveConversation(String conversationId) {
        // Get messages
        List<Message> messages = chatMemory.get(conversationId);
        
        // Generate summary using AI
        String summary = generateConversationSummary(messages);
        
        // Save to archive
        ArchivedConversation archive = new ArchivedConversation(
            conversationId,
            jsonMapper.writeValueAsString(messages),
            LocalDateTime.now(),
            getLastMessageTime(messages),
            summary
        );
        
        archiveRepository.save(archive);
        
        // Remove from active memory
        chatMemory.clear(conversationId);
    }
    
    private String generateConversationSummary(List<Message> messages) {
        String conversation = messages.stream()
            .map(Message::getContent)
            .collect(joining("\n"));
            
        return summaryClient.prompt()
            .system("Summarize this conversation in 2-3 sentences")
            .user(conversation)
            .call()
            .content();
    }
}
```
</details>

## 🎯 Real-World Scenario: Customer Service Memory

**Scenario**: You're building a customer service chatbot that needs to:
1. Remember customer information across sessions
2. Maintain conversation history for quality assurance
3. Handle high volume with good performance
4. Comply with data retention policies (GDPR)

**Your Task**: Design the memory architecture for this system.

### Solution Architecture

```java
// Multi-tier memory strategy
@Configuration
public class CustomerServiceMemoryConfig {
    
    // Hot memory - recent active conversations
    @Bean("hotMemory")
    public ChatMemory hotMemory() {
        return MessageWindowChatMemory.builder()
            .chatMemoryRepository(redisRepository())  // Redis for speed
            .maxMessages(50)
            .build();
    }
    
    // Warm memory - recent but inactive conversations  
    @Bean("warmMemory")
    public ChatMemory warmMemory() {
        return MessageWindowChatMemory.builder()
            .chatMemoryRepository(jdbcRepository())  // Database for persistence
            .maxMessages(200)
            .build();
    }
    
    // Cold storage - archived conversations
    @Bean
    public ArchiveService archiveService() {
        return new ArchiveService(s3Repository());  // S3 for long-term storage
    }
}

// Smart memory router
@Service
public class SmartMemoryRouter {
    
    public ChatMemory getMemoryForConversation(String conversationId) {
        ConversationMetadata metadata = getConversationMetadata(conversationId);
        
        if (metadata.getLastActivity().isAfter(LocalDateTime.now().minusHours(1))) {
            return hotMemory;  // Active conversation
        }
        
        if (metadata.getLastActivity().isAfter(LocalDateTime.now().minusDays(7))) {
            return warmMemory;  // Recent conversation
        }
        
        return coldMemory;  // Archive access
    }
}
```

## 🔗 Next Steps

Outstanding work! You've mastered conversation memory and persistence strategies.

**What you've learned**:
- ✅ In-memory vs JDBC storage trade-offs
- ✅ MessageChatMemoryAdvisor configuration
- ✅ Memory window management strategies
- ✅ Database schema design for chat storage
- ✅ Production-ready memory architectures

**Ready for advanced state management?** 

👉 **Continue to [Tutorial S4: State Management Patterns](./S4-state-management.md)** to learn about:
- Advanced conversation lifecycle management
- Automated conversation cleanup strategies
- Multi-user conversation isolation
- Performance optimization techniques

## 📚 Additional Resources

- 📖 [S3 Module Guide](../modules/S3-context.md) - Complete memory management reference
- 🏗️ [Memory Architecture](../architecture/patterns.md#memory-patterns) - Advanced memory patterns
- 🔧 [Database Performance](../guides/database-optimization.md) - Production database tips

---

**🎉 Excellent progress!** You now understand how to build scalable, persistent chat applications.

The next tutorial will show you advanced state management patterns for handling complex conversation workflows and performance optimization.

[← Previous: S2 Components](./S2-components-deep-dive.md) | [Next: S4 State Management →](./S4-state-management.md)