# 🎭 Tutorial S7: Advisor Patterns Mastery

> **⏱️ Duration**: 75 minutes  
> **🎯 Difficulty**: 🔴 Advanced  
> **📋 Prerequisites**: Complete [Tutorial S6](./S6-advanced-features.md)

## 🎯 Learning Objectives

By the end of this tutorial, you will:
- ✅ Master custom advisor implementations and patterns
- ✅ Build advisor chains for complex processing pipelines
- ✅ Implement performance optimization and caching advisors
- ✅ Create security and validation advisor patterns
- ✅ Design reusable advisor composition strategies

## 🛠️ Hands-On Exercise: Build a Comprehensive Advisor System

### Step 1: Explore the S7 Advisor Architecture

Let's examine the sophisticated advisor implementations:

```bash
# Navigate to the S7 module
cd src/main/java/com/coherentsolutions/l4aichat/s7advisors

# Check the advisor implementations
find . -name "*Advisor*.java" -type f
```

**🤔 Question**: What makes advisors the "middleware" of Spring AI applications?

<details>
<summary>💡 Click to reveal the answer</summary>

**Advisor Patterns in Spring AI**:

1. **Request Interceptors**: Modify prompts before sending to AI
2. **Response Transformers**: Process and enhance AI responses
3. **Context Injectors**: Add dynamic context (memory, RAG, etc.)
4. **Security Filters**: Validate and sanitize inputs/outputs
5. **Performance Optimizers**: Cache, rate limit, and optimize calls
6. **Monitoring Agents**: Log, track, and analyze interactions

**Why "Middleware"**:
- **Separation of Concerns**: Keep AI logic separate from cross-cutting concerns
- **Reusability**: Same advisor works across different chat clients
- **Composability**: Chain multiple advisors for complex processing
- **Testability**: Easy to test individual advisor behaviors
- **Maintainability**: Centralized implementation of common patterns
</details>

### Step 2: Start the S7 Application

```bash
# Start S7 application with advisor features
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s7advisors.AdvisorApplication
```

### Step 3: Test Basic Advisor Functionality

Test the foundational advisor implementations:

```bash
# Test logging advisor
curl -X POST http://localhost:8080/api/s7/chat/with-logging \
  -H "Content-Type: application/json" \
  -d '{"message": "Explain the Repository pattern in Spring"}'
```

**🔍 Check Logs**: You should see detailed request/response logging in the console.

```bash
# Test safety advisor
curl -X POST http://localhost:8080/api/s7/chat/with-safety \
  -H "Content-Type: application/json" \
  -d '{"message": "How do I hack into a database system?"}'
```

**Expected Response**:
```json
{
  "message": "I can't provide information about unauthorized access to systems. Instead, I can help you learn about legitimate database security practices and ethical programming techniques.",
  "safetyCheck": {
    "blocked": true,
    "reason": "Request contains potentially harmful content",
    "suggestion": "Please ask about legitimate technical topics"
  }
}
```

### Step 4: Test Re-Reading (Re2) Advisor

Experience improved reasoning through re-reading:

```bash
# Test Re2 advisor for complex reasoning
curl -X POST http://localhost:8080/api/s7/chat/with-re2 \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Design a microservices architecture for an e-commerce platform. Consider scalability, data consistency, and fault tolerance.",
    "complexity": "HIGH"
  }'
```

**🎯 Observation**: Notice how the Re2 advisor provides more thoughtful, structured responses for complex questions.

### Step 5: Test Advisor Chaining

Test multiple advisors working together:

```bash
# Test full advisor chain
curl -X POST http://localhost:8080/api/s7/chat/full-chain \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Create a secure API authentication system",
    "includeCodeExamples": true,
    "securityLevel": "HIGH"
  }'
```

### Step 6: Test Performance Optimizations

```bash
# Test cached responses
curl -X POST http://localhost:8080/api/s7/chat/cached \
  -H "Content-Type: application/json" \
  -d '{"message": "What is Spring Boot?"}'

# Repeat the same request (should be faster)
time curl -X POST http://localhost:8080/api/s7/chat/cached \
  -H "Content-Type: application/json" \
  -d '{"message": "What is Spring Boot?"}'
```

## 💡 Concept Deep-Dive: Advanced Advisor Patterns

### **1. Custom Advisor Implementation Patterns**

Building sophisticated advisors that transform AI interactions:

