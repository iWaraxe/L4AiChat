package com.coherentsolutions.l4aichat.s6advanced.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {
    private static final int MAX_HISTORY_TOKENS = 4000;
    private static final String CONVERSATION_ID_PARAM = "chat_memory_conversation_id";
    private static final String MEMORY_SIZE_PARAM = "chat_memory_response_size";

    private final ChatClient defaultChatClient;
    private final ChatMemory chatMemory;
    private final ConcurrentHashMap<String, ChatClient> personalityClients;

    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatMemory = new InMemoryChatMemory();
        this.defaultChatClient = chatClientBuilder
                .defaultSystem("You are a helpful AI assistant.")
                .defaultAdvisors(new MessageChatMemoryAdvisor(this.chatMemory))
                .build();
        this.personalityClients = new ConcurrentHashMap<>();

        // Create personality-specific clients
        this.personalityClients.put("friendly", chatClientBuilder
                .defaultSystem("""
                You are a friendly and helpful assistant. Use casual language and emoticons.
                Keep responses brief and conversational.
                """)
                .defaultAdvisors(new MessageChatMemoryAdvisor(this.chatMemory))
                .build());

        this.personalityClients.put("professional", chatClientBuilder
                .defaultSystem("""
                You are a professional business assistant. Use formal and concise language.
                Provide structured responses.
                """)
                .defaultAdvisors(new MessageChatMemoryAdvisor(this.chatMemory))
                .build());

        this.personalityClients.put("technical", chatClientBuilder
                .defaultSystem("""
                You are a technical support specialist with deep software expertise.
                Provide detailed, step-by-step responses, including code examples when appropriate.
                """)
                .defaultAdvisors(new MessageChatMemoryAdvisor(this.chatMemory))
                .build());
    }

    public String createNewConversation() {
        return UUID.randomUUID().toString();
    }

    public String processMessage(String userMessage, String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            conversationId = createNewConversation();
        }
        final String finalConvId = conversationId;
        return defaultChatClient.prompt()
                .user(userMessage)
                .advisors(a -> a
                        .param(CONVERSATION_ID_PARAM, finalConvId)
                        .param(MEMORY_SIZE_PARAM, MAX_HISTORY_TOKENS))
                .call()
                .content();
    }

    public String processMessageWithPersonality(String userMessage, String personality, String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            conversationId = createNewConversation();
        }
        final String finalConvId = conversationId;
        ChatClient client = personalityClients.getOrDefault(personality, personalityClients.get("friendly"));
        return client.prompt()
                .user(userMessage)
                .advisors(a -> a
                        .param(CONVERSATION_ID_PARAM, finalConvId)
                        .param(MEMORY_SIZE_PARAM, MAX_HISTORY_TOKENS))
                .call()
                .content();
    }

    public String processMessageWithOptions(String userMessage, ChatOptions options, String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            conversationId = createNewConversation();
        }
        final String finalConvId = conversationId;
        return defaultChatClient.prompt()
                .user(userMessage)
                .advisors(a -> a
                        .param(CONVERSATION_ID_PARAM, finalConvId)
                        .param(MEMORY_SIZE_PARAM, MAX_HISTORY_TOKENS))
                .options(options)
                .call()
                .content();
    }

    public Flux<String> streamResponse(String userMessage, String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            conversationId = createNewConversation();
        }
        final String finalConvId = conversationId;
        return defaultChatClient.prompt()
                .user(userMessage)
                .advisors(a -> a
                        .param(CONVERSATION_ID_PARAM, finalConvId)
                        .param(MEMORY_SIZE_PARAM, MAX_HISTORY_TOKENS))
                .stream()
                .content();
    }

    public List<Message> getConversationHistory(String conversationId) {
        return this.chatMemory.get(conversationId, -1);
    }

    /**
     * Manage conversation history by trimming if too many messages exist.
     * In M7, we remove old history by simply clearing the conversation,
     * since manual re-addition using addMessage is no longer supported.
     */
    public void manageConversationHistory(String conversationId) {
        List<Message> messages = this.chatMemory.get(conversationId, -1);
        if (messages.size() > 15) { // If there are more than 15 messages
            this.chatMemory.clear(conversationId);
        }
    }
}