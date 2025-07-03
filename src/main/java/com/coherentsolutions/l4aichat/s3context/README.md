# S3 Context Module

## Overview

The S3 Context module demonstrates advanced context management using Spring AI 1.0.0's MessageChatMemoryAdvisor. This module shows how to maintain conversation context across multiple interactions using both in-memory and JDBC-based storage.

## Features

- **MessageChatMemoryAdvisor**: Modern Spring AI 1.0.0 context management
- **In-Memory Storage**: Fast, session-based conversation memory
- **JDBC Storage**: Persistent conversation history across restarts
- **Context Window Management**: Configurable message history limits
- **Conversation Isolation**: Separate contexts per conversation ID

## Endpoints

### Chat Endpoints
- `POST /api/s3/chat/new` - Start new conversation with context
- `POST /api/s3/chat/{conversationId}` - Continue conversation
- `DELETE /api/s3/chat/{conversationId}` - Clear conversation context

### Health Check
- `GET /api/s3/health` - Module health and memory status

## Usage Examples

### Start New Conversation
```bash
curl -X POST http://localhost:8080/api/s3/chat/new \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello, I am learning Spring AI"}'
```

### Continue Conversation
```bash
curl -X POST http://localhost:8080/api/s3/chat/conv-123 \
  -H "Content-Type: application/json" \
  -d '{"message": "What did I just tell you?"}'
```

## Key Classes

- `ChatController` - Main conversation endpoint handler
- `InMemoryChatService` - In-memory context management
- `JdbcChatService` - Database-backed context storage
- `ChatService` - Common service interface

## Configuration

### In-Memory Configuration
```java
@Bean
public ChatMemory chatMemory() {
    return MessageWindowChatMemory.builder()
        .maxMessages(10)
        .build();
}
```

### JDBC Configuration
Requires H2 database setup with appropriate schema for message storage.

## Migration from Manual Memory Management

This module has been fully migrated from manual memory handling to Spring AI 1.0.0's advisor system:

### Before (Manual)
```java
// Manual conversation history management
List<Message> conversationHistory = getHistory(conversationId);
conversationHistory.add(new UserMessage(userMessage));
ChatResponse response = chatModel.call(new Prompt(conversationHistory));
```

### After (Advisors)
```java
// Automatic context management via advisors
String response = chatClient.prompt()
    .user(userMessage)
    .advisors(advisor -> advisor
        .param(ChatMemory.CONVERSATION_ID, conversationId)
    )
    .call()
    .content();
```

## Learning Objectives

1. **Advisor Pattern**: Understand Spring AI's advisor-based architecture
2. **Memory Management**: Learn context window and storage strategies
3. **State Persistence**: Compare in-memory vs database storage
4. **Conversation Isolation**: Manage multiple concurrent conversations

## Best Practices

- Use appropriate memory window sizes (5-20 messages typical)
- Implement conversation cleanup for long-running applications
- Choose storage type based on persistence requirements
- Monitor memory usage in production environments

## Migration Notes

Successfully migrated to Spring AI 1.0.0:
- ✅ Replaced manual memory management with MessageChatMemoryAdvisor
- ✅ Updated to MessageWindowChatMemory.builder() pattern
- ✅ Implemented proper advisor parameter handling
- ✅ Maintained backward compatibility with existing API