```java
// Base advisor interface extension
public abstract class AdvancedRequestAdvisor implements RequestAdvisor {
    
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final MeterRegistry meterRegistry;
    
    public AdvancedRequestAdvisor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    @Override
    public final AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String advisorName = getClass().getSimpleName();
        
        try {
            log.debug("Executing advisor: {}", advisorName);
            AdvisedRequest result = doAdviseRequest(request, context);
            
            // Track successful executions
            meterRegistry.counter("advisor.executions", 
                "advisor", advisorName, "status", "success").increment();
                
            return result;
            
        } catch (Exception e) {
            log.error("Advisor {} failed", advisorName, e);
            meterRegistry.counter("advisor.executions",
                "advisor", advisorName, "status", "error").increment();
            throw e;
            
        } finally {
            sample.stop(Timer.builder("advisor.execution.time")
                .tag("advisor", advisorName)
                .register(meterRegistry));
        }
    }
    
    protected abstract AdvisedRequest doAdviseRequest(AdvisedRequest request, Map<String, Object> context);
}

// Re-Reading (Re2) Advisor for improved reasoning
@Component
public class Re2Advisor extends AdvancedRequestAdvisor {
    
    private final ChatClient analysisClient;
    
    public Re2Advisor(ChatClient.Builder chatClientBuilder, MeterRegistry meterRegistry) {
        super(meterRegistry);
        this.analysisClient = chatClientBuilder
            .defaultOptions(OpenAiChatOptions.builder()
                .withTemperature(0.3)  // More focused for analysis
                .build())
            .build();
    }
    
    @Override
    protected AdvisedRequest doAdviseRequest(AdvisedRequest request, Map<String, Object> context) {
        String originalPrompt = extractUserMessage(request);
        
        // Determine if re-reading would be beneficial
        if (!shouldApplyRe2(originalPrompt, context)) {
            return request;
        }
        
        // Step 1: Initial analysis of the question
        String analysis = analyzeQuestion(originalPrompt);
        
        // Step 2: Re-read and enhance the prompt
        String enhancedPrompt = enhancePrompt(originalPrompt, analysis);
        
        // Step 3: Update the request with enhanced prompt
        return updateRequestWithEnhancedPrompt(request, enhancedPrompt);
    }
    
    private boolean shouldApplyRe2(String prompt, Map<String, Object> context) {
        // Apply Re2 for complex questions
        return prompt.length() > 100 ||
               prompt.contains("design") ||
               prompt.contains("architecture") ||
               prompt.contains("complex") ||
               context.getOrDefault("complexity", "").equals("HIGH");
    }
    
    private String analyzeQuestion(String originalPrompt) {
        return analysisClient.prompt()
            .system("""
                Analyze the complexity and requirements of this question.
                Identify:
                1. Key concepts that need to be addressed
                2. Potential sub-questions or areas to explore
                3. The level of detail that would be most helpful
                4. Any context that should be considered
                
                Provide a brief analysis to guide a comprehensive response.
                """)
            .user("Question to analyze: " + originalPrompt)
            .call()
            .content();
    }
    
    private String enhancePrompt(String originalPrompt, String analysis) {
        return String.format("""
            Original Question: %s
            
            Analysis of Question Requirements:
            %s
            
            Please provide a comprehensive, well-structured response that addresses
            all the key aspects identified in the analysis. Use clear organization
            with headings, examples, and practical insights.
            """, originalPrompt, analysis);
    }
}

// Safety and Content Filtering Advisor
@Component
public class SafetyAdvisor extends AdvancedRequestAdvisor {
    
    private final List<String> prohibitedTopics;
    private final List<Pattern> dangerousPatterns;
    private final ChatClient safetyClient;
    
    public SafetyAdvisor(ChatClient.Builder chatClientBuilder, MeterRegistry meterRegistry) {
        super(meterRegistry);
        this.prohibitedTopics = loadProhibitedTopics();
        this.dangerousPatterns = loadDangerousPatterns();
        this.safetyClient = chatClientBuilder
            .defaultSystem("You are a content safety analyzer.")
            .build();
    }
    
    @Override
    protected AdvisedRequest doAdviseRequest(AdvisedRequest request, Map<String, Object> context) {
        String userMessage = extractUserMessage(request);
        
        // Quick pattern-based filtering
        SafetyResult quickCheck = performQuickSafetyCheck(userMessage);
        if (!quickCheck.isSafe()) {
            throw new UnsafeContentException(quickCheck.getReason());
        }
        
        // AI-based safety analysis for complex cases
        if (needsDeepSafetyAnalysis(userMessage)) {
            SafetyResult deepCheck = performDeepSafetyAnalysis(userMessage);
            if (!deepCheck.isSafe()) {
                throw new UnsafeContentException(deepCheck.getReason());
            }
        }
        
        return request;
    }
    
    private SafetyResult performQuickSafetyCheck(String message) {
        String lowerMessage = message.toLowerCase();
        
        // Check prohibited topics
        for (String topic : prohibitedTopics) {
            if (lowerMessage.contains(topic)) {
                return SafetyResult.unsafe("Contains prohibited topic: " + topic);
            }
        }
        
        // Check dangerous patterns
        for (Pattern pattern : dangerousPatterns) {
            if (pattern.matcher(message).find()) {
                return SafetyResult.unsafe("Matches dangerous pattern");
            }
        }
        
        return SafetyResult.safe();
    }
    
    private SafetyResult performDeepSafetyAnalysis(String message) {
        String safetyPrompt = String.format("""
            Analyze this message for potential safety concerns:
            "%s"
            
            Check for:
            1. Requests for illegal activities
            2. Harmful or dangerous instructions
            3. Inappropriate content generation
            4. Privacy violations or personal data requests
            5. Potential misuse for malicious purposes
            
            Respond with: SAFE or UNSAFE and a brief reason.
            """, message);
            
        String response = safetyClient.prompt()
            .user(safetyPrompt)
            .call()
            .content();
            
        if (response.startsWith("UNSAFE")) {
            return SafetyResult.unsafe(response.substring(6).trim());
        }
        
        return SafetyResult.safe();
    }
}
```

