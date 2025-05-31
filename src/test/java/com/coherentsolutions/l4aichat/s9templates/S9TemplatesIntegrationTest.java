package com.coherentsolutions.l4aichat.s9templates;

import com.coherentsolutions.l4aichat.testutils.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TemplatesApplication.class)
@AutoConfigureMockMvc
class S9TemplatesIntegrationTest extends IntegrationTestBase {

    @Test
    void shouldExecuteCodeReviewTemplate() throws Exception {
        String codeReviewRequestJson = """
            {
                "templateName": "code-review",
                "variables": {
                    "code": "public void processData() { System.out.println(\\"Processing\\"); }",
                    "language": "java",
                    "focus": ["security", "performance", "maintainability"]
                }
            }
            """;

        mockMvc.perform(post("/api/s9/templates/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(codeReviewRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("Test AI response"))
                .andExpect(jsonPath("$.templateUsed").value("code-review"))
                .andExpect(jsonPath("$.variables.language").value("java"))
                .andExpect(jsonPath("$.metadata.executionTime").exists());
    }

    @Test
    void shouldExecuteArchitectureAnalysisTemplate() throws Exception {
        String archAnalysisRequestJson = """
            {
                "templateName": "architecture-analysis",
                "variables": {
                    "system": "e-commerce platform",
                    "requirements": ["scalability", "high availability", "security"],
                    "constraints": ["budget", "timeline"],
                    "stakeholders": ["developers", "operations", "business"]
                }
            }
            """;

        mockMvc.perform(post("/api/s9/templates/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(archAnalysisRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("Test AI response"))
                .andExpect(jsonPath("$.templateUsed").value("architecture-analysis"))
                .andExpect(jsonPath("$.variables.system").value("e-commerce platform"))
                .andExpect(jsonPath("$.metadata.templateVersion").exists());
    }

    @Test
    void shouldExecuteDocumentationTemplate() throws Exception {
        String docRequestJson = """
            {
                "templateName": "api-documentation",
                "variables": {
                    "apiName": "User Management API",
                    "endpoints": [
                        {"method": "GET", "path": "/users", "description": "List all users"},
                        {"method": "POST", "path": "/users", "description": "Create new user"}
                    ],
                    "authentication": "JWT",
                    "version": "v1.0"
                }
            }
            """;

        mockMvc.perform(post("/api/s9/templates/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(docRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("Test AI response"))
                .andExpect(jsonPath("$.templateUsed").value("api-documentation"))
                .andExpect(jsonPath("$.variables.apiName").value("User Management API"));
    }

    @Test
    void shouldExecuteTestGenerationTemplate() throws Exception {
        String testGenRequestJson = """
            {
                "templateName": "test-generation",
                "variables": {
                    "className": "UserService",
                    "methods": [
                        {"name": "createUser", "parameters": ["User user"], "returnType": "User"},
                        {"name": "findById", "parameters": ["Long id"], "returnType": "Optional<User>"}
                    ],
                    "testFramework": "JUnit 5",
                    "mockFramework": "Mockito"
                }
            }
            """;

        mockMvc.perform(post("/api/s9/templates/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testGenRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("Test AI response"))
                .andExpect(jsonPath("$.templateUsed").value("test-generation"))
                .andExpect(jsonPath("$.variables.className").value("UserService"));
    }

    @Test
    void shouldExecuteTechnicalWritingTemplate() throws Exception {
        String techWritingRequestJson = """
            {
                "templateName": "technical-writing",
                "variables": {
                    "topic": "Microservices Design Patterns",
                    "audience": "senior developers",
                    "length": "detailed",
                    "includeExamples": true,
                    "format": "tutorial"
                }
            }
            """;

        mockMvc.perform(post("/api/s9/templates/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(techWritingRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("Test AI response"))
                .andExpect(jsonPath("$.templateUsed").value("technical-writing"))
                .andExpect(jsonPath("$.variables.topic").value("Microservices Design Patterns"));
    }

    @Test
    void shouldExecuteEmailTemplate() throws Exception {
        String emailRequestJson = """
            {
                "templateName": "professional-email",
                "variables": {
                    "recipient": "development team",
                    "subject": "Code review process updates",
                    "content": "New guidelines for code reviews",
                    "tone": "professional",
                    "urgency": "medium"
                }
            }
            """;

        mockMvc.perform(post("/api/s9/templates/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emailRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("Test AI response"))
                .andExpect(jsonPath("$.templateUsed").value("professional-email"))
                .andExpect(jsonPath("$.variables.recipient").value("development team"));
    }

    @Test
    void shouldListAvailableTemplates() throws Exception {
        mockMvc.perform(get("/api/s9/templates/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.templates").isArray())
                .andExpect(jsonPath("$.templates[0].name").exists())
                .andExpect(jsonPath("$.templates[0].description").exists())
                .andExpect(jsonPath("$.templates[0].requiredVariables").isArray())
                .andExpect(jsonPath("$.templates[0].optionalVariables").isArray())
                .andExpect(jsonPath("$.count").exists());
    }

    @Test
    void shouldGetTemplateDetails() throws Exception {
        mockMvc.perform(get("/api/s9/templates/details/code-review"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("code-review"))
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.systemPrompt").exists())
                .andExpect(jsonPath("$.userPrompt").exists())
                .andExpect(jsonPath("$.requiredVariables").isArray())
                .andExpect(jsonPath("$.optionalVariables").isArray())
                .andExpect(jsonPath("$.examples").isArray());
    }

    @Test
    void shouldValidateTemplateRequest() throws Exception {
        // Test missing template name
        mockMvc.perform(post("/api/s9/templates/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Template name is required"));
    }

    @Test
    void shouldValidateRequiredVariables() throws Exception {
        String incompleteRequestJson = """
            {
                "templateName": "code-review",
                "variables": {
                    "language": "java"
                }
            }
            """;

        mockMvc.perform(post("/api/s9/templates/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Missing required variable: code"))
                .andExpect(jsonPath("$.missingVariables").isArray())
                .andExpect(jsonPath("$.missingVariables[0]").value("code"));
    }

    @Test
    void shouldHandleNonExistentTemplate() throws Exception {
        String nonExistentRequestJson = """
            {
                "templateName": "non-existent-template",
                "variables": {}
            }
            """;

        mockMvc.perform(post("/api/s9/templates/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nonExistentRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("TEMPLATE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Template 'non-existent-template' not found"));
    }

    @Test
    void shouldSupportTemplateCustomization() throws Exception {
        String customRequestJson = """
            {
                "templateName": "code-review",
                "variables": {
                    "code": "function hello() { console.log('Hello'); }",
                    "language": "javascript",
                    "focus": ["performance"]
                },
                "customization": {
                    "additionalInstructions": "Focus on ES6+ features",
                    "outputFormat": "markdown",
                    "includeExamples": true
                }
            }
            """;

        mockMvc.perform(post("/api/s9/templates/execute/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(customRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("Test AI response"))
                .andExpect(jsonPath("$.customization.applied").value(true))
                .andExpect(jsonPath("$.customization.additionalInstructions").value("Focus on ES6+ features"));
    }

    @Test
    void shouldSupportBatchTemplateExecution() throws Exception {
        String batchRequestJson = """
            {
                "requests": [
                    {
                        "templateName": "code-review",
                        "variables": {
                            "code": "public void test1() {}",
                            "language": "java",
                            "focus": ["security"]
                        }
                    },
                    {
                        "templateName": "technical-writing",
                        "variables": {
                            "topic": "Unit Testing",
                            "audience": "junior developers",
                            "length": "brief"
                        }
                    }
                ]
            }
            """;

        mockMvc.perform(post("/api/s9/templates/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(batchRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[0].result").value("Test AI response"))
                .andExpect(jsonPath("$.results[0].templateUsed").value("code-review"))
                .andExpect(jsonPath("$.results[1].templateUsed").value("technical-writing"))
                .andExpect(jsonPath("$.batchId").exists())
                .andExpect(jsonPath("$.totalProcessingTime").exists());
    }

    @Test
    void shouldProvideTemplateStatistics() throws Exception {
        mockMvc.perform(get("/api/s9/templates/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTemplates").exists())
                .andExpect(jsonPath("$.totalExecutions").exists())
                .andExpect(jsonPath("$.popularTemplates").isArray())
                .andExpect(jsonPath("$.popularTemplates[0].name").exists())
                .andExpect(jsonPath("$.popularTemplates[0].usageCount").exists())
                .andExpect(jsonPath("$.averageExecutionTime").exists());
    }

    @Test
    void shouldValidateTemplateVariableTypes() throws Exception {
        String invalidTypeRequestJson = """
            {
                "templateName": "code-review",
                "variables": {
                    "code": "public void test() {}",
                    "language": "java",
                    "focus": "security"
                }
            }
            """;

        mockMvc.perform(post("/api/s9/templates/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidTypeRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Variable 'focus' must be an array"));
    }

    @Test
    void shouldSupportTemplatePreview() throws Exception {
        String previewRequestJson = """
            {
                "templateName": "code-review",
                "variables": {
                    "code": "public void example() {}",
                    "language": "java",
                    "focus": ["performance"]
                }
            }
            """;

        mockMvc.perform(post("/api/s9/templates/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(previewRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.renderedSystemPrompt").exists())
                .andExpect(jsonPath("$.renderedUserPrompt").exists())
                .andExpect(jsonPath("$.variableSubstitutions").isArray())
                .andExpect(jsonPath("$.estimatedTokens").exists())
                .andExpect(jsonPath("$.templateValidation.valid").value(true));
    }
}