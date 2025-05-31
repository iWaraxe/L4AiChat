# 📄 Module S1: Multi-turn Conversations Guide

## Overview

This module introduces the fundamentals of building multi-turn conversations with Spring AI. It demonstrates the progression from simple single-turn interactions to stateful conversations with memory.

## Learning Objectives

By completing this module, you will:
- ✅ Understand the difference between stateless and stateful conversations
- ✅ Implement basic ChatClient usage patterns
- ✅ Learn when and why to use conversation memory
- ✅ Build your first multi-turn chat application

## Why Start Here?

This module is the foundation because:
1. **Gradual Introduction** - Start with the simplest possible example
2. **Core Concepts** - Learn ChatClient basics before advanced features
3. **Real Problem** - Immediately see why memory matters
4. **Building Block** - All other modules build on these concepts

## Module Structure

```
s1multiturn/
├── AiChatApplication.java       # Spring Boot entry point
├── controller/
│   ├── SingleTurnChatController.java    # Stateless examples
│   ├── MultiTurnChatController.java     # Stateful examples
│   └── ChatMessageDemoController.java   # Message types demo
└── 1sREADME.md                          # Quick reference
```

## Key Concepts

### 1. Single-Turn Conversations (Stateless)

#### The Code
```java
@PostMapping("/simple")
public Map<String, String> simpleChat(@RequestBody Map<String, String> request) {
    String response = this.chatClient.prompt()
        .user(request.get("message"))
        .call()
        .content();
    
    return Map.of("response", response);
}
```

#### Why This Pattern?

**Advantages:**
- ✅ **Simplicity** - No state to manage
- ✅ **Scalability** - Any instance can handle any request
- ✅ **Cost-Effective** - Minimal token usage
- ✅ **Testing** - Easy to test in isolation

**When to Use:**
- Simple Q&A
- One-off tasks (translation, summarization)
- Stateless operations
- High-volume, low-context scenarios

**Limitations:**
- ❌ No conversation context
- ❌ Can't reference previous exchanges
- ❌ Not suitable for complex interactions

### 2. Single-Turn with Options

#### The Code
```java
@PostMapping("/options")
public Map<String, String> chatWithOptions(@RequestBody Map<String, String> request) {
    String response = this.chatClient.prompt()
        .user(request.get("message"))
        .options(OpenAiChatOptions.builder()
            .temperature(0.1)      // Low creativity
            .maxTokens(150)        // Limit response length
            .build())
        .call()
        .content();
    
    return Map.of("response", response);
}
```

#### Why Configure Options?

**Temperature (0.0 - 1.0):**
- `0.0` = Deterministic, same input → same output
- `0.7` = Balanced creativity (default)
- `1.0` = Maximum creativity, different each time

**Use Cases by Temperature:**
```java
// Factual Q&A - Use low temperature
.temperature(0.1)  // "What is the capital of France?"

// Creative writing - Use high temperature  
.temperature(0.9)  // "Write a poem about spring"

// Balanced tasks - Use medium temperature
.temperature(0.5)  // "Explain this concept"
```

**Max Tokens:**
- Controls response length and cost
- 1 token ≈ 4 characters (rough estimate)
- Set based on expected response size

### 3. Multi-Turn Conversations (Stateful)