### **2. Performance Optimization Advisors**

Sophisticated caching and optimization patterns:

```java
// Intelligent Caching Advisor
@Component
public class IntelligentCachingAdvisor extends AdvancedRequestAdvisor {
    
    private final Cache<String, CachedResponse> responseCache;
    private final ChatClient cacheAnalysisClient;
    
    public IntelligentCachingAdvisor(CacheManager cacheManager, 
                                   ChatClient.Builder chatClientBuilder,
                                   MeterRegistry meterRegistry) {
        super(meterRegistry);
        this.responseCache = cacheManager.getCache("ai-responses");
        this.cacheAnalysisClient = chatClientBuilder
            .defaultSystem("Analyze if questions are semantically similar for caching.")
            .build();
    }
    
    @Override
    protected AdvisedRequest doAdviseRequest(AdvisedRequest request, Map<String, Object> context) {
        String userMessage = extractUserMessage(request);
        
        // Generate cache key based on semantic meaning
        String cacheKey = generateSemanticCacheKey(userMessage);
        
        // Check for cached response
        CachedResponse cached = responseCache.getIfPresent(cacheKey);
        if (cached != null && isCacheValid(cached, context)) {
            log.info("Cache hit for semantic key: {}", cacheKey);
            meterRegistry.counter("advisor.cache", "result", "hit").increment();
            
            // Inject cached response into context
            context.put("cachedResponse", cached.getResponse());
            context.put("cacheHit", true);
            
            return request;
        }
        
        meterRegistry.counter("advisor.cache", "result", "miss").increment();
        context.put("cacheKey", cacheKey);
        return request;
    }
    
    private String generateSemanticCacheKey(String message) {
        // Normalize the message for better cache hits
        String normalized = normalizeMessage(message);
        
        // For similar questions, generate similar cache keys
        if (isTechnicalQuestion(normalized)) {
            return generateTechnicalCacheKey(normalized);
        }
        
        // Default to hash-based key
        return DigestUtils.sha256Hex(normalized);
    }
    
    private String generateTechnicalCacheKey(String message) {
        String analysisPrompt = String.format("""
            Extract the core technical concept from this question for caching purposes:
            "%s"
            
            Return just the main technical topic (e.g., "spring-boot-configuration", 
            "java-streams", "microservices-patterns") that would be the same for 
            similar questions about the same topic.
            """, message);
            
        String topic = cacheAnalysisClient.prompt()
            .user(analysisPrompt)
            .call()
            .content()
            .toLowerCase()
            .replaceAll("[^a-z0-9-]", "");
            
        return "tech:" + topic;
    }
}

// Rate Limiting Advisor
@Component
public class RateLimitingAdvisor extends AdvancedRequestAdvisor {
    
    private final RateLimiter globalRateLimiter;
    private final Map<String, RateLimiter> userRateLimiters;
    private final RedisTemplate<String, String> redisTemplate;
    
    public RateLimitingAdvisor(MeterRegistry meterRegistry) {
        super(meterRegistry);
        this.globalRateLimiter = RateLimiter.create(100.0); // 100 req/sec globally
        this.userRateLimiters = new ConcurrentHashMap<>();
    }
    
    @Override
    protected AdvisedRequest doAdviseRequest(AdvisedRequest request, Map<String, Object> context) {
        String userId = extractUserId(context);
        
        // Check global rate limit
        if (!globalRateLimiter.tryAcquire()) {
            meterRegistry.counter("advisor.ratelimit", "level", "global", "result", "exceeded").increment();
            throw new RateLimitExceededException("Global rate limit exceeded");
        }
        
        // Check user-specific rate limit
        RateLimiter userLimiter = getUserRateLimiter(userId);
        if (!userLimiter.tryAcquire()) {
            meterRegistry.counter("advisor.ratelimit", "level", "user", "result", "exceeded").increment();
            throw new RateLimitExceededException("User rate limit exceeded");
        }
        
        // Check Redis-based distributed rate limiting
        if (!checkDistributedRateLimit(userId)) {
            meterRegistry.counter("advisor.ratelimit", "level", "distributed", "result", "exceeded").increment();
            throw new RateLimitExceededException("Distributed rate limit exceeded");
        }
        
        meterRegistry.counter("advisor.ratelimit", "result", "allowed").increment();
        return request;
    }
    
    private RateLimiter getUserRateLimiter(String userId) {
        return userRateLimiters.computeIfAbsent(userId, 
            k -> RateLimiter.create(10.0)); // 10 req/sec per user
    }
    
    private boolean checkDistributedRateLimit(String userId) {
        String key = "rate_limit:user:" + userId;
        String script = """
            local key = KEYS[1]
            local limit = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local current = redis.call('GET', key)
            
            if current == false then
                redis.call('SET', key, 1)
                redis.call('EXPIRE', key, window)
                return 1
            else
                current = tonumber(current)
                if current < limit then
                    redis.call('INCR', key)
                    return 1
                else
                    return 0
                end
            end
            """;
            
        Long result = redisTemplate.execute(
            RedisScript.of(script, Long.class),
            List.of(key),
            "60", "60"  // 60 requests per 60 seconds
        );
        
        return result != null && result == 1;
    }
}
```

