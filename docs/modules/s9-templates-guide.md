# 📄 Module S9: Advanced Prompt Templates Guide

## Overview

This module explores sophisticated prompt templating patterns in Spring AI, demonstrating how to build reusable, maintainable, and powerful prompt templates. You'll learn to create dynamic templates that adapt to different contexts and use cases.

## Learning Objectives

By completing this module, you will:
- ✅ Master advanced prompt template patterns and composition
- ✅ Build dynamic templates that adapt to context
- ✅ Implement template inheritance and reusability patterns
- ✅ Create domain-specific template libraries
- ✅ Design templates for complex multi-step workflows

## Why Advanced Templates Matter

**Without Templates** (hardcoded, inflexible):
```java
public String analyzeCode(String code, String language) {
    return chatClient.prompt()
        .user("Review this " + language + " code and find bugs:\n" + code)
        .call()
        .content();
}
```

**With Advanced Templates** (reusable, maintainable):
```java
public String analyzeCode(String code, String language) {
    return templateService.execute("code-review", Map.of(
        "code", code,
        "language", language,
        "focus", "security,performance,bugs"
    ));
}
```

Templates provide:
1. **Consistency** - Same format every time
2. **Reusability** - Write once, use everywhere
3. **Maintainability** - Update in one place
4. **Flexibility** - Adapt to different contexts
5. **Quality** - Well-tested prompt patterns

## Module Structure

```
s9templates/
├── TemplatesApplication.java              # Main application
├── service/
│   ├── PromptTemplateService.java         # Core template engine
│   ├── TemplateComposer.java              # Template composition
│   └── TemplateLibraryService.java        # Template management
├── templates/
│   ├── BaseTemplateRenderer.java          # Base template functionality
│   ├── ConditionalTemplateRenderer.java   # Conditional logic
│   └── CompositeTemplateRenderer.java     # Template composition
├── library/
│   ├── AnalysisTemplates.java             # Analysis-focused templates
│   ├── CreativeTemplates.java             # Creative writing templates
│   ├── TechnicalTemplates.java            # Technical/code templates
│   └── BusinessTemplates.java             # Business-focused templates
├── controller/
│   └── TemplateController.java            # Template demonstration
└── config/
    └── TemplateConfiguration.java         # Template setup
```

## Core Template Patterns

### 1. Dynamic Template Engine
```java
@Service
public class PromptTemplateService {
    private final Map<String, TemplateDefinition> templates;
    private final ChatClient chatClient;
    private final TemplateVariableResolver variableResolver;
    
    public PromptTemplateService(ChatClient chatClient, List<TemplateLibrary> libraries) {
        this.chatClient = chatClient;
        this.templates = loadTemplatesFromLibraries(libraries);
        this.variableResolver = new TemplateVariableResolver();
    }
    
    public String execute(String templateName, Map<String, Object> variables) {
        TemplateDefinition template = templates.get(templateName);
        if (template == null) {
            throw new TemplateNotFoundException("Template not found: " + templateName);
        }
        
        return executeTemplate(template, variables);
    }
    
    public <T> T execute(String templateName, Map<String, Object> variables, Class<T> responseType) {
        TemplateDefinition template = templates.get(templateName);
        if (template == null) {
            throw new TemplateNotFoundException("Template not found: " + templateName);
        }
        
        return executeTemplateWithStructuredOutput(template, variables, responseType);
    }
    
    private String executeTemplate(TemplateDefinition template, Map<String, Object> variables) {
        // Resolve all variables in the template
        String resolvedSystemPrompt = variableResolver.resolve(template.getSystemPrompt(), variables);
        String resolvedUserPrompt = variableResolver.resolve(template.getUserPrompt(), variables);
        
        // Build the chat request
        var promptBuilder = chatClient.prompt();
        
        if (resolvedSystemPrompt != null && !resolvedSystemPrompt.trim().isEmpty()) {
            promptBuilder.system(resolvedSystemPrompt);
        }
        
        // Apply template options if specified
        if (template.getOptions() != null) {
            ChatOptions options = buildChatOptions(template.getOptions(), variables);
            promptBuilder.options(options);
        }
        
        return promptBuilder
            .user(resolvedUserPrompt)
            .call()
            .content();
    }
    
    private <T> T executeTemplateWithStructuredOutput(TemplateDefinition template, 
                                                     Map<String, Object> variables, 
                                                     Class<T> responseType) {
        String resolvedSystemPrompt = variableResolver.resolve(template.getSystemPrompt(), variables);
        String resolvedUserPrompt = variableResolver.resolve(template.getUserPrompt(), variables);
        
        var promptBuilder = chatClient.prompt();
        
        if (resolvedSystemPrompt != null && !resolvedSystemPrompt.trim().isEmpty()) {
            promptBuilder.system(resolvedSystemPrompt);
        }
        
        if (template.getOptions() != null) {
            ChatOptions options = buildChatOptions(template.getOptions(), variables);
            promptBuilder.options(options);
        }
        
        return promptBuilder
            .user(resolvedUserPrompt)
            .call()
            .entity(responseType);
    }
}
```

