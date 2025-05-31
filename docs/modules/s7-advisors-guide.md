# 📄 Module S7: Advisor Patterns Guide

## Overview

This module explores the powerful Advisor pattern in Spring AI, demonstrating how to build custom cross-cutting concerns that enhance AI interactions. You'll learn to create reusable components for logging, safety, performance optimization, and more.

## Learning Objectives

By completing this module, you will:
- ✅ Master the Advisor pattern for cross-cutting concerns
- ✅ Build custom advisors for logging, safety, and performance
- ✅ Understand advisor ordering and composition
- ✅ Implement the Re-Reading (Re2) pattern for improved reasoning
- ✅ Design enterprise-grade AI middleware

## Why Advisors Matter

Advisors provide a clean way to add functionality without modifying core chat logic:

**Without Advisors** (tightly coupled):
```java
public String chat(String message) {
    // Logging mixed with business logic
    logger.info("Processing: {}", message);
    
    // Security checks mixed in
    if (containsProhibitedContent(message)) {
        throw new SecurityException("Prohibited content");
    }
    
    // Performance tracking mixed in
    long start = System.currentTimeMillis();
    String response = chatClient.call(message);
    metrics.record(System.currentTimeMillis() - start);
    
    return response;
}
```

**With Advisors** (clean separation):
```java
public String chat(String message) {
    return chatClient.prompt()
        .user(message)
        .advisors(loggingAdvisor, securityAdvisor, performanceAdvisor)
        .call()
        .content();
}
```

## Module Structure

```
s7advisors/
├── AdvisorsApplication.java             # Main application
├── advisors/
│   ├── LoggingAdvisor.java             # Request/response logging
│   ├── PerformanceAdvisor.java         # Metrics and timing
│   ├── Re2Advisor.java                 # Re-reading pattern
│   ├── SafetyAdvisor.java              # Content filtering
│   └── ContextEnrichmentAdvisor.java   # Dynamic context injection
├── controller/
│   └── AdvisorDemoController.java      # Demonstration endpoints
├── service/
│   └── AdvisorDemoService.java         # Service layer patterns
└── config/
    └── AdvisorConfiguration.java       # Advisor setup and ordering
```

## Core Advisor Concepts

### 1. Basic Advisor Implementation

#### Simple Logging Advisor
```java
@Component
public class SimpleLoggingAdvisor implements Advisor {
    private static final Logger logger = LoggerFactory.getLogger(SimpleLoggingAdvisor.class);
    
    @Override
    public AdvisedResponse advise(AdvisedRequest request, CallAroundChain chain) {
        String userMessage = extractUserMessage(request);
        String conversationId = extractConversationId(request);
        
        logger.info("Chat request - Conversation: {}, Message: {}", conversationId, userMessage);
        
        long startTime = System.currentTimeMillis();
        AdvisedResponse response = chain.nextAroundAdvise(request);
        long duration = System.currentTimeMillis() - startTime;
        
        logger.info("Chat response - Conversation: {}, Duration: {}ms, Response length: {}", 
                   conversationId, duration, response.getResponse().getResult().getOutput().getContent().length());
        
        return response;
    }
    
    private String extractUserMessage(AdvisedRequest request) {
        return request.getPrompt().getInstructions().stream()
            .filter(message -> message.getType() == Message.MessageType.USER)
            .map(Message::getContent)
            .findFirst()
            .orElse("N/A");
    }
    
    private String extractConversationId(AdvisedRequest request) {
        return (String) request.getAdvisorContext()
            .getOrDefault(ChatMemory.CONVERSATION_ID, "unknown");
    }
}
```

**Why This Pattern?**
- **Separation of Concerns** - Logging separate from business logic
- **Reusability** - Use across different chat clients
- **Testability** - Test logging independently
- **Configuration** - Enable/disable logging easily

### 2. Advanced Advisor: Re-Reading (Re2) Pattern

