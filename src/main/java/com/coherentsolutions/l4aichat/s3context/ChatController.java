package com.coherentsolutions.l4aichat.s3context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the chatbot
 */
@RestController("s3ChatController")
@RequestMapping("/api/s3/chat")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(InMemoryChatService chatService) {
        // By default, use the InMemoryChatService
        // When profiles are activated, Spring will inject the appropriate implementation
        this.chatService = chatService;
    }

    /**
     * Start a new conversation
     *
     * @return A new conversation with a welcome message
     */
    @PostMapping("/new")
    public ResponseEntity<ChatResponse> startNewChat() {
        String conversationId = chatService.createNewConversation();
        String welcomeMessage = "Hello! I'm your " + chatService.getImplementationType() +
                " backed assistant. How can I help you today?";
        return ResponseEntity.ok(new ChatResponse(welcomeMessage, conversationId));
    }

    /**
     * Send a message to an existing conversation
     *
     * @param conversationId The ID of the conversation
     * @param request The chat request containing the user's message
     * @return The AI response
     */
    @PostMapping("/{conversationId}")
    public ResponseEntity<ChatResponse> chat(
            @PathVariable String conversationId,
            @RequestBody ChatRequest request) {

        String response = chatService.processMessage(conversationId, request.message());
        return ResponseEntity.ok(new ChatResponse(response, conversationId));
    }

    /**
     * End a conversation and clear its history
     *
     * @param conversationId The ID of the conversation
     * @return 200 OK
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> endChat(@PathVariable String conversationId) {
        chatService.clearConversation(conversationId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get information about the chat service
     *
     * @return Information about the currently active chat service
     */
    @GetMapping("/info")
    public ResponseEntity<String> getChatServiceInfo() {
        return ResponseEntity.ok("Active chat service: " + chatService.getImplementationType());
    }
}