### 2. Template Definition System
```java
public class TemplateDefinition {
    private final String name;
    private final String description;
    private final String systemPrompt;
    private final String userPrompt;
    private final Map<String, Object> defaultVariables;
    private final TemplateOptions options;
    private final List<String> requiredVariables;
    private final List<TemplateValidation> validations;
    
    public static Builder builder(String name) {
        return new Builder(name);
    }
    
    public static class Builder {
        private String name;
        private String description;
        private String systemPrompt;
        private String userPrompt;
        private Map<String, Object> defaultVariables = new HashMap<>();
        private TemplateOptions options;
        private List<String> requiredVariables = new ArrayList<>();
        private List<TemplateValidation> validations = new ArrayList<>();
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }
        
        public Builder userPrompt(String userPrompt) {
            this.userPrompt = userPrompt;
            return this;
        }
        
        public Builder defaultVariable(String key, Object value) {
            this.defaultVariables.put(key, value);
            return this;
        }
        
        public Builder requiredVariable(String name) {
            this.requiredVariables.add(name);
            return this;
        }
        
        public Builder validation(TemplateValidation validation) {
            this.validations.add(validation);
            return this;
        }
        
        public Builder options(TemplateOptions options) {
            this.options = options;
            return this;
        }
        
        public TemplateDefinition build() {
            return new TemplateDefinition(this);
        }
    }
}

public class TemplateOptions {
    private final Double temperature;
    private final Integer maxTokens;
    private final String model;
    private final Map<String, Object> additionalOptions;
    
    // Builder pattern implementation...
}
```