### **3. Advanced Advisor Composition**

Building sophisticated advisor chains and orchestration:

```java
// Advisor Chain Builder
@Component
public class AdvisorChainBuilder {
    
    public enum AdvisorProfile {
        DEVELOPMENT,    // Logging, debugging, relaxed safety
        PRODUCTION,     // Caching, rate limiting, strict safety
        RESEARCH,       // Re2, deep analysis, comprehensive logging
        ENTERPRISE      // Full security, compliance, monitoring
    }
    
    private final Map<AdvisorProfile, List<RequestAdvisor>> advisorProfiles;
    
    public AdvisorChainBuilder(ApplicationContext context) {
        this.advisorProfiles = buildAdvisorProfiles(context);
    }
    
    public ChatClient buildChatClientWithProfile(ChatClient.Builder builder, AdvisorProfile profile) {
        List<RequestAdvisor> advisors = advisorProfiles.get(profile);
        
        return builder
            .defaultAdvisors(advisors.toArray(new RequestAdvisor[0]))
            .build();
    }
    
    public ChatClient buildCustomChain(ChatClient.Builder builder, AdvisorChainConfig config) {
        List<RequestAdvisor> chain = new ArrayList<>();
        
        // Security layer (always first)
        if (config.isSecurityEnabled()) {
            chain.add(getAdvisor(SafetyAdvisor.class));
        }
        
        // Rate limiting layer
        if (config.isRateLimitingEnabled()) {
            chain.add(getAdvisor(RateLimitingAdvisor.class));
        }
        
        // Caching layer
        if (config.isCachingEnabled()) {
            chain.add(getAdvisor(IntelligentCachingAdvisor.class));
        }
        
        // Enhancement layer
        if (config.isRe2Enabled()) {
            chain.add(getAdvisor(Re2Advisor.class));
        }
        
        // Context injection layer
        if (config.isMemoryEnabled()) {
            chain.add(getAdvisor(MessageChatMemoryAdvisor.class));
        }
        
        // Monitoring layer (always last)
        if (config.isMonitoringEnabled()) {
            chain.add(getAdvisor(ComprehensiveLoggingAdvisor.class));
        }
        
        return builder
            .defaultAdvisors(chain.toArray(new RequestAdvisor[0]))
            .build();
    }
    
    private Map<AdvisorProfile, List<RequestAdvisor>> buildAdvisorProfiles(ApplicationContext context) {
        Map<AdvisorProfile, List<RequestAdvisor>> profiles = new EnumMap<>(AdvisorProfile.class);
        
        // Development profile
        profiles.put(AdvisorProfile.DEVELOPMENT, List.of(
            context.getBean(SimpleLoggingAdvisor.class),
            context.getBean(MemoryAdvisor.class)
        ));
        
        // Production profile
        profiles.put(AdvisorProfile.PRODUCTION, List.of(
            context.getBean(SafetyAdvisor.class),
            context.getBean(RateLimitingAdvisor.class),
            context.getBean(IntelligentCachingAdvisor.class),
            context.getBean(MemoryAdvisor.class),
            context.getBean(MonitoringAdvisor.class)
        ));
        
        // Research profile
        profiles.put(AdvisorProfile.RESEARCH, List.of(
            context.getBean(Re2Advisor.class),
            context.getBean(DeepAnalysisAdvisor.class),
            context.getBean(ComprehensiveLoggingAdvisor.class),
            context.getBean(MemoryAdvisor.class)
        ));
        
        // Enterprise profile
        profiles.put(AdvisorProfile.ENTERPRISE, List.of(
            context.getBean(EnterpriseSecurityAdvisor.class),
            context.getBean(ComplianceAdvisor.class),
            context.getBean(RateLimitingAdvisor.class),
            context.getBean(IntelligentCachingAdvisor.class),
            context.getBean(Re2Advisor.class),
            context.getBean(MemoryAdvisor.class),
            context.getBean(AuditingAdvisor.class),
            context.getBean(MetricsAdvisor.class)
        ));
        
        return profiles;
    }
}

// Dynamic Advisor Selection
@Service
public class DynamicAdvisorService {
    
    public ChatClient createContextAwareChatClient(ChatRequest request, UserContext userContext) {
        AdvisorChainConfig config = determineOptimalAdvisorChain(request, userContext);
        
        return advisorChainBuilder.buildCustomChain(chatClientBuilder, config);
    }
    
    private AdvisorChainConfig determineOptimalAdvisorChain(ChatRequest request, UserContext userContext) {
        AdvisorChainConfig.Builder configBuilder = AdvisorChainConfig.builder();
        
        // Security based on user role and content
        if (userContext.requiresStrictSecurity() || containsSensitiveContent(request)) {
            configBuilder.enableSecurity();
        }
        
        // Rate limiting based on user tier
        if (userContext.getTier() != UserTier.ENTERPRISE) {
            configBuilder.enableRateLimiting();
        }
        
        // Caching for common questions
        if (isCommonQuestion(request.getMessage())) {
            configBuilder.enableCaching();
        }
        
        // Re2 for complex questions
        if (isComplexQuestion(request.getMessage())) {
            configBuilder.enableRe2();
        }
        
        // Memory for conversational context
        if (request.getConversationId() != null) {
            configBuilder.enableMemory();
        }
        
        // Always enable monitoring
        configBuilder.enableMonitoring();
        
        return configBuilder.build();
    }
}
```

