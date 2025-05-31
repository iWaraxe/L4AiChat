# 📄 Module S6: Advanced Features Guide

## Overview

This module demonstrates cutting-edge features of Spring AI, including structured output with complex data types, advanced streaming patterns, and sophisticated chat response handling. It showcases how to build AI applications that go beyond simple text generation.

## Learning Objectives

By completing this module, you will:
- ✅ Master structured output with complex nested objects
- ✅ Implement real-time streaming with proper error handling
- ✅ Design type-safe AI response patterns
- ✅ Handle complex data transformations from AI responses
- ✅ Build production-ready advanced chat features

## Why Advanced Features Matter

Moving beyond simple text responses unlocks powerful capabilities:

1. **Structured Data** - AI becomes a data transformation engine
2. **Type Safety** - Compile-time guarantees for AI responses
3. **Rich Applications** - Build complex workflows with AI
4. **Integration** - Seamlessly connect AI to existing systems
5. **User Experience** - Provide rich, interactive responses

## Module Structure

```
s6advanced/
├── ChatbotApplication.java              # Main application
├── controller/
│   ├── AdvancedChatController.java     # Streaming & advanced patterns
│   └── StructuredChatController.java   # Complex structured output
├── dto/
│   ├── ChatRequest.java               # Request DTOs
│   └── ChatResponse.java              # Response DTOs
└── service/
    └── ChatService.java               # Advanced business logic
```

## Advanced Structured Output Patterns

### 1. Complex Nested Entities

#### Business Analysis Response
```java
public record BusinessAnalysis(
    CompanyOverview company,
    MarketAnalysis market,
    FinancialMetrics financials,
    List<Recommendation> recommendations,
    RiskAssessment risks
) {
    public record CompanyOverview(
        String name,
        String industry,
        String description,
        List<String> keyProducts,
        int employeeCount,
        String headquarters
    ) {}
    
    public record MarketAnalysis(
        String marketSize,
        String growthRate,
        List<Competitor> mainCompetitors,
        List<String> marketTrends,
        CompetitivePosition position
    ) {}
    
    public record FinancialMetrics(
        Revenue revenue,
        Profitability profitability,
        Valuation valuation,
        String financialHealth
    ) {
        public record Revenue(
            String current,
            String growth,
            List<RevenueStream> streams
        ) {}
        
        public record Profitability(
            String grossMargin,
            String netMargin,
            String operatingMargin
        ) {}
    }
    
    public record Recommendation(
        String category,
        String action,
        String reasoning,
        Priority priority,
        String timeframe
    ) {}
    
    public enum Priority { HIGH, MEDIUM, LOW }
}

@PostMapping("/business-analysis")
public BusinessAnalysis analyzeCompany(@RequestBody String companyName) {
    return chatClient.prompt()
        .system("You are an expert business analyst. Provide comprehensive analysis with specific data points.")
        .user(u -> u.text("""
            Analyze the company '{company}' and provide a comprehensive business analysis.
            Include market position, financial insights, and strategic recommendations.
            Be specific with numbers and data where possible.
            """)
            .param("company", companyName))
        .call()
        .entity(BusinessAnalysis.class);
}
```

**Why This Complexity Works:**
- **Model Intelligence** - GPT-4 understands complex schemas
- **Type Safety** - Compile-time verification
- **Documentation** - Schema serves as API contract
- **Validation** - Can add constraints easily

#### Technical Architecture Design
```java
public record SystemArchitecture(
    ApplicationOverview application,
    List<Component> components,
    List<Integration> integrations,
    Infrastructure infrastructure,
    SecurityConsiderations security,
    ScalabilityPlan scalability
) {
    public record Component(
        String name,
        ComponentType type,
        String purpose,
        List<String> responsibilities,
        List<Technology> technologies,
        List<String> dependencies
    ) {}
    
    public record Integration(
        String source,
        String target,
        IntegrationType type,
        String protocol,
        DataFormat dataFormat,
        String frequency
    ) {}
    
    public record Infrastructure(
        DeploymentModel deployment,
        List<Server> servers,
        Database database,
        Monitoring monitoring
    ) {}
    
    public enum ComponentType { SERVICE, DATABASE, UI, API_GATEWAY, MESSAGE_QUEUE }
    public enum IntegrationType { SYNCHRONOUS, ASYNCHRONOUS, BATCH, REAL_TIME }
    public enum DataFormat { JSON, XML, CSV, BINARY }
}

@PostMapping("/architecture-design")
public SystemArchitecture designArchitecture(@RequestBody ArchitectureRequest request) {
    return chatClient.prompt()
        .system("You are a senior software architect. Design scalable, maintainable systems.")
        .user(u -> u.text("""
            Design a system architecture for: {description}
            
            Requirements:
            - Users: {expectedUsers}
            - Budget: {budget}
            - Timeline: {timeline}
            - Technologies: {preferredTech}
            
            Provide detailed component breakdown, integration patterns, and infrastructure recommendations.
            """)
            .param("description", request.description())
            .param("expectedUsers", request.expectedUsers())
            .param("budget", request.budget())
            .param("timeline", request.timeline())
            .param("preferredTech", String.join(", ", request.preferredTechnologies())))
        .call()
        .entity(SystemArchitecture.class);
}
```

