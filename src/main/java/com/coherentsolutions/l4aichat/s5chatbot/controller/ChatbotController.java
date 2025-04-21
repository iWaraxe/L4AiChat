package com.coherentsolutions.l4aichat.s5chatbot.controller;

import com.coherentsolutions.l4aichat.s5chatbot.dto.ChatRequest;
import com.coherentsolutions.l4aichat.s5chatbot.dto.ChatResponse;
import com.coherentsolutions.l4aichat.s5chatbot.service.ChatbotService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/api/s5/chat")
public class ChatbotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);
    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    /**
     * Start a new conversation
     *
     * @return A welcome message with a new conversation ID
     */
    @PostMapping("/new")
    public ResponseEntity<ChatResponse> startNewChat() {
        logger.info("Starting new chat session");
        String conversationId = chatbotService.createNewConversation();
        return ResponseEntity.ok(new ChatResponse(
                "Hello! I'm an AI assistant. How can I help you today?",
                conversationId));
    }

    /**
     * Send a message in an existing conversation
     *
     * @param conversationId The conversation identifier
     * @param request The chat request containing the user's message
     * @return The assistant's response
     */
    @PostMapping("/{conversationId}")
    public ResponseEntity<ChatResponse> chat(
            @PathVariable String conversationId,
            @Valid @RequestBody ChatRequest request) {

        logger.info("Received message in conversation {}: {}",
                conversationId, request.getMessage());

        String response = chatbotService.processMessage(conversationId, request.getMessage());
        return ResponseEntity.ok(new ChatResponse(response, conversationId));
    }

    /**
     * Stream a message response character by character
     *
     * @param conversationId The conversation identifier
     * @param request The chat request containing the user's message
     * @return A streaming response of characters
     */
    @PostMapping(
            value = "/stream/{conversationId}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<String> streamChat(
            @PathVariable String conversationId,
            @Valid @RequestBody ChatRequest request) {

        logger.info("Received streaming request in conversation {}: {}",
                conversationId, request.getMessage());

        // Get the full response
        String fullResponse = chatbotService.processMessage(conversationId, request.getMessage());

        // Convert to character stream with delays
        return Flux.fromArray(fullResponse.split(""))
                .delayElements(Duration.ofMillis(50));
    }

    /**
     * End a conversation and clear its history
     *
     * @param conversationId The conversation to end
     * @return Confirmation response
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<ChatResponse> endChat(@PathVariable String conversationId) {
        logger.info("Ending chat session: {}", conversationId);
        chatbotService.clearConversation(conversationId);
        return ResponseEntity.ok(new ChatResponse("Conversation ended", conversationId));
    }
}