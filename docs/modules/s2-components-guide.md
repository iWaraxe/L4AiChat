# 📄 Module S2: ChatClient Components Guide

## Overview

This module explores the core components of Spring AI's ChatClient API, demonstrating different ways to interact with AI models. It's designed to help you understand the building blocks before combining them into complex applications.

## Learning Objectives

By completing this module, you will:
- ✅ Master the ChatClient fluent API
- ✅ Understand different message types and when to use them
- ✅ Learn structured output patterns for type-safe responses
- ✅ Compare ChatClient vs ChatModel approaches
- ✅ Implement proper entity mapping from AI responses

## Why These Components Matter

Before building complex chat applications, you need to understand:
1. **How to construct prompts** - Different message types serve different purposes
2. **How to extract structured data** - AI responses aren't just text
3. **How to choose the right API** - ChatClient vs ChatModel trade-offs
4. **How to handle responses** - From simple strings to complex objects

## Module Structure

```
s2components/
├── ComponentsApplication.java           # Main application
└── controller/
    ├── ChatClientDemoController.java   # ChatClient patterns
    ├── ChatModelDemoController.java    # Lower-level ChatModel
    ├── MessageTypesDemoController.java # Message construction
    └── StructuredOutputDemoController.java # Entity mapping
```

## Core Concepts

### 1. ChatClient vs ChatModel

#### ChatModel (Lower Level)
```java
@RestController
public class ChatModelDemoController {
    private final ChatModel chatModel;
    
    @PostMapping("/chat-model")
    public String chatWithModel(@RequestBody String userInput) {
        return chatModel.call(userInput);
    }
}
```

**When to Use ChatModel:**
- ✅ Need fine-grained control over request/response
- ✅ Building custom abstractions
- ✅ Integrating with legacy code
- ✅ Performance-critical paths

**Why It Exists:**
- **Foundation Layer** - Everything builds on this
- **Flexibility** - Access to all model features
- **Compatibility** - Works with any model provider

#### ChatClient (Higher Level)
```java
@RestController
public class ChatClientDemoController {
    private final ChatClient chatClient;
    
    @PostMapping("/chat-client")
    public String chatWithClient(@RequestBody String userInput) {
        return chatClient.prompt()
            .user(userInput)
            .call()
            .content();
    }
}
```

**When to Use ChatClient:**
- ✅ Most application development
- ✅ Need fluent, readable API
- ✅ Want built-in features (advisors, streaming)
- ✅ Rapid prototyping

**Why It's Better for Most Cases:**
- **Fluent API** - Readable, chainable methods
- **Feature Rich** - Advisors, streaming, memory built-in
- **Type Safe** - Compile-time checking
- **Future Proof** - New features added here first

### 2. Message Types and Their Purpose

#### System Messages - Setting Context
```java
@PostMapping("/system-message")
public String systemMessageExample(@RequestBody String userInput) {
    return chatClient.prompt()
        .system("You are a helpful assistant specializing in Java programming. "
              + "Always provide code examples and explain your reasoning.")
        .user(userInput)
        .call()
        .content();
}
```

**Why System Messages?**
- **Behavior Definition** - Set AI personality and constraints
- **Consistency** - Same behavior across conversations
- **Domain Focus** - Specialize responses
- **Safety** - Establish boundaries

**Best Practices:**
```java
// ✅ Good: Clear, specific instructions
.system("You are a Spring Boot expert. Provide concise, production-ready code examples.")

// ❌ Bad: Vague or contradictory
.system("You are an expert in everything. Be creative but also precise.")
```

#### User Messages - The Query
```java
@PostMapping("/user-message")
public String userMessageExample(@RequestBody String userInput) {
    // Simple text
    return chatClient.prompt()
        .user(userInput)
        .call()
        .content();
}
```

**Advanced User Messages with Media:**
```java
@PostMapping("/user-with-image")
public String analyzeImage(@RequestParam String imageUrl, 
                          @RequestParam String question) {
    return chatClient.prompt()
        .user(u -> u
            .text(question)
            .media(MimeTypeUtils.IMAGE_PNG, imageUrl))
        .call()
        .content();
}
```

**Why Support Media?**
- **Multimodal AI** - Modern models understand images
- **Rich Context** - Pictures worth 1000 words
- **Use Cases** - Screenshot analysis, diagram understanding

#### Assistant Messages - Prior Context
```java
@PostMapping("/with-context")
public String withAssistantContext(@RequestBody String userInput) {
    return chatClient.prompt()
        .messages(
            new SystemMessage("You are a Spring expert."),
            new UserMessage("How do I configure security?"),
            new AssistantMessage("To configure Spring Security, you need to..."),
            new UserMessage(userInput)  // Follow-up question
        )
        .call()
        .content();
}
```

