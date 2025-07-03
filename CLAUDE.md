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

### 🔄 REMAINING TASKS
4. **Enhancement**: Add comprehensive testing for all modules
5. **Documentation**: Create module-specific README files

## Project-Specific Run Commands

```bash
# Run specific module applications
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s1multiturn.AiChatApplication
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s2components.ComponentsApplication
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s3context.ContextApplication
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s4statemanagement.ChatbotApplication
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s5chatbot.SpringAiChatbotApplication
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s6advanced.AdvancedInteractionApplication

# Run new module applications (s7-s9)
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s7advisors.AdvisorsApplication
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s8multimodel.MultiModelApplication
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s9templates.TemplatesApplication

# Run main application (all modules available)
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.L4AiChatApplication

# Run with JDBC profile for persistent storage
./mvnw spring-boot:run -Dspring-boot.run.profiles=jdbc
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

- Each module has its own Application class - run the specific one needed
- Module-specific README files contain detailed API documentation
- CORS is configured in s5 and s6 modules for frontend integration
- Use `@EnableWebFlux` for streaming support in servlet applications
EOF < /dev/null