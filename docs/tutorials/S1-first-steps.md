# 🚀 Tutorial S1: First Steps with Spring AI

> **⏱️ Duration**: 30 minutes  
> **🎯 Difficulty**: 🟢 Beginner  
> **📋 Prerequisites**: Basic Spring Boot knowledge

## 🎯 Learning Objectives

By the end of this tutorial, you will:
- ✅ Understand the ChatClient vs ChatModel API
- ✅ Build your first AI-powered endpoint
- ✅ Handle multi-turn conversations with memory
- ✅ Debug and test AI integrations

## 🛠️ Hands-On Exercise: Build Your First Chat API

### Step 1: Explore the Project Structure

First, let's understand what we're working with:

```bash
# Navigate to the S1 module
cd src/main/java/com/coherentsolutions/l4aichat/s1multiturn

# Explore the structure
ls -la
```

**🤔 Question**: What's the difference between the `ChatModel` and `ChatClient` approaches?

<details>
<summary>💡 Click to reveal the answer</summary>

**ChatModel** (Legacy approach):
- Lower-level API requiring manual prompt building
- More verbose code for simple tasks
- Direct model interaction

**ChatClient** (Modern approach - Spring AI 1.0.0):
- Fluent, builder-pattern API
- Built-in memory and advisor support
- Higher-level abstraction for common tasks

**Example Comparison**:
```java
// ChatModel (verbose)
Prompt prompt = new Prompt(List.of(
    new SystemMessage("You are a helpful assistant"),
    new UserMessage("Hello!")
));
ChatResponse response = chatModel.call(prompt);

// ChatClient (fluent)
String response = chatClient.prompt()
    .system("You are a helpful assistant")
    .user("Hello!")
    .call()
    .content();
```
</details>

### Step 2: Run Your First Chat API

Let's start the S1 application and test it:

```bash
# Start the S1 application
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s1multiturn.AiChatApplication

# In another terminal, test the ChatClient endpoint
curl -X POST http://localhost:8080/api/s1/chat/client/new \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello! My name is Alice."}'
```

**Expected Response**:
```json
{
  "message": "Hello Alice! Nice to meet you. How can I help you today?",
  "conversationId": "conv-12345",
  "timestamp": "2024-01-01T10:00:00Z"
}
```

### Step 3: Test Multi-Turn Conversation

Now let's test the conversation memory:

```bash
# Use the conversationId from the previous response
curl -X POST http://localhost:8080/api/s1/chat/client/conv-12345 \
  -H "Content-Type: application/json" \
  -d '{"message": "What'\''s my name?"}'
```

**🎯 Challenge**: The AI should remember your name from the previous message!

### Step 4: Compare ChatModel vs ChatClient

Test both endpoints with the same conversation:

```bash
# Test ChatModel endpoint
curl -X POST http://localhost:8080/api/s1/chat/model/new \
  -H "Content-Type: application/json" \
  -d '{"message": "I like programming in Java"}'

# Test ChatClient endpoint  
curl -X POST http://localhost:8080/api/s1/chat/client/new \
  -H "Content-Type: application/json" \
  -d '{"message": "I like programming in Java"}'
```

**🤔 Observation Exercise**: Notice any differences in response style or capabilities?

## 💡 Concept Deep-Dive: Why ChatClient is Better

### **1. Fluent API Design**

The ChatClient uses a fluent builder pattern that makes code more readable:

```java
// ✅ ChatClient - Clear and concise
String response = chatClient.prompt()
    .system("You are a coding mentor")
    .user("Explain Spring Boot")
    .call()
    .content();

// ❌ ChatModel - Verbose and complex
List<Message> messages = List.of(
    new SystemMessage("You are a coding mentor"),
    new UserMessage("Explain Spring Boot")
);
Prompt prompt = new Prompt(messages);
ChatResponse response = chatModel.call(prompt);
String content = response.getResult().getOutput().getContent();
```

### **2. Built-in Memory Management**

ChatClient integrates seamlessly with Spring AI's memory system:

```java
// Memory is automatically handled with advisors
ChatClient chatClient = ChatClient.builder()
    .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
    .build();

// Each conversation automatically maintains context
String response = chatClient.prompt()
    .user(userMessage)
    .advisors(a -> a.param(CONVERSATION_ID, conversationId))
    .call()
    .content();
```

### **3. Extensibility with Advisors**

Advisors allow you to intercept and modify requests/responses:

```java
// Add logging, safety checks, or custom processing
ChatClient chatClient = ChatClient.builder()
    .defaultAdvisors(
        new LoggingAdvisor(),
        new SafetyAdvisor(),
        MessageChatMemoryAdvisor.builder(chatMemory).build()
    )
    .build();
```