### 3. Advanced Variable Resolution
```java
@Component
public class TemplateVariableResolver {
    private final Map<String, VariableFunction> functions;
    
    public TemplateVariableResolver() {
        this.functions = initializeFunctions();
    }
    
    public String resolve(String template, Map<String, Object> variables) {
        if (template == null) {
            return null;
        }
        
        String resolved = template;
        
        // Resolve simple variables: {variable}
        resolved = resolveSimpleVariables(resolved, variables);
        
        // Resolve conditional blocks: {if condition} ... {endif}
        resolved = resolveConditionalBlocks(resolved, variables);
        
        // Resolve loops: {for item in list} ... {endfor}
        resolved = resolveLoops(resolved, variables);
        
        // Resolve functions: {function(args)}
        resolved = resolveFunctions(resolved, variables);
        
        return resolved;
    }
    
    private String resolveSimpleVariables(String template, Map<String, Object> variables) {
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(template);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = getNestedValue(variables, variableName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    private String resolveConditionalBlocks(String template, Map<String, Object> variables) {
        Pattern pattern = Pattern.compile("\\{if\\s+([^}]+)\\}(.*?)\\{endif\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(template);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String condition = matcher.group(1);
            String content = matcher.group(2);
            
            if (evaluateCondition(condition, variables)) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(content));
            } else {
                matcher.appendReplacement(result, "");
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    private String resolveLoops(String template, Map<String, Object> variables) {
        Pattern pattern = Pattern.compile("\\{for\\s+(\\w+)\\s+in\\s+(\\w+)\\}(.*?)\\{endfor\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(template);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String itemVar = matcher.group(1);
            String listVar = matcher.group(2);
            String content = matcher.group(3);
            
            Object listValue = variables.get(listVar);
            if (listValue instanceof List<?> list) {
                StringBuilder loopResult = new StringBuilder();
                for (Object item : list) {
                    Map<String, Object> loopVariables = new HashMap<>(variables);
                    loopVariables.put(itemVar, item);
                    loopResult.append(resolve(content, loopVariables));
                }
                matcher.appendReplacement(result, Matcher.quoteReplacement(loopResult.toString()));
            } else {
                matcher.appendReplacement(result, "");
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    private boolean evaluateCondition(String condition, Map<String, Object> variables) {
        // Simple condition evaluation
        if (condition.contains("!=")) {
            String[] parts = condition.split("!=", 2);
            Object left = getNestedValue(variables, parts[0].trim());
            String right = parts[1].trim().replaceAll("^['\"]|['\"]$", "");
            return !Objects.equals(left != null ? left.toString() : null, right);
        } else if (condition.contains("==")) {
            String[] parts = condition.split("==", 2);
            Object left = getNestedValue(variables, parts[0].trim());
            String right = parts[1].trim().replaceAll("^['\"]|['\"]$", "");
            return Objects.equals(left != null ? left.toString() : null, right);
        } else {
            // Treat as boolean variable
            Object value = getNestedValue(variables, condition.trim());
            return value instanceof Boolean bool ? bool : (value != null);
        }
    }
    
    private Object getNestedValue(Map<String, Object> variables, String path) {
        String[] parts = path.split("\\.");
        Object current = variables;
        
        for (String part : parts) {
            if (current instanceof Map<?, ?> map) {
                current = map.get(part);
            } else {
                return null;
            }
        }
        
        return current;
    }
}
```

## Domain-Specific Template Libraries

### 1. Technical Analysis Templates
```java
@Component
public class TechnicalTemplates implements TemplateLibrary {
    
    @Override
    public Map<String, TemplateDefinition> getTemplates() {
        Map<String, TemplateDefinition> templates = new HashMap<>();
        
        // Code Review Template
        templates.put("code-review", TemplateDefinition.builder("code-review")
            .description("Comprehensive code review with security and performance analysis")
            .systemPrompt("""
                You are a senior software engineer and security expert.
                Provide comprehensive code reviews focusing on:
                {if focus.contains('security')}
                - Security vulnerabilities and best practices
                {endif}
                {if focus.contains('performance')}
                - Performance optimizations and bottlenecks
                {endif}
                {if focus.contains('maintainability')}
                - Code maintainability and readability
                {endif}
                {if focus.contains('testing')}
                - Test coverage and quality
                {endif}
                
                Be constructive and provide specific suggestions with examples.
                """)
            .userPrompt("""
                Review this {language} code:
                
                ```{language}
                {code}
                ```
                
                {if context}
                Additional context: {context}
                {endif}
                
                {if requirements}
                Specific requirements to check:
                {for requirement in requirements}
                - {requirement}
                {endfor}
                {endif}
                
                Please provide a detailed review covering the requested focus areas.
                """)
            .requiredVariable("code")
            .requiredVariable("language")
            .defaultVariable("focus", List.of("security", "performance", "maintainability"))
            .options(TemplateOptions.builder()
                .temperature(0.2)
                .maxTokens(2000)
                .build())
            .build());
        
        // Architecture Design Template
        templates.put("architecture-design", TemplateDefinition.builder("architecture-design")
            .description("System architecture design with best practices")
            .systemPrompt("""
                You are a senior software architect with expertise in:
                - Distributed systems design
                - Microservices architecture
                - Cloud-native applications
                - Security by design
                - Performance and scalability
                
                Design systems that are maintainable, scalable, and secure.
                Consider trade-offs and provide clear reasoning for decisions.
                """)
            .userPrompt("""
                Design a system architecture for: {description}
                
                Requirements:
                - Expected users: {expectedUsers}
                - Performance requirements: {performance}
                - Security requirements: {security}
                - Budget constraints: {budget}
                - Timeline: {timeline}
                
                {if existingTech}
                Existing technology stack:
                {for tech in existingTech}
                - {tech}
                {endfor}
                {endif}
                
                {if constraints}
                Additional constraints:
                {for constraint in constraints}
                - {constraint}
                {endfor}
                {endif}
                
                Provide a comprehensive architecture design including:
                1. High-level architecture diagram description
                2. Component breakdown
                3. Technology recommendations
                4. Security considerations
                5. Scalability strategy
                6. Deployment approach
                """)
            .requiredVariable("description")
            .requiredVariable("expectedUsers")
            .defaultVariable("performance", "Standard web application performance")
            .defaultVariable("security", "Standard security requirements")
            .build());
        
        return templates;
    }
}
```

