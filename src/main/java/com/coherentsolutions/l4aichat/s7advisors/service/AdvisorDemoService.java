package com.coherentsolutions.l4aichat.s7advisors.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service demonstrating different advisor patterns and configurations
 * in Spring AI 1.0.0
 */
@Service
public class AdvisorDemoService {

    private static final Logger logger = LoggerFactory.getLogger(AdvisorDemoService.class);
    private static final String CONVERSATION_ID_PARAM = ChatMemory.CONVERSATION_ID;

    private final ChatClient basicChatClient;
    private final ChatClient memoryEnabledClient;
    private final ChatClient contextAwareClient;
    private final ChatMemory chatMemory;

    public AdvisorDemoService(ChatClient.Builder chatClientBuilder) {
        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10)
                .build();

        // Basic ChatClient without advisors
        this.basicChatClient = chatClientBuilder
                .defaultSystem("You are a helpful assistant.")
                .build();

        // ChatClient with MessageChatMemoryAdvisor
        this.memoryEnabledClient = chatClientBuilder
                .defaultSystem("You are a helpful assistant with memory of our conversation.")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(this.chatMemory).build())
                .build();

        // ChatClient with context-aware system message
        this.contextAwareClient = chatClientBuilder
                .defaultSystem("""
                    You are a helpful assistant that provides contextual responses.
                    Consider the user's location and preferences when responding.
                    Be concise but informative.
                    """)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(this.chatMemory).build())
                .build();

        logger.info("AdvisorDemoService initialized with multiple ChatClient configurations");
    }

    /**
     * Basic chat without any advisors
     */
    public String basicChat(String userMessage) {
        logger.info("Processing basic chat: {}", userMessage);
        
        return this.basicChatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }

    /**
     * Chat with memory advisor enabled
     */
    public String memoryEnabledChat(String conversationId, String userMessage) {
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }
        
        logger.info("Processing memory-enabled chat for conversation {}: {}", 
                   conversationId, userMessage);
        
        final String finalConvId = conversationId;
        
        return this.memoryEnabledClient.prompt()
                .user(userMessage)
                .advisors(a -> a.param(CONVERSATION_ID_PARAM, finalConvId))
                .call()
                .content();
    }

    /**
     * Context-aware chat with enhanced system prompt and memory
     */
    public String contextAwareChat(String conversationId, String userMessage, 
                                 String userLocation, String userPreferences) {
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }
        
        logger.info("Processing context-aware chat for conversation {}: {}", 
                   conversationId, userMessage);
        
        final String finalConvId = conversationId;
        
        // Build enhanced user message with context
        StringBuilder enhancedMessage = new StringBuilder();
        
        if (userLocation != null && !userLocation.isEmpty()) {
            enhancedMessage.append("User location: ").append(userLocation).append("\\n");
        }
        
        if (userPreferences != null && !userPreferences.isEmpty()) {
            enhancedMessage.append("User preferences: ").append(userPreferences).append("\\n");
        }
        
        enhancedMessage.append("User message: ").append(userMessage);
        
        return this.contextAwareClient.prompt()
                .user(enhancedMessage.toString())
                .advisors(a -> a.param(CONVERSATION_ID_PARAM, finalConvId))
                .call()
                .content();
    }

    /**
     * Chat with custom options and memory
     */
    public String customOptionsChat(String conversationId, String userMessage, 
                                  double temperature, int maxTokens) {
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }
        
        logger.info("Processing custom options chat for conversation {}: {} " +
                   "(temperature: {}, maxTokens: {})", 
                   conversationId, userMessage, temperature, maxTokens);
        
        final String finalConvId = conversationId;
        
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
        
        return this.memoryEnabledClient.prompt()
                .user(userMessage)
                .options(options)
                .advisors(a -> a.param(CONVERSATION_ID_PARAM, finalConvId))
                .call()
                .content();
    }

    /**
     * Get conversation history for debugging
     */
    public List<Message> getConversationHistory(String conversationId) {
        logger.info("Retrieving conversation history for: {}", conversationId);
        return this.chatMemory.get(conversationId);
    }

    /**
     * Clear conversation memory
     */
    public void clearConversation(String conversationId) {
        logger.info("Clearing conversation: {}", conversationId);
        this.chatMemory.clear(conversationId);
    }

    /**
     * Create a new conversation ID
     */
    public String createNewConversation() {
        String conversationId = UUID.randomUUID().toString();
        logger.info("Created new conversation: {}", conversationId);
        return conversationId;
    }

    /**
     * Get conversation statistics
     */
    public int getConversationMessageCount(String conversationId) {
        return this.chatMemory.get(conversationId).size();
    }
}