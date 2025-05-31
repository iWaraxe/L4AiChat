# 🔄 Migration Guide: Spring AI 1.0.0-M7 to 1.0.0

This guide helps you migrate your Spring AI applications from version 1.0.0-M7 to the 1.0.0 final release.

## Overview of Changes

Spring AI 1.0.0 introduces several breaking changes focused on:
- 🏗️ **API Consistency** - More intuitive method names
- 🔧 **Builder Patterns** - Consistent configuration approach  
- 📦 **Memory Redesign** - Better abstraction for chat memory
- 🎯 **Type Safety** - Provider-specific options

## Step-by-Step Migration

### 1. Update Maven Dependencies

#### Update Version
```xml
<!-- Old -->
<spring-ai.version>1.0.0-M7</spring-ai.version>

<!-- New -->
<spring-ai.version>1.0.0</spring-ai.version>
```

#### Add New Dependencies (if using features)
```xml
<!-- For streaming support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- For JDBC memory (optional) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-chat-memory-repository-jdbc</artifactId>
</dependency>
```

### 2. Memory API Changes

The memory API has been completely redesigned for better flexibility.

#### ❌ Old Way (M7)
```java
import org.springframework.ai.chat.memory.InMemoryChatMemory;

// Direct instantiation
ChatMemory chatMemory = new InMemoryChatMemory();

// MessageChatMemoryAdvisor constructor
MessageChatMemoryAdvisor advisor = new MessageChatMemoryAdvisor(chatMemory);
```

#### ✅ New Way (1.0.0)
```java
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;

// Use builder with repository
ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(new InMemoryChatMemoryRepository())
    .maxMessages(20)  // Specify window size
    .build();

// MessageChatMemoryAdvisor uses builder
MessageChatMemoryAdvisor advisor = MessageChatMemoryAdvisor.builder(chatMemory)
    .build();
```

**Why This Change?**
- **Flexibility** - Separate storage from windowing logic
- **Configurability** - Easy to set window size
- **Extensibility** - Simple to add custom repositories

### 3. ChatMemory Method Changes

#### ❌ Old Way (M7)
```java
// Get with message count
List<Message> messages = chatMemory.get(conversationId, 10);

// Add message
chatMemory.add(conversationId, message);
```

#### ✅ New Way (1.0.0)
```java
// Get returns all messages (windowing handled by implementation)
List<Message> messages = chatMemory.get(conversationId);

// Add message (same)
chatMemory.add(conversationId, message);
```

**Why This Change?**
- **Simplification** - Window size configured once
- **Consistency** - Same API regardless of implementation

### 4. Advisor Parameter Names

#### ❌ Old Way (M7)
```java
// Custom parameter names
.advisors(a -> a
    .param("chat_memory_conversation_id", conversationId)
    .param("chat_memory_response_size", 100))
```

#### ✅ New Way (1.0.0)
```java
// Use constants
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

.advisors(a -> a.param(CONVERSATION_ID, conversationId))
```

**Why This Change?**
- **Type Safety** - No magic strings
- **Discoverability** - IDE autocomplete
- **Consistency** - Standard parameter names

### 5. ChatOptions to Provider-Specific Options

#### ❌ Old Way (M7)
```java
import org.springframework.ai.chat.prompt.ChatOptions;

ChatOptions options = ChatOptions.builder()
    .withTemperature(0.7f)
    .withMaxTokens(150)
    .build();
```

#### ✅ New Way (1.0.0)
```java
import org.springframework.ai.openai.OpenAiChatOptions;

OpenAiChatOptions options = OpenAiChatOptions.builder()
    .temperature(0.7)    // Note: double, not float
    .maxTokens(150)
    .model("gpt-4")      // Provider-specific options
    .build();
```

**Why This Change?**
- **Provider Features** - Access provider-specific options
- **Type Safety** - Only valid options available
- **Clarity** - Know which provider you're configuring

### 6. Response Content Access

#### ❌ Old Way (M7)
```java
// Sometimes getText(), sometimes getContent()
String content = response.getGeneration().getText();
// or
String content = response.getResult().getOutput().getText();
```

#### ✅ New Way (1.0.0)
```java
// Consistent getContent()
String content = response.getResult().getOutput().getContent();
// or use fluent API
String content = chatClient.prompt()
    .user("Hello")
    .call()
    .content();  // Direct content access
```

**Why This Change?**
- **Consistency** - One method name
- **Fluent API** - Easier direct access

### 7. JDBC Configuration

#### ❌ Old Way (M7)
```java
@Configuration
public class JdbcConfig {
    @Bean
    public DataSource dataSource() {
        // Manual DataSource configuration
    }
    
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    
    @Bean
    public ChatMemory chatMemory(JdbcTemplate jdbcTemplate) {
        return new JdbcChatMemory(jdbcTemplate);
    }
}
```

