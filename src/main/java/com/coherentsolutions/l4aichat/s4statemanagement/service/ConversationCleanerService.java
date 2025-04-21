package com.coherentsolutions.l4aichat.s4statemanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service responsible for managing conversation lifecycles.
 * - Cleans up inactive conversations
 * - Monitors conversation resource usage
 */
@Service
public class ConversationCleanerService {
    private static final Logger logger = LoggerFactory.getLogger(ConversationCleanerService.class);

    // Conversations inactive for 30 minutes will be removed
    private static final long CONVERSATION_TIMEOUT_MINUTES = 30;

    private final ConversationService conversationService;

    public ConversationCleanerService(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * Scheduled task that runs every 15 minutes to clean up inactive conversations.
     * This helps manage memory usage and maintain system performance.
     */
    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES)
    public void cleanupInactiveConversations() {
        logger.info("Starting scheduled cleanup of inactive conversations");

        Map<String, Long> recentConversations = conversationService.getRecentConversations(1000);
        long currentTime = System.currentTimeMillis();
        int removedCount = 0;

        for (Map.Entry<String, Long> entry : recentConversations.entrySet()) {
            String conversationId = entry.getKey();
            long lastActivityTime = entry.getValue();

            Duration inactiveDuration = Duration.between(
                    Instant.ofEpochMilli(lastActivityTime),
                    Instant.ofEpochMilli(currentTime));

            if (inactiveDuration.toMinutes() >= CONVERSATION_TIMEOUT_MINUTES) {
                logger.debug("Removing inactive conversation: {}, last active: {} minutes ago",
                        conversationId, inactiveDuration.toMinutes());

                conversationService.clearConversation(conversationId);
                removedCount++;
            }
        }

        logger.info("Completed cleanup of inactive conversations: {} removed", removedCount);
    }
}