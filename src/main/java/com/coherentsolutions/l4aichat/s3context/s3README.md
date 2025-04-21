# Spring AI Chatbot with Memory

This project demonstrates how to build a chatbot using Spring Boot and **Spring AI** (version 1.0.0-M7) with the ability to maintain conversation context across multiple messages.

## Key Features

- Contextual conversations using **Spring AI ChatMemory**
- Two memory implementations:
    - **InMemoryChatMemory** – Stores conversations in memory (default)
    - **JdbcChatMemory** – Stores conversations in a relational database (H2 for demo)
- RESTful API for chat interactions
- Simple web interface

## Prerequisites

- Java 21
- Maven
- OpenAI API key

## Getting Started

1. **Clone the repository**
2. **Set your OpenAI API key** as an environment variable:
   ```
    export OPENAI_API_KEY=your-api-key
   ```
3.	**Build the project**:
     mvn clean package
    
4.	**Run the application**:
   ```
   java -jar target/chatbot-demo-0.0.1-SNAPSHOT.jar
   ```
5.	**Open your browser** to http://localhost:8080 to interact with the chatbot.

## Memory Implementation Profiles

By default, the application uses `InMemoryChatMemory`. To switch to a JDBC implementation, activate the jdbc profile:
    ```
    java -jar target/chatbot-demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=jdbc
    ```

In that case, you’ll need to configure your database properties in application-jdbc.properties.

## API Endpoints
•	**POST** /api/s3/chat/new – Start a new conversation
•	**POST** /api/s3/chat/{conversationId} – Send a message to an existing conversation
•	**DELETE** /api/s3/chat/{conversationId} – End a conversation and clear its history
•	**GET** /api/s3/chat/info – Get information about the active chat service

## Architecture
1.	Controller Layer – Handles HTTP requests
2.	Service Layer – Contains chat logic and delegates to memory
3.	Memory Layer – Implementation of the ChatMemory interface, either in-memory or JDBC
4.	Spring AI Components – ChatClient for AI calls, MessageChatMemoryAdvisor for injecting conversation context

## Spring AI Components Used
•	ChatClient – High-level client for communication with AI models
•	ChatMemory – Interface for storing and retrieving chat messages
•	MessageChatMemoryAdvisor – Advisor that injects conversation history into prompts
•	UserMessage / AssistantMessage – Represent the roles of messages in a conversation

## How It Works
1.	The user starts a new conversation, which generates a conversationId.
2.	Each subsequent message is stored in the chosen memory implementation.
3.	MessageChatMemoryAdvisor retrieves existing messages for that conversationId.
4.	These messages are appended to the prompt for context-aware AI responses.
5.	The AI model receives both the new message and conversation history to generate a reply.

## Tradeoffs Between Memory Implementations

### InMemoryChatMemory
•	Pros: Simple, no extra dependencies, fast
•	Cons: Data is lost on application restart, not production-ready

### JdbcChatMemory
•	Pros: Persistent storage, works with any SQL DB, more robust than in-memory
•	Cons: Potential performance considerations with large histories