#### ✅ New Way (1.0.0)
```java
@Configuration
public class JdbcConfig {
    @Bean
    public ChatMemory chatMemory() {
        // Auto-configured with spring-ai-starter-model-chat-memory-repository-jdbc
        return MessageWindowChatMemory.builder()
            .maxMessages(50)
            .build();
    }
}
```

**Why This Change?**
- **Simplification** - Auto-configuration
- **Spring Boot Style** - Convention over configuration

### 8. Streaming API

#### ❌ Old Way (M7)
```java
// Manual streaming implementation
public Flux<String> streamResponse(String message) {
    String response = chatClient.prompt()
        .user(message)
        .call()
        .content();
    
    // Simulate streaming
    return Flux.fromStream(response.chars()
        .mapToObj(c -> String.valueOf((char) c)))
        .delayElements(Duration.ofMillis(50));
}
```

#### ✅ New Way (1.0.0)
```java
// Native streaming support
public Flux<String> streamResponse(String message) {
    return chatClient.prompt()
        .user(message)
        .stream()    // Native streaming
        .content();
}
```

**Why This Change?**
- **Performance** - Real streaming from provider
- **Simplicity** - No manual implementation
- **Features** - Supports cancellation

## Complete Migration Example

Here's a complete before/after example:

### Before (M7)
```java
@Service
public class ChatService {
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    
    public ChatService(ChatClient.Builder builder) {
        this.chatMemory = new InMemoryChatMemory();
        this.chatClient = builder
            .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
            .build();
    }
    
    public String chat(String conversationId, String message) {
        ChatOptions options = ChatOptions.builder()
            .withTemperature(0.7f)
            .build();
            
        return chatClient.prompt()
            .user(message)
            .options(options)
            .advisors(a -> a
                .param("chat_memory_conversation_id", conversationId)
                .param("chat_memory_response_size", 100))
            .call()
            .chatResponse()
            .getGeneration()
            .getText();
    }
    
    public List<Message> getHistory(String conversationId) {
        return chatMemory.get(conversationId, -1);
    }
}
```

### After (1.0.0)
```java
@Service
public class ChatService {
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    
    public ChatService(ChatClient.Builder builder) {
        this.chatMemory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(new InMemoryChatMemoryRepository())
            .maxMessages(20)
            .build();
            
        this.chatClient = builder
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build();
    }
    
    public String chat(String conversationId, String message) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
            .temperature(0.7)
            .build();
            
        return chatClient.prompt()
            .user(message)
            .options(options)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .content();  // Direct content access
    }
    
    public List<Message> getHistory(String conversationId) {
        return chatMemory.get(conversationId);  // No message count parameter
    }
}
```

## Common Migration Issues

### Issue 1: Cannot find InMemoryChatMemory
```
error: cannot find symbol
symbol: class InMemoryChatMemory
```

**Solution:** Replace with MessageWindowChatMemory + InMemoryChatMemoryRepository

### Issue 2: Constructor MessageChatMemoryAdvisor cannot be applied
```
error: constructor MessageChatMemoryAdvisor cannot be applied to given types
```

**Solution:** Use builder pattern: `MessageChatMemoryAdvisor.builder(chatMemory).build()`

### Issue 3: Method get(String, int) not found
```
error: method get in interface ChatMemory cannot be applied to given types
```

**Solution:** Remove the second parameter: `chatMemory.get(conversationId)`

### Issue 4: Cannot find ChatOptions
```
error: cannot find symbol
symbol: class ChatOptions
```

**Solution:** Use provider-specific options like `OpenAiChatOptions`

## Testing Your Migration

1. **Compile Test**
   ```bash
   mvn clean compile
   ```

2. **Run Tests**
   ```bash
   mvn test
   ```

3. **Integration Test**
   ```bash
   mvn spring-boot:run
   # Test your endpoints
   ```

## Benefits After Migration

1. **Better API** - More intuitive and consistent
2. **Type Safety** - Fewer runtime errors
3. **Performance** - Native streaming support
4. **Flexibility** - Better memory management options
5. **Future Proof** - On the stable release path

## Need Help?

- 📖 [Spring AI 1.0.0 Documentation](https://docs.spring.io/spring-ai/reference/)
- 💬 [Migration Support](https://github.com/spring-projects/spring-ai/discussions)
- 🐛 [Report Migration Issues](https://github.com/spring-projects/spring-ai/issues)

---

[← Back to Guides](../README.md#practical-guides) | [Quick Start →](quick-start.md)