#### Re2Advisor Implementation
```java
@Component
public class Re2Advisor implements Advisor {
    private static final Logger logger = LoggerFactory.getLogger(Re2Advisor.class);
    private final ChatClient analysisClient;
    
    public Re2Advisor(ChatClient.Builder builder) {
        // Separate client for self-reflection
        this.analysisClient = builder.clone()
            .defaultOptions(OpenAiChatOptions.builder()
                .withTemperature(0.1)  // Low temperature for analysis
                .withModel("gpt-4")    // Use most capable model
                .build())
            .build();
    }
    
    @Override
    public AdvisedResponse advise(AdvisedRequest request, CallAroundChain chain) {
        // First, get the initial response
        AdvisedResponse initialResponse = chain.nextAroundAdvise(request);
        
        // Check if Re2 is enabled for this request
        if (!shouldApplyRe2(request)) {
            return initialResponse;
        }
        
        // Apply Re-Reading pattern
        return applyRe2Pattern(request, initialResponse);
    }
    
    private boolean shouldApplyRe2(AdvisedRequest request) {
        // Apply Re2 for complex questions or when explicitly requested
        String userMessage = extractUserMessage(request);
        
        return request.getAdvisorContext().getOrDefault("re2.enabled", false).equals(true) ||
               isComplexQuestion(userMessage);
    }
    
    private boolean isComplexQuestion(String message) {
        // Heuristics for complex questions
        return message.length() > 100 ||
               message.toLowerCase().contains("analyze") ||
               message.toLowerCase().contains("compare") ||
               message.toLowerCase().contains("recommend") ||
               message.toLowerCase().contains("strategy");
    }
    
    private AdvisedResponse applyRe2Pattern(AdvisedRequest originalRequest, AdvisedResponse initialResponse) {
        String originalQuestion = extractUserMessage(originalRequest);
        String initialAnswer = initialResponse.getResponse().getResult().getOutput().getContent();
        
        // Step 1: Self-critique
        String critique = analysisClient.prompt()
            .system("""
                You are a critical analyst. Review the given answer to a question and identify:
                1. Any potential errors or inaccuracies
                2. Missing important information
                3. Areas that could be explained more clearly
                4. Alternative perspectives to consider
                
                Be thorough but constructive.
                """)
            .user(String.format("""
                Original Question: %s
                
                Answer to Review: %s
                
                Provide a critical analysis of this answer:
                """, originalQuestion, initialAnswer))
            .call()
            .content();
        
        // Step 2: Generate improved response
        String improvedResponse = analysisClient.prompt()
            .system("""
                You are an expert assistant. Based on the critique provided, generate an improved answer 
                that addresses the identified issues while maintaining accuracy and clarity.
                """)
            .user(String.format("""
                Original Question: %s
                
                Initial Answer: %s
                
                Critique: %s
                
                Please provide an improved answer that addresses the critique:
                """, originalQuestion, initialAnswer, critique))
            .call()
            .content();
        
        logger.info("Applied Re2 pattern - Original length: {}, Improved length: {}", 
                   initialAnswer.length(), improvedResponse.length());
        
        // Return the improved response
        return new AdvisedResponse(
            ChatResponse.builder()
                .withResult(new Generation(new AssistantMessage(improvedResponse)))
                .build()
        );
    }
}
```

**Why Re-Reading (Re2)?**
- **Quality Improvement** - Self-correction leads to better responses
- **Error Reduction** - Catches and fixes mistakes automatically
- **Depth** - Encourages more thorough analysis
- **Consistency** - Reduces response variability

### 3. Safety and Content Filtering

