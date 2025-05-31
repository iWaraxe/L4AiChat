# 📗 ChatClient Deep Dive

## Table of Contents
1. [Understanding ChatClient](#understanding-chatclient)
2. [The Fluent API Design](#the-fluent-api-design)
3. [Building Blocks](#building-blocks)
4. [Advanced Patterns](#advanced-patterns)
5. [Best Practices](#best-practices)
6. [Common Pitfalls](#common-pitfalls)

## Understanding ChatClient

### What is ChatClient?

ChatClient is Spring AI's primary interface for interacting with AI language models. It provides a fluent, builder-based API that makes AI conversations feel natural in Java code.

### Why ChatClient?

The design addresses several challenges:

1. **Complexity Management** - AI APIs have many parameters
2. **Type Safety** - Prevent runtime errors with compile-time checks
3. **Readability** - Code should express intent clearly
4. **Flexibility** - Support simple to complex use cases

## The Fluent API Design

### Basic Flow

```java
String response = chatClient.prompt()
    .user("What is Spring Boot?")
    .call()
    .content();
```

### Why This Design?

1. **Method Chaining** - Reduces verbosity, improves readability
2. **Progressive Disclosure** - Start simple, add complexity as needed
3. **IDE Support** - Auto-completion guides you through the API
4. **Immutability** - Each method returns a new instance, preventing side effects

### Comparison with Traditional Approaches

#### ❌ Traditional HTTP Client
```java
// Complex, error-prone, lots of boilerplate
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
    .header("Authorization", "Bearer " + apiKey)
    .header("Content-Type", "application/json")
    .POST(HttpRequest.BodyPublishers.ofString(
        "{\"model\":\"gpt-4\",\"messages\":[{\"role\":\"user\",\"content\":\"Hello\"}]}"
    ))
    .build();
```

#### ✅ Spring AI ChatClient
```java
// Clean, type-safe, intention-revealing
String response = chatClient.prompt()
    .user("Hello")
    .call()
    .content();
```

## Building Blocks

### 1. Prompt Construction

#### User Messages
```java
.user("Ask a question")
.user(u -> u.text("Question").media(imageResource))  // With media
```

**Why:** Separates user input from system instructions

#### System Messages
```java
chatClient.prompt()
    .system("You are a helpful assistant")
    .user("Hello")
```

**Why:** Sets behavior without polluting user messages

#### Multiple Messages
```java
.messages(
    new SystemMessage("Be concise"),
    new UserMessage("Explain quantum physics"),
    new AssistantMessage("Previous response...")
)
```

**Why:** Full conversation control for complex scenarios

### 2. Options and Configuration

#### Provider-Specific Options
```java
.options(OpenAiChatOptions.builder()
    .temperature(0.7)
    .maxTokens(150)
    .model("gpt-4")
    .build())
```

**Why Different Option Types?**
- **Type Safety** - Only valid options for each provider
- **Discoverability** - IDE shows available options
- **Validation** - Catch configuration errors early

#### When to Use Options
- 🎯 **Temperature** - Control creativity (0.0 = deterministic, 1.0 = creative)
- 📏 **Max Tokens** - Limit response length and cost
- 🎲 **Top P** - Alternative to temperature for randomness
- 🏷️ **Model Selection** - Choose speed vs quality trade-offs

### 3. Response Handling

#### Content Extraction
```java
// Simple string content
String content = response.content();

// Full response object
ChatResponse fullResponse = response.chatResponse();

// Metadata access
Usage usage = fullResponse.getMetadata().getUsage();
```

**Why Multiple Response Types?**
- **Simple Cases** - Just get the text
- **Monitoring** - Access token usage, model info
- **Debugging** - Full response for troubleshooting

#### Structured Output
```java
// Type-safe response parsing
ProductInfo product = chatClient.prompt()
    .user("Describe the iPhone 15")
    .call()
    .entity(ProductInfo.class);
```

**Why:** Eliminates manual JSON parsing, ensures type safety

### 4. Streaming Responses

```java
Flux<String> stream = chatClient.prompt()
    .user("Write a story")
    .stream()
    .content();
```

**Why Stream?**
- **User Experience** - Show progress for long responses
- **Memory Efficiency** - Don't buffer entire response
- **Interactivity** - Can cancel mid-stream
- **Real-time Feel** - Similar to ChatGPT interface

## Advanced Patterns

### 1. Advisor Integration

```java
chatClient.prompt()
    .user("Hello")
    .advisors(advisor -> advisor
        .param("conversationId", "123")
        .param("userId", "user-456"))
    .call()
```

**Why Advisors?**
- **Separation of Concerns** - AI logic vs cross-cutting concerns
- **Reusability** - Apply same patterns across endpoints
- **Testability** - Test advisors independently
- **Composability** - Chain multiple behaviors

### 2. Dynamic Prompt Building

```java
public String generateReport(ReportRequest request) {
    var promptSpec = chatClient.prompt();
    
    // Conditionally add context
    if (request.includeHistory()) {
        promptSpec = promptSpec.system("Include historical context");
    }
    
    // Dynamic user message
    promptSpec = promptSpec.user(u -> u
        .text("Generate report for: " + request.getTopic())
        .param("format", request.getFormat())
        .param("length", request.getLength()));
    
    return promptSpec.call().content();
}
```

**Why:** Flexible prompt construction based on runtime conditions

### 3. Error Handling Patterns

```java
public String safeChat(String message) {
    try {
        return chatClient.prompt()
            .user(message)
            .options(OpenAiChatOptions.builder()
                .temperature(0.7)
                .build())
            .call()
            .content();
    } catch (ChatClientException e) {
        logger.error("AI call failed", e);
        // Fallback behavior
        return "I'm having trouble processing your request.";
    }
}
```

**Why:** Graceful degradation when AI services fail

## Best Practices

### 1. Builder Reuse

```java
@Configuration
public class ChatConfig {
    @Bean
    public ChatClient.Builder chatClientBuilder() {
        return ChatClient.builder()
            .defaultSystem("You are a helpful assistant")
            .defaultOptions(OpenAiChatOptions.builder()
                .temperature(0.7)
                .build());
    }
}
```

**Why:** Consistent configuration across your application

### 2. Prompt Templates

```java
@Component
public class PromptTemplates {
    private final ChatClient chatClient;
    
    public String summarize(String text) {
        return chatClient.prompt()
            .system("You are a summarization expert")
            .user(u -> u.text("""
                Summarize the following text in 3 bullet points:
                
                %s
                """.formatted(text)))
            .call()
            .content();
    }
}
```

**Why:** Reusable, testable prompt patterns

### 3. Conversation Scoping

```java
@Service
@Scope("session")
public class ConversationService {
    private final String conversationId = UUID.randomUUID().toString();
    
    public String chat(String message) {
        return chatClient.prompt()
            .user(message)
            .advisors(a -> a.param(CONVERSATION_ID, conversationId))
            .call()
            .content();
    }
}
```

**Why:** Proper conversation isolation in multi-user environments

## Common Pitfalls

### ❌ Pitfall 1: Mutable State
```java
// Bad: Reusing prompt builder
var prompt = chatClient.prompt().system("Be helpful");
prompt.user("Question 1");  // Modifies shared state!
```

### ✅ Solution: Fresh Builder
```java
// Good: New prompt for each request
chatClient.prompt()
    .system("Be helpful")
    .user("Question 1")
    .call();
```

### ❌ Pitfall 2: Blocking in Reactive Context
```java
// Bad: Blocks reactive pipeline
webClient.get()
    .retrieve()
    .bodyToMono(String.class)
    .map(data -> chatClient.prompt()
        .user(data)
        .call()  // Blocks!
        .content());
```

### ✅ Solution: Use Streaming
```java
// Good: Fully reactive
webClient.get()
    .retrieve()
    .bodyToMono(String.class)
    .flatMapMany(data -> chatClient.prompt()
        .user(data)
        .stream()
        .content());
```

### ❌ Pitfall 3: Ignoring Token Limits
```java
// Bad: No length control
chatClient.prompt()
    .user(veryLongText)  // Might exceed model limits
    .call();
```

### ✅ Solution: Set Limits
```java
// Good: Explicit limits
chatClient.prompt()
    .user(veryLongText)
    .options(OpenAiChatOptions.builder()
        .maxTokens(1000)
        .build())
    .call();
```

## Key Takeaways

1. **ChatClient's fluent API** makes AI interactions intuitive and safe
2. **Progressive disclosure** lets you start simple and add complexity
3. **Type safety** prevents runtime errors and improves IDE support
4. **Advisors** enable clean separation of concerns
5. **Streaming** provides better user experience for long responses

## Next Steps

Ready to manage conversation state? Continue with:
- 📙 [Memory and State Management](03-memory-and-state.md)
- 📄 [Module S2 Guide](modules/s2-components-guide.md) - Explore components
- 🏗️ [Architectural Patterns](architecture/patterns.md) - Design decisions

---

[← Spring AI Fundamentals](01-spring-ai-fundamentals.md) | [Back to Main README](../README.md) | [Memory and State Management →](03-memory-and-state.md)