## 🧪 Live Experiment: Advanced Advisor Patterns

Let's experiment with sophisticated advisor combinations:

### Experiment 1: Conditional Advisor Activation

Create smart advisors that activate based on context:

```java
// Context-Aware Advisor
@Component
public class ContextAwareAdvisor extends AdvancedRequestAdvisor {
    
    private final Map<String, RequestAdvisor> conditionalAdvisors;
    
    @Override
    protected AdvisedRequest doAdviseRequest(AdvisedRequest request, Map<String, Object> context) {
        String userMessage = extractUserMessage(request);
        UserContext userContext = extractUserContext(context);
        
        // Determine which advisors to apply
        List<RequestAdvisor> applicableAdvisors = determineApplicableAdvisors(userMessage, userContext);
        
        // Apply advisors in sequence
        AdvisedRequest currentRequest = request;
        for (RequestAdvisor advisor : applicableAdvisors) {
            currentRequest = advisor.adviseRequest(currentRequest, context);
        }
        
        return currentRequest;
    }
    
    private List<RequestAdvisor> determineApplicableAdvisors(String message, UserContext userContext) {
        List<RequestAdvisor> advisors = new ArrayList<>();
        
        // Security advisor for sensitive users
        if (userContext.getSecurityLevel() == SecurityLevel.HIGH) {
            advisors.add(conditionalAdvisors.get("security"));
        }
        
        // Code analysis advisor for development questions
        if (containsCode(message) || isAboutProgramming(message)) {
            advisors.add(conditionalAdvisors.get("codeAnalysis"));
        }
        
        // Research advisor for academic users
        if (userContext.getRole() == UserRole.RESEARCHER) {
            advisors.add(conditionalAdvisors.get("research"));
        }
        
        return advisors;
    }
}
```

### Experiment 2: Performance Benchmarking Advisor

Build an advisor that measures and optimizes performance:

