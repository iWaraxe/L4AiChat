# Spring AI Chatbot Demo - Using Spring AI 1.0.0 and ChatCompletion API
This project demonstrates how to build chatbots using Spring AI 1.0.0 and OpenAI's ChatCompletion API. It showcases both single-turn and multi-turn conversation patterns.

## About Spring AI 1.0.0
This project uses Spring AI 1.0.0 final release. The code demonstrates the current best practices for building AI-powered applications with Spring AI, including the use of ChatClient, advisors, and structured memory management.

## Prerequisites

- Java 17 or higher
- Maven
- OpenAI API key (set as environment variable OPENAI_API_KEY)

## Running the Application

1. Set your OpenAI API key:
   ```
   export OPENAI_API_KEY=your_api_key_here
   ```

2. Run the application:
   ```
   mvn spring-boot:run
   ```

3. The application will start on http://localhost:8080

## API Endpoints

### Single-Turn Conversations

Single-turn conversations do not maintain any context between requests.

- `POST /api/s1/chat/single-turn/model` - Uses ChatModel directly
- `POST /api/s1/chat/single-turn/client` - Uses the more fluent ChatClient API
- `POST /api/s1/chat/single-turn/with-options` - Demonstrates runtime options

Example request body:
```json
{
  "message": "What is Spring Boot?"
}
```

### Multi-Turn Conversations

Multi-turn conversations maintain context between requests using a conversation ID.

- `POST /api/s1/chat/multi-turn/start` - Start a new conversation
- `POST /api/s1/chat/multi-turn/continue/{conversationId}` - Continue an existing conversation
- `GET /api/s1/chat/multi-turn/conversations` - List all active conversations
- `GET /api/s1/chat/multi-turn/history/{conversationId}` - Get the history of a conversation
- `DELETE /api/s1/chat/multi-turn/{conversationId}` - End a conversation and clear its history

### Message Structure Demos

These endpoints demonstrate how message structure works in the Chat API.

- `POST /api/s1/chat/messages/manual-construction` - Shows how to manually construct message sequences
- `POST /api/s1/chat/messages/client-with-context` - Shows how to use the ChatClient with manual context
- `POST /api/s1/chat/messages/context-comparison` - Compares responses with and without context

## Key Concepts

- **Single-turn vs. Multi-turn**: Single-turn treats each request as independent, while multi-turn maintains conversation history.
- **Message Types**: SystemMessage, UserMessage, and AssistantMessage each serve different roles.
- **Conversation State**: ConversationService and ChatMemory manage conversation state.
- **ChatModel vs. ChatClient**: ChatModel provides low-level access, while ChatClient offers a more fluent API.
- **Spring AI 1.0.0 Patterns**: This implementation demonstrates Spring AI's fluent API pattern for chat interaction, including proper handling of options, message construction, and the advisor-based ChatMemory interface.

## Testing with cURL

### Single-Turn Example
```bash
curl -X POST http://localhost:8080/api/s1/chat/single-turn/client \
  -H "Content-Type: application/json" \
  -d '{"message": "What is Spring Boot?"}'
```

### Multi-Turn Example
```bash
# Start a conversation
conversation=$(curl -X POST http://localhost:8080/api/s1/chat/multi-turn/start \
  -H "Content-Type: application/json" \
  -d '{"message": "My name is Alice."}')

# Extract the conversation ID
conversationId=$(echo $conversation | jq -r '.conversationId')

# Continue the conversation
curl -X POST "http://localhost:8080/api/s1/chat/multi-turn/continue/$conversationId" \
  -H "Content-Type: application/json" \
  -d '{"message": "What is my name?"}'
```

## Configuration
The application uses Spring Boot auto-configuration with Spring AI 1.0.0 starters. Add the following to your application.properties:

```properties
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4
```

## Dependencies

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```