#### The Architecture
```java
@RestController
public class MultiTurnChatController {
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public MultiTurnChatController(ChatClient.Builder builder) {
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

#### Why This Design?

**MessageWindowChatMemory:**
- **Sliding Window** - Keeps last N messages
- **Token Management** - Prevents context overflow
- **Performance** - Bounded memory usage

**Why 20 Messages?**
- ~1000-2000 tokens (depends on message length)
- Fits within GPT-3.5/4 context window
- Balances context with cost
- Covers most conversation needs

**MessageChatMemoryAdvisor:**
- **Automatic Integration** - Injects history into prompts
- **Transparent** - Your code doesn't change
- **Configurable** - Control what gets remembered

#### The Flow

```java
@PostMapping("/{conversationId}")
public Map<String, String> chat(
    @PathVariable String conversationId,
    @RequestBody Map<String, String> request) {
    
    String response = this.chatClient.prompt()
        .user(request.get("message"))
        .advisors(a -> a.param(CONVERSATION_ID_PARAM, conversationId))
        .call()
        .content();
    
    return Map.of(
        "response", response,
        "conversationId", conversationId
    );
}
```

**What Happens Behind the Scenes:**
1. User sends message with conversation ID
2. Advisor retrieves conversation history
3. History + new message sent to AI
4. AI responds with full context
5. New exchange saved to memory
6. Response returned to user

### 4. Conversation Management

#### Starting Conversations
```java
@PostMapping("/new")
public Map<String, String> startNewChat() {
    String conversationId = UUID.randomUUID().toString();
    return Map.of(
        "conversationId", conversationId,
        "response", "Hello! I'm ready to chat."
    );
}
```

**Why Generate IDs?**
- **Isolation** - Each conversation is separate
- **Security** - Can't guess other conversations
- **Scalability** - Distributed ID generation

#### Viewing History
```java
@GetMapping("/{conversationId}/history")
public List<Message> getHistory(@PathVariable String conversationId) {
    return this.chatMemory.get(conversationId);
}
```

**Why Expose History?**
- **Debugging** - See what AI remembers
- **Transparency** - Users can review
- **Features** - Export, search, analyze

#### Clearing Conversations
```java
@DeleteMapping("/{conversationId}")
public Map<String, String> clearChat(@PathVariable String conversationId) {
    this.chatMemory.clear(conversationId);
    return Map.of("message", "Conversation cleared");
}
```

**Why Allow Clearing?**
- **Privacy** - User control over data
- **Fresh Start** - Reset confused conversations
- **Resource Management** - Free up memory

## Common Patterns and Best Practices

### 1. Conversation ID Management

❌ **Don't: Client-Generated IDs**
```java
// Security risk - predictable IDs
String conversationId = "user123-chat1";
```

✅ **Do: Server-Generated UUIDs**
```java
String conversationId = UUID.randomUUID().toString();
```

### 2. Error Handling

❌ **Don't: Expose Internal Errors**
```java
try {
    return chatClient.prompt().user(message).call().content();
} catch (Exception e) {
    return Map.of("error", e.getMessage()); // Leaks internals
}
```

✅ **Do: User-Friendly Errors**
```java
try {
    return chatClient.prompt().user(message).call().content();
} catch (Exception e) {
    logger.error("Chat error", e);
    return Map.of("error", "Unable to process your message. Please try again.");
}
```

### 3. Memory Lifecycle

❌ **Don't: Unbounded Growth**
```java
// No limits - memory leak risk
MessageWindowChatMemory.builder()
    .chatMemoryRepository(new InMemoryChatMemoryRepository())
    .build();
```

✅ **Do: Set Reasonable Limits**
```java
MessageWindowChatMemory.builder()
    .chatMemoryRepository(new InMemoryChatMemoryRepository())
    .maxMessages(20)  // Bounded growth
    .build();
```

## Testing the Module

### 1. Start the Application
```bash
mvn spring-boot:run -pl :L4AiChat \
  -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s1multiturn.AiChatApplication
```

### 2. Test Single-Turn
```bash
# Simple chat
curl -X POST http://localhost:8080/api/s1/chat/simple \
  -H "Content-Type: application/json" \
  -d '{"message": "What is Spring Boot?"}'

# With options
curl -X POST http://localhost:8080/api/s1/chat/options \
  -H "Content-Type: application/json" \
  -d '{"message": "List 3 benefits of Spring Boot"}'
```

### 3. Test Multi-Turn
```bash
# Start conversation
curl -X POST http://localhost:8080/api/s1/chat/new

# Continue conversation (use returned ID)
curl -X POST http://localhost:8080/api/s1/chat/{conversationId} \
  -H "Content-Type: application/json" \
  -d '{"message": "My name is Alice"}'

# Test memory
curl -X POST http://localhost:8080/api/s1/chat/{conversationId} \
  -H "Content-Type: application/json" \
  -d '{"message": "What is my name?"}'
```

## Exercises

### 1. Experiment with Temperature
Modify the `/options` endpoint to accept temperature as a parameter. Test how it affects responses.

### 2. Add Token Counting
Use the response metadata to track token usage:
```java
ChatResponse response = chatClient.prompt()
    .user(message)
    .call()
    .chatResponse();
    
int tokensUsed = response.getMetadata().getUsage().getTotalTokens();
```

### 3. Implement Conversation Timeout
Add a feature to automatically clear conversations after 30 minutes of inactivity.

## Key Takeaways

1. **Start simple** - Single-turn for basic needs
2. **Add memory when needed** - Not all apps require state
3. **Configure appropriately** - Temperature and tokens matter
4. **Manage lifecycle** - Clear old conversations
5. **Think about scale** - In-memory works for demos, not production

## What's Next?

Now that you understand the basics, explore:
- 📄 [S2: Components Guide](s2-components-guide.md) - Dive deeper into ChatClient
- 📄 [S3: Context Guide](s3-context-guide.md) - Advanced memory patterns
- 🏗️ [Architecture Patterns](../architecture/patterns.md) - Design decisions

---

[← Back to Modules](../README.md#module-guides) | [Next: S2 Components →](s2-components-guide.md)