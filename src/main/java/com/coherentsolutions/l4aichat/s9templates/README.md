# S9 - Prompt Engineering Module

## Overview
This module demonstrates advanced prompt templating and dynamic prompt generation techniques. It showcases how to create reusable, parameterized prompts for various use cases including code generation, email composition, technical explanations, and creative writing.

## Key Features

### 1. Template-Based Prompting
- **Parameterized Templates**: Dynamic prompt generation with variable substitution
- **Template Library**: Pre-built templates for common use cases
- **Template Validation**: Ensures required variables are provided

### 2. Specialized Templates
- **Code Generation**: Language-specific code creation with style preferences
- **Email Generation**: Professional email composition with tone control
- **Technical Explanation**: Educational content with audience-appropriate complexity
- **Creative Writing**: Genre-specific creative content generation
- **Business Analysis**: Strategic insights with data-driven framework
- **Educational Content**: Structured learning materials with assessments

### 3. Dynamic Prompt Construction
- **Variable Substitution**: Replace placeholders with runtime values
- **Template Composition**: Combine multiple templates for complex scenarios
- **Context Enhancement**: Add situational context to improve responses

## API Endpoints

### Template Processing
```
POST /api/s9/templates/process
Content-Type: application/json

{
  "templateName": "code_generation",
  "variables": {
    "language": "Java",
    "style": "Spring Boot best practices"
  },
  "userMessage": "Create a REST controller for user management"
}
```

### Code Generation
```
POST /api/s9/templates/code
Content-Type: application/json

{
  "language": "Python",
  "description": "Create a function to calculate Fibonacci numbers",
  "style": "functional programming"
}
```

### Email Generation
```
POST /api/s9/templates/email
Content-Type: application/json

{
  "tone": "professional",
  "recipient": "development team",
  "purpose": "project update",
  "content": "Sprint completion and next steps"
}
```

### Technical Explanation
```
POST /api/s9/templates/explain
Content-Type: application/json

{
  "audience": "junior developers",
  "complexity": "intermediate",
  "concept": "microservices architecture"
}
```

### Creative Writing
```
POST /api/s9/templates/creative
Content-Type: application/json

{
  "genre": "science fiction",
  "mood": "mysterious",
  "length": "short story",
  "prompt": "A programmer discovers their code is alive"
}
```

### Template Management
```
# Get available templates
GET /api/s9/templates/available

# Get template details
GET /api/s9/templates/{templateName}
```

## Template Structure

### Basic Template Format
```
Template Name: {template_name}
Variables: {var1}, {var2}, {var3}
Content: Dynamic prompt with {variable} placeholders
```

### Code Generation Template
```
You are an expert {language} developer. Generate clean, efficient, and well-documented code.

Requirements:
- Use {style} coding style
- Include appropriate comments
- Follow best practices for {language}
- Provide a brief explanation of the code

Please generate {language} code for the following requirement:
```

### Email Generation Template
```
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
```

## DTOs

### TemplateRequest
```java
public record TemplateRequest(
    String templateName,
    Map<String, String> variables,
    String userMessage,
    Double temperature,      // Optional
    Integer maxTokens        // Optional
) {}
```

### TemplateResponse
```java
public record TemplateResponse(
    String response,
    String templateUsed,
    Map<String, String> variables,
    Long processingTimeMs
) {}
```

## Technical Implementation

### Template Engine
```java
private String populateTemplate(String template, Map<String, String> variables) {
    String result = template;
    for (Map.Entry<String, String> entry : variables.entrySet()) {
        String placeholder = "{" + entry.getKey() + "}";
        result = result.replace(placeholder, entry.getValue());
    }
    return result;
}
```

### Dynamic Prompt Construction
```java
public String processWithTemplate(String templateName, Map<String, String> variables, String userMessage) {
    String template = promptTemplates.get(templateName);
    String populatedTemplate = populateTemplate(template, variables);
    String fullPrompt = populatedTemplate + "\n\nUser Request: " + userMessage;
    
    return chatClient.prompt()
            .user(fullPrompt)
            .call()
            .content();
}
```

## Template Library

### Available Templates
1. **code_generation**: Language-specific code creation
2. **email_generation**: Professional email composition
3. **technical_explanation**: Educational content creation
4. **creative_writing**: Genre-specific creative content
5. **business_analysis**: Strategic business insights
6. **educational_content**: Structured learning materials

### Template Variables
Each template supports specific variables:

| Template | Variables | Description |
|----------|-----------|-------------|
| code_generation | language, style | Programming language and coding style |
| email_generation | tone, recipient, purpose | Email tone and context |
| technical_explanation | audience, complexity | Target audience and complexity level |
| creative_writing | genre, mood, length | Creative writing parameters |
| business_analysis | industry, timeframe | Business context |
| educational_content | level, format | Educational parameters |

