package com.coherentsolutions.l4aichat.businesslogic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for conversation ID validation and generation logic.
 * This tests business logic without Spring AI dependencies.
 */
@DisplayName("Conversation ID Validation Tests")
class ConversationIdValidationTest {

    @Nested
    @DisplayName("UUID Generation")
    class UuidGenerationTests {

        @Test
        @DisplayName("Should generate valid UUID format")
        void shouldGenerateValidUuidFormat() {
            // When
            String uuid = UUID.randomUUID().toString();

            // Then
            assertThat(uuid)
                .isNotNull()
                .isNotEmpty()
                .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("Should generate unique UUIDs")
        void shouldGenerateUniqueUuids() {
            // When
            String uuid1 = UUID.randomUUID().toString();
            String uuid2 = UUID.randomUUID().toString();
            String uuid3 = UUID.randomUUID().toString();

            // Then
            assertThat(uuid1)
                .isNotEqualTo(uuid2)
                .isNotEqualTo(uuid3);
            assertThat(uuid2).isNotEqualTo(uuid3);
        }

        @Test
        @DisplayName("Should maintain UUID format consistency")
        void shouldMaintainUuidFormatConsistency() {
            // When - Generate multiple UUIDs
            String[] uuids = new String[10];
            for (int i = 0; i < 10; i++) {
                uuids[i] = UUID.randomUUID().toString();
            }

            // Then - All should match UUID format
            for (String uuid : uuids) {
                assertThat(uuid)
                    .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
                    .hasSize(36); // UUID string length
            }
        }
    }

    @Nested
    @DisplayName("Conversation ID Validation")
    class ConversationIdValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should identify invalid conversation IDs")
        void shouldIdentifyInvalidConversationIds(String invalidId) {
            // When
            boolean isValid = isValidConversationId(invalidId);

            // Then
            assertThat(isValid).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "550e8400-e29b-41d4-a716-446655440000",
            "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
            "01234567-89ab-cdef-0123-456789abcdef"
        })
        @DisplayName("Should accept valid UUID format conversation IDs")
        void shouldAcceptValidUuidFormatConversationIds(String validId) {
            // When
            boolean isValid = isValidConversationId(validId);

            // Then
            assertThat(isValid).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "not-a-uuid",
            "550e8400-e29b-41d4-a716", // Too short
            "550e8400-e29b-41d4-a716-446655440000-extra", // Too long
            "550e8400-e29b-41d4-a716-44665544000G", // Invalid character
            "550e8400_e29b_41d4_a716_446655440000" // Wrong separator
        })
        @DisplayName("Should reject malformed conversation IDs")
        void shouldRejectMalformedConversationIds(String malformedId) {
            // When
            boolean isValid = isValidConversationId(malformedId);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should handle edge cases gracefully")
        void shouldHandleEdgeCasesGracefully() {
            // Given various edge case inputs
            String[] edgeCases = {
                null, "", "   ", "\t\n",
                "00000000-0000-0000-0000-000000000000", // All zeros (valid UUID)
                "ffffffff-ffff-ffff-ffff-ffffffffffff"  // All f's (valid UUID)
            };

            // When & Then
            assertThat(isValidConversationId(edgeCases[0])).isFalse(); // null
            assertThat(isValidConversationId(edgeCases[1])).isFalse(); // empty
            assertThat(isValidConversationId(edgeCases[2])).isFalse(); // spaces
            assertThat(isValidConversationId(edgeCases[3])).isFalse(); // whitespace
            assertThat(isValidConversationId(edgeCases[4])).isTrue();  // all zeros
            assertThat(isValidConversationId(edgeCases[5])).isTrue();  // all f's
        }
    }

    @Nested
    @DisplayName("Business Logic Helpers")
    class BusinessLogicHelpersTests {

        @Test
        @DisplayName("Should provide fallback conversation ID when needed")
        void shouldProvideFallbackConversationIdWhenNeeded() {
            // When
            String fallbackId = generateFallbackConversationId(null);
            String fallbackId2 = generateFallbackConversationId("");
            String fallbackId3 = generateFallbackConversationId("   ");

            // Then
            assertThat(fallbackId)
                .isNotNull()
                .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
            assertThat(fallbackId2)
                .isNotNull()
                .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
            assertThat(fallbackId3)
                .isNotNull()
                .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

            // All fallback IDs should be unique
            assertThat(fallbackId).isNotEqualTo(fallbackId2);
            assertThat(fallbackId2).isNotEqualTo(fallbackId3);
            assertThat(fallbackId).isNotEqualTo(fallbackId3);
        }

        @Test
        @DisplayName("Should return valid ID unchanged")
        void shouldReturnValidIdUnchanged() {
            // Given
            String validId = "550e8400-e29b-41d4-a716-446655440000";

            // When
            String result = generateFallbackConversationId(validId);

            // Then
            assertThat(result).isEqualTo(validId);
        }

        @Test
        @DisplayName("Should handle conversation ID normalization")
        void shouldHandleConversationIdNormalization() {
            // Given
            String idWithSpaces = "  550e8400-e29b-41d4-a716-446655440000  ";
            String upperCaseId = "550E8400-E29B-41D4-A716-446655440000";

            // When
            String normalized1 = normalizeConversationId(idWithSpaces);
            String normalized2 = normalizeConversationId(upperCaseId);

            // Then
            assertThat(normalized1).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
            assertThat(normalized2).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        }
    }

    // Helper methods that implement the business logic being tested
    private boolean isValidConversationId(String conversationId) {
        if (conversationId == null || conversationId.trim().isEmpty()) {
            return false;
        }
        
        // UUID regex pattern
        String uuidPattern = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
        return conversationId.trim().toLowerCase().matches(uuidPattern);
    }

    private String generateFallbackConversationId(String originalId) {
        if (isValidConversationId(originalId)) {
            return originalId;
        }
        return UUID.randomUUID().toString();
    }

    private String normalizeConversationId(String conversationId) {
        if (conversationId == null) {
            return null;
        }
        return conversationId.trim().toLowerCase();
    }
}