package com.coherentsolutions.l4aichat.s5chatbot.service;

import com.coherentsolutions.l4aichat.s5chatbot.exception.ChatbotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);
    // Spring AI 1.0.0 uses constants
    private static final String CONVERSATION_ID_PARAM = ChatMemory.CONVERSATION_ID;

    private static final int MAX_CONVERSATION_TOKENS = 4000;

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ConcurrentHashMap<String, Long> lastInteractionTimes;

    public ChatbotService(ChatClient.Builder chatClientBuilder) {
        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
        this.lastInteractionTimes = new ConcurrentHashMap<>();

        // Create ChatClient with memory and custom system message
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                    You are a helpful assistant that provides clear, concise answers.
                    Be friendly and conversational while keeping responses informative.
                    If you don't know something, admit it rather than making up information.
                    """)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(this.chatMemory).build())
                .build();

        logger.info("ChatbotService initialized with MessageWindowChatMemory and InMemoryChatMemoryRepository");
    }

    /**
     * Create a new conversation with a unique ID
     *
     * @return A new conversation ID as a String
     */
    public String createNewConversation() {
        String conversationId = UUID.randomUUID().toString();
        logger.info("Created new conversation with ID: {}", conversationId);
        lastInteractionTimes.put(conversationId, System.currentTimeMillis());
        return conversationId;
    }

    /**
     * Process a user message within a conversation context
     *
     * @param conversationId The conversation identifier
     * @param userMessage The user's message
     * @return The assistant's response
     */
    public String processMessage(String conversationId, String userMessage) {
        try {
            if (conversationId == null || conversationId.isEmpty()) {
                conversationId = createNewConversation();
            }

            lastInteractionTimes.put(conversationId, System.currentTimeMillis());

            // Store it in a final variable so it's effectively final in the lambda
            final String finalConvId = conversationId;

            // Use ChatClient to process message
            String response = this.chatClient.prompt()
                    .user(userMessage)
                    .advisors(a -> a.param(CONVERSATION_ID_PARAM, finalConvId))
                    .call()
                    .content();

            logger.debug("Processed message for conversation {}: '{}' -> response length: {}",
                    finalConvId, userMessage, response.length());

            return response;
        } catch (Exception e) {
            logger.error("Error processing message for conversation {}: {}",
                    conversationId, e.getMessage(), e);
            throw new ChatbotException("Failed to process your message: " + e.getMessage(), e);
        }
    }

    /**
     * Process a user message with streaming response
     *
     * @param conversationId The conversation identifier
     * @param userMessage The user's message
     * @return A Flux of response chunks
     */
    public Flux<String> streamMessage(String conversationId, String userMessage) {
        try {
            if (conversationId == null || conversationId.isEmpty()) {
                conversationId = createNewConversation();
            }

            lastInteractionTimes.put(conversationId, System.currentTimeMillis());

            // Store it in a final variable so it's effectively final in the lambda
            final String finalConvId = conversationId;

            // Use ChatClient streaming to process message
            return this.chatClient.prompt()
                    .user(userMessage)
                    .advisors(a -> a.param(CONVERSATION_ID_PARAM, finalConvId))
                    .stream()
                    .content();

        } catch (Exception e) {
            logger.error("Error streaming message for conversation {}: {}",
                    conversationId, e.getMessage(), e);
            return Flux.error(new ChatbotException("Failed to stream your message: " + e.getMessage(), e));
        }
    }

    /**
     * Get the conversation history for a specific conversation
     *
     * @param conversationId The conversation identifier
     * @return List of messages in the conversation
     */
    public List<Message> getConversationHistory(String conversationId) {
        if (conversationId == null || conversationId.isEmpty()) {
            throw new ChatbotException("Invalid conversation ID");
        }

        // In Spring AI 1.0.0, we use .get(conversationId)
        return this.chatMemory.get(conversationId);
    }

    /**
     * Clear a conversation's history
     *
     * @param conversationId The conversation to clear
     */
    public void clearConversation(String conversationId) {
        if (conversationId == null || conversationId.isEmpty()) {
            throw new ChatbotException("Invalid conversation ID");
        }

        this.chatMemory.clear(conversationId);
        lastInteractionTimes.remove(conversationId);
        logger.info("Cleared conversation with ID: {}", conversationId);
    }
}