# 🔧 Tutorial S2: Core Components Deep Dive

> **⏱️ Duration**: 45 minutes  
> **🎯 Difficulty**: 🟢 Beginner  
> **📋 Prerequisites**: Complete [Tutorial S1](./S1-first-steps.md)

## 🎯 Learning Objectives

By the end of this tutorial, you will:
- ✅ Understand Spring AI's core component architecture
- ✅ Work with different message types (Text, Media, System)
- ✅ Implement streaming responses with Flux
- ✅ Configure model options and parameters
- ✅ Generate structured output from AI responses

## 🛠️ Hands-On Exercise: Build a Multi-Modal Chat API

### Step 1: Explore the S2 Components Module

Let's dive into the component architecture:

```bash
# Navigate to the S2 module
cd src/main/java/com/coherentsolutions/l4aichat/s2components

# Explore the structure
find . -name "*.java" -type f
```

**🤔 Question**: What are the four main component types in Spring AI?

<details>
<summary>💡 Click to reveal the answer</summary>

**The Four Core Components**:
1. **ChatClient**: High-level fluent API for chat interactions
2. **ChatModel**: Lower-level model interface for direct access
3. **Message Types**: UserMessage, SystemMessage, AssistantMessage, MediaMessage
4. **ChatOptions**: Model configuration (temperature, max tokens, etc.)

**Component Hierarchy**:
```
ChatClient (Fluent API)
    ↓ uses
ChatModel (Model Interface)
    ↓ sends
Messages (Different Types)
    ↓ with
ChatOptions (Configuration)
```
</details>

### Step 2: Start the S2 Application

```bash
# Start the S2 components application
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s2components.ComponentsApplication
```

### Step 3: Test ChatClient vs ChatModel Approaches

Let's compare the different approaches side by side:

```bash
# Test ChatClient demo
curl -X GET http://localhost:8080/api/s2/demo/chat-client
```

**Expected Response**:
```json
{
  "approach": "ChatClient",
  "response": "Hello! I'm Claude, an AI assistant...",
  "features": ["Fluent API", "Built-in advisors", "Easy configuration"]
}
```

```bash
# Test ChatModel demo  
curl -X GET http://localhost:8080/api/s2/demo/chat-model
```

**🎯 Challenge**: Compare the response times and notice any differences in the response structure!

### Step 4: Explore Message Types

Test different message types to understand their purposes:

```bash
# Test system, user, and assistant message demo
curl -X GET http://localhost:8080/api/s2/demo/message-types
```

**Expected Response**:
```json
{
  "systemMessage": {
    "role": "system",
    "content": "You are a helpful programming tutor..."
  },
  "userMessage": {
    "role": "user", 
    "content": "Explain Spring Boot basics"
  },
  "assistantMessage": {
    "role": "assistant",
    "content": "Spring Boot is a framework that simplifies..."
  }
}
```

### Step 5: Test Structured Output Generation

One of Spring AI's powerful features is generating structured data:

```bash
# Test structured output with entity mapping
curl -X POST http://localhost:8080/api/s2/demo/structured-output \
  -H "Content-Type: application/json" \
  -d '{"topic": "Java Collections"}'
```

**Expected Response**:
```json
{
  "topic": "Java Collections",
  "summary": "A comprehensive framework for storing and manipulating groups of objects",
  "keyPoints": [
    "List, Set, and Map interfaces",
    "ArrayList vs LinkedList performance",
    "HashMap internal structure"
  ],
  "difficulty": "INTERMEDIATE",
  "estimatedHours": 8
}
```

**🎯 Challenge**: Try different topics like "Spring Security" or "Docker Containers"!

## 💡 Concept Deep-Dive: Message Types & Their Purposes

### **1. System Messages - Setting Context**

System messages define the AI's behavior and role:

```java
// ✅ Good system message - Specific and actionable
SystemMessage systemMessage = new SystemMessage(
    "You are a code reviewer. Analyze code for best practices, " +
    "security issues, and performance improvements. " +
    "Always provide specific suggestions with examples."
);

// ❌ Poor system message - Too vague
SystemMessage vague = new SystemMessage("Be helpful");
```

### **2. User Messages - Human Input**

User messages represent what the human is asking:

```java
// ✅ Clear user message
UserMessage userMessage = new UserMessage(
    "Review this Spring Boot controller for security vulnerabilities"
);

// 🔄 User message with media (coming in future versions)
UserMessage mediaMessage = new UserMessage(
    List.of(
        new TextContent("What's in this image?"),
        new ImageContent("data:image/jpeg;base64,...")
    )
);
```

