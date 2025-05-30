package com.coherentsolutions.l4aichat.s6advanced.controller;

import com.coherentsolutions.l4aichat.s6advanced.service.ChatService;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/s6/chat")
public class AdvancedChatController {

    private final ChatService chatService;

    public AdvancedChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Basic chat endpoint that processes a message with default settings
     */
    @PostMapping("/basic")
    public ResponseEntity<Map<String, String>> basicChat(
            @RequestBody Map<String, String> request,
            @RequestParam(required = false) String conversationId) {

        String userMessage = request.get("message");
        String response = chatService.processMessage(userMessage, conversationId);

        return ResponseEntity.ok(Map.of(
                "response", response,
                "conversationId", conversationId != null ? conversationId : chatService.createNewConversation()
        ));
    }

    /**
     * Chat endpoint with customizable personality through system prompt
     */
    @PostMapping("/personality/{personality}")
    public ResponseEntity<Map<String, String>> personalityChat(
            @PathVariable String personality,
            @RequestBody Map<String, String> request,
            @RequestParam(required = false) String conversationId) {

        String userMessage = request.get("message");
        String response = chatService.processMessageWithPersonality(userMessage, personality, conversationId);

        return ResponseEntity.ok(Map.of(
                "response", response,
                "conversationId", conversationId != null ? conversationId : chatService.createNewConversation()
        ));
    }

    /**
     * Chat endpoint with customizable generation parameters
     */
    @PostMapping("/custom-params")
    public ResponseEntity<Map<String, String>> customParamsChat(
            @RequestBody Map<String, String> request,
            @RequestParam(required = false) String conversationId,
            @RequestParam(defaultValue = "0.7") Double temperature,
            @RequestParam(defaultValue = "300") int maxTokens,
            @RequestParam(required = false) Double topP,
            @RequestParam(required = false) Integer topK) {

        String userMessage = request.get("message");

        // Build OpenAiChatOptions using a fresh builder
        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                .temperature(temperature)
                .maxTokens(maxTokens);
        if (topP != null) {
            optionsBuilder.topP(topP);
        }
        OpenAiChatOptions chatOptions = optionsBuilder.build();

        String response = chatService.processMessageWithOptions(userMessage, chatOptions, conversationId);

        return ResponseEntity.ok(Map.of(
                "response", response,
                "conversationId", conversationId != null ? conversationId : chatService.createNewConversation()
        ));
    }

    /**
     * Streaming chat endpoint for character-by-character responses
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(
            @RequestBody Map<String, String> request,
            @RequestParam(required = false) String conversationId) {

        String userMessage = request.get("message");
        return chatService.streamResponse(userMessage, conversationId);
    }

    /**
     * Retrieve conversation history for a given session
     */
    @GetMapping("/history/{conversationId}")
    public ResponseEntity<List<Message>> getConversationHistory(@PathVariable String conversationId) {
        List<Message> history = chatService.getConversationHistory(conversationId);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/history/{conversationId}")
    public ResponseEntity<Void> clearConversation(@PathVariable String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            chatService.manageConversationHistory(conversationId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Optionally log the error:
            // logger.error("Error clearing conversation {}: {}", conversationId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}