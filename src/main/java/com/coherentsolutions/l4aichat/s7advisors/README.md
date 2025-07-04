# S7 - Custom Advisors Module

## Overview
This module demonstrates advanced advisor patterns and configurations in Spring AI 1.0.0. It showcases different ChatClient configurations with various advisor implementations, memory management, and custom OpenAI options.

## Key Features

### 1. Multiple ChatClient Configurations
- **Basic ChatClient**: Simple chat without advisors
- **Memory-Enabled ChatClient**: With `MessageChatMemoryAdvisor` for conversation continuity
- **Context-Aware ChatClient**: Enhanced system prompts with location and preference context
- **Custom Options ChatClient**: Configurable temperature and token limits

### 2. Advanced Advisor Patterns
- **MessageChatMemoryAdvisor**: Manages conversation history with configurable message windows
- **Context Enrichment**: Enhances user messages with location and preference data
- **Custom Options**: Dynamic OpenAI configuration per request

### 3. Memory Management
- **In-Memory Repository**: Fast conversation storage for development
- **Message Window**: Configurable history size (default: 10 messages)
- **Conversation Lifecycle**: Create, maintain, and clear conversations

## API Endpoints

### Basic Chat (No Advisors)
```
POST /api/s7/advisors/basic
Content-Type: application/json

{
  "message": "Hello, how are you?"
}
```

### Memory-Enabled Chat
```
# Start new conversation
POST /api/s7/advisors/memory/new

# Continue conversation
POST /api/s7/advisors/memory/{conversationId}
Content-Type: application/json

{
  "message": "What did we discuss earlier?"
}
```

### Context-Aware Chat
```
POST /api/s7/advisors/context/{conversationId}
Content-Type: application/json

{
  "message": "What's the weather like?",
  "userLocation": "New York",
  "userPreferences": "prefers detailed explanations"
}
```

### Custom Options Chat
```
POST /api/s7/advisors/custom-options/{conversationId}?temperature=0.3&maxTokens=100
Content-Type: application/json

{
  "message": "Give me a creative story"
}
```

### Conversation Management
```
# Get conversation history
GET /api/s7/advisors/history/{conversationId}

# Get conversation statistics
GET /api/s7/advisors/stats/{conversationId}

# Clear conversation
DELETE /api/s7/advisors/conversation/{conversationId}
```

## DTOs

### ChatRequest
```java
public record ChatRequest(
    String message,
    String userLocation,      // Optional
    String userPreferences    // Optional
) {}
```

### ChatResponse
```java
public record ChatResponse(
    String response,
    String conversationId,
    List<String> advisorsUsed,
    Long processingTimeMs
) {}
```

## Technical Implementation

### Service Layer
The `AdvisorDemoService` demonstrates:
- **Constructor Injection**: Multiple ChatClient instances with different configurations
- **Advisor Chaining**: Combining multiple advisors for enhanced functionality
- **Memory Management**: Conversation lifecycle and history retrieval
- **Error Handling**: Graceful degradation when advisors fail

### Key Design Patterns
1. **Builder Pattern**: ChatClient configuration with fluent API
2. **Strategy Pattern**: Different chat strategies based on advisor configuration
3. **Factory Pattern**: Conversation ID generation and ChatClient creation
4. **Repository Pattern**: In-memory storage for conversation history

## Configuration

### Required Dependencies
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

### Environment Variables
```
OPENAI_API_KEY=your_openai_api_key
```

## Testing

### Unit Tests
- Service layer testing with mocked ChatClient
- Controller testing with MockMvc
- Memory management validation

### Integration Tests
- End-to-end conversation flows
- Advisor chaining verification
- Performance measurement

## Performance Considerations

### Memory Usage
- In-memory storage suitable for development
- Consider Redis for production with high conversation volumes
- Message window limits prevent unbounded growth

### Response Times
- Basic chat: ~500ms
- Memory-enabled: ~800ms (includes history retrieval)
- Context-aware: ~900ms (includes context processing)

## Learning Objectives

1. **Advisor Pattern**: Understanding how advisors enhance ChatClient functionality
2. **Memory Management**: Implementing conversation continuity
3. **Configuration Flexibility**: Multiple ChatClient configurations in single service
4. **Performance Optimization**: Measuring and optimizing response times
5. **Context Enhancement**: Enriching user messages with additional data

## Next Steps

This module prepares for:
- **S8 Multi-Model**: Different AI models with advisor compatibility
- **S9 Prompt Engineering**: Advanced prompt templates with advisor integration
- **Production Deployment**: Scaling advisor patterns for enterprise use

## Common Issues

### Memory Leaks
- Ensure conversation cleanup in production
- Monitor memory usage with many concurrent conversations

### Advisor Conflicts
- Order matters when chaining multiple advisors
- Test advisor combinations thoroughly

### Performance Bottlenecks
- Context enrichment can slow response times
- Consider caching for frequently accessed context data