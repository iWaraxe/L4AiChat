package com.coherentsolutions.l4aichat.s2components;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This controller demonstrates different message types and multimodal capabilities
 */
@RestController
@RequestMapping("/api/s2/messages")
public class MessageTypesDemoController {

    private final ChatClient chatClient;

    public MessageTypesDemoController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Example showing different message types with specific roles
     */
    @PostMapping("/types")
    public Map<String, Object> messageTypes() {
        Map<String, Object> result = new HashMap<>();

        // Examples of different message types
        result.put("userMessage", new UserMessage("Hello AI"));
        result.put("userMessageTypes", MessageType.values());

        return result;
    }

    /**
     * Example showing how to build a UserMessage with parameters
     */
    @PostMapping("/user-message")
    public Map<String, Object> userMessageExample(@RequestBody Map<String, Object> request) {
        String text = (String) request.get("text");
        Map<String, Object> params = request.containsKey("params") ?
                (Map<String, Object>) request.get("params") :
                Map.of();

        // Create a UserMessage with parameters
        UserMessage userMessage = new UserMessage(text);
        userMessage.getMetadata().putAll(params);

        String response = this.chatClient.prompt()
                .messages(userMessage)
                .call()
                .content();

        return Map.of(
                "userMessage", userMessage,
                "response", response
        );
    }

    /**
     * Example showing multi-turn conversation with different message types
     */
    @PostMapping("/conversation")
    public Map<String, Object> conversationExample(@RequestBody Map<String, Object> request) {
        String topic = (String) request.getOrDefault("topic", "Spring Framework");

        String response = this.chatClient.prompt()
                .system("You are a knowledgeable teacher.")
                .user("Tell me about " + topic)
                .call()
                .content();

        String followUpResponse = this.chatClient.prompt()
                .system("You are a knowledgeable teacher.")
                .user("Tell me about " + topic)
                .messages(new AssistantMessage(response))
                .user("Give me 3 practical examples of using it.")
                .call()
                .content();

        return Map.of(
                "initialQuestion", "Tell me about " + topic,
                "initialResponse", response,
                "followUpQuestion", "Give me 3 practical examples of using it.",
                "followUpResponse", followUpResponse
        );
    }

    /**
     * Example showing how to handle file uploads for multimodal inputs
     * Note: This requires a model that supports multimodal inputs like GPT-4o
     */
    @PostMapping(value = "/multimodal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> multimodalExample(
            @RequestParam("file") MultipartFile file,
            @RequestParam("question") String question) throws IOException {

        // Create a Media object from the uploaded file
        Media media = new Media(
                MimeType.valueOf(file.getContentType()),
                new ByteArrayResource(file.getBytes())
        );

        // Create a UserMessage with both text and media
        UserMessage userMessage = new UserMessage(question, List.of(media));

        String response = this.chatClient.prompt()
                .messages(userMessage)
                .call()
                .content();

        return Map.of(
                "question", question,
                "fileType", file.getContentType(),
                "fileSize", String.valueOf(file.getSize()),
                "response", response
        );
    }
}