**Why Include Assistant Messages?**
- **Conversation Continuity** - Provide history
- **Few-Shot Learning** - Show expected format
- **Context Setting** - Resume conversations

### 3. Structured Output - Beyond Text

#### Simple Entity Mapping
```java
public record MovieRecommendation(
    String title,
    String genre,
    int year,
    String reason
) {}

@PostMapping("/movie-recommendation")
public MovieRecommendation getMovieRecommendation(@RequestBody String preferences) {
    return chatClient.prompt()
        .user("Recommend a movie for someone who likes: " + preferences)
        .call()
        .entity(MovieRecommendation.class);
}
```

**Why Structured Output?**
- **Type Safety** - Compile-time guarantees
- **Automatic Parsing** - No manual JSON handling
- **Validation** - Can add constraints
- **API Contracts** - Clear response structure

#### Complex Entity Mapping
```java
public record TravelPlan(
    String destination,
    List<DayItinerary> itinerary,
    Budget estimatedBudget,
    List<String> packingList
) {
    public record DayItinerary(
        int dayNumber,
        List<Activity> activities
    ) {}
    
    public record Activity(
        String time,
        String name,
        String description,
        double estimatedCost
    ) {}
    
    public record Budget(
        double accommodation,
        double food,
        double activities,
        double transportation
    ) {}
}

@PostMapping("/travel-plan")
public TravelPlan generateTravelPlan(@RequestBody TravelRequest request) {
    String prompt = String.format(
        "Create a detailed %d-day travel plan for %s with a budget of $%d",
        request.days(), request.destination(), request.budget()
    );
    
    return chatClient.prompt()
        .user(prompt)
        .call()
        .entity(TravelPlan.class);
}
```

**Why This Complexity Works:**
- **Model Intelligence** - GPT-4 understands nested structures
- **Spring AI Magic** - Automatic JSON Schema generation
- **Developer Experience** - Work with POJOs, not strings

#### List Entity Mapping
```java
public record ProductIdea(
    String name,
    String description,
    String targetMarket,
    List<String> keyFeatures
) {}

@PostMapping("/product-ideas")
public List<ProductIdea> generateProductIdeas(@RequestParam String industry,
                                              @RequestParam int count) {
    return chatClient.prompt()
        .user(f -> f.text("Generate {count} innovative product ideas for the {industry} industry")
                   .param("count", count)
                   .param("industry", industry))
        .call()
        .entity(new ParameterizedTypeReference<List<ProductIdea>>() {});
}
```

**Why List Entities?**
- **Batch Operations** - Generate multiple items efficiently
- **Comparisons** - Get alternatives in one call
- **Efficiency** - One API call vs many

### 4. Advanced Patterns

#### Prompt Templates with Parameters
```java
@PostMapping("/code-review")
public CodeReview reviewCode(@RequestBody CodeReviewRequest request) {
    return chatClient.prompt()
        .user(u -> u.text("""
            Review this {language} code for:
            - Code quality
            - Performance issues  
            - Security concerns
            - Best practices
            
            Code:
            ```{language}
            {code}
            ```
            """)
            .param("language", request.language())
            .param("code", request.code()))
        .call()
        .entity(CodeReview.class);
}
```

**Why Templates?**
- **Reusability** - Define once, use many times
- **Consistency** - Same format every time
- **Maintainability** - Easy to update prompts
- **Safety** - Prevent injection attacks

#### Options Configuration
```java
@PostMapping("/creative-writing")
public String creativeWriting(@RequestBody String prompt) {
    return chatClient.prompt()
        .user(prompt)
        .options(OpenAiChatOptions.builder()
            .withTemperature(0.9)  // High creativity
            .withMaxTokens(500)
            .withFrequencyPenalty(0.5)  // Reduce repetition
            .build())
        .call()
        .content();
}

@PostMapping("/factual-analysis")
public String factualAnalysis(@RequestBody String data) {
    return chatClient.prompt()
        .user("Analyze this data: " + data)
        .options(OpenAiChatOptions.builder()
            .withTemperature(0.1)  // Low creativity, high consistency
            .withMaxTokens(1000)
            .build())
        .call()
        .content();
}
```

**Why Configure Options?**
- **Task Optimization** - Different tasks need different settings
- **Cost Control** - Limit token usage
- **Quality Control** - Balance creativity vs accuracy
- **Provider Features** - Access model-specific capabilities