### **3. Assistant Messages - AI Responses**

Assistant messages are the AI's previous responses (for context):

```java
// Used in conversation history
AssistantMessage previousResponse = new AssistantMessage(
    "I found several security concerns in your controller..."
);
```

### **4. Media Messages - Multi-Modal Input**

Spring AI supports images, documents, and other media types:

```java
// Image analysis (GPT-4 Vision)
MediaMessage imageMessage = new MediaMessage(
    MediaType.IMAGE_JPEG,
    imageBytes
);

// Document processing
MediaMessage documentMessage = new MediaMessage(
    MediaType.APPLICATION_PDF,
    pdfBytes
);
```

## 🧪 Live Experiment: Customize Model Behavior

Let's experiment with different model configurations:

### Experiment 1: Temperature Settings

1. Open `ChatClientDemoController.java`
2. Find the ChatClient configuration
3. Try different temperature values:

```java
// Original (balanced creativity)
ChatClient chatClient = chatClientBuilder
    .defaultOptions(OpenAiChatOptions.builder()
        .withTemperature(0.7)
        .build())
    .build();

// 🧪 Try: Creative mode
ChatClient creative = chatClientBuilder
    .defaultOptions(OpenAiChatOptions.builder()
        .withTemperature(1.0)  // More creative, less predictable
        .build())
    .build();

// 🧪 Try: Deterministic mode  
ChatClient deterministic = chatClientBuilder
    .defaultOptions(OpenAiChatOptions.builder()
        .withTemperature(0.0)  // Very consistent responses
        .build())
    .build();
```

### Experiment 2: Model Selection

Test different models for different tasks:

```java
// Code-focused tasks
ChatClient codeAssistant = chatClientBuilder
    .defaultOptions(OpenAiChatOptions.builder()
        .withModel("gpt-4-turbo-preview")  // Better for complex reasoning
        .withTemperature(0.2)
        .build())
    .build();

// Creative writing tasks
ChatClient creativeAssistant = chatClientBuilder
    .defaultOptions(OpenAiChatOptions.builder()
        .withModel("gpt-3.5-turbo")  // Faster, good for simple tasks
        .withTemperature(0.9)
        .build())
    .build();
```

### Experiment 3: Response Length Control

Control how verbose the AI responses are:

```java
ChatClient conciseAssistant = chatClientBuilder
    .defaultOptions(OpenAiChatOptions.builder()
        .withMaxTokens(150)  // Short responses
        .build())
    .build();

ChatClient detailedAssistant = chatClientBuilder
    .defaultOptions(OpenAiChatOptions.builder()
        .withMaxTokens(1000)  // Longer, detailed responses
        .build())
    .build();
```

## 🌊 Streaming Responses in Action

One of Spring AI's most powerful features is real-time streaming:

### Understanding Flux vs Single Response

```java
// ❌ Traditional blocking approach
String response = chatClient.prompt()
    .user("Write a long explanation about microservices")
    .call()
    .content();
// User waits for complete response

// ✅ Streaming approach - Real-time updates
Flux<String> responseStream = chatClient.prompt()
    .user("Write a long explanation about microservices")
    .stream()
    .content();
// User sees words appear as they're generated
```

### Test Streaming in Real-Time

```bash
# Test streaming endpoint (if available)
curl -X POST http://localhost:8080/api/s2/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"message": "Explain the evolution of Java from version 8 to 21 in detail"}' \
  --no-buffer
```

**🎯 Observation**: Notice how words appear progressively rather than all at once!

## ✅ Check Your Understanding

### Quick Quiz

1. **Which message type sets the AI's behavior and personality?**
   - A) UserMessage
   - B) SystemMessage  
   - C) AssistantMessage
   - D) MediaMessage

<details>
<summary>Answer</summary>
**B) SystemMessage** - System messages define the AI's role, personality, and behavioral guidelines.
</details>

2. **What happens when you set temperature to 0.0?**
   - A) The AI becomes more creative
   - B) The AI responds faster
   - C) The AI gives more deterministic/consistent responses
   - D) The AI uses less memory

<details>
<summary>Answer</summary>
**C) The AI gives more deterministic/consistent responses** - Temperature 0.0 makes the AI choose the most likely next token, resulting in consistent outputs.
</details>

3. **What's the main advantage of streaming responses?**
   - A) They use less bandwidth
   - B) They're more accurate
   - C) Users see real-time progress instead of waiting
   - D) They're more secure

<details>
<summary>Answer</summary>
**C) Users see real-time progress instead of waiting** - Streaming provides a better user experience by showing responses as they're generated.
</details>

