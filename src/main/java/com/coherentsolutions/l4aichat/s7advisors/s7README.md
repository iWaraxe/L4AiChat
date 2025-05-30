# Spring AI Advisors Demo (1.0.0) – README

This module demonstrates advanced advisor patterns and configurations in **Spring AI 1.0.0**. It showcases different ways to use advisors to enhance AI chat interactions with memory, context awareness, and custom options.

## Features

- **Multiple ChatClient Configurations** with different advisor setups
- **MessageChatMemoryAdvisor** for conversation context management
- **Context-Aware Prompting** with user location and preferences
- **Custom OpenAI Options** (temperature, max tokens) with advisors
- **Conversation Management** with statistics and history
- **RESTful API** endpoints for different advisor patterns

## Advisor Patterns Demonstrated

### 1. Basic Chat (No Advisors)
- Pure ChatClient without any advisors
- Stateless interactions
- Endpoint: `POST /api/s7/advisors/basic`

### 2. Memory-Enabled Chat
- Uses `MessageChatMemoryAdvisor` for conversation context
- Maintains conversation history across requests
- Endpoints:
  - `POST /api/s7/advisors/memory/new` - Start new conversation
  - `POST /api/s7/advisors/memory/{conversationId}` - Continue conversation

### 3. Context-Aware Chat
- Enhanced system prompts with contextual information
- Incorporates user location and preferences
- Uses memory advisor for conversation continuity
- Endpoint: `POST /api/s7/advisors/context/{conversationId}`

### 4. Custom Options Chat
- Demonstrates advisor usage with custom `OpenAiChatOptions`
- Configurable temperature and max tokens
- Endpoint: `POST /api/s7/advisors/custom-options/{conversationId}`

## Key Technical Concepts

### MessageChatMemoryAdvisor
```java
MessageChatMemoryAdvisor.builder(chatMemory).build()
```
- Automatically injects conversation history into prompts
- Uses `MessageWindowChatMemory` with configurable message limits
- Conversation isolation by conversation ID

### Context Enrichment
- Manual context injection through enhanced user messages
- Demonstrates how to programmatically enrich prompts
- Combines user location, preferences, and original message

### Multiple ChatClient Configurations
```java
// Basic client
chatClientBuilder.defaultSystem("You are a helpful assistant.").build()

// Memory-enabled client  
chatClientBuilder
    .defaultSystem("You are a helpful assistant with memory.")
    .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
    .build()
```

## API Endpoints

### Chat Operations
- `POST /api/s7/advisors/basic` - Basic chat without advisors
- `POST /api/s7/advisors/memory/new` - Start memory conversation
- `POST /api/s7/advisors/memory/{id}` - Memory-enabled chat
- `POST /api/s7/advisors/context/{id}` - Context-aware chat
- `POST /api/s7/advisors/custom-options/{id}` - Custom options chat

### Conversation Management
- `GET /api/s7/advisors/history/{id}` - Get conversation history
- `GET /api/s7/advisors/stats/{id}` - Get conversation statistics
- `DELETE /api/s7/advisors/conversation/{id}` - Clear conversation

## Request Format

```json
{
  "message": "Your message here",
  "userLocation": "New York, NY", 
  "userPreferences": "Technical explanations",
  "enableContextEnrichment": true,
  "enableContentFilter": true,
  "enableLogging": true
}
```

## Response Format

```json
{
  "message": "AI response",
  "conversationId": "uuid",
  "timestamp": "2024-01-15T10:30:00",
  "advisorsUsed": ["MessageChatMemoryAdvisor", "ContextEnrichment"],
  "contentFiltered": false,
  "processingTimeMs": 1500
}
```

## Usage Examples

### 1. Start a Memory Conversation
```bash
curl -X POST http://localhost:8080/api/s7/advisors/memory/new
```

### 2. Continue Conversation with Context
```bash
curl -X POST http://localhost:8080/api/s7/advisors/context/{conversationId} \\
  -H "Content-Type: application/json" \\
  -d '{
    "message": "What's the weather like?",
    "userLocation": "San Francisco, CA",
    "userPreferences": "Brief responses"
  }'
```

### 3. Custom Options Chat
```bash
curl -X POST "http://localhost:8080/api/s7/advisors/custom-options/{conversationId}?temperature=0.9&maxTokens=100" \\
  -H "Content-Type: application/json" \\
  -d '{"message": "Be creative and write a short poem"}'
```

## Configuration

The module uses:
- **MessageWindowChatMemory** with 10 message limit
- **InMemoryChatMemoryRepository** for development
- **OpenAiChatOptions** for provider-specific settings
- **Multiple ChatClient instances** with different configurations

## Benefits of This Approach

1. **Modular Design** - Different clients for different use cases
2. **Advisor Composition** - Easy to combine multiple advisor patterns
3. **Context Management** - Flexible context enrichment strategies
4. **Performance Monitoring** - Built-in timing and advisor tracking
5. **Conversation Isolation** - Clean separation between conversations

This module serves as a comprehensive example of how to effectively use Spring AI 1.0.0 advisors in production applications.