```java
@Component
public class PerformanceBenchmarkingAdvisor extends AdvancedRequestAdvisor {
    
    private final PerformanceMetricsCollector metricsCollector;
    
    @Override
    protected AdvisedRequest doAdviseRequest(AdvisedRequest request, Map<String, Object> context) {
        String requestId = UUID.randomUUID().toString();
        PerformanceBenchmark benchmark = startBenchmark(requestId, request);
        
        try {
            // Analyze request complexity
            RequestComplexity complexity = analyzeComplexity(request);
            benchmark.setComplexity(complexity);
            
            // Predict response time
            Duration predictedTime = predictResponseTime(complexity);
            benchmark.setPredictedTime(predictedTime);
            
            // Set performance hints in context
            context.put("performanceHints", generatePerformanceHints(complexity));
            context.put("benchmark", benchmark);
            
            return request;
            
        } finally {
            benchmark.recordRequestProcessingTime();
        }
    }
    
    private RequestComplexity analyzeComplexity(AdvisedRequest request) {
        String message = extractUserMessage(request);
        
        int score = 0;
        score += message.length() / 10;  // Length factor
        score += countQuestions(message) * 2;  // Question complexity
        score += countCodeBlocks(message) * 5;  // Code complexity
        score += countTechnicalTerms(message) * 1;  // Technical complexity
        
        return RequestComplexity.fromScore(score);
    }
    
    private Duration predictResponseTime(RequestComplexity complexity) {
        // Use historical data to predict response time
        List<Duration> historicalTimes = metricsCollector.getHistoricalTimes(complexity);
        
        if (historicalTimes.isEmpty()) {
            return getDefaultTime(complexity);
        }
        
        // Calculate median time for better prediction
        return calculateMedian(historicalTimes);
    }
}
```

### Experiment 3: Multi-Tenant Advisor

Create advisors that work in multi-tenant environments:

```java
@Component
public class MultiTenantAdvisor extends AdvancedRequestAdvisor {
    
    private final TenantConfigurationService tenantConfigService;
    private final Map<String, ChatClient> tenantChatClients;
    
    @Override
    protected AdvisedRequest doAdviseRequest(AdvisedRequest request, Map<String, Object> context) {
        String tenantId = extractTenantId(context);
        TenantConfiguration tenantConfig = tenantConfigService.getConfiguration(tenantId);
        
        // Apply tenant-specific transformations
        AdvisedRequest tenantRequest = applyTenantContext(request, tenantConfig);
        
        // Set tenant-specific parameters
        context.put("tenantId", tenantId);
        context.put("tenantConfig", tenantConfig);
        context.put("modelConfig", tenantConfig.getModelConfiguration());
        
        return tenantRequest;
    }
    
    private AdvisedRequest applyTenantContext(AdvisedRequest request, TenantConfiguration config) {
        // Add tenant-specific system message
        String tenantSystemMessage = config.getSystemMessagePrefix() + 
                                   extractSystemMessage(request) +
                                   config.getSystemMessageSuffix();
        
        // Apply tenant-specific prompt templates
        String userMessage = applyTenantTemplate(extractUserMessage(request), config);
        
        return updateRequestWithMessages(request, tenantSystemMessage, userMessage);
    }
    
    private String applyTenantTemplate(String originalMessage, TenantConfiguration config) {
        if (config.hasCustomTemplate()) {
            return config.getPromptTemplate()
                .replace("{{original_message}}", originalMessage)
                .replace("{{tenant_context}}", config.getContextInformation())
                .replace("{{brand_voice}}", config.getBrandVoice());
        }
        
        return originalMessage;
    }
}
```

## ✅ Check Your Understanding

### Quick Quiz

1. **What is the primary benefit of the advisor pattern in Spring AI?**
   - A) Faster response times
   - B) Separation of concerns and reusable cross-cutting functionality
   - C) Reduced memory usage
   - D) Better AI accuracy

<details>
<summary>Answer</summary>
**B) Separation of concerns and reusable cross-cutting functionality** - Advisors allow you to separate AI logic from concerns like logging, security, caching, and validation, making them reusable across different chat clients.
</details>

2. **Why is advisor ordering important in a chain?**
   - A) For performance optimization
   - B) Because later advisors can depend on context set by earlier ones
   - C) To reduce complexity
   - D) For better error handling

<details>
<summary>Answer</summary>
**B) Because later advisors can depend on context set by earlier ones** - Advisor order matters because advisors often depend on context, transformations, or validation performed by previous advisors in the chain.
</details>

3. **What makes the Re2 (Re-Reading) advisor effective?**
   - A) It caches responses
   - B) It analyzes questions first, then enhances prompts for better responses
   - C) It filters unsafe content
   - D) It compresses prompts

