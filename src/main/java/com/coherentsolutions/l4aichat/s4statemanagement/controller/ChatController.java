package com.coherentsolutions.l4aichat.s4statemanagement.controller;

import com.coherentsolutions.l4aichat.s4statemanagement.model.ChatRequest;
import com.coherentsolutions.l4aichat.s4statemanagement.model.ChatResponse;
import com.coherentsolutions.l4aichat.s4statemanagement.model.ConversationHistoryResponse;
import com.coherentsolutions.l4aichat.s4statemanagement.service.ConversationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for chatbot interactions (Section 4 - State Management).
 * Provides endpoints for:
 *  - Starting new conversations
 *  - Sending/receiving messages
 *  - Retrieving conversation history
 *  - Clearing conversation history
 *  - Streaming responses
 */
@RestController("s4ChatController")
@RequestMapping("/api/s4/chat")
public class ChatController {

    private final ConversationService conversationService;

    public ChatController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * Start a new conversation with the chatbot.
     *
     * @return Response with welcome message and new conversation ID
     */
    @PostMapping("/new")
    public ResponseEntity<ChatResponse> startNewConversation() {
        String conversationId = conversationService.generateConversationId();

        // Provide all 3 record params: message, conversationId, role
        return ResponseEntity.ok(
                new ChatResponse(
                        "Hello! I'm an AI assistant. How can I help you today?",
                        conversationId,
                        "ASSISTANT"
                )
        );
    }

    /**
     * Send a message in an existing conversation.
     *
     * @param conversationId The ID of the conversation
     * @param request The chat request containing the user's message
     * @return The AI's response with the conversation ID
     */
    @PostMapping("/{conversationId}")
    public ResponseEntity<ChatResponse> chat(
            @PathVariable String conversationId,
            @RequestBody ChatRequest request
    ) {
        String responseMessage = conversationService.processMessage(
                conversationId,
                request.message() // record accessor
        );

        // Provide 3rd param as role for the AI's response
        return ResponseEntity.ok(
                new ChatResponse(
                        responseMessage,
                        conversationId,
                        "ASSISTANT"
                )
        );
    }

    /**
     * Stream a response to a message in chunks.
     * This provides a more interactive, real-time feel.
     *
     * @param conversationId The ID of the conversation
     * @param request The chat request containing the user message
     * @return A stream of text chunks forming the complete response
     */
    @PostMapping(value = "/stream/{conversationId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(
            @PathVariable String conversationId,
            @RequestBody ChatRequest request
    ) {
        return conversationService.streamResponse(
                conversationId,
                request.message()
        );
    }

    /**
     * Retrieve the conversation history.
     *
     * @param conversationId The ID of the conversation
     * @return List of messages in the conversation
     */
    @GetMapping("/{conversationId}/history")
    public ResponseEntity<ConversationHistoryResponse> getHistory(
            @PathVariable String conversationId
    ) {
        List<ChatResponse> messages = conversationService
                .getConversationHistory(conversationId)
                .stream()
                .map(message -> new ChatResponse(
                        message.getText(),
                        conversationId,
                        message.getMessageType().toString() // possibly USER/ASSISTANT/SYSTEM
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ConversationHistoryResponse(conversationId, messages)
        );
    }

    /**
     * Clear the conversation history.
     *
     * @param conversationId The ID of the conversation to clear
     * @return Empty response with HTTP 200 OK status
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> clearConversation(
            @PathVariable String conversationId
    ) {
        conversationService.clearConversation(conversationId);
        return ResponseEntity.ok().build();
    }
}