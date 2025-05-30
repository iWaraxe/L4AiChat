package com.coherentsolutions.l4aichat.s2components.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This controller demonstrates the fluent ChatClient API for Spring AI 1.0.0
 */
@RestController
@RequestMapping("/api/s2/client")
public class ChatClientDemoController {

    private final ChatClient chatClient;

    public ChatClientDemoController(ChatClient.Builder chatClientBuilder) {
        // Configure the ChatClient with default system message or advisors if desired
        this.chatClient = chatClientBuilder
                .defaultSystem("You are a friendly AI assistant with expertise in Spring Framework.")
                .build();
    }

    /**
     * Simple example using the fluent API
     */
    @PostMapping("/basic")
    public Map<String, String> basicCompletion(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // Build a prompt and retrieve the result as a String
        String response = this.chatClient
                .prompt()
                .user(userMessage)
                .call()
                .content();

        return Map.of("response", response);
    }

    /**
     * Example showing how to override the system message per request
     */
    @PostMapping("/system")
    public Map<String, String> systemCompletion(@RequestBody Map<String, Object> request) {
        String userMessage = (String) request.get("message");
        String systemMessage = (String) request.getOrDefault(
                "system",
                "You are a technical expert in Java development."
        );

        String response = this.chatClient
                .prompt()
                .system(systemMessage)  // override the default system text
                .user(userMessage)
                .call()
                .content();

        return Map.of("response", response);
    }

    /**
     * Example showing how to use parameters with a prompt template style
     */
    @PostMapping("/template")
    public Map<String, String> templateCompletion(@RequestBody Map<String, Object> request) {
        String topic = (String) request.get("topic");
        Integer count = (Integer) request.getOrDefault("count", 3);

        String response = this.chatClient
                .prompt()
                .user(u -> u.text("Generate {count} interesting facts about {topic}.")
                        .param("count", count)
                        .param("topic", topic))
                .call()
                .content();

        return Map.of("response", response);
    }

    /**
     * Example showing how to access ChatResponse metadata
     */
    @PostMapping("/metadata")
    public Map<String, Object> metadataCompletion(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        ChatResponse chatResponse = this.chatClient
                .prompt()
                .user(userMessage)
                .call()
                .chatResponse();

        Map<String, Object> responseData = new HashMap<>();
        // AssistantMessage text is retrieved via getText() in Spring AI 1.0.0
        responseData.put("content",  chatResponse.getResult().getOutput().getText());
        responseData.put("messageType", chatResponse.getResult().getOutput().getMessageType());

        // If usage info is available (e.g. token counts), retrieve them
        if (chatResponse.getMetadata() != null && chatResponse.getMetadata().getUsage() != null) {
            responseData.put("promptTokens",     chatResponse.getMetadata().getUsage().getPromptTokens());
            responseData.put("completionTokens", chatResponse.getMetadata().getUsage().getCompletionTokens());
            responseData.put("totalTokens",      chatResponse.getMetadata().getUsage().getTotalTokens());
        }

        return responseData;
    }

    /**
     * Example showing how to use streaming with the fluent API
     */
    @PostMapping(value = "/stream", produces = "text/event-stream")
    public Flux<String> streamCompletion(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // Use streaming if your chat model supports it
        return this.chatClient
                .prompt()
                .user(userMessage)
                .stream()
                .content();
    }

    /**
     * Example showing how to use entity mapping for structured outputs
     */
    @PostMapping("/structured")
    public WeatherReport structuredOutput(@RequestBody Map<String, String> request) {
        String location = request.get("location");

        return this.chatClient
                .prompt()
                .system("""
                You are a weather information service API.
                Always respond with a valid JSON object that matches the following structure:
                {
                  "location": "city name",
                  "temperature": number,
                  "unit": "C" or "F",
                  "conditions": "text description",
                  "forecast": ["day1", "day2", "day3"]
                }
                """)
                .user("What's the weather in " + location + "?")
                .call()
                .entity(WeatherReport.class);
    }

    /**
     * Record for structured output mapping
     */
    public record WeatherReport(
            String location,
            double temperature,
            String unit,
            String conditions,
            List<String> forecast
    ) {}
}
