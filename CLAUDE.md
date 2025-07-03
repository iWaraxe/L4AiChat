# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

@~/.claude/spring-ai-course.md

## Spring AI 1.0.0 Migration Status

This project has been successfully upgraded to Spring AI 1.0.0 final release.

### ✅ COMPLETED MIGRATIONS
1. **Version Upgrade**: ✅ All branches updated to Spring AI 1.0.0
2. **Module Refactoring**:
   - **s1multiturn**: ✅ ChatClient-centric approach with OpenAiChatOptions
   - **s2components**: ✅ ChatClient features with entity mapping and streaming
   - **s3context**: ✅ MessageChatMemoryAdvisor migration completed
   - **s4statemanagement**: ✅ Built-in ChatMemory advisors implementation
   - **s5chatbot**: ✅ Proper streaming with Flux<ChatResponse>
   - **s6advanced**: ✅ Advisor patterns and enhanced structured output

### ✅ ADVANCED FEATURES IMPLEMENTED
3. **Advanced Modules**: ✅ All implemented and functional
   - **s7advisors**: ✅ Custom advisor implementations with multiple ChatClient configurations
   - **s8multimodel**: ✅ Multiple model configurations (GPT-4, GPT-3.5 Turbo, GPT-4 Turbo)
   - **s9templates**: ✅ Advanced prompt templating with PromptTemplateService

### ✅ FINAL IMPLEMENTATIONS
4. **Enhancement**: ✅ Comprehensive testing implemented (72 tests total)
5. **Documentation**: ✅ Create module-specific README files

## Project-Specific Run Commands

```bash
# Run main application (all modules available)
./mvnw spring-boot:run

# Run with JDBC profile for persistent storage
./mvnw spring-boot:run -Dspring.profiles.active=jdbc

# Build and run
./mvnw clean package && java -jar target/L4AiChat-0.0.1-SNAPSHOT.jar

# Run with specific OpenAI API key
OPENAI_API_KEY=your_key_here ./mvnw spring-boot:run
```

## Module Architecture

### Current Module Progression (Spring AI 1.0.0 Migrated)
1. **s1multiturn**: ✅ Basic multi-turn conversations using ChatModel vs ChatClient
2. **s2components**: ✅ Core Spring AI components and message types  
3. **s3context**: ✅ Context management with in-memory and JDBC storage using MessageChatMemoryAdvisor
4. **s4statemanagement**: ✅ Advanced state management with conversation history
5. **s5chatbot**: ✅ Complete REST API chatbot with streaming and error handling
6. **s6advanced**: ✅ Advanced chat patterns and structured responses

### Advanced Modules (Already Implemented)
7. **s7advisors**: ✅ Custom advisor implementations including:
   - AdvisorDemoService with multiple ChatClient configurations
   - Advisor ordering and chaining patterns
   - Integration with Spring AI 1.0.0 advisor framework

8. **s8multimodel**: ✅ Multiple model configurations featuring:
   - MultiModelService with GPT-4, GPT-3.5 Turbo, and GPT-4 Turbo
   - Model-specific configurations and fallback patterns
   - Performance comparison capabilities

9. **s9templates**: ✅ Advanced prompt templating implementing:
   - PromptTemplateService with 6 different templates
   - Parameter substitution and template composition
   - JSON-safe template handling

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

- **Unified Architecture**: Single main application with all modules integrated
- **Centralized Configuration**: Spring AI beans configured in L4AiChatApplication
- **Module-specific README files**: Detailed API documentation for each module
- **CORS Configuration**: Enabled for frontend integration at http://localhost:3000
- **Streaming Support**: WebFlux enabled for real-time response streaming
- **Health Endpoints**: Each module has dedicated health checks at /api/s{n}/health

## Configuration Features

- **ChatClient.Builder**: Auto-configured for all service classes
- **ChatClient**: Pre-configured with GPT-4 and temperature 0.7
- **OpenAI Integration**: Automatic model and API key configuration
- **Database Support**: H2 in-memory database for JDBC profile
- **Profile Support**: Switch between default and JDBC profiles