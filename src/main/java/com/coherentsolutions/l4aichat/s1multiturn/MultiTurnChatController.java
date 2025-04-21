package com.coherentsolutions.l4aichat.s1multiturn;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller that demonstrates multi-turn conversations with ChatGPT.
 * In multi-turn conversations, we maintain conversation history between requests
 * so that the AI can reference previous messages in its responses.
 */
@RestController
@RequestMapping("/api/s1/chat/multi-turn")
public class MultiTurnChatController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    // Define constant for the conversation ID parameter name
    private static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "conversation_id";

    // Track conversation IDs for demonstration purposes
    private final ConcurrentHashMap<String, String> conversations = new ConcurrentHashMap<>();

    public MultiTurnChatController(ChatClient.Builder chatClientBuilder) {
        // Initialize ChatMemory to store conversation history
        this.chatMemory = new InMemoryChatMemory();

        // Build the ChatClient with the MessageChatMemoryAdvisor
        // This advisor automatically maintains conversation history
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new MessageChatMemoryAdvisor(this.chatMemory))
                .build();
    }

    /**
     * Start a new conversation and get the first response
     */
    @PostMapping("/start")
    public Map<String, String> startConversation(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String conversationId = UUID.randomUUID().toString();

        // Store a user-friendly name for this conversation
        conversations.put(conversationId, "Conversation about: " + userMessage);

        // Make the API call with the conversation ID parameter
        String response = this.chatClient.prompt()
                .system("You are a helpful AI assistant. Maintain context from our conversation and refer back to previous messages when relevant.")
                .user(userMessage)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .call()
                .content();

        return Map.of(
                "conversationId", conversationId,
                "response", response
        );
    }

    /**
     * Continue an existing conversation
     */
    @PostMapping("/continue/{conversationId}")
    public Map<String, String> continueConversation(
            @PathVariable String conversationId,
            @RequestBody Map<String, String> request) {

        String userMessage = request.get("message");

        // Make the API call with the same conversation ID to maintain context
        String response = this.chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .call()
                .content();

        return Map.of(
                "conversationId", conversationId,
                "response", response
        );
    }

    /**
     * List all active conversations
     */
    @GetMapping("/conversations")
    public Map<String, String> listConversations() {
        return conversations;
    }

    /**
     * Get the history of a specific conversation
     */
    @GetMapping("/history/{conversationId}")
    public List<Message> getConversationHistory(@PathVariable String conversationId) {
        return chatMemory.get(conversationId,-1);
    }

    /**
     * End a conversation and clear its history
     */
    @DeleteMapping("/{conversationId}")
    public Map<String, String> endConversation(@PathVariable String conversationId) {
        chatMemory.clear(conversationId);
        conversations.remove(conversationId);

        return Map.of("message", "Conversation ended and history cleared");
    }
}