## Common Patterns and Best Practices

### 1. Builder Pattern for Complex Prompts
```java
private ChatClient.ChatClientRequest.CallPromptSpec buildPrompt(String task, Map<String, Object> context) {
    var promptBuilder = chatClient.prompt();
    
    // Add system message if needed
    if (context.containsKey("systemPrompt")) {
        promptBuilder.system(context.get("systemPrompt").toString());
    }
    
    // Build user message with all context
    promptBuilder.user(u -> {
        u.text(task);
        context.forEach((key, value) -> u.param(key, value));
        return u;
    });
    
    // Add options if specified
    if (context.containsKey("temperature")) {
        promptBuilder.options(OpenAiChatOptions.builder()
            .withTemperature((Double) context.get("temperature"))
            .build());
    }
    
    return promptBuilder;
}
```

### 2. Error Handling for Entity Mapping
```java
@PostMapping("/safe-entity")
public ResponseEntity<?> safeEntityMapping(@RequestBody String userInput) {
    try {
        BusinessEntity result = chatClient.prompt()
            .user(userInput)
            .call()
            .entity(BusinessEntity.class);
            
        // Validate the result
        if (result == null || !isValid(result)) {
            return ResponseEntity.badRequest()
                .body("Invalid response from AI");
        }
        
        return ResponseEntity.ok(result);
        
    } catch (Exception e) {
        logger.error("Entity mapping failed", e);
        return ResponseEntity.internalServerError()
            .body("Failed to process request");
    }
}
```

### 3. Streaming for Large Responses
```java
@GetMapping(value = "/stream-analysis", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> streamAnalysis(@RequestParam String topic) {
    return chatClient.prompt()
        .user("Provide a comprehensive analysis of: " + topic)
        .stream()
        .content();
}
```

## Testing Strategies

### 1. Unit Testing with Mocks
```java
@Test
void testStructuredOutput() {
    // Given
    when(chatClient.prompt()).thenReturn(promptSpec);
    when(promptSpec.user(anyString())).thenReturn(promptSpec);
    when(promptSpec.call()).thenReturn(callResponse);
    when(callResponse.entity(MovieRecommendation.class))
        .thenReturn(new MovieRecommendation("Inception", "Sci-Fi", 2010, "Mind-bending plot"));
    
    // When
    MovieRecommendation result = controller.getMovieRecommendation("sci-fi thrillers");
    
    // Then
    assertNotNull(result);
    assertEquals("Inception", result.title());
}
```

### 2. Integration Testing
```java
@Test
@SpringBootTest
void testRealEntityMapping() {
    // This requires actual API key
    TravelPlan plan = controller.generateTravelPlan(
        new TravelRequest("Paris", 3, 2000)
    );
    
    assertNotNull(plan);
    assertNotNull(plan.destination());
    assertFalse(plan.itinerary().isEmpty());
    assertEquals(3, plan.itinerary().size());
}
```

## Common Pitfalls and Solutions

### ❌ Pitfall: Over-Prompting
```java
// Bad: Too much in one prompt
.user("Analyze this code, fix all bugs, optimize performance, add tests, " +
      "update documentation, and deploy to production")
```

### ✅ Solution: Break Down Tasks
```java
// Good: Focused, single-purpose prompts
.user("Identify potential bugs in this code: " + code)
// Then separately:
.user("Suggest performance optimizations for this function: " + function)
```

### ❌ Pitfall: Ignoring Token Limits
```java
// Bad: No limits
.user(entireBookContent)
```

### ✅ Solution: Chunk and Summarize
```java
// Good: Handle large content appropriately
.user("Summarize this chapter: " + chapter)
.options(OpenAiChatOptions.builder().withMaxTokens(500).build())
```

## Key Takeaways

1. **ChatClient > ChatModel** for most use cases due to better API
2. **System messages** set behavior, user messages ask questions
3. **Structured output** provides type safety and better UX
4. **Templates** improve maintainability and consistency
5. **Options** let you optimize for specific tasks
6. **Entity mapping** turns AI into a data transformation tool

## What's Next?

Ready to manage conversation state? Continue to:
- 📄 [S3: Context Management](s3-context-guide.md) - Memory patterns
- 📄 [S4: State Management](s4-state-guide.md) - Advanced state
- 🏗️ [Architecture Patterns](../architecture/patterns.md) - Design decisions

---

[← S1: Multi-turn](s1-multiturn-guide.md) | [Back to Modules](../README.md#module-guides) | [S3: Context →](s3-context-guide.md)