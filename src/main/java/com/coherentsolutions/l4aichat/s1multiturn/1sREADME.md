# Spring AI Chatbot Demo - Using Spring AI M7 and ChatCompletion API
This project demonstrates how to build chatbots using Spring AI M7 and OpenAI's ChatCompletion API. It showcases both single-turn and multi-turn conversation patterns.

## About Spring AI M7
This project uses Spring AI 1.0.0-M7, which includes significant API changes from previous versions. The code demonstrates the current best practices for building AI-powered applications with Spring AI.

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

- `POST /api/chat/single-turn/model` - Uses ChatModel directly
- `POST /api/chat/single-turn/client` - Uses the more fluent ChatClient API
- `POST /api/chat/single-turn/with-options` - Demonstrates runtime options

Example request body:
```json
{
  "message": "What is Spring Boot?"
}
```

### Multi-Turn Conversations

Multi-turn conversations maintain context between requests using a conversation ID.

- `POST /api/chat/multi-turn/start` - Start a new conversation
- `POST /api/chat/multi-turn/continue/{conversationId}` - Continue an existing conversation
- `GET /api/chat/multi-turn/conversations` - List all active conversations
- `GET /api/chat/multi-turn/history/{conversationId}` - Get the history of a conversation
- `DELETE /api/chat/multi-turn/{conversationId}` - End a conversation and clear its history

### Message Structure Demos

These endpoints demonstrate how message structure works in the Chat API.

- `POST /api/chat/messages/manual-construction` - Shows how to manually construct message sequences
- `POST /api/chat/messages/client-with-context` - Shows how to use the ChatClient with manual context
- `POST /api/chat/messages/context-comparison` - Compares responses with and without context

## Key Concepts

- **Single-turn vs. Multi-turn**: Single-turn treats each request as independent, while multi-turn maintains conversation history.
- **Message Types**: SystemMessage, UserMessage, and AssistantMessage each serve different roles.
- **Conversation State**: ConversationService and ChatMemory manage conversation state.
- **ChatModel vs. ChatClient**: ChatModel provides low-level access, while ChatClient offers a more fluent API.
- **Spring AI M7 Patterns**: This implementation demonstrates M7's fluent API pattern for chat interaction, including proper handling of options, message construction, and the updated ChatMemory interface.

## Testing with cURL

### Single-Turn Example
```bash
curl -X POST http://localhost:8080/api/chat/single-turn/client \
  -H "Content-Type: application/json" \
  -d '{"message": "What is Spring Boot?"}'
```

### Multi-Turn Example
```bash
# Start a conversation
conversation=$(curl -X POST http://localhost:8080/api/chat/multi-turn/start \
  -H "Content-Type: application/json" \
  -d '{"message": "My name is Alice."}')

# Extract the conversation ID
conversationId=$(echo $conversation | jq -r '.conversationId')

# Continue the conversation
curl -X POST "http://localhost:8080/api/chat/multi-turn/continue/$conversationId" \
  -H "Content-Type: application/json" \
  -d '{"message": "What is my name?"}'
```

## Configuration
The application uses Spring Boot auto-configuration with Spring AI M7 starters. Add the following to your application.properties:

```properties
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4.1
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
            <version>1.0.0-M7</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```