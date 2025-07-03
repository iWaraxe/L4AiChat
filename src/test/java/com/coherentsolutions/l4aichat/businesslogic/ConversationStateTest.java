package com.coherentsolutions.l4aichat.businesslogic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for conversation state management logic.
 * This tests business logic without Spring AI dependencies.
 */
@DisplayName("Conversation State Management Tests")
class ConversationStateTest {

    @Nested
    @DisplayName("Conversation Session Management")
    class ConversationSessionManagementTests {

        private ConversationStateManager stateManager;

        @BeforeEach
        void setUp() {
            stateManager = new ConversationStateManager();
        }

        @Test
        @DisplayName("Should create new conversation session")
        void shouldCreateNewConversationSession() {
            // When
            String conversationId = stateManager.createConversation();

            // Then
            assertThat(conversationId)
                .isNotNull()
                .isNotEmpty()
                .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
            
            assertThat(stateManager.conversationExists(conversationId)).isTrue();
        }

        @Test
        @DisplayName("Should track conversation last interaction time")
        void shouldTrackConversationLastInteractionTime() {
            // Given
            String conversationId = stateManager.createConversation();
            LocalDateTime beforeInteraction = LocalDateTime.now();

            // When
            stateManager.updateLastInteraction(conversationId);
            LocalDateTime afterInteraction = LocalDateTime.now();

            // Then
            LocalDateTime lastInteraction = stateManager.getLastInteractionTime(conversationId);
            assertThat(lastInteraction)
                .isNotNull()
                .isBetween(beforeInteraction, afterInteraction);
        }