#### SafetyAdvisor Implementation
```java
@Component
public class SafetyAdvisor implements Advisor {
    private static final Logger logger = LoggerFactory.getLogger(SafetyAdvisor.class);
    private final ContentFilterService contentFilter;
    private final ChatClient moderationClient;
    
    @Override
    public AdvisedResponse advise(AdvisedRequest request, CallAroundChain chain) {
        // Pre-process: Check input safety
        String userMessage = extractUserMessage(request);
        SafetyResult inputSafety = contentFilter.checkContent(userMessage);
        
        if (!inputSafety.isSafe()) {
            logger.warn("Unsafe input detected: {}", inputSafety.getViolations());
            return createSafetyViolationResponse(inputSafety);
        }
        
        // Process the request
        AdvisedResponse response = chain.nextAroundAdvise(request);
        
        // Post-process: Check output safety
        String outputContent = response.getResponse().getResult().getOutput().getContent();
        SafetyResult outputSafety = contentFilter.checkContent(outputContent);
        
        if (!outputSafety.isSafe()) {
            logger.warn("Unsafe output detected: {}", outputSafety.getViolations());
            return createSafeAlternativeResponse(request);
        }
        
        return response;
    }
    
    private AdvisedResponse createSafetyViolationResponse(SafetyResult safety) {
        String safeResponse = "I can't help with that request as it violates content policy. " +
                            "Please rephrase your question in a way that doesn't involve " +
                            safety.getViolationCategories().stream()
                                  .collect(Collectors.joining(", ")) + ".";
        
        return new AdvisedResponse(
            ChatResponse.builder()
                .withResult(new Generation(new AssistantMessage(safeResponse)))
                .build()
        );
    }
    
    private AdvisedResponse createSafeAlternativeResponse(AdvisedRequest request) {
        // Generate a safer alternative response
        String safeResponse = moderationClient.prompt()
            .system("""
                Provide a helpful response that addresses the user's question while ensuring
                the content is safe, appropriate, and follows content policies.
                """)
            .user("Provide a safe, helpful response to: " + extractUserMessage(request))
            .call()
            .content();
            
        return new AdvisedResponse(
            ChatResponse.builder()
                .withResult(new Generation(new AssistantMessage(safeResponse)))
                .build()
        );
    }
}

// Supporting classes
public record SafetyResult(
    boolean isSafe,
    List<String> violations,
    List<String> violationCategories,
    double confidenceScore
) {}

@Service
public class ContentFilterService {
    public SafetyResult checkContent(String content) {
        // Implement content filtering logic
        // Could integrate with Azure Content Safety, AWS Comprehend, etc.
        List<String> violations = new ArrayList<>();
        
        if (containsHateSpeech(content)) {
            violations.add("Hate speech detected");
        }
        
        if (containsViolence(content)) {
            violations.add("Violence detected");
        }
        
        if (containsPersonalInfo(content)) {
            violations.add("Personal information detected");
        }
        
        return new SafetyResult(
            violations.isEmpty(),
            violations,
            extractCategories(violations),
            calculateConfidence(violations)
        );
    }
    
    // Implementation methods...
}
```

### 4. Performance and Metrics Advisor

