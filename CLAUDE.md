# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

@~/.claude/spring-ai-course.md

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

## Project-Specific Run Commands

```bash
# Run specific module applications
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s1multiturn.AiChatApplication
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s2components.ComponentsApplication
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s3context.ContextApplication
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s4statemanagement.ChatbotApplication
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s5chatbot.SpringAiChatbotApplication
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s6advanced.ChatbotApplication

# Run with JDBC profile for persistent storage
./mvnw spring-boot:run -Dspring-boot.run.profiles=jdbc
```

## Module Architecture

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

## Project-Specific Configuration

**Database**: When using JDBC profile, H2 in-memory database is auto-configured. Access H2 console at `/h2-console` with:
- URL: `jdbc:h2:mem:chatdb`  
- Username: `sa`
- Password: (empty)

## API Endpoint Patterns

All modules follow these REST endpoint patterns:
- `POST /api/s{n}/chat/new` - Create new conversation
- `POST /api/s{n}/chat/{conversationId}` - Send message
- `DELETE /api/s{n}/chat/{conversationId}` - Clear conversation
- `GET /api/s{n}/chat/history/{conversationId}` - Get history (s4+)
- `POST /api/s{n}/chat/stream/{conversationId}` - Stream response (s5+)

## Development Notes

- Each module has its own Application class - run the specific one needed
- Module-specific README files contain detailed API documentation
- CORS is configured in s5 and s6 modules for frontend integration
- Use `@EnableWebFlux` for streaming support in servlet applications
EOF < /dev/null