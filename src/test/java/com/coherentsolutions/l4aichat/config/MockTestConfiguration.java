package com.coherentsolutions.l4aichat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

/**
 * Test configuration that provides mock implementations for Spring AI components.
 * Simplified to basic mocks to avoid complex API interactions in tests.
 */
@TestConfiguration
public class MockTestConfiguration {
    
    @Bean
    @Primary
    public ChatModel chatModel() {
        return mock(ChatModel.class);
    }
    
    @Bean
    @Primary  
    public ChatClient chatClient() {
        return mock(ChatClient.class);
    }
    
    @Bean
    @Primary
    public ChatClient.Builder chatClientBuilder() {
        ChatClient.Builder mockBuilder = mock(ChatClient.Builder.class);
        return mockBuilder;
    }
}