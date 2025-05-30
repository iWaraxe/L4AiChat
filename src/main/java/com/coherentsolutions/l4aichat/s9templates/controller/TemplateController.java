package com.coherentsolutions.l4aichat.s9templates.controller;

import com.coherentsolutions.l4aichat.s9templates.dto.TemplateRequest;
import com.coherentsolutions.l4aichat.s9templates.service.PromptTemplateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/s9/templates")
public class TemplateController {

    private final PromptTemplateService templateService;

    public TemplateController(PromptTemplateService templateService) {
        this.templateService = templateService;
    }

    /**
     * Process request using a template
     */
    @PostMapping("/process")
    public ResponseEntity<String> processWithTemplate(@Valid @RequestBody TemplateRequest request) {
        String response = templateService.processWithTemplate(
            request.getTemplateName(),
            request.getVariables() != null ? request.getVariables() : Map.of(),
            request.getMessage()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Generate code using template
     */
    @PostMapping("/code")
    public ResponseEntity<String> generateCode(
            @RequestParam String language,
            @RequestParam(defaultValue = "clean and readable") String style,
            @RequestBody String description) {
        
        String response = templateService.generateCode(language, description, style);
        return ResponseEntity.ok(response);
    }

    /**
     * Generate email using template
     */
    @PostMapping("/email")
    public ResponseEntity<String> generateEmail(
            @RequestParam String tone,
            @RequestParam String recipient,
            @RequestParam String purpose,
            @RequestBody String content) {
        
        String response = templateService.generateEmail(tone, recipient, purpose, content);
        return ResponseEntity.ok(response);
    }

    /**
     * Technical explanation using template
     */
    @PostMapping("/explain")
    public ResponseEntity<String> explainTechnical(
            @RequestParam String audience,
            @RequestParam String complexity,
            @RequestBody String concept) {
        
        String response = templateService.explainTechnicalConcept(audience, complexity, concept);
        return ResponseEntity.ok(response);
    }

    /**
     * Creative writing using template
     */
    @PostMapping("/creative")
    public ResponseEntity<String> creativeWriting(
            @RequestParam String genre,
            @RequestParam String mood,
            @RequestParam String length,
            @RequestBody String prompt) {
        
        String response = templateService.creativeWriting(genre, mood, length, prompt);
        return ResponseEntity.ok(response);
    }

    /**
     * Get available templates
     */
    @GetMapping("/available")
    public ResponseEntity<Map<String, String>> getAvailableTemplates() {
        Map<String, String> templates = templateService.getAvailableTemplates();
        return ResponseEntity.ok(templates);
    }
}