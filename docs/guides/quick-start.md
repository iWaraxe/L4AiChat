# 🚀 Quick Start Guide

Get up and running with Spring AI chat applications in 5 minutes!

## Prerequisites

Before you begin, ensure you have:

- ☕ **Java 21** or higher
- 📦 **Maven 3.8+**
- 🔑 **OpenAI API Key**
- 💻 **Your favorite IDE** (IntelliJ IDEA, VS Code, Eclipse)

## Step 1: Clone the Repository

```bash
git clone https://github.com/iWaraxe/L4AiChat.git
cd L4AiChat
```

## Step 2: Set Your OpenAI API Key

### Option A: Environment Variable (Recommended)
```bash
export OPENAI_API_KEY=sk-proj-your-api-key-here
```

### Option B: Application Properties
Edit `src/main/resources/application.properties`:
```properties
spring.ai.openai.api-key=sk-proj-your-api-key-here
```

### Option C: System Property
```bash
mvn spring-boot:run -Dspring.ai.openai.api-key=sk-proj-your-api-key-here
```

## Step 3: Build the Project

```bash
mvn clean install
```

This will:
- Download all dependencies
- Compile all modules
- Run tests
- Package the application

## Step 4: Run Your First Chat Application

### Simple Single-Turn Chat (Module S1)

```bash
mvn spring-boot:run -pl :L4AiChat \
  -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s1multiturn.AiChatApplication
```

Test it:
```bash
curl -X POST http://localhost:8080/api/s1/chat/simple \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello, Spring AI!"}'
```

Expected response:
```json
{
  "response": "Hello! I'm here to help. Spring AI is a framework..."
}
```

### Production-Ready Chatbot (Module S5)

```bash
mvn spring-boot:run -pl :L4AiChat \
  -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s5chatbot.SpringAiChatbotApplication
```

Start a conversation:
```bash
# 1. Create new conversation
curl -X POST http://localhost:8080/api/s5/chat/new

# Response:
{
  "message": "Hello! I'm an AI assistant. How can I help you today?",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000"
}

# 2. Continue conversation (use the conversationId from above)
curl -X POST http://localhost:8080/api/s5/chat/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{"message": "My name is Alice"}'

# 3. Test memory
curl -X POST http://localhost:8080/api/s5/chat/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{"message": "What is my name?"}'

# Response:
{
  "message": "Your name is Alice.",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000"
}
```

## Step 5: Explore Different Modules

Each module demonstrates different capabilities:

### S2: ChatClient Components
```bash
mvn spring-boot:run -pl :L4AiChat \
  -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s2components.ComponentsApplication
```

### S7: Advanced Advisors
```bash
mvn spring-boot:run -pl :L4AiChat \
  -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s7advisors.AdvisorsApplication
```

### S8: Multi-Model Comparison
```bash
mvn spring-boot:run -pl :L4AiChat \
  -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s8multimodel.MultiModelApplication
```

## Common Issues and Solutions

### Issue: API Key Not Found
```
Error: API key not configured
```

**Solution:** Ensure your API key is set correctly:
```bash
echo $OPENAI_API_KEY  # Should show your key
```

### Issue: Port Already in Use
```
Port 8080 is already in use
```

**Solution:** Use a different port:
```bash
mvn spring-boot:run -Dserver.port=8081
```

### Issue: Timeout Errors
```
OpenAI API request timed out
```

**Solution:** Increase timeout in `application.properties`:
```properties
spring.ai.openai.chat.options.timeout=60s
```

### Issue: Rate Limiting
```
429 Too Many Requests
```

**Solution:** Add delay between requests or upgrade your OpenAI plan.

## Next Steps

### 1. Understand the Basics
Read through the modules in order:
- [S1: Multi-turn Conversations](../modules/s1-multiturn-guide.md)
- [S2: ChatClient Components](../modules/s2-components-guide.md)
- [S3: Context Management](../modules/s3-context-guide.md)

### 2. Try Advanced Features
- **Streaming Responses**: See S5 `/stream` endpoints
- **Multiple Models**: Try S8 model comparison
- **Custom Templates**: Explore S9 prompt templates

### 3. Build Your Own
1. Start with S1 as a template
2. Add features you need
3. Deploy to production (see [Production Guide](production.md))

## Quick Examples

### Example 1: Simple Q&A Bot
```java
@RestController
public class SimpleBot {
    private final ChatClient chatClient;
    
    @PostMapping("/ask")
    public String ask(@RequestBody String question) {
        return chatClient.prompt()
            .user(question)
            .call()
            .content();
    }
}
```

### Example 2: Chatbot with Memory
```java
@RestController
public class MemoryBot {
    private final ChatClient chatClient;
    private final ChatMemory memory;
    
    @PostMapping("/chat/{sessionId}")
    public String chat(@PathVariable String sessionId, 
                      @RequestBody String message) {
        return chatClient.prompt()
            .user(message)
            .advisors(a -> a.param(CONVERSATION_ID, sessionId))
            .call()
            .content();
    }
}
```

### Example 3: Streaming Response
```java
@PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> stream(@RequestBody String message) {
    return chatClient.prompt()
        .user(message)
        .stream()
        .content();
}
```

## Useful Commands

### Run Tests
```bash
mvn test
```

### Build Without Tests
```bash
mvn clean install -DskipTests
```

### Run Specific Module
```bash
mvn spring-boot:run -pl :L4AiChat \
  -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s1multiturn.AiChatApplication
```

### Check Dependencies
```bash
mvn dependency:tree
```

## Resources

- 📖 [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- 💬 [Community Support](https://github.com/spring-projects/spring-ai/discussions)
- 🐛 [Report Issues](https://github.com/iWaraxe/L4AiChat/issues)
- 📺 [Video Tutorials](https://www.youtube.com/spring-ai)

## Ready to Build?

You now have everything you need to start building AI-powered chat applications with Spring AI!

**Pro Tips:**
- 💡 Start with S1 for learning basics
- 🚀 Use S5 as a template for production apps
- 🧪 Always test with small prompts first
- 💰 Monitor your API usage to control costs

Happy coding! 🎉

---

[← Back to Main README](../../README.md) | [Module S1 Guide →](../modules/s1-multiturn-guide.md)