### 2. Dynamic Schema Generation

#### Flexible Data Processing
```java
@PostMapping("/analyze-data")
public Map<String, Object> analyzeData(@RequestBody DataAnalysisRequest request) {
    // Create dynamic schema based on data type
    String schema = generateSchemaForDataType(request.dataType());
    
    return chatClient.prompt()
        .system("You are a data scientist. Analyze the provided data and return results matching the schema.")
        .user(u -> u.text("""
            Analyze this {dataType} data:
            {data}
            
            Return analysis following this schema:
            {schema}
            """)
            .param("dataType", request.dataType())
            .param("data", request.data())
            .param("schema", schema))
        .call()
        .entity(new ParameterizedTypeReference<Map<String, Object>>() {});
}

private String generateSchemaForDataType(String dataType) {
    return switch (dataType.toLowerCase()) {
        case "sales" -> """
            {
                "summary": "string",
                "trends": ["string"],
                "topProducts": [{"name": "string", "revenue": "number"}],
                "insights": ["string"],
                "recommendations": ["string"]
            }
            """;
        case "user_behavior" -> """
            {
                "patterns": ["string"],
                "demographics": {"age": "string", "location": "string"},
                "engagement": {"avgSessionTime": "number", "bounceRate": "number"},
                "segments": [{"name": "string", "characteristics": ["string"]}]
            }
            """;
        default -> "{}";
    };
}
```

**Why Dynamic Schemas?**
- **Flexibility** - Handle different data types
- **Reusability** - One endpoint for multiple use cases
- **Maintainability** - Schema changes don't require code changes
- **Extensibility** - Easy to add new data types

### 3. Advanced Streaming Patterns

#### Server-Sent Events with Rich Data
```java
@GetMapping(value = "/stream/analysis/{topic}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<AnalysisChunk>> streamAnalysis(@PathVariable String topic) {
    
    return chatClient.prompt()
        .system("Provide a comprehensive analysis. Structure your response in clear sections.")
        .user("Provide detailed analysis of: " + topic)
        .stream()
        .content()
        .map(this::parseAnalysisChunk)
        .filter(Objects::nonNull)
        .map(chunk -> ServerSentEvent.<AnalysisChunk>builder()
            .id(UUID.randomUUID().toString())
            .event("analysis-chunk")
            .data(chunk)
            .build());
}

private AnalysisChunk parseAnalysisChunk(String content) {
    // Parse streaming content into structured chunks
    if (content.contains("## Summary")) {
        return new AnalysisChunk("summary", content);
    } else if (content.contains("## Key Points")) {
        return new AnalysisChunk("key_points", content);
    } else if (content.contains("## Recommendations")) {
        return new AnalysisChunk("recommendations", content);
    }
    return new AnalysisChunk("content", content);
}

public record AnalysisChunk(String type, String content) {}
```

#### Progressive Structured Output
```java
@GetMapping(value = "/stream/project-plan", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<ProjectPlanSection>> streamProjectPlan(@RequestParam String description) {
    
    return chatClient.prompt()
        .system("""
            You are a project manager. Create a detailed project plan.
            Structure your response with clear JSON sections:
            OVERVIEW: {...}
            PHASES: [{...}]
            RESOURCES: {...}
            TIMELINE: {...}
            RISKS: [...] 
            """)
        .user("Create a project plan for: " + description)
        .stream()
        .content()
        .buffer(Duration.ofMillis(500)) // Collect chunks
        .flatMap(this::parseProjectPlanSections)
        .map(section -> ServerSentEvent.<ProjectPlanSection>builder()
            .event("project-section")
            .data(section)
            .build());
}

private Flux<ProjectPlanSection> parseProjectPlanSections(List<String> chunks) {
    String combined = String.join("", chunks);
    List<ProjectPlanSection> sections = new ArrayList<>();
    
    // Parse JSON sections from the combined text
    if (combined.contains("OVERVIEW:")) {
        extractAndParseSection(combined, "OVERVIEW", ProjectOverview.class)
            .ifPresent(overview -> sections.add(new ProjectPlanSection("overview", overview)));
    }
    
    if (combined.contains("PHASES:")) {
        extractAndParseSection(combined, "PHASES", new TypeReference<List<ProjectPhase>>() {})
            .ifPresent(phases -> sections.add(new ProjectPlanSection("phases", phases)));
    }
    
    return Flux.fromIterable(sections);
}
```

