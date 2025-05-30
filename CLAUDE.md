# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Spring AI 1.0.0 Migration Plan

This project is being upgraded from Spring AI 1.0.0-M7 to 1.0.0 final release. The migration involves:

### Phase 1: Core Refactoring (High Priority)
1. **Version Upgrade**: Update pom.xml from Spring AI 1.0.0-M7 to 1.0.0
2. **Module Refactoring**:
   - **s1multiturn**: Migrate from ChatModel-centric to ChatClient-centric approach
   - **s2components**: Update to showcase ChatClient features (entity mapping, streaming)
   - **s3context**: Replace manual memory management with MessageChatMemoryAdvisor
   - **s4statemanagement**: Use built-in ChatMemory advisors instead of custom implementation
   - **s5chatbot**: Implement proper streaming with Flux<ChatResponse>
   - **s6advanced**: Add advisor patterns and enhanced structured output

### Phase 2: New Features (Medium Priority)
3. **New Modules**:
   - **s7advisors**: Custom advisor implementations (Re2Advisor, LoggingAdvisor, SafetyAdvisor)
   - **s8multimodel**: Multiple model configurations (OpenAI, Anthropic, Groq via mutate())
   - **s9templates**: Advanced prompt templating with custom TemplateRenderer

### Phase 3: Testing & Documentation (Medium Priority)
4. **Comprehensive Testing**: Add unit and integration tests for all modules
5. **Documentation Updates**: Update README files with new API patterns

## Build and Run Commands

```bash
# Build project
./mvnw clean install

# Run tests
./mvnw test

# Run specific module application
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s1multiturn.AiChatApplication
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s2components.ComponentsApplication
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s3context.ContextApplication
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s4statemanagement.ChatbotApplication
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s5chatbot.SpringAiChatbotApplication
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s6advanced.ChatbotApplication

# Run with JDBC profile for persistent storage
./mvnw spring-boot:run -Dspring-boot.run.profiles=jdbc

# Package application
./mvnw package
```

## Architecture Overview

This is a Spring Boot educational project demonstrating progressive AI chatbot implementations using Spring AI. The codebase is organized into modules, each building upon previous concepts:

### Current Module Progression (Being Refactored)
1. **s1multiturn**: Basic multi-turn conversations using ChatModel vs ChatClient
2. **s2components**: Core Spring AI components and message types
3. **s3context**: Context management with in-memory and JDBC storage
4. **s4statemanagement**: Advanced state management with conversation history
5. **s5chatbot**: Complete REST API chatbot with streaming and error handling
6. **s6advanced**: Advanced chat patterns and structured responses

### New Modules (To Be Created)
7. **s7advisors**: Custom advisor implementations demonstrating:
   - Re-Reading (Re2) Advisor for improved reasoning
   - SimpleLoggerAdvisor for debugging
   - Custom SafetyAdvisor for content filtering
   - Advisor ordering and chaining patterns

8. **s8multimodel**: Multiple model configurations showcasing:
   - Working with multiple ChatClient instances
   - OpenAI-compatible endpoints (Groq, etc.)
   - Model-specific configurations
   - A/B testing patterns

9. **s9templates**: Advanced prompt templating featuring:
   - Custom TemplateRenderer implementations
   - JSON-safe template delimiters
   - Complex parameter substitution
   - Template composition patterns

### Key Architectural Patterns (Spring AI 1.0.0)

**ChatClient-Centric Design**: 
```java
@Bean
ChatClient chatClient(ChatClient.Builder builder) {
    return builder
        .defaultSystem("You are a helpful assistant")
        .defaultAdvisors(
            MessageChatMemoryAdvisor.builder(chatMemory).build()
        )
        .build();
}
```

**Advisor Pattern**: Interceptors for enhancing AI interactions:
- Pre-processing: Modify prompts before sending to LLM
- Post-processing: Transform responses before returning
- Context injection: Add memory, RAG data, or other context

**Memory Management with Advisors**:
- `MessageChatMemoryAdvisor`: Maintains conversation as message list
- `PromptChatMemoryAdvisor`: Appends history to system prompt
- `VectorStoreChatMemoryAdvisor`: Retrieves relevant context from vector store

**Structured Output**: Entity mapping from AI responses:
```java
ActorFilms films = chatClient.prompt()
    .user("Generate filmography")
    .call()
    .entity(ActorFilms.class);
```

### Configuration Requirements

**OpenAI API Key**: Must be set as environment variable:
```bash
export OPENAI_API_KEY=your_api_key_here
```

**Database**: When using JDBC profile, H2 in-memory database is auto-configured. Access H2 console at `/h2-console` with:
- URL: `jdbc:h2:mem:chatdb`
- Username: `sa`
- Password: (empty)

**ChatClient Autoconfiguration**: Disable for multiple models:
```properties
spring.ai.chat.client.enabled=false
```

### API Conventions

All modules follow similar REST endpoint patterns:
- `POST /api/s{n}/chat/new` - Create new conversation
- `POST /api/s{n}/chat/{conversationId}` - Send message
- `DELETE /api/s{n}/chat/{conversationId}` - Clear conversation
- `GET /api/s{n}/chat/history/{conversationId}` - Get history (s4+)
- `POST /api/s{n}/chat/stream/{conversationId}` - Stream response (s5+)

### Spring AI 1.0.0 Key Changes

1. **ChatClient is Primary**: ChatModel is now lower-level; use ChatClient for most cases
2. **Advisors Replace Manual Context**: No more manual prompt building for memory/RAG
3. **Fluent API**: Cleaner syntax with `.prompt().user().call().content()`
4. **Built-in Memory**: ChatMemory with multiple repository implementations
5. **Streaming Improvements**: Better Flux handling with proper backpressure

### Development Notes

- Each module has its own Application class - run the specific one needed
- Module-specific README files contain detailed API documentation
- Spring AI 1.0.0 requires Spring Boot 3.4.0+
- Default model is GPT-4 with temperature 0.7
- CORS is configured in s5 and s6 modules for frontend integration
- Use `@EnableWebFlux` for streaming support in servlet applications