## 🧪 Live Experiment: Modify the Code

Let's make some changes to see how the system behaves:

### Experiment 1: Custom System Prompt

1. Open `MultiTurnChatController.java`
2. Find the ChatClient configuration
3. Modify the system prompt:

```java
// Original
this.chatClient = chatClientBuilder
    .defaultSystem("You are a helpful AI assistant.")
    .build();

// 🧪 Try this instead
this.chatClient = chatClientBuilder
    .defaultSystem("You are a friendly programming tutor who explains concepts with simple examples.")
    .build();
```

4. Restart the application and test the difference!

### Experiment 2: Memory Window Size

1. Open the `chatMemory` configuration
2. Change the window size:

```java
// Original
this.chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(new InMemoryChatMemoryRepository())
    .maxMessages(20)  // Try changing this to 5
    .build();
```

3. Test how it affects long conversations

### Experiment 3: Add Response Preprocessing

Add custom logic before returning responses:

```java
public ChatResponse processMessage(String conversationId, String userMessage) {
    String response = this.chatClient.prompt()
        .user(userMessage)
        .advisors(a -> a.param(CONVERSATION_ID, finalConvId))
        .call()
        .content();
    
    // 🧪 Add custom processing
    if (response.length() > 500) {
        response = "That's a long answer! Here's a summary: " + 
                  response.substring(0, 200) + "...";
    }
    
    return new ChatResponse(response, finalConvId, Instant.now());
}
```

## ✅ Check Your Understanding

### Quick Quiz

1. **What's the main advantage of ChatClient over ChatModel?**
   - A) It's faster
   - B) It has a fluent API and built-in memory support
   - C) It uses less memory
   - D) It's more secure

<details>
<summary>Answer</summary>
**B) It has a fluent API and built-in memory support** - ChatClient provides a more developer-friendly interface with integrated advisors and memory management.
</details>

2. **How does conversation memory work in this example?**
   - A) It's stored in a database
   - B) It's stored in memory using MessageWindowChatMemory
   - C) It's handled by OpenAI
   - D) There is no memory

<details>
<summary>Answer</summary>
**B) It's stored in memory using MessageWindowChatMemory** - The example uses an in-memory implementation with a configurable window size.
</details>

### Coding Challenge 🏆

**Challenge**: Create a new endpoint that counts how many messages have been sent in a conversation.

**Requirements**:
1. Create a new method in `MultiTurnChatController`
2. Return a count of messages for a given conversation ID
3. Test it with curl

**Hint**: You can access the chat memory to get message count.

<details>
<summary>💡 Solution</summary>

```java
@GetMapping("/chat/count/{conversationId}")
public ResponseEntity<Map<String, Object>> getMessageCount(@PathVariable String conversationId) {
    List<Message> messages = chatMemory.get(conversationId);
    
    Map<String, Object> response = Map.of(
        "conversationId", conversationId,
        "messageCount", messages.size(),
        "hasMemory", !messages.isEmpty()
    );
    
    return ResponseEntity.ok(response);
}
```

Test it:
```bash
curl http://localhost:8080/api/s1/chat/count/your-conversation-id
```
</details>

## 🎯 Real-World Scenario

**Scenario**: You're building a customer service chatbot that needs to:
1. Remember customer information within a session
2. Maintain a professional tone
3. Keep conversations concise (under 200 words)

**Your Task**: Modify the S1 implementation to meet these requirements.

**Consider**:
- How would you adjust the system prompt?
- What memory window size would be appropriate?
- How would you implement response length limits?

## 🔗 Next Steps

Congratulations! You've mastered the basics of Spring AI ChatClient. 

**What you've learned**:
- ✅ ChatClient fluent API
- ✅ Multi-turn conversation memory
- ✅ Basic configuration options
- ✅ Testing AI integrations

**Ready for the next level?** 

👉 **Continue to [Tutorial S2: Core Components Deep Dive](./S2-components-deep-dive.md)** to learn about:
- Message types and media handling
- Streaming responses
- Model options and configuration
- Structured output generation

## 📚 Additional Resources

- 📖 [S1 Module Guide](../modules/S1-multiturn.md) - Complete reference
- 🏗️ [ChatClient Architecture](../architecture/patterns.md#chatclient-patterns) - Design patterns
- 🔧 [Troubleshooting Guide](../guides/troubleshooting.md) - Common issues

---

**🎉 Great job completing your first Spring AI tutorial!** 

Ready to dive deeper? The next tutorial will show you how to handle different message types, implement streaming, and work with structured output.

[← Back to Tutorial Index](./README.md) | [Next Tutorial: S2 Components →](./S2-components-deep-dive.md)