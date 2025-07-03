# S1 Multi-Turn Module

## Overview

The S1 Multi-Turn module demonstrates the fundamental differences between single-turn and multi-turn conversations using Spring AI 1.0.0. This module provides a foundation for understanding how to maintain conversation context and history in AI-powered applications.

## Features

- **Single-Turn Conversations**: Each request is independent with no context retention
- **Multi-Turn Conversations**: Maintains conversation history across requests
- **ChatModel vs ChatClient**: Demonstrates both low-level and high-level API approaches
- **Option Configuration**: Shows how to customize model parameters per request

## Endpoints

### Single-Turn Chat Endpoints
- `POST /api/s1/chat/single-turn/model` - Direct ChatModel usage
- `POST /api/s1/chat/single-turn/client` - ChatClient fluent API
- `POST /api/s1/chat/single-turn/with-options` - Runtime option overrides

### Multi-Turn Chat Endpoints  
- `POST /api/s1/chat/multi-turn/new` - Start new conversation
- `POST /api/s1/chat/multi-turn/{conversationId}` - Continue conversation
- `DELETE /api/s1/chat/multi-turn/{conversationId}` - Clear conversation

### Health Check
- `GET /api/s1/health` - Module health and status

## Usage Examples

### Single-Turn Request
```bash
curl -X POST http://localhost:8080/api/s1/chat/single-turn/client \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello, how are you?"}'
```

### Multi-Turn Conversation
```bash
# Start new conversation
curl -X POST http://localhost:8080/api/s1/chat/multi-turn/new \
  -H "Content-Type: application/json" \
  -d '{"message": "My name is John"}'

# Continue conversation (use returned conversationId)
curl -X POST http://localhost:8080/api/s1/chat/multi-turn/conv-123 \
  -H "Content-Type: application/json" \
  -d '{"message": "What is my name?"}'
```

## Key Classes

- `SingleTurnChatController` - Handles independent chat requests
- `MultiTurnChatController` - Manages conversation state and history
- `ChatMessageDemoController` - Demonstrates message type handling

## Configuration

The module uses the main application's Spring AI configuration:
- OpenAI API integration via `spring.ai.openai.api-key`
- Default model: GPT-3.5-turbo
- Configurable temperature and token limits

## Learning Objectives

1. **API Patterns**: Understand ChatModel vs ChatClient approaches
2. **State Management**: Learn conversation persistence strategies  
3. **Context Handling**: Master message history maintenance
4. **Option Customization**: Control model behavior per request

## Migration Notes

This module has been updated for Spring AI 1.0.0:
- Uses `OpenAiChatOptions` instead of generic `ChatOptions`
- Leverages new fluent ChatClient API patterns
- Follows modern Spring AI best practices