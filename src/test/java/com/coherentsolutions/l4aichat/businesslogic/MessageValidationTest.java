package com.coherentsolutions.l4aichat.businesslogic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for message validation and processing logic.
 * This tests business logic without Spring AI dependencies.
 */
@DisplayName("Message Validation Tests")
class MessageValidationTest {

    @Nested
    @DisplayName("Message Content Validation")
    class MessageContentValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "  \t\n  "})
        @DisplayName("Should identify invalid messages")
        void shouldIdentifyInvalidMessages(String invalidMessage) {
            // When
            boolean isValid = isValidMessage(invalidMessage);

            // Then
            assertThat(isValid).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Hello",
            "How are you?",
            "Tell me about Spring AI",
            "What is the weather like today?",
            "Can you help me with my code?",
            "A", // Single character
            "   Hello   " // Message with whitespace (should be valid after trimming)
        })
        @DisplayName("Should accept valid messages")
        void shouldAcceptValidMessages(String validMessage) {
            // When
            boolean isValid = isValidMessage(validMessage);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should handle very long messages")
        void shouldHandleVeryLongMessages() {
            // Given
            String veryLongMessage = "x".repeat(10000);
            String extremelyLongMessage = "x".repeat(100000);

            // When
            boolean isValidLong = isValidMessage(veryLongMessage);
            boolean isValidExtreme = isValidMessage(extremelyLongMessage);

            // Then
            assertThat(isValidLong).isTrue();
            // Depending on business rules, extremely long messages might be invalid
            assertThat(isValidExtreme).isTrue(); // Adjust based on actual requirements
        }

        @Test
        @DisplayName("Should handle special characters and unicode")
        void shouldHandleSpecialCharactersAndUnicode() {
            // Given
            String[] specialMessages = {
                "Hello! 🌟 How are you?",
                "Testing with émojis and àccénts",
                "Chinese characters: 你好世界",
                "Japanese: こんにちは",
                "Mathematical symbols: ∑∏∫∆",
                "Special chars: !@#$%^&*()_+-=[]{}|;:,.<>?"
            };

            // When & Then
            for (String message : specialMessages) {
                assertThat(isValidMessage(message))
                    .withFailMessage("Message should be valid: " + message)
                    .isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Message Sanitization")
    class MessageSanitizationTests {

        @Test
        @DisplayName("Should trim whitespace from messages")
        void shouldTrimWhitespaceFromMessages() {
            // Given
            String messageWithWhitespace = "   Hello, world!   ";

            // When
            String sanitized = sanitizeMessage(messageWithWhitespace);

            // Then
            assertThat(sanitized).isEqualTo("Hello, world!");
        }

        @Test
        @DisplayName("Should normalize line endings")
        void shouldNormalizeLineEndings() {
            // Given
            String messageWithLineEndings = "Line 1\r\nLine 2\rLine 3\nLine 4";

            // When
            String sanitized = sanitizeMessage(messageWithLineEndings);

            // Then
            assertThat(sanitized).isEqualTo("Line 1\nLine 2\nLine 3\nLine 4");
        }

        @Test
        @DisplayName("Should handle null and empty input gracefully")
        void shouldHandleNullAndEmptyInputGracefully() {
            // When
            String sanitizedNull = sanitizeMessage(null);
            String sanitizedEmpty = sanitizeMessage("");
            String sanitizedWhitespace = sanitizeMessage("   ");

            // Then
            assertThat(sanitizedNull).isNull();
            assertThat(sanitizedEmpty).isEmpty();
            assertThat(sanitizedWhitespace).isEmpty();
        }

        @Test
        @DisplayName("Should preserve meaningful content")
        void shouldPreserveMeaningfulContent() {
            // Given
            String meaningfulMessage = "This is a test message with numbers 123 and symbols @#$";

            // When
            String sanitized = sanitizeMessage(meaningfulMessage);

            // Then
            assertThat(sanitized).isEqualTo(meaningfulMessage);
        }
    }

    @Nested
    @DisplayName("Message Length Validation")
    class MessageLengthValidationTests {

        @Test
        @DisplayName("Should validate minimum message length")
        void shouldValidateMinimumMessageLength() {
            // Given
            int minLength = 1;

            // When & Then
            assertThat(isValidMessageLength("", minLength, 1000)).isFalse();
            assertThat(isValidMessageLength("A", minLength, 1000)).isTrue();
            assertThat(isValidMessageLength("Hello", minLength, 1000)).isTrue();
        }

        @Test
        @DisplayName("Should validate maximum message length")
        void shouldValidateMaximumMessageLength() {
            // Given
            int maxLength = 10;
            String shortMessage = "Hello";
            String exactMessage = "1234567890";
            String longMessage = "This message is too long";

            // When & Then
            assertThat(isValidMessageLength(shortMessage, 1, maxLength)).isTrue();
            assertThat(isValidMessageLength(exactMessage, 1, maxLength)).isTrue();
            assertThat(isValidMessageLength(longMessage, 1, maxLength)).isFalse();
        }

        @Test
        @DisplayName("Should handle edge cases in length validation")
        void shouldHandleEdgeCasesInLengthValidation() {
            // When & Then
            assertThat(isValidMessageLength(null, 1, 100)).isFalse();
            assertThat(isValidMessageLength("   ", 1, 100)).isFalse(); // Whitespace only
            assertThat(isValidMessageLength("A", 0, 100)).isTrue(); // Min length 0
            assertThat(isValidMessageLength("", 0, 100)).isTrue(); // Empty with min length 0
        }
    }

    @Nested
    @DisplayName("Message Type Classification")
    class MessageTypeClassificationTests {

        @Test
        @DisplayName("Should classify question messages")
        void shouldClassifyQuestionMessages() {
            // Given
            String[] questions = {
                "How are you?",
                "What is the weather like?",
                "Can you help me?",
                "Why is the sky blue?",
                "Where is the nearest store?"
            };

            // When & Then
            for (String question : questions) {
                assertThat(isQuestionMessage(question))
                    .withFailMessage("Should be classified as question: " + question)
                    .isTrue();
            }
        }

        @Test
        @DisplayName("Should classify command messages")
        void shouldClassifyCommandMessages() {
            // Given
            String[] commands = {
                "Tell me about Spring AI",
                "Explain quantum physics",
                "Show me the code",
                "Generate a summary",
                "Create a report"
            };

            // When & Then
            for (String command : commands) {
                assertThat(isCommandMessage(command))
                    .withFailMessage("Should be classified as command: " + command)
                    .isTrue();
            }
        }

        @Test
        @DisplayName("Should classify greeting messages")
        void shouldClassifyGreetingMessages() {
            // Given
            String[] greetings = {
                "Hello",
                "Hi there",
                "Good morning",
                "Hey!",
                "Greetings"
            };

            // When & Then
            for (String greeting : greetings) {
                assertThat(isGreetingMessage(greeting))
                    .withFailMessage("Should be classified as greeting: " + greeting)
                    .isTrue();
            }
        }
    }

    // Helper methods that implement the business logic being tested

    private boolean isValidMessage(String message) {
        if (message == null) {
            return false;
        }
        String trimmed = message.trim();
        return !trimmed.isEmpty();
    }

    private String sanitizeMessage(String message) {
        if (message == null) {
            return null;
        }
        
        // Trim whitespace
        String sanitized = message.trim();
        
        // Normalize line endings
        sanitized = sanitized.replaceAll("\\r\\n|\\r", "\n");
        
        return sanitized;
    }

    private boolean isValidMessageLength(String message, int minLength, int maxLength) {
        if (message == null) {
            return false;
        }
        
        String trimmed = message.trim();
        if (trimmed.isEmpty() && minLength > 0) {
            return false;
        }
        
        int length = trimmed.length();
        return length >= minLength && length <= maxLength;
    }

    private boolean isQuestionMessage(String message) {
        if (message == null) {
            return false;
        }
        String trimmed = message.trim().toLowerCase();
        return trimmed.endsWith("?") || 
               trimmed.startsWith("how ") ||
               trimmed.startsWith("what ") ||
               trimmed.startsWith("why ") ||
               trimmed.startsWith("where ") ||
               trimmed.startsWith("when ") ||
               trimmed.startsWith("can ") ||
               trimmed.startsWith("could ") ||
               trimmed.startsWith("would ");
    }

    private boolean isCommandMessage(String message) {
        if (message == null) {
            return false;
        }
        String trimmed = message.trim().toLowerCase();
        return trimmed.startsWith("tell ") ||
               trimmed.startsWith("explain ") ||
               trimmed.startsWith("show ") ||
               trimmed.startsWith("generate ") ||
               trimmed.startsWith("create ") ||
               trimmed.startsWith("make ") ||
               trimmed.startsWith("build ");
    }

    private boolean isGreetingMessage(String message) {
        if (message == null) {
            return false;
        }
        String trimmed = message.trim().toLowerCase();
        return trimmed.equals("hello") ||
               trimmed.equals("hi") ||
               trimmed.startsWith("hi ") ||
               trimmed.startsWith("hello ") ||
               trimmed.startsWith("good ") ||
               trimmed.equals("hey") ||
               trimmed.equals("hey!") ||
               trimmed.startsWith("hey ") ||
               trimmed.startsWith("greetings");
    }
}