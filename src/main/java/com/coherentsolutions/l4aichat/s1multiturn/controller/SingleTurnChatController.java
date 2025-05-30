package com.coherentsolutions.l4aichat.s1multiturn.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller that demonstrates single-turn interactions with OpenAI.
 * In single-turn interactions, we don't maintain any conversation history
 * between requests. Each request is treated as a brand new conversation.
 */
@RestController
@RequestMapping("/api/s1/chat/single-turn")
public class SingleTurnChatController {

    // Directly using ChatModel for lower-level control
    private final ChatModel chatModel;

    // Using ChatClient for a more fluent API
    private final ChatClient chatClient;

    public SingleTurnChatController(ChatModel chatModel, ChatClient.Builder builder) {
        this.chatModel = chatModel;
        this.chatClient = builder.build();
    }

    /**
     * Simple endpoint using ChatModel directly to demonstrate a basic interaction
     */
    @PostMapping("/model")
    public Map<String, String> chatWithModel(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // Create a prompt with just the user's message
        Prompt prompt = new Prompt(new UserMessage(userMessage));

        // Call the ChatModel directly
        String response = this.chatModel.call(prompt).getResult().getOutput().getText();

        return Map.of(
                "input", userMessage,
                "response", response
        );
    }

    /**
     * Endpoint using ChatClient to demonstrate a more fluent API
     */
    @PostMapping("/client")
    public Map<String, String> chatWithClient(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // Use the fluent API of ChatClient
        String response = this.chatClient.prompt()
                .system("You are a helpful AI assistant who writes in verses.")
                .user(userMessage)
                .call()
                .content();

        return Map.of(
                "input", userMessage,
                "response", response
        );
    }

    /**
     * Endpoint showing how to override model options at runtime
     */
    @PostMapping("/with-options")
    public Map<String, String> chatWithOptions(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // Override model options for this specific request
        String response = this.chatClient.prompt()
                .user(userMessage)
                .options(OpenAiChatOptions.builder()
                        .temperature(0.1)
                        .maxTokens(150)
                        .build())    // Limiting response length
                .call()
                .content();

        return Map.of(
                "input", userMessage,
                "response", response
        );
    }
}