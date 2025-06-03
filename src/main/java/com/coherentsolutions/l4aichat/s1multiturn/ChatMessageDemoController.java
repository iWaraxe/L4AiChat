package com.coherentsolutions.l4aichat.s1multiturn;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller that demonstrates how messages work in Spring AI and OpenAI's Chat API
 */
@RestController
@RequestMapping("/api/s1/chat/messages")
public class ChatMessageDemoController {

    private final ChatModel chatModel;
    private final ChatClient chatClient;

    public ChatMessageDemoController(ChatModel chatModel, ChatClient.Builder chatClientBuilder) {
        this.chatModel = chatModel;
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Demonstrate manual message construction with the three core message types:
     * - SystemMessage: Sets overall instructions and persona for the AI
     * - UserMessage: Contains the user's input
     * - AssistantMessage: Contains AI-generated responses
     */
    @PostMapping("/manual-construction")
    public Map<String, Object> manualMessageConstruction(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // Create a list of messages manually to represent a conversation
        List<Message> messages = new ArrayList<>();

        // System message sets the context for the entire conversation
        messages.add(new SystemMessage("You are a helpful AI assistant with a focus on programming."));

        // Add a previous exchange to demonstrate context
        messages.add(new UserMessage("I'm learning Spring Boot."));
        messages.add(new AssistantMessage("That's great! Spring Boot is an excellent framework for building Java applications quickly."));

        // Add the current user message
        messages.add(new UserMessage(userMessage));

        // Create a prompt with our manually constructed message list
        Prompt prompt = new Prompt(messages);

        // Call the ChatModel
        String response = this.chatModel.call(prompt).getResult().getOutput().getText();

        return Map.of(
                "messages", messages,
                "response", response
        );
    }

    /**
     * Demonstrate using the ChatClient fluent API with manual context management
     */
    @PostMapping("/client-with-context")
    public Map<String, Object> clientWithContext(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // Create messages for previous exchange
        UserMessage previousUserMessage = new UserMessage("I'm learning Spring Boot.");
        AssistantMessage previousAssistantMessage = new AssistantMessage(
                "That's great! Spring Boot is an excellent framework for building Java applications quickly.");

        // Use the ChatClient fluent API while specifying a custom conversation context
        String response = this.chatClient.prompt()
                .system("You are a helpful AI assistant with a focus on programming.")
                .messages(List.of(previousUserMessage, previousAssistantMessage))
                .user(userMessage)
                .call()
                .content();

        // Return both the constructed context and the response
        List<Message> context = List.of(
                new SystemMessage("You are a helpful AI assistant with a focus on programming."),
                previousUserMessage,
                previousAssistantMessage,
                new UserMessage(userMessage)
        );

        return Map.of(
                "context", context,
                "response", response
        );
    }

    /**
     * This endpoint demonstrates the difference between having context and not having it
     */
    @PostMapping("/context-comparison")
    public Map<String, Object> contextComparison(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // First request: Without context
        String responseWithoutContext = this.chatClient.prompt()
                .user(userMessage)
                .call()
                .content();

        // Second request: With context from a previous exchange
        String responseWithContext = this.chatClient.prompt()
                .system("You are a helpful AI assistant.")
                .messages(List.of(
                        new UserMessage("My name is John and I'm working on a Spring Boot project."),
                        new AssistantMessage("Hello John! I'd be happy to help with your Java project. What kind of assistance do you need?")
                ))
                .user(userMessage)
                .call()
                .content();

        return Map.of(
                "withoutContext", responseWithoutContext,
                "withContext", responseWithContext,
                "originalMessage", userMessage
        );
    }
}