<details>
<summary>Answer</summary>
**B) It analyzes questions first, then enhances prompts for better responses** - Re2 advisor improves response quality by first analyzing the question's complexity and requirements, then enhancing the prompt to guide more thoughtful, comprehensive answers.
</details>

### Coding Challenge 🏆

**Challenge**: Create a "Smart Response Enhancement Advisor" that:
1. Analyzes the quality of AI responses
2. Automatically requests improvements for low-quality responses
3. Tracks improvement patterns over time
4. Adapts enhancement strategies based on user feedback

**Requirements**:
```java
@Component
public class SmartResponseEnhancementAdvisor implements ResponseAdvisor {
    
    public AdvisedResponse adviseResponse(AdvisedResponse response, Map<String, Object> context) {
        // Analyze response quality
        // Enhance if needed
        // Track improvements
        // Return enhanced response
    }
    
    // Support methods for quality analysis and enhancement
}
```

<details>
<summary>💡 Solution</summary>

```java
// Response quality assessment
public record ResponseQuality(
    double overallScore,
    double clarity,
    double completeness,
    double accuracy,
    double helpfulness,
    List<String> issues,
    List<String> suggestions
) {}

// Smart response enhancement advisor
@Component
public class SmartResponseEnhancementAdvisor implements ResponseAdvisor {
    
    private final ChatClient qualityAnalysisClient;
    private final ChatClient enhancementClient;
    private final ResponseQualityTracker qualityTracker;
    
    public SmartResponseEnhancementAdvisor(ChatClient.Builder chatClientBuilder,
                                         ResponseQualityTracker qualityTracker) {
        this.qualityAnalysisClient = chatClientBuilder
            .defaultSystem("You are a response quality analyst.")
            .build();
            
        this.enhancementClient = chatClientBuilder
            .defaultSystem("You are an expert at improving AI responses.")
            .build();
            
        this.qualityTracker = qualityTracker;
    }
    
    @Override
    public AdvisedResponse adviseResponse(AdvisedResponse response, Map<String, Object> context) {
        String originalResponse = response.getResponse().getResult().getOutput().getContent();
        String originalPrompt = getOriginalPrompt(context);
        
        // Analyze response quality
        ResponseQuality quality = analyzeResponseQuality(originalResponse, originalPrompt);
        
        // Track quality metrics
        qualityTracker.recordQuality(quality, context);
        
        // Enhance if quality is below threshold
        if (quality.overallScore() < 7.0) {
            String enhancedResponse = enhanceResponse(originalResponse, originalPrompt, quality);
            
            // Verify improvement
            ResponseQuality enhancedQuality = analyzeResponseQuality(enhancedResponse, originalPrompt);
            
            if (enhancedQuality.overallScore() > quality.overallScore()) {
                qualityTracker.recordImprovement(quality, enhancedQuality, context);
                return updateResponseContent(response, enhancedResponse);
            }
        }
        
        return response;
    }
    
    private ResponseQuality analyzeResponseQuality(String response, String originalPrompt) {
        String qualityPrompt = String.format("""
            Analyze the quality of this AI response:
            
            Original Question: %s
            AI Response: %s
            
            Rate each aspect from 1-10:
            1. Clarity: Is the response clear and easy to understand?
            2. Completeness: Does it fully address the question?
            3. Accuracy: Is the information correct and up-to-date?
            4. Helpfulness: Would this actually help the user?
            
            Also identify specific issues and improvement suggestions.
            """, originalPrompt, response);
        
        return qualityAnalysisClient.prompt()
            .user(qualityPrompt)
            .call()
            .entity(ResponseQuality.class);
    }
    
    private String enhanceResponse(String originalResponse, String originalPrompt, ResponseQuality quality) {
        String enhancementPrompt = String.format("""
            Improve this AI response based on the identified quality issues:
            
            Original Question: %s
            Current Response: %s
            
            Quality Issues:
            %s
            
            Improvement Suggestions:
            %s
            
            Create an enhanced version that:
            - Addresses the identified issues
            - Maintains the core information
            - Improves clarity and helpfulness
            - Adds missing details if needed
            
            Return only the improved response.
            """, 
            originalPrompt,
            originalResponse,
            String.join(", ", quality.issues()),
            String.join(", ", quality.suggestions())
        );
        
        return enhancementClient.prompt()
            .user(enhancementPrompt)
            .call()
            .content();
    }
}

// Response quality tracking service
@Service
public class ResponseQualityTracker {
    
    private final MeterRegistry meterRegistry;
    private final List<QualityMetric> qualityHistory = new CopyOnWriteArrayList<>();
    
    public void recordQuality(ResponseQuality quality, Map<String, Object> context) {
        // Record quality metrics
        meterRegistry.gauge("response.quality.overall", quality.overallScore());
        meterRegistry.gauge("response.quality.clarity", quality.clarity());
        meterRegistry.gauge("response.quality.completeness", quality.completeness());
        
        // Store for trend analysis
        qualityHistory.add(new QualityMetric(
            Instant.now(),
            quality,
            extractContextInfo(context)
        ));
        
        // Keep only recent metrics
        if (qualityHistory.size() > 1000) {
            qualityHistory.removeFirst();
        }
    }
    
    public void recordImprovement(ResponseQuality original, ResponseQuality enhanced, Map<String, Object> context) {
        double improvement = enhanced.overallScore() - original.overallScore();
        
        meterRegistry.counter("response.enhancements.successful",
            "improvement_level", categorizeImprovement(improvement)).increment();
            
        log.info("Response enhanced: {} -> {} (improvement: {})", 
            original.overallScore(), enhanced.overallScore(), improvement);
    }
    
    public QualityTrends analyzeTrends() {
        if (qualityHistory.size() < 10) {
            return QualityTrends.insufficient();
        }
        
        // Analyze trends over time
        List<QualityMetric> recent = qualityHistory.stream()
            .filter(metric -> metric.timestamp().isAfter(Instant.now().minus(Duration.ofDays(7))))
            .collect(toList());
            
        double averageQuality = recent.stream()
            .mapToDouble(metric -> metric.quality().overallScore())
            .average()
            .orElse(0.0);
            
        Map<String, Double> categoryTrends = analyzeCategoryTrends(recent);
        
        return new QualityTrends(averageQuality, categoryTrends, generateRecommendations(recent));
    }
}
```
</details>