**Why Progressive Streaming?**
- **User Experience** - Show results as they're generated
- **Performance** - Process sections as they arrive
- **Feedback** - Users see progress immediately
- **Cancellation** - Can stop generation early

### 4. Error Handling and Validation

#### Robust Entity Processing
```java
@PostMapping("/safe-structured-output")
public ResponseEntity<?> safeStructuredOutput(@RequestBody StructuredRequest request) {
    try {
        // Attempt structured parsing
        Object result = chatClient.prompt()
            .user(request.prompt())
            .call()
            .entity(request.targetClass());
            
        // Validate the result
        ValidationResult validation = validateEntity(result);
        if (!validation.isValid()) {
            // Fallback: try again with explicit schema
            return retryWithExplicitSchema(request, validation.getErrors());
        }
        
        return ResponseEntity.ok(result);
        
    } catch (Exception e) {
        logger.error("Structured output failed", e);
        
        // Fallback: return as plain text
        String fallbackResponse = chatClient.prompt()
            .user(request.prompt())
            .call()
            .content();
            
        return ResponseEntity.ok(Map.of(
            "type", "fallback",
            "content", fallbackResponse,
            "error", "Structured parsing failed"
        ));
    }
}

private ResponseEntity<?> retryWithExplicitSchema(StructuredRequest request, List<String> errors) {
    String enhancedPrompt = request.prompt() + "\n\nPlease ensure your response follows this exact format:\n" +
        generateSchemaExample(request.targetClass()) + 
        "\n\nPrevious errors to fix: " + String.join(", ", errors);
        
    Object result = chatClient.prompt()
        .user(enhancedPrompt)
        .call()
        .entity(request.targetClass());
        
    return ResponseEntity.ok(result);
}
```

#### Stream Error Recovery
```java
@GetMapping(value = "/resilient-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> resilientStream(@RequestParam String query) {
    return chatClient.prompt()
        .user(query)
        .stream()
        .content()
        .onErrorResume(throwable -> {
            logger.error("Streaming error", throwable);
            // Return error message as part of stream
            return Flux.just("Error occurred: " + throwable.getMessage() + "\n\nAttempting recovery...\n")
                .concatWith(fallbackResponse(query));
        })
        .timeout(Duration.ofMinutes(5))
        .onErrorReturn("Stream timeout - please try again with a shorter query");
}

private Flux<String> fallbackResponse(String query) {
    // Simpler fallback prompt
    return chatClient.prompt()
        .system("Provide a brief response.")
        .user("Briefly answer: " + query)
        .stream()
        .content();
}
```

## Advanced Configuration Patterns

### 1. Multi-Model Strategy
```java
@Service
public class AdvancedChatService {
    private final ChatClient analyticsClient;    // Optimized for data analysis
    private final ChatClient creativeClient;     // Optimized for creative tasks
    private final ChatClient factualClient;      // Optimized for factual responses
    
    public AdvancedChatService(ChatClient.Builder builder) {
        this.analyticsClient = builder.clone()
            .defaultOptions(OpenAiChatOptions.builder()
                .withTemperature(0.1)
                .withModel("gpt-4")
                .build())
            .build();
            
        this.creativeClient = builder.clone()
            .defaultOptions(OpenAiChatOptions.builder()
                .withTemperature(0.9)
                .withModel("gpt-4")
                .build())
            .build();
            
        this.factualClient = builder.clone()
            .defaultOptions(OpenAiChatOptions.builder()
                .withTemperature(0.0)
                .withModel("gpt-3.5-turbo")
                .build())
            .build();
    }
    
    public <T> T processWithOptimalModel(String prompt, Class<T> responseType, TaskType taskType) {
        ChatClient client = switch (taskType) {
            case ANALYSIS -> analyticsClient;
            case CREATIVE -> creativeClient;
            case FACTUAL -> factualClient;
        };
        
        return client.prompt()
            .user(prompt)
            .call()
            .entity(responseType);
    }
}
```

