package com.coherentsolutions.l4aichat.s3context.service;

import org.springframework.ai.chat.messages.Message;
import java.util.List;

/**
 * Interface for chat services that maintain conversation context
 */
public interface ChatService {

    /**
     * Process a message for a given conversation
     *
     * @param conversationId The ID of the conversation
     * @param userMessage The user's message
     * @return The AI response
     */
    String processMessage(String conversationId, String userMessage);

    /**
     * Get the message history for a conversation
     *
     * @param conversationId The ID of the conversation
     * @return The list of messages
     */
    List<Message> getConversationHistory(String conversationId);

    /**
     * Clear a conversation's history
     *
     * @param conversationId The ID of the conversation
     */
    void clearConversation(String conversationId);

    /**
     * Create a new conversation
     *
     * @return The ID of the new conversation
     */
    String createNewConversation();

    /**
     * Get the implementation type
     *
     * @return The implementation type name
     */
    String getImplementationType();
}