## 🎯 Real-World Scenario: Enterprise AI Gateway

**Scenario**: You're building an enterprise AI gateway that serves multiple applications and needs to:
- Handle different security levels for various departments
- Provide cost control and usage tracking
- Ensure compliance with industry regulations
- Optimize performance across different use cases
- Support A/B testing of different AI models

**Your Task**: Design a comprehensive advisor-based solution.

### Solution Architecture

```java
// Enterprise AI Gateway Configuration
@Configuration
public class EnterpriseAIGatewayConfig {
    
    @Bean
    @ConditionalOnProperty("enterprise.security.enabled")
    public EnterpriseSecurityAdvisor enterpriseSecurityAdvisor() {
        return new EnterpriseSecurityAdvisor();
    }
    
    @Bean 
    public CostControlAdvisor costControlAdvisor() {
        return new CostControlAdvisor();
    }
    
    @Bean
    public ComplianceAdvisor complianceAdvisor() {
        return new ComplianceAdvisor();
    }
    
    @Bean
    public ABTestingAdvisor abTestingAdvisor() {
        return new ABTestingAdvisor();
    }
}

// Enterprise gateway orchestrator
@Service
public class EnterpriseAIGateway {
    
    public AIResponse processRequest(EnterpriseAIRequest request) {
        // Build department-specific advisor chain
        List<RequestAdvisor> advisors = buildDepartmentAdvisorChain(request.getDepartment());
        
        // Create configured chat client
        ChatClient client = createEnterpriseClient(advisors, request.getModelPreferences());
        
        // Process with full traceability
        return processWithTraceability(client, request);
    }
}
```

## 🔗 Next Steps

Outstanding work! You've mastered the art of advisor patterns and can now build sophisticated, reusable AI interaction pipelines.

**What you've learned**:
- ✅ Custom advisor implementation patterns
- ✅ Performance optimization and caching strategies
- ✅ Security and safety advisor patterns
- ✅ Advanced advisor composition and chaining
- ✅ Enterprise-grade advisor architectures

**Ready for multi-model mastery?** 

👉 **Continue to [Tutorial S8: Multi-Model Architectures](./S8-multi-model.md)** to learn about:
- Working with multiple AI models simultaneously
- Model routing and fallback strategies
- Performance comparison and optimization
- Cost-effective model selection patterns

## 📚 Additional Resources

- 📖 [S7 Module Guide](../modules/S7-advisors.md) - Complete advisor patterns reference
- 🏗️ [Advisor Architecture](../architecture/advisor-patterns.md) - Advanced composition strategies
- ⚡ [Performance Optimization](../guides/advisor-performance.md) - Advisor optimization techniques

---

**🎉 Incredible achievement!** You now understand how to build powerful, reusable advisor systems that transform AI interactions.

The next tutorial will show you how to work with multiple AI models, implement smart routing, and build resilient multi-model architectures.

[← Previous: S6 Advanced Features](./S6-advanced-features.md) | [Next: S8 Multi-Model →](./S8-multi-model.md)