### 2. Response Transformation Pipeline
```java
@Component
public class ResponseTransformationPipeline {
    
    public <T> T processWithTransformations(String prompt, Class<T> targetType) {
        return chatClient.prompt()
            .user(prompt)
            .call()
            .entity(targetType)
            .transform(this::validateResponse)
            .transform(this::enrichWithMetadata)
            .transform(this::applyBusinessRules);
    }
    
    private <T> T validateResponse(T response) {
        // Validate business rules
        ValidationResult result = validator.validate(response);
        if (!result.isValid()) {
            throw new ResponseValidationException(result.getErrors());
        }
        return response;
    }
    
    private <T> T enrichWithMetadata(T response) {
        // Add metadata through reflection or custom annotations
        if (response instanceof MetadataEnrichable enrichable) {
            enrichable.setGeneratedAt(LocalDateTime.now());
            enrichable.setVersion(getSchemaVersion(response.getClass()));
        }
        return response;
    }
    
    private <T> T applyBusinessRules(T response) {
        // Apply post-processing business logic
        businessRuleEngine.apply(response);
        return response;
    }
}
```

## Testing Advanced Features

### 1. Structured Output Testing
```java
@Test
void testComplexStructuredOutput() {
    // Given
    String companyName = "TechCorp Inc";
    
    // When
    BusinessAnalysis analysis = controller.analyzeCompany(companyName);
    
    // Then
    assertThat(analysis).isNotNull();
    assertThat(analysis.company().name()).isEqualTo(companyName);
    assertThat(analysis.recommendations()).isNotEmpty();
    assertThat(analysis.financials().revenue()).isNotNull();
    
    // Validate nested structure
    assertThat(analysis.market().mainCompetitors()).allSatisfy(competitor -> {
        assertThat(competitor.name()).isNotBlank();
        assertThat(competitor.marketShare()).isNotNull();
    });
}
```

### 2. Streaming Integration Testing
```java
@Test
void testStreamingWithStructuredData() {
    // Given
    String topic = "Artificial Intelligence";
    
    // When
    StepVerifier.create(controller.streamAnalysis(topic))
        .expectNextMatches(event -> 
            event.event().equals("analysis-chunk") && 
            event.data().type().equals("summary"))
        .expectNextMatches(event -> 
            event.data().type().equals("key_points"))
        .expectNextMatches(event -> 
            event.data().type().equals("recommendations"))
        .verifyComplete();
}
```

### 3. Error Recovery Testing
```java
@Test
void testStreamErrorRecovery() {
    // Mock streaming failure
    when(chatClient.prompt()).thenThrow(new RuntimeException("API Error"));
    
    StepVerifier.create(controller.resilientStream("test query"))
        .expectNext("Error occurred: API Error")
        .expectNext("Attempting recovery...")
        .expectNextMatches(content -> content.contains("Brief response"))
        .verifyComplete();
}
```

## Common Pitfalls and Solutions

### ❌ Pitfall: Over-Complex Schemas
```java
// Bad: Overly nested, unclear purpose
public record OverComplexResponse(
    Map<String, Map<String, List<Map<String, Object>>>> data
) {}
```

### ✅ Solution: Clear, Purpose-Driven Design
```java
// Good: Clear structure with business meaning
public record ProductAnalysis(
    ProductInfo product,
    MarketData market,
    List<Insight> insights
) {}
```

### ❌ Pitfall: Ignoring Streaming Errors
```java
// Bad: No error handling in streams
return chatClient.stream().content();
```

### ✅ Solution: Comprehensive Error Handling
```java
// Good: Error recovery and timeouts
return chatClient.stream()
    .content()
    .onErrorResume(this::handleStreamError)
    .timeout(Duration.ofMinutes(5));
```

## Key Takeaways

1. **Design schemas thoughtfully** - clarity over complexity
2. **Handle streaming errors gracefully** - provide fallbacks
3. **Validate structured responses** - don't trust blindly
4. **Use appropriate models** - match capability to task
5. **Test thoroughly** - complex features need comprehensive tests
6. **Plan for failures** - always have fallback strategies

## What's Next?

Ready to explore custom patterns? Continue to:
- 📄 [S7: Advisor Patterns](s7-advisors-guide.md) - Custom cross-cutting concerns
- 📄 [S8: Multi-Model](s8-multimodel-guide.md) - Model comparison strategies
- 🏗️ [Performance Guide](../architecture/performance.md) - Optimization techniques

---

[← S5: Production Chatbot](s5-chatbot-guide.md) | [Back to Modules](../README.md#module-guides) | [S7: Advisors →](s7-advisors-guide.md)