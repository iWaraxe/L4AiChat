package com.coherentsolutions.l4aichat.s2components.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * This controller demonstrates the recommended ChatClient-based usage in Spring AI M7
 */
@RestController
@RequestMapping("/api/s2/model")
public class ChatModelDemoController {

    private final ChatClient chatClient;
    private final ChatClient streamingChatClient;

    // Replace ChatModel & StreamingChatModel with ChatClient.Builder injections
    public ChatModelDemoController(ChatClient.Builder chatClientBuilder,
                                   ChatClient.Builder streamingChatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.streamingChatClient = streamingChatClientBuilder.build();
    }

    /**
     * Simple example of using ChatClient with a single user message
     */
    @PostMapping("/basic")
    public Map<String, String> basicCompletion(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        // The simplest way to use ChatClient is a text string
        String response = chatClient.prompt()
                .user(userMessage)
                .call()
                .content();

        return Map.of("response", response);
    }

    /**
     * Example using Prompt to structure messages with system, user roles
     */
    @PostMapping("/prompt")
    public Map<String, Object> promptCompletion(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("You are a helpful AI assistant who specializes in Java and Spring Boot."));
        messages.add(new UserMessage(userMessage));
        Prompt prompt = new Prompt(messages);

        ChatResponse chatResponse = chatClient
                .prompt(prompt)
                .call()
                .chatResponse();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("content", chatResponse.getResult().getOutput().getText());
        responseData.put("role", chatResponse.getResult().getOutput().getMessageType().toString());
        responseData.put("generations", chatResponse.getResults().size());
        return responseData;
    }

    /**
     * Example with custom ChatOptions
     */
    @PostMapping("/options")
    public Map<String, Object> optionsCompletion(@RequestBody Map<String, Object> request) {
        String userMessage = (String) request.get("message");
        // Use Double for temperature if we want to be fully M7-compliant
        Double temperature = (Double) request.getOrDefault("temperature", 0.7d);
        // maxCompletionTokens is recommended for OpenAI (but maxTokens is still accepted)
        Integer maxTokens = (Integer) request.getOrDefault("maxTokens", 150);

        ChatOptions options = ChatOptions.builder()
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();

        Prompt prompt = new Prompt(
                List.of(new UserMessage(userMessage)),
                options
        );

        ChatResponse chatResponse = chatClient
                .prompt(prompt)
                .call()
                .chatResponse();

        return Map.of(
                "content", chatResponse.getResult().getOutput().getText(),
                "temperature", temperature,
                "maxTokens", maxTokens
        );
    }

    /**
     * Example using a multi-turn conversation in a single request
     */
    @PostMapping("/conversation")
    public Map<String, Object> conversationCompletion(@RequestBody Map<String, List<String>> request) {
        List<String> messages = request.get("messages");
        if (messages == null || messages.isEmpty()) {
            return Map.of("error", "Please provide a list of messages");
        }

        List<Message> conversationMessages = new ArrayList<>();
        conversationMessages.add(new SystemMessage("You are a helpful assistant."));

        boolean isUserMessage = true;
        for (String msg : messages) {
            if (isUserMessage) {
                conversationMessages.add(new UserMessage(msg));
            } else {
                conversationMessages.add(new AssistantMessage(msg));
            }
            isUserMessage = !isUserMessage;
        }
        Prompt prompt = new Prompt(conversationMessages);

        ChatResponse chatResponse = chatClient
                .prompt(prompt)
                .call()
                .chatResponse();

        return Map.of(
                "response", chatResponse.getResult().getOutput().getText(),
                "messageCount", conversationMessages.size()
        );
    }

    /**
     * Example using streaming API for token-by-token responses
     */
    @PostMapping(value = "/stream", produces = "text/event-stream")
    public Flux<String> streamCompletion(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        Prompt prompt = new Prompt(List.of(new UserMessage(userMessage)));

        return streamingChatClient.prompt(prompt)
                .stream()
                .content() // Flux<String>
                .map(chunk -> chunk); // pass-through or additional logic
    }
}
