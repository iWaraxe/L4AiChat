# Spring AI Chatbot (M7) – README

This project demonstrates a stateful chatbot built with **Spring Boot** and **Spring AI 1.0.0-M7**. The chatbot maintains conversation context across multiple messages, allowing users to have more natural, multi-turn dialogues.

## Features

- **RESTful** API endpoints for sending messages, creating/deleting conversations, and streaming responses
- **Stateful** conversation context management with Spring AI’s `ChatMemory` and `MessageChatMemoryAdvisor`
- **InMemory** or **JDBC** storage for chat history
- **Validation & Error Handling** via Spring Boot’s Bean Validation and a global exception handler
- **CORS** configuration for cross-origin requests
- **Simple** front-end integration (HTML/JS) for demo

## Prerequisites

- **Java 21** (or whichever version you use)
- **Maven** for building
- **OpenAI** API key (if you’re using the OpenAI model)
- (Optional) A **database** if you switch to JDBC memory

## Getting Started

1. **Clone** or download this repository.

2. **Set up** your environment:
   ```bash
   export OPENAI_API_KEY=your-api-key

or define it in your application.properties.
3.	Build the project:

mvn clean package


4.	Run the application:

java -jar target/your-chatbot.jar

By default, it starts on port 8080.

	5.	Test in your browser or with a tool like Postman:
	•	POST /api/chat/new to create a new conversation
	•	POST /api/chat/{conversationId} to send a message
	•	DELETE /api/chat/{conversationId} to clear conversation history
	•	POST /api/chat/stream/{conversationId} for streaming responses

How It Works
1.	ChatClient & Memory
    •	ChatClient is built using a builder pattern and registered with a MessageChatMemoryAdvisor for conversation context.
    •	ChatMemory is either InMemoryChatMemory or JdbcChatMemory depending on the profile.
2.	Conversation Flow
   •	A user calls POST /api/chat/new to start a new conversation, receiving a unique conversation ID.
   •	Each subsequent message includes that ID in the path, e.g. POST /api/chat/{conversationId}.
   •	The MessageChatMemoryAdvisor injects prior messages from the memory to ensure context-aware responses.
3.	Prompt Lifecycle
   1.	User sends text and conversation ID.
   2.	Advisor fetches old messages from memory, merges them into a single prompt.
   3.	ChatClient calls the AI model (e.g., OpenAI GPT).
   4.	AI returns an answer using the entire conversation context.
   5.	Service stores the new message + AI reply in memory.
   6.	Controller returns a JSON response to the user.
4.	Validation & Error Handling
   •	ChatRequest is annotated with e.g. @NotBlank or @Size.
   •	The global exception handler catches validation or server errors, returning consistent ErrorResponse objects.
5.	CORS
   •	A small config class uses WebMvcConfigurer to permit cross-origin requests on /api/**.

Switching to JDBC Memory

To persist conversation data:
1.	Add:
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-chat-memory-jdbc</artifactId>
    <version>1.0.0-M7</version>
</dependency>
```
and spring-boot-starter-jdbc.

2.	Configure your database credentials in application.properties.
3.	Use a profile that sets up JdbcChatService or JdbcChatMemory, e.g. --spring.profiles.active=jdbc.

Extensibility
•	Retrieval-Augmented Generation: Incorporate a QuestionAnswerAdvisor + vector store for domain knowledge.
•	Structured Output: Use .entity(...) or StructuredOutputConverter to parse AI responses into typed records.
•	Function (Tool) Calls: If you need advanced tool invocation, you can define ToolCallbacks and pass them to the ChatClient.

⸻

Enjoy building multi-turn AI chat experiences with Spring AI in your Spring Boot application! If you have any questions or want to add more features, consult the official Spring AI docs or open a GitHub issue in this repository.