        @Test
        @DisplayName("Should handle multiple concurrent conversations")
        void shouldHandleMultipleConcurrentConversations() {
            // When
            String conv1 = stateManager.createConversation();
            String conv2 = stateManager.createConversation();
            String conv3 = stateManager.createConversation();

            // Then
            assertThat(conv1).isNotEqualTo(conv2);
            assertThat(conv2).isNotEqualTo(conv3);
            assertThat(conv1).isNotEqualTo(conv3);
            
            assertThat(stateManager.conversationExists(conv1)).isTrue();
            assertThat(stateManager.conversationExists(conv2)).isTrue();
            assertThat(stateManager.conversationExists(conv3)).isTrue();
            
            assertThat(stateManager.getActiveConversationCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should remove conversation when cleared")
        void shouldRemoveConversationWhenCleared() {
            // Given
            String conversationId = stateManager.createConversation();
            assertThat(stateManager.conversationExists(conversationId)).isTrue();

            // When
            stateManager.clearConversation(conversationId);

            // Then
            assertThat(stateManager.conversationExists(conversationId)).isFalse();
            assertThat(stateManager.getActiveConversationCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Conversation Cleanup and Maintenance")
    class ConversationCleanupTests {

        private ConversationStateManager stateManager;

        @BeforeEach
        void setUp() {
            stateManager = new ConversationStateManager();
        }

        @Test
        @DisplayName("Should identify inactive conversations")
        void shouldIdentifyInactiveConversations() {
            // Given
            String activeConv = stateManager.createConversation();
            String inactiveConv = stateManager.createConversation();
            
            stateManager.updateLastInteraction(activeConv);
            // Make inactive conversation old
            stateManager.setLastInteractionTime(inactiveConv, 
                LocalDateTime.now().minus(2, ChronoUnit.HOURS));

            // When
            List<String> inactiveConversations = stateManager.getInactiveConversations(
                Duration.ofMinutes(30));

            // Then
            assertThat(inactiveConversations)
                .contains(inactiveConv)
                .doesNotContain(activeConv);
        }

        @Test
        @DisplayName("Should cleanup inactive conversations")
        void shouldCleanupInactiveConversations() {
            // Given
            String activeConv = stateManager.createConversation();
            String inactiveConv1 = stateManager.createConversation();
            String inactiveConv2 = stateManager.createConversation();
            
            // Make conversations inactive
            LocalDateTime twoHoursAgo = LocalDateTime.now().minus(2, ChronoUnit.HOURS);
            stateManager.setLastInteractionTime(inactiveConv1, twoHoursAgo);
            stateManager.setLastInteractionTime(inactiveConv2, twoHoursAgo);
            stateManager.updateLastInteraction(activeConv); // Keep this active

            // When
            int cleanedCount = stateManager.cleanupInactiveConversations(
                Duration.ofMinutes(30));

            // Then
            assertThat(cleanedCount).isEqualTo(2);
            assertThat(stateManager.conversationExists(activeConv)).isTrue();
            assertThat(stateManager.conversationExists(inactiveConv1)).isFalse();
            assertThat(stateManager.conversationExists(inactiveConv2)).isFalse();
            assertThat(stateManager.getActiveConversationCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle cleanup with no inactive conversations")
        void shouldHandleCleanupWithNoInactiveConversations() {
            // Given
            String conv1 = stateManager.createConversation();
            String conv2 = stateManager.createConversation();
            
            stateManager.updateLastInteraction(conv1);
            stateManager.updateLastInteraction(conv2);

            // When
            int cleanedCount = stateManager.cleanupInactiveConversations(
                Duration.ofMinutes(30));

            // Then
            assertThat(cleanedCount).isEqualTo(0);
            assertThat(stateManager.getActiveConversationCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Conversation Statistics")
    class ConversationStatisticsTests {

        private ConversationStateManager stateManager;

        @BeforeEach
        void setUp() {
            stateManager = new ConversationStateManager();
        }

        @Test
        @DisplayName("Should track conversation statistics")
        void shouldTrackConversationStatistics() {
            // Given
            String conv1 = stateManager.createConversation();
            String conv2 = stateManager.createConversation();
            String conv3 = stateManager.createConversation();

            // When
            ConversationStats stats = stateManager.getStatistics();

            // Then
            assertThat(stats.getTotalConversations()).isEqualTo(3);
            assertThat(stats.getActiveConversations()).isEqualTo(3);
            assertThat(stats.getAverageConversationAge()).isNotNull();
        }

        @Test
        @DisplayName("Should calculate conversation age correctly")
        void shouldCalculateConversationAgeCorrectly() {
            // Given
            String conversationId = stateManager.createConversation();
            LocalDateTime oneHourAgo = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
            stateManager.setLastInteractionTime(conversationId, oneHourAgo);

            // When
            Duration age = stateManager.getConversationAge(conversationId);

            // Then
            assertThat(age).isGreaterThan(Duration.ofMinutes(59));
            assertThat(age).isLessThan(Duration.ofMinutes(61));
        }
    }

    // Helper classes for testing business logic

    private static class ConversationStateManager {
        private final Map<String, LocalDateTime> conversations = new ConcurrentHashMap<>();

        public String createConversation() {
            String id = UUID.randomUUID().toString();
            conversations.put(id, LocalDateTime.now());
            return id;
        }

        public boolean conversationExists(String conversationId) {
            return conversations.containsKey(conversationId);
        }

        public void updateLastInteraction(String conversationId) {
            if (conversations.containsKey(conversationId)) {
                conversations.put(conversationId, LocalDateTime.now());
            }
        }

        public LocalDateTime getLastInteractionTime(String conversationId) {
            return conversations.get(conversationId);
        }

        public void setLastInteractionTime(String conversationId, LocalDateTime time) {
            if (conversations.containsKey(conversationId)) {
                conversations.put(conversationId, time);
            }
        }

        public int getActiveConversationCount() {
            return conversations.size();
        }

        public void clearConversation(String conversationId) {
            conversations.remove(conversationId);
        }

        public List<String> getInactiveConversations(Duration inactivityThreshold) {
            LocalDateTime cutoff = LocalDateTime.now().minus(inactivityThreshold.toMinutes(), ChronoUnit.MINUTES);
            return conversations.entrySet().stream()
                .filter(entry -> entry.getValue().isBefore(cutoff))
                .map(Map.Entry::getKey)
                .toList();
        }

        public int cleanupInactiveConversations(Duration inactivityThreshold) {
            List<String> inactive = getInactiveConversations(inactivityThreshold);
            inactive.forEach(this::clearConversation);
            return inactive.size();
        }

        public ConversationStats getStatistics() {
            return new ConversationStats(
                conversations.size(),
                conversations.size(),
                calculateAverageAge()
            );
        }

        public Duration getConversationAge(String conversationId) {
            LocalDateTime lastInteraction = conversations.get(conversationId);
            if (lastInteraction == null) {
                return Duration.ZERO;
            }
            return Duration.between(lastInteraction, LocalDateTime.now());
        }

        private Duration calculateAverageAge() {
            if (conversations.isEmpty()) {
                return Duration.ZERO;
            }
            
            LocalDateTime now = LocalDateTime.now();
            long totalMinutes = conversations.values().stream()
                .mapToLong(time -> ChronoUnit.MINUTES.between(time, now))
                .sum();
            
            return Duration.ofMinutes(totalMinutes / conversations.size());
        }
    }

    private static class ConversationStats {
        private final int totalConversations;
        private final int activeConversations;
        private final Duration averageConversationAge;

        public ConversationStats(int totalConversations, int activeConversations, Duration averageConversationAge) {
            this.totalConversations = totalConversations;
            this.activeConversations = activeConversations;
            this.averageConversationAge = averageConversationAge;
        }

        public int getTotalConversations() { return totalConversations; }
        public int getActiveConversations() { return activeConversations; }
        public Duration getAverageConversationAge() { return averageConversationAge; }
    }

}