### 2. Business Analysis Templates
```java
@Component
public class BusinessTemplates implements TemplateLibrary {
    
    @Override
    public Map<String, TemplateDefinition> getTemplates() {
        Map<String, TemplateDefinition> templates = new HashMap<>();
        
        // Market Analysis Template
        templates.put("market-analysis", TemplateDefinition.builder("market-analysis")
            .description("Comprehensive market analysis for business planning")
            .systemPrompt("""
                You are an expert business analyst and market researcher with deep knowledge of:
                - Market sizing and segmentation
                - Competitive analysis
                - Industry trends and dynamics
                - Consumer behavior analysis
                - Business model evaluation
                
                Provide data-driven insights and actionable recommendations.
                When specific data isn't available, clearly indicate assumptions.
                """)
            .userPrompt("""
                Conduct a market analysis for: {product}
                
                Industry: {industry}
                Target market: {targetMarket}
                Geographic focus: {geography}
                
                {if existingCompetitors}
                Known competitors:
                {for competitor in existingCompetitors}
                - {competitor}
                {endfor}
                {endif}
                
                {if budgetRange}
                Budget range for market entry: {budgetRange}
                {endif}
                
                Please provide analysis covering:
                1. Market size and growth potential
                2. Target customer segments
                3. Competitive landscape
                4. Market trends and opportunities
                5. Entry barriers and challenges
                6. Pricing strategy recommendations
                7. Go-to-market strategy outline
                """)
            .requiredVariable("product")
            .requiredVariable("industry")
            .defaultVariable("targetMarket", "General market")
            .defaultVariable("geography", "United States")
            .build());
        
        // Strategic Planning Template
        templates.put("strategic-plan", TemplateDefinition.builder("strategic-plan")
            .description("Strategic business planning and roadmap development")
            .systemPrompt("""
                You are a strategic business consultant with expertise in:
                - Strategic planning and roadmap development
                - SWOT analysis
                - Competitive positioning
                - Resource allocation
                - Risk management
                - Performance metrics and KPIs
                
                Create actionable strategic plans with clear timelines and success metrics.
                """)
            .userPrompt("""
                Develop a strategic plan for: {organization}
                
                Current situation:
                - Company size: {companySize}
                - Industry: {industry}
                - Current challenges: {challenges}
                - Goals: {goals}
                - Timeline: {timeline}
                
                {if strengths}
                Key strengths:
                {for strength in strengths}
                - {strength}
                {endfor}
                {endif}
                
                {if resources}
                Available resources:
                - Budget: {resources.budget}
                - Team size: {resources.team}
                - Technology: {resources.technology}
                {endif}
                
                Create a comprehensive strategic plan including:
                1. Executive summary
                2. SWOT analysis
                3. Strategic objectives
                4. Implementation roadmap
                5. Resource requirements
                6. Risk mitigation strategies
                7. Success metrics and KPIs
                8. Timeline with milestones
                """)
            .requiredVariable("organization")
            .requiredVariable("industry")
            .requiredVariable("challenges")
            .requiredVariable("goals")
            .defaultVariable("timeline", "12 months")
            .build());
        
        return templates;
    }
}
```