### Coding Challenge 🏆

**Challenge**: Create a "Smart Code Reviewer" endpoint that:
1. Takes code snippets as input
2. Uses structured output to return review results
3. Configures the model for code analysis (low temperature, specific system prompt)

**Requirements**:
```java
// Create this data structure
public record CodeReview(
    String overallRating,      // "EXCELLENT", "GOOD", "NEEDS_IMPROVEMENT"
    List<String> strengths,
    List<String> issues,
    List<String> suggestions,
    int securityScore          // 1-10
) {}
```

<details>
<summary>💡 Solution</summary>

```java
@PostMapping("/code-review")
public ResponseEntity<CodeReview> reviewCode(@RequestBody CodeRequest request) {
    ChatClient codeReviewer = chatClientBuilder
        .defaultSystem("""
            You are an expert code reviewer. Analyze code for:
            - Best practices and design patterns
            - Security vulnerabilities  
            - Performance optimizations
            - Code clarity and maintainability
            
            Return structured feedback with specific, actionable suggestions.
            """)
        .defaultOptions(OpenAiChatOptions.builder()
            .withTemperature(0.2)  // More consistent for code analysis
            .withModel("gpt-4-turbo-preview")
            .build())
        .build();
    
    CodeReview review = codeReviewer.prompt()
        .user("Review this code:\n\n" + request.code())
        .call()
        .entity(CodeReview.class);
    
    return ResponseEntity.ok(review);
}
```

Test it:
```bash
curl -X POST http://localhost:8080/api/s2/code-review \
  -H "Content-Type: application/json" \
  -d '{
    "code": "public class User { public String password; public User(String pwd) { this.password = pwd; } }"
  }'
```
</details>

## 🎯 Real-World Scenario: Build a Learning Assistant

**Scenario**: You're building an educational platform that needs to:
1. Adapt explanations to different skill levels
2. Generate structured learning materials
3. Provide real-time feedback during learning

**Your Task**: Design the component architecture for this system.

**Consider**:
- Which message types would you use for different interactions?
- How would you configure models for educational content?
- Where would streaming be most beneficial?
- What structured outputs would be useful?

### Solution Architecture

```java
// Beginner-friendly tutor
ChatClient beginnerTutor = chatClientBuilder
    .defaultSystem("""
        You are a patient programming tutor for beginners. 
        Use simple language, provide lots of examples, 
        and break complex topics into small steps.
        """)
    .defaultOptions(OpenAiChatOptions.builder()
        .withTemperature(0.5)  // Balanced creativity and consistency
        .withMaxTokens(300)    // Concise explanations
        .build())
    .build();

// Advanced technical mentor
ChatClient advancedMentor = chatClientBuilder
    .defaultSystem("""
        You are a senior software architect mentoring experienced developers.
        Focus on advanced patterns, performance implications, 
        and architectural decisions. Assume deep technical knowledge.
        """)
    .defaultOptions(OpenAiChatOptions.builder()
        .withTemperature(0.3)  // More technical precision
        .withModel("gpt-4-turbo-preview")
        .build())
    .build();

// Structured lesson generator
public record Lesson(
    String title,
    String difficulty,      // "BEGINNER", "INTERMEDIATE", "ADVANCED"
    List<String> objectives,
    List<CodeExample> examples,
    List<Exercise> exercises,
    int estimatedMinutes
) {}
```

## 🔗 Next Steps

Excellent work! You've mastered Spring AI's core components and their interactions.

**What you've learned**:
- ✅ Component architecture (ChatClient, ChatModel, Messages, Options)
- ✅ Message types and their specific purposes
- ✅ Model configuration for different use cases
- ✅ Structured output generation
- ✅ Streaming response concepts

**Ready for the next challenge?** 

👉 **Continue to [Tutorial S3: Context & Memory Management](./S3-context-management.md)** to learn about:
- In-memory vs persistent conversation storage
- MessageChatMemoryAdvisor configuration
- JDBC-based chat history
- Memory window management strategies

## 📚 Additional Resources

- 📖 [S2 Module Guide](../modules/S2-components.md) - Complete component reference
- 🏗️ [Message Architecture](../architecture/patterns.md#message-patterns) - Message design patterns
- 🔧 [Model Configuration](../guides/model-configuration.md) - Advanced model setup

---

**🎉 Fantastic progress!** You now understand the building blocks of Spring AI applications.

The next tutorial will show you how to maintain conversation context and implement persistent memory - essential for real-world chat applications.

[← Previous: S1 First Steps](./S1-first-steps.md) | [Next: S3 Context Management →](./S3-context-management.md)