## Best Practices

### 1. Template Design
- **Clear Instructions**: Provide specific guidance to the AI
- **Variable Naming**: Use descriptive variable names
- **Default Values**: Handle missing variables gracefully
- **Validation**: Ensure required variables are provided

### 2. Prompt Engineering
- **Context Setting**: Establish role and expertise
- **Constraint Definition**: Specify requirements and limitations
- **Output Format**: Define expected response structure
- **Examples**: Provide examples when beneficial

### 3. Variable Management
- **Type Safety**: Validate variable types and formats
- **Sanitization**: Clean input to prevent injection
- **Default Handling**: Provide sensible defaults
- **Validation**: Check for required variables

## Performance Optimization

### Template Caching
```java
private final Map<String, String> promptTemplates;

// Initialize once during service construction
this.promptTemplates = initializeTemplates();
```

### Variable Substitution
```java
// Efficient string replacement
String result = template;
for (Map.Entry<String, String> entry : variables.entrySet()) {
    String placeholder = "{" + entry.getKey() + "}";
    result = result.replace(placeholder, entry.getValue());
}
```

## Testing Strategies

### Unit Tests
```java
@Test
void testTemplatePopulation() {
    String template = "Hello {name}, welcome to {platform}";
    Map<String, String> variables = Map.of(
        "name", "John",
        "platform", "Spring AI"
    );
    
    String result = service.populateTemplate(template, variables);
    assertEquals("Hello John, welcome to Spring AI", result);
}
```

### Integration Tests
```java
@Test
void testCodeGeneration() {
    // Test complete code generation workflow
    // Verify template processing
    // Validate output quality
}
```

## Use Cases

### 1. Code Generation
- **API Development**: Generate REST controllers and services
- **Database Operations**: Create repository classes and queries
- **Testing**: Generate unit tests and integration tests
- **Documentation**: Create API documentation and comments

### 2. Content Creation
- **Technical Writing**: Generate documentation and tutorials
- **Business Communications**: Create professional emails and reports
- **Educational Materials**: Develop learning content and assessments
- **Creative Content**: Generate stories, articles, and marketing copy

### 3. Analysis and Insights
- **Code Review**: Generate code review comments and suggestions
- **Business Analysis**: Create market analysis and strategic insights
- **Technical Explanations**: Generate explanations for complex concepts
- **Problem Solving**: Create step-by-step solution guides

## Advanced Features

### Template Inheritance
```java
// Base template
String baseTemplate = "You are an expert {domain} professional...";

// Specialized template
String specializedTemplate = baseTemplate + "\nSpecific instructions for {task}...";
```

### Conditional Templates
```java
public String getTemplateForComplexity(String complexity) {
    return switch (complexity) {
        case "beginner" -> templates.get("beginner_explanation");
        case "intermediate" -> templates.get("intermediate_explanation");
        case "advanced" -> templates.get("advanced_explanation");
        default -> templates.get("general_explanation");
    };
}
```

### Template Composition
```java
public String compositeTemplate(String[] templateNames, Map<String, String> variables) {
    StringBuilder composite = new StringBuilder();
    for (String templateName : templateNames) {
        String template = promptTemplates.get(templateName);
        composite.append(populateTemplate(template, variables)).append("\n\n");
    }
    return composite.toString();
}
```

## Learning Objectives

1. **Prompt Engineering**: Master the art of crafting effective prompts
2. **Template Design**: Create reusable, parameterized prompt templates
3. **Dynamic Generation**: Build prompts dynamically based on context
4. **Use Case Specialization**: Develop templates for specific domains
5. **Performance Optimization**: Efficient template processing and caching

## Next Steps

This module prepares for:
- **Advanced RAG Systems**: Template-based query enhancement
- **Multi-Agent Systems**: Specialized prompts for different agent roles
- **Production Deployment**: Scalable template management
- **A/B Testing**: Template effectiveness comparison

## Common Patterns

### Role-Based Templates
```java
String roleTemplate = """
    You are a {role} with expertise in {domain}.
    Your task is to {task} while considering {constraints}.
    """;
```

### Structured Output Templates
```java
String structuredTemplate = """
    Provide your response in the following format:
    1. Summary: {summary}
    2. Details: {details}
    3. Recommendations: {recommendations}
    """;
```

### Context-Aware Templates
```java
String contextTemplate = """
    Given the context: {context}
    Target audience: {audience}
    Objective: {objective}
    
    Please provide a response that addresses the following:
    """;
```