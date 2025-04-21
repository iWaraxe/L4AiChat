package com.coherentsolutions.l4aichat.s5chatbot.exception;

public class ChatbotException extends RuntimeException {

    public ChatbotException(String message) {
        super(message);
    }

    public ChatbotException(String message, Throwable cause) {
        super(message, cause);
    }
}