#### PerformanceAdvisor Implementation
```java
@Component
public class PerformanceAdvisor implements Advisor {
    private final MeterRegistry meterRegistry;
    private final ChatMetricsService metricsService;
    
    public PerformanceAdvisor(MeterRegistry meterRegistry, ChatMetricsService metricsService) {
        this.meterRegistry = meterRegistry;
        this.metricsService = metricsService;
    }
    
    @Override
    public AdvisedResponse advise(AdvisedRequest request, CallAroundChain chain) {
        String conversationId = extractConversationId(request);
        String modelName = extractModelName(request);
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            AdvisedResponse response = chain.nextAroundAdvise(request);
            
            // Record success metrics
            recordMetrics(conversationId, modelName, response, sample, true);
            
            return response;
            
        } catch (Exception e) {
            // Record error metrics
            recordErrorMetrics(conversationId, modelName, sample, e);
            throw e;
        }
    }
    
    private void recordMetrics(String conversationId, String modelName, 
                              AdvisedResponse response, Timer.Sample sample, boolean success) {
        
        sample.stop(Timer.builder("chat.request.duration")
            .tag("conversation", conversationId)
            .tag("model", modelName)
            .tag("success", String.valueOf(success))
            .register(meterRegistry));
        
        // Token usage metrics
        Usage usage = response.getResponse().getMetadata().getUsage();
        if (usage != null) {
            meterRegistry.gauge("chat.tokens.prompt", 
                Tags.of("conversation", conversationId, "model", modelName),
                usage.getPromptTokens());
                
            meterRegistry.gauge("chat.tokens.completion",
                Tags.of("conversation", conversationId, "model", modelName),
                usage.getGenerationTokens());
                
            meterRegistry.gauge("chat.tokens.total",
                Tags.of("conversation", conversationId, "model", modelName),
                usage.getTotalTokens());
        }
        
        // Response length
        String content = response.getResponse().getResult().getOutput().getContent();
        meterRegistry.gauge("chat.response.length",
            Tags.of("conversation", conversationId, "model", modelName),
            content.length());
        
        // Custom business metrics
        metricsService.recordConversationActivity(conversationId, usage);
    }
    
    private void recordErrorMetrics(String conversationId, String modelName, 
                                   Timer.Sample sample, Exception e) {
        sample.stop(Timer.builder("chat.request.duration")
            .tag("conversation", conversationId)
            .tag("model", modelName)
            .tag("success", "false")
            .tag("error", e.getClass().getSimpleName())
            .register(meterRegistry));
            
        meterRegistry.counter("chat.errors",
            "conversation", conversationId,
            "model", modelName,
            "error", e.getClass().getSimpleName())
            .increment();
    }
}
```

### 5. Context Enrichment Advisor

#### Dynamic Context Injection
```java
@Component
public class ContextEnrichmentAdvisor implements Advisor {
    private final UserProfileService userProfileService;
    private final ConversationContextService contextService;
    
    @Override
    public AdvisedResponse advise(AdvisedRequest request, CallAroundChain chain) {
        // Extract context information
        String conversationId = extractConversationId(request);
        String userId = extractUserId(request);
        
        // Enrich the request with additional context
        AdvisedRequest enrichedRequest = enrichRequestWithContext(request, conversationId, userId);
        
        return chain.nextAroundAdvise(enrichedRequest);
    }
    
    private AdvisedRequest enrichRequestWithContext(AdvisedRequest request, String conversationId, String userId) {
        List<Message> originalMessages = new ArrayList<>(request.getPrompt().getInstructions());
        
        // Add user profile context
        if (userId != null) {
            UserProfile profile = userProfileService.getProfile(userId);
            String contextMessage = buildUserContextMessage(profile);
            originalMessages.add(0, new SystemMessage(contextMessage));
        }
        
        // Add conversation context
        ConversationContext context = contextService.getContext(conversationId);
        if (context.hasRelevantHistory()) {
            String historyContext = buildHistoryContextMessage(context);
            originalMessages.add(0, new SystemMessage(historyContext));
        }
        
        // Add temporal context
        String timeContext = buildTimeContextMessage();
        originalMessages.add(0, new SystemMessage(timeContext));
        
        return new AdvisedRequest(
            new Prompt(originalMessages, request.getPrompt().getOptions()),
            request.getAdvisorContext()
        );
    }
    
    private String buildUserContextMessage(UserProfile profile) {
        return String.format("""
            User context:
            - Preferred language: %s
            - Expertise level: %s
            - Interests: %s
            - Communication style: %s
            
            Adapt your responses accordingly.
            """, 
            profile.getPreferredLanguage(),
            profile.getExpertiseLevel(),
            String.join(", ", profile.getInterests()),
            profile.getCommunicationStyle()
        );
    }
    
    private String buildHistoryContextMessage(ConversationContext context) {
        return String.format("""
            Conversation context:
            - Previous topics discussed: %s
            - User's stated goals: %s
            - Ongoing projects: %s
            
            Reference this context when relevant.
            """,
            String.join(", ", context.getTopics()),
            String.join(", ", context.getGoals()),
            String.join(", ", context.getProjects())
        );
    }
    
    private String buildTimeContextMessage() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("""
            Current context:
            - Date: %s
            - Time: %s
            - Day of week: %s
            
            Consider temporal relevance in your responses.
            """,
            now.toLocalDate(),
            now.toLocalTime().truncatedTo(ChronoUnit.MINUTES),
            now.getDayOfWeek()
        );
    }
}
```