### 3. Creative Writing Templates
```java
@Component
public class CreativeTemplates implements TemplateLibrary {
    
    @Override
    public Map<String, TemplateDefinition> getTemplates() {
        Map<String, TemplateDefinition> templates = new HashMap<>();
        
        // Story Generation Template
        templates.put("story-generator", TemplateDefinition.builder("story-generator")
            .description("Creative story generation with customizable elements")
            .systemPrompt("""
                You are a creative writing expert specializing in {genre} stories.
                
                {if tone == 'humorous'}
                Focus on humor, wit, and lighthearted moments.
                {endif}
                {if tone == 'dramatic'}
                Emphasize emotional depth and character development.
                {endif}
                {if tone == 'suspenseful'}
                Build tension and keep readers on edge.
                {endif}
                {if tone == 'romantic'}
                Focus on relationships and emotional connections.
                {endif}
                
                Write engaging, well-structured narratives with:
                - Compelling characters
                - Clear story arc
                - Vivid descriptions
                - Authentic dialogue
                {if targetAudience == 'children'}
                - Age-appropriate language and themes
                - Educational or moral elements
                {endif}
                {if targetAudience == 'young-adult'}
                - Relatable teenage experiences
                - Coming-of-age themes
                {endif}
                """)
            .userPrompt("""
                Write a {genre} story with the following elements:
                
                Setting: {setting}
                Main character: {mainCharacter}
                {if conflict}
                Central conflict: {conflict}
                {endif}
                {if theme}
                Theme to explore: {theme}
                {endif}
                
                Story length: {length}
                
                {if includeElements}
                Must include these elements:
                {for element in includeElements}
                - {element}
                {endfor}
                {endif}
                
                {if avoidElements}
                Avoid these elements:
                {for element in avoidElements}
                - {element}
                {endfor}
                {endif}
                
                Create an engaging story that captures the reader's attention from the first sentence.
                """)
            .requiredVariable("genre")
            .requiredVariable("setting")
            .requiredVariable("mainCharacter")
            .defaultVariable("length", "short story (1000-2000 words)")
            .defaultVariable("tone", "balanced")
            .defaultVariable("targetAudience", "general")
            .options(TemplateOptions.builder()
                .temperature(0.8)
                .maxTokens(3000)
                .build())
            .build());
        
        return templates;
    }
}
```

## Template Composition and Inheritance

### 1. Template Composition System
```java
@Service
public class TemplateComposer {
    private final PromptTemplateService templateService;
    
    public String executeComposite(String... templateNames) {
        return executeComposite(Map.of(), templateNames);
    }
    
    public String executeComposite(Map<String, Object> variables, String... templateNames) {
        StringBuilder composedResult = new StringBuilder();
        
        for (String templateName : templateNames) {
            String result = templateService.execute(templateName, variables);
            composedResult.append(result).append("\n\n");
        }
        
        return composedResult.toString().trim();
    }
    
    public String executeWorkflow(WorkflowDefinition workflow, Map<String, Object> initialVariables) {
        Map<String, Object> workflowVariables = new HashMap<>(initialVariables);
        StringBuilder workflowResult = new StringBuilder();
        
        for (WorkflowStep step : workflow.getSteps()) {
            // Execute the template for this step
            String stepResult = templateService.execute(step.getTemplateName(), workflowVariables);
            
            // Store result in variables for next steps
            workflowVariables.put(step.getOutputVariable(), stepResult);
            
            // Add to overall result if not intermediate
            if (!step.isIntermediate()) {
                workflowResult.append("## ").append(step.getTitle()).append("\n\n");
                workflowResult.append(stepResult).append("\n\n");
            }
        }
        
        return workflowResult.toString();
    }
}

public class WorkflowDefinition {
    private final List<WorkflowStep> steps;
    private final String name;
    private final String description;
    
    public static class WorkflowStep {
        private final String templateName;
        private final String title;
        private final String outputVariable;
        private final boolean intermediate;
        private final Map<String, String> variableMappings;
        
        // Constructor and getters...
    }
}
```

