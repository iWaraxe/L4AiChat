package com.coherentsolutions.l4aichat.s7advisors.controller;

import com.coherentsolutions.l4aichat.s7advisors.dto.ChatRequest;
import com.coherentsolutions.l4aichat.s7advisors.dto.ChatResponse;
import com.coherentsolutions.l4aichat.s7advisors.service.AdvisorDemoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/s7/advisors")
public class AdvisorDemoController {

    private static final Logger logger = LoggerFactory.getLogger(AdvisorDemoController.class);
    private final AdvisorDemoService advisorDemoService;

    public AdvisorDemoController(AdvisorDemoService advisorDemoService) {
        this.advisorDemoService = advisorDemoService;
    }

    /**
     * Basic chat without any advisors
     */
    @PostMapping("/basic")
    public ResponseEntity<ChatResponse> basicChat(@Valid @RequestBody ChatRequest request) {
        logger.info("Basic chat request: {}", request.getMessage());
        
        long startTime = System.currentTimeMillis();
        String response = advisorDemoService.basicChat(request.getMessage());
        long processingTime = System.currentTimeMillis() - startTime;
        
        ChatResponse chatResponse = new ChatResponse(response, null);
        chatResponse.setAdvisorsUsed(List.of("None"));
        chatResponse.setProcessingTimeMs(processingTime);
        
        return ResponseEntity.ok(chatResponse);
    }

    /**
     * Start a new conversation with memory
     */
    @PostMapping("/memory/new")
    public ResponseEntity<ChatResponse> startMemoryConversation() {
        String conversationId = advisorDemoService.createNewConversation();
        
        ChatResponse response = new ChatResponse(
            "Hello! I'm ready to chat and I'll remember our conversation. What would you like to talk about?",
            conversationId
        );
        response.setAdvisorsUsed(List.of("MessageChatMemoryAdvisor"));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Chat with memory advisor
     */
    @PostMapping("/memory/{conversationId}")
    public ResponseEntity<ChatResponse> memoryChat(
            @PathVariable String conversationId,
            @Valid @RequestBody ChatRequest request) {
        
        logger.info("Memory chat for conversation {}: {}", conversationId, request.getMessage());
        
        long startTime = System.currentTimeMillis();
        String response = advisorDemoService.memoryEnabledChat(
            conversationId, request.getMessage());
        long processingTime = System.currentTimeMillis() - startTime;
        
        ChatResponse chatResponse = new ChatResponse(response, conversationId);
        chatResponse.setAdvisorsUsed(List.of("MessageChatMemoryAdvisor"));
        chatResponse.setProcessingTimeMs(processingTime);
        
        return ResponseEntity.ok(chatResponse);
    }

    /**
     * Context-aware chat with enhanced prompting
     */
    @PostMapping("/context/{conversationId}")
    public ResponseEntity<ChatResponse> contextAwareChat(
            @PathVariable String conversationId,
            @Valid @RequestBody ChatRequest request) {
        
        logger.info("Context-aware chat for conversation {}: {}", 
                   conversationId, request.getMessage());
        
        long startTime = System.currentTimeMillis();
        String response = advisorDemoService.contextAwareChat(
            conversationId, 
            request.getMessage(),
            request.getUserLocation(),
            request.getUserPreferences()
        );
        long processingTime = System.currentTimeMillis() - startTime;
        
        ChatResponse chatResponse = new ChatResponse(response, conversationId);
        chatResponse.setAdvisorsUsed(List.of("MessageChatMemoryAdvisor", "ContextEnrichment"));
        chatResponse.setProcessingTimeMs(processingTime);
        
        return ResponseEntity.ok(chatResponse);
    }

    /**
     * Chat with custom OpenAI options
     */
    @PostMapping("/custom-options/{conversationId}")
    public ResponseEntity<ChatResponse> customOptionsChat(
            @PathVariable String conversationId,
            @Valid @RequestBody ChatRequest request,
            @RequestParam(defaultValue = "0.7") double temperature,
            @RequestParam(defaultValue = "150") int maxTokens) {
        
        logger.info("Custom options chat for conversation {}: {} (temp: {}, tokens: {})", 
                   conversationId, request.getMessage(), temperature, maxTokens);
        
        long startTime = System.currentTimeMillis();
        String response = advisorDemoService.customOptionsChat(
            conversationId, request.getMessage(), temperature, maxTokens);
        long processingTime = System.currentTimeMillis() - startTime;
        
        ChatResponse chatResponse = new ChatResponse(response, conversationId);
        chatResponse.setAdvisorsUsed(List.of("MessageChatMemoryAdvisor", "CustomOptions"));
        chatResponse.setProcessingTimeMs(processingTime);
        
        return ResponseEntity.ok(chatResponse);
    }

    /**
     * Get conversation history
     */
    @GetMapping("/history/{conversationId}")
    public ResponseEntity<List<Message>> getConversationHistory(@PathVariable String conversationId) {
        List<Message> history = advisorDemoService.getConversationHistory(conversationId);
        return ResponseEntity.ok(history);
    }

    /**
     * Get conversation statistics
     */
    @GetMapping("/stats/{conversationId}")
    public ResponseEntity<Object> getConversationStats(@PathVariable String conversationId) {
        int messageCount = advisorDemoService.getConversationMessageCount(conversationId);
        
        record ConversationStats(String conversationId, int messageCount, String status) {}
        
        ConversationStats stats = new ConversationStats(
            conversationId, 
            messageCount, 
            messageCount > 0 ? "active" : "empty"
        );
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Clear conversation
     */
    @DeleteMapping("/conversation/{conversationId}")
    public ResponseEntity<ChatResponse> clearConversation(@PathVariable String conversationId) {
        advisorDemoService.clearConversation(conversationId);
        
        ChatResponse response = new ChatResponse(
            "Conversation cleared successfully", conversationId);
        
        return ResponseEntity.ok(response);
    }
}