## Advisor Composition and Ordering

### 1. Advisor Configuration
```java
@Configuration
public class AdvisorConfiguration {
    
    @Bean
    @Order(1)  // Execute first
    public SafetyAdvisor safetyAdvisor(ContentFilterService contentFilter) {
        return new SafetyAdvisor(contentFilter);
    }
    
    @Bean
    @Order(2)  // Execute second
    public ContextEnrichmentAdvisor contextEnrichmentAdvisor(
            UserProfileService userProfileService,
            ConversationContextService contextService) {
        return new ContextEnrichmentAdvisor(userProfileService, contextService);
    }
    
    @Bean
    @Order(3)  // Execute third
    public PerformanceAdvisor performanceAdvisor(MeterRegistry meterRegistry,
                                                ChatMetricsService metricsService) {
        return new PerformanceAdvisor(meterRegistry, metricsService);
    }
    
    @Bean
    @Order(4)  // Execute fourth
    public LoggingAdvisor loggingAdvisor() {
        return new LoggingAdvisor();
    }
    
    @Bean
    @Order(5)  // Execute last (expensive operation)
    public Re2Advisor re2Advisor(ChatClient.Builder builder) {
        return new Re2Advisor(builder);
    }
    
    @Bean
    public ChatClient advisorChainChatClient(ChatClient.Builder builder,
                                           List<Advisor> advisors) {
        return builder
            .defaultAdvisors(advisors.toArray(new Advisor[0]))
            .build();
    }
}
```

**Why This Ordering?**
1. **Safety First** - Block unsafe content immediately
2. **Context Enrichment** - Add context before processing
3. **Performance Monitoring** - Wrap the actual processing
4. **Logging** - Log the enriched request/response
5. **Re2 Last** - Most expensive, only when needed

### 2. Conditional Advisor Application
```java
@Service
public class AdvisorDemoService {
    private final ChatClient basicClient;
    private final ChatClient enhancedClient;
    private final ChatClient secureClient;
    
    public AdvisorDemoService(ChatClient.Builder builder,
                             List<Advisor> allAdvisors) {
        
        // Basic client - minimal advisors
        this.basicClient = builder.clone()
            .defaultAdvisors(
                findAdvisor(allAdvisors, LoggingAdvisor.class),
                findAdvisor(allAdvisors, PerformanceAdvisor.class)
            )
            .build();
            
        // Enhanced client - quality advisors
        this.enhancedClient = builder.clone()
            .defaultAdvisors(
                findAdvisor(allAdvisors, LoggingAdvisor.class),
                findAdvisor(allAdvisors, PerformanceAdvisor.class),
                findAdvisor(allAdvisors, ContextEnrichmentAdvisor.class),
                findAdvisor(allAdvisors, Re2Advisor.class)
            )
            .build();
            
        // Secure client - all safety features
        this.secureClient = builder.clone()
            .defaultAdvisors(allAdvisors.toArray(new Advisor[0]))
            .build();
    }
    
    public String processBasic(String message) {
        return basicClient.prompt().user(message).call().content();
    }
    
    public String processEnhanced(String conversationId, String message) {
        return enhancedClient.prompt()
            .user(message)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .content();
    }
    
    public String processSecure(String conversationId, String userId, String message) {
        return secureClient.prompt()
            .user(message)
            .advisors(a -> a
                .param(ChatMemory.CONVERSATION_ID, conversationId)
                .param("user.id", userId)
                .param("re2.enabled", true))
            .call()
            .content();
    }
}
```

