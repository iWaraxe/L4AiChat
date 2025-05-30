package com.coherentsolutions.l4aichat.s9templates.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service demonstrating advanced prompt templates and dynamic prompt generation
 */
@Service
public class PromptTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(PromptTemplateService.class);
    
    private final ChatClient chatClient;
    private final Map<String, String> promptTemplates;

    public PromptTemplateService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("You are a helpful assistant that follows provided templates and instructions carefully.")
                .build();
        
        this.promptTemplates = initializeTemplates();
        logger.info("PromptTemplateService initialized with {} templates", promptTemplates.size());
    }

    /**
     * Process message using a specific template
     */
    public String processWithTemplate(String templateName, Map<String, String> variables, String userMessage) {
        String template = promptTemplates.get(templateName);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }
        
        String populatedTemplate = populateTemplate(template, variables);
        String fullPrompt = populatedTemplate + "\\n\\nUser Request: " + userMessage;
        
        logger.info("Processing with template '{}': {}", templateName, userMessage);
        
        return chatClient.prompt()
                .user(fullPrompt)
                .call()
                .content();
    }

    /**
     * Code generation template
     */
    public String generateCode(String language, String description, String style) {
        Map<String, String> variables = Map.of(
            "language", language,
            "style", style != null ? style : "clean and readable"
        );
        
        return processWithTemplate("code_generation", variables, description);
    }

    /**
     * Email template
     */
    public String generateEmail(String tone, String recipient, String purpose, String content) {
        Map<String, String> variables = Map.of(
            "tone", tone,
            "recipient", recipient,
            "purpose", purpose
        );
        
        return processWithTemplate("email_generation", variables, content);
    }

    /**
     * Technical explanation template
     */
    public String explainTechnicalConcept(String audience, String complexity, String concept) {
        Map<String, String> variables = Map.of(
            "audience", audience,
            "complexity", complexity
        );
        
        return processWithTemplate("technical_explanation", variables, concept);
    }

    /**
     * Creative writing template
     */
    public String creativeWriting(String genre, String mood, String length, String prompt) {
        Map<String, String> variables = Map.of(
            "genre", genre,
            "mood", mood,
            "length", length
        );
        
        return processWithTemplate("creative_writing", variables, prompt);
    }

    /**
     * Get available templates
     */
    public Map<String, String> getAvailableTemplates() {
        return Map.copyOf(promptTemplates);
    }

    private String populateTemplate(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            result = result.replace(placeholder, entry.getValue());
        }
        return result;
    }

    private Map<String, String> initializeTemplates() {
        Map<String, String> templates = new HashMap<>();
        
        templates.put("code_generation", """
            You are an expert {language} developer. Generate clean, efficient, and well-documented code.
            
            Requirements:
            - Use {style} coding style
            - Include appropriate comments
            - Follow best practices for {language}
            - Provide a brief explanation of the code
            
            Please generate {language} code for the following requirement:
            """);
        
        templates.put("email_generation", """
            You are a professional email assistant. Generate a {tone} email.
            
            Context:
            - Recipient: {recipient}
            - Purpose: {purpose}
            - Tone: {tone}
            
            Requirements:
            - Appropriate subject line
            - Professional structure
            - Clear and concise language
            - Proper email etiquette
            
            Generate an email based on the following content:
            """);
        
        templates.put("technical_explanation", """
            You are a technical educator explaining concepts to {audience}.
            
            Guidelines:
            - Complexity level: {complexity}
            - Target audience: {audience}
            - Use appropriate examples and analogies
            - Structure the explanation clearly
            - Include practical applications if relevant
            
            Please explain the following technical concept:
            """);
        
        templates.put("creative_writing", """
            You are a creative writer specializing in {genre} writing.
            
            Style requirements:
            - Genre: {genre}
            - Mood: {mood}
            - Target length: {length}
            - Use vivid descriptions and engaging narrative
            - Maintain consistent tone throughout
            
            Write a creative piece based on the following prompt:
            """);
        
        templates.put("business_analysis", """
            You are a business analyst providing strategic insights.
            
            Analysis framework:
            - Identify key business metrics
            - Assess opportunities and risks
            - Provide actionable recommendations
            - Use data-driven reasoning
            - Consider market context
            
            Analyze the following business scenario:
            """);
        
        templates.put("educational_content", """
            You are an educational content creator developing learning materials.
            
            Content requirements:
            - Clear learning objectives
            - Step-by-step explanations
            - Practical examples
            - Assessment questions
            - Engaging presentation
            
            Create educational content for the following topic:
            """);
        
        return templates;
    }
}