### 2. Template Inheritance Pattern
```java
@Component
public class TemplateInheritanceProcessor {
    
    public TemplateDefinition processInheritance(TemplateDefinition child, TemplateDefinition parent) {
        return TemplateDefinition.builder(child.getName())
            .description(child.getDescription() != null ? child.getDescription() : parent.getDescription())
            .systemPrompt(combinePrompts(parent.getSystemPrompt(), child.getSystemPrompt()))
            .userPrompt(combinePrompts(parent.getUserPrompt(), child.getUserPrompt()))
            .defaultVariables(mergeVariables(parent.getDefaultVariables(), child.getDefaultVariables()))
            .requiredVariables(mergeRequiredVariables(parent.getRequiredVariables(), child.getRequiredVariables()))
            .options(mergeOptions(parent.getOptions(), child.getOptions()))
            .build();
    }
    
    private String combinePrompts(String parentPrompt, String childPrompt) {
        if (parentPrompt == null) return childPrompt;
        if (childPrompt == null) return parentPrompt;
        
        // If child prompt contains {parent}, replace with parent prompt
        if (childPrompt.contains("{parent}")) {
            return childPrompt.replace("{parent}", parentPrompt);
        }
        
        // Otherwise, concatenate
        return parentPrompt + "\n\n" + childPrompt;
    }
    
    private Map<String, Object> mergeVariables(Map<String, Object> parent, Map<String, Object> child) {
        Map<String, Object> merged = new HashMap<>(parent);
        merged.putAll(child); // Child overrides parent
        return merged;
    }
}
```

## Testing Template Systems

### 1. Template Execution Testing
```java
@SpringBootTest
class PromptTemplateServiceTest {
    
    @Autowired
    private PromptTemplateService templateService;
    
    @Test
    void testCodeReviewTemplate() {
        // Given
        Map<String, Object> variables = Map.of(
            "code", "public void test() { String s = null; s.length(); }",
            "language", "java",
            "focus", List.of("security", "bugs")
        );
        
        // When
        String result = templateService.execute("code-review", variables);
        
        // Then
        assertThat(result).isNotBlank();
        assertThat(result.toLowerCase()).contains("null");
        assertThat(result.toLowerCase()).contains("exception");
    }
    
    @Test
    void testStructuredTemplateOutput() {
        // Given
        Map<String, Object> variables = Map.of(
            "product", "AI-powered chatbot",
            "industry", "SaaS",
            "targetMarket", "Small businesses"
        );
        
        // When
        MarketAnalysis result = templateService.execute("market-analysis", variables, MarketAnalysis.class);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.marketSize()).isNotBlank();
        assertThat(result.competitors()).isNotEmpty();
    }
}
```

### 2. Template Validation Testing
```java
@Test
void testTemplateValidation() {
    // Test missing required variables
    assertThrows(TemplateValidationException.class, () -> {
        templateService.execute("code-review", Map.of("language", "java"));
    });
    
    // Test variable type validation
    assertThrows(TemplateValidationException.class, () -> {
        templateService.execute("code-review", Map.of(
            "code", 123, // Should be string
            "language", "java"
        ));
    });
}
```

## Key Takeaways

1. **Templates improve consistency** and maintainability of prompts
2. **Variable resolution** enables dynamic, context-aware prompts
3. **Domain-specific libraries** provide reusable patterns
4. **Template composition** supports complex workflows
5. **Inheritance patterns** reduce duplication
6. **Structured output** makes templates more powerful

## What's Next?

You've completed all module guides! Continue exploring:
- 🏗️ [Architecture Patterns](../architecture/patterns.md) - Design decisions
- 📄 [Performance Guide](../architecture/performance.md) - Optimization
- 📄 [Production Guide](../guides/production.md) - Deployment strategies

---

[← S8: Multi-Model](s8-multimodel-guide.md) | [Back to Modules](../README.md#module-guides) | [Architecture →](../architecture/patterns.md)