## Testing Advisor Patterns

### 1. Unit Testing Individual Advisors
```java
@ExtendWith(MockitoExtension.class)
class Re2AdvisorTest {
    
    @Mock
    private ChatClient.Builder mockBuilder;
    
    @Mock
    private ChatClient mockAnalysisClient;
    
    @Mock
    private CallAroundChain mockChain;
    
    private Re2Advisor re2Advisor;
    
    @BeforeEach
    void setUp() {
        when(mockBuilder.clone()).thenReturn(mockBuilder);
        when(mockBuilder.defaultOptions(any())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockAnalysisClient);
        
        re2Advisor = new Re2Advisor(mockBuilder);
    }
    
    @Test
    void shouldApplyRe2ForComplexQuestions() {
        // Given
        String complexQuestion = "Analyze the strategic implications of AI adoption in enterprise software";
        AdvisedRequest request = createRequest(complexQuestion);
        AdvisedResponse initialResponse = createResponse("Initial analysis...");
        
        when(mockChain.nextAroundAdvise(request)).thenReturn(initialResponse);
        when(mockAnalysisClient.prompt()).thenReturn(mockPromptSpec);
        when(mockPromptSpec.system(anyString())).thenReturn(mockPromptSpec);
        when(mockPromptSpec.user(anyString())).thenReturn(mockPromptSpec);
        when(mockPromptSpec.call()).thenReturn(mockCallResponse);
        when(mockCallResponse.content()).thenReturn("Improved analysis...");
        
        // When
        AdvisedResponse result = re2Advisor.advise(request, mockChain);
        
        // Then
        verify(mockAnalysisClient, times(2)).prompt(); // Critique + improvement
        assertThat(result.getResponse().getResult().getOutput().getContent())
            .isEqualTo("Improved analysis...");
    }
}
```

### 2. Integration Testing Advisor Chains
```java
@SpringBootTest
class AdvisorChainIntegrationTest {
    
    @Autowired
    private AdvisorDemoService advisorDemoService;
    
    @Test
    void testEnhancedProcessingChain() {
        // Given
        String conversationId = "test-conversation";
        String message = "Explain quantum computing applications";
        
        // When
        String response = advisorDemoService.processEnhanced(conversationId, message);
        
        // Then
        assertThat(response).isNotBlank();
        // Verify advisor effects (check logs, metrics, etc.)
    }
    
    @Test
    void testSecureProcessingWithAllAdvisors() {
        // Given
        String conversationId = "secure-conversation";
        String userId = "test-user";
        String message = "How can I improve my team's productivity?";
        
        // When
        String response = advisorDemoService.processSecure(conversationId, userId, message);
        
        // Then
        assertThat(response).isNotBlank();
        // Response should be enhanced with context and Re2 processing
    }
}
```

## Key Takeaways

1. **Advisors enable clean separation** of cross-cutting concerns
2. **Order matters** - design advisor chains thoughtfully
3. **Conditional application** allows flexible advisor usage
4. **Re2 pattern** significantly improves response quality
5. **Safety advisors** are essential for production systems
6. **Performance monitoring** should be built into advisor chains

## What's Next?

Ready to explore multi-model strategies? Continue to:
- 📄 [S8: Multi-Model](s8-multimodel-guide.md) - Model comparison patterns
- 📄 [S9: Templates](s9-templates-guide.md) - Advanced prompt patterns
- 🏗️ [Performance Guide](../architecture/performance.md) - Optimization strategies

---

[← S6: Advanced Features](s6-advanced-guide.md) | [Back to Modules](../README.md#module-guides) | [S8: Multi-Model →](s8-multimodel-guide.md)