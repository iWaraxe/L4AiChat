# ⚡ Performance Guide: Optimizing Spring AI Applications

## Overview

This guide covers performance optimization strategies for Spring AI applications, from reducing latency and costs to scaling for high throughput. Performance in AI applications involves unique challenges compared to traditional web applications.

## Table of Contents
1. [Performance Fundamentals](#performance-fundamentals)
2. [Token Optimization](#token-optimization)
3. [Response Time Optimization](#response-time-optimization)
4. [Memory and Resource Management](#memory-and-resource-management)
5. [Scaling Strategies](#scaling-strategies)
6. [Cost Optimization](#cost-optimization)
7. [Monitoring and Metrics](#monitoring-and-metrics)

## Performance Fundamentals

### AI Application Performance Characteristics

Unlike traditional web applications, AI applications have unique performance patterns:

```java
// Traditional web app - predictable performance
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return userService.findById(id); // ~5-50ms
}

// AI app - variable performance
@PostMapping("/chat")
public String chat(@RequestBody String message) {
    return chatClient.prompt()
        .user(message)  // Can be 500ms to 30+ seconds
        .call()
        .content();
}
```

**Key Performance Factors:**

1. **Token Count** - More tokens = higher cost and latency
2. **Model Selection** - GPT-4 vs GPT-3.5 Turbo performance difference
3. **Provider Latency** - Network calls to external APIs
4. **Memory Management** - Conversation history growth
5. **Concurrent Requests** - Rate limiting and throttling

### Performance Metrics That Matter

```java
@Component
public class AIPerformanceMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordChatMetrics(ChatMetrics metrics) {
        // Latency - time to first token and total time
        meterRegistry.timer("ai.chat.latency.first_token",
            "model", metrics.model(),
            "complexity", metrics.complexity())
            .record(metrics.timeToFirstToken());
            
        meterRegistry.timer("ai.chat.latency.total",
            "model", metrics.model())
            .record(metrics.totalTime());
        
        // Throughput - requests per second
        meterRegistry.counter("ai.chat.requests.total",
            "model", metrics.model(),
            "status", metrics.status())
            .increment();
        
        // Token efficiency
        meterRegistry.gauge("ai.chat.tokens.per_request",
            Tags.of("model", metrics.model()),
            metrics.tokenCount());
        
        // Cost tracking
        meterRegistry.gauge("ai.chat.cost.per_request",
            Tags.of("model", metrics.model()),
            metrics.estimatedCost());
    }
}
```

## Token Optimization

### 1. Smart Memory Management

#### Sliding Window with Summarization
```java
@Service
public class OptimizedMemoryService {
    private static final int MAX_TOKENS_PER_REQUEST = 4000;
    private static final int SUMMARY_TRIGGER_SIZE = 20;
    
    public String processWithOptimizedMemory(String conversationId, String message) {
        List<Message> history = chatMemory.get(conversationId);
        
        // Check if we need to optimize
        if (calculateTokenCount(history) > MAX_TOKENS_PER_REQUEST) {
            history = optimizeHistory(conversationId, history);
        }
        
        return chatClient.prompt()
            .messages(history)
            .user(message)
            .call()
            .content();
    }
    
    private List<Message> optimizeHistory(String conversationId, List<Message> history) {
        if (history.size() <= SUMMARY_TRIGGER_SIZE) {
            return history.subList(Math.max(0, history.size() - 10), history.size());
        }
        
        // Summarize older messages
        List<Message> oldMessages = history.subList(0, history.size() / 2);
        List<Message> recentMessages = history.subList(history.size() / 2, history.size());
        
        String summary = createSummary(oldMessages);
        
        List<Message> optimized = new ArrayList<>();
        optimized.add(new SystemMessage("Previous conversation summary: " + summary));
        optimized.addAll(recentMessages);
        
        return optimized;
    }
    
    private String createSummary(List<Message> messages) {
        return chatClient.prompt()
            .system("Summarize this conversation concisely, preserving key information.")
            .messages(messages)
            .options(OpenAiChatOptions.builder()
                .withModel("gpt-3.5-turbo")  // Use cheaper model for summaries
                .withMaxTokens(200)
                .withTemperature(0.1)
                .build())
            .call()
            .content();
    }
    
    private int calculateTokenCount(List<Message> messages) {
        // Rough estimation: 1 token ≈ 4 characters for English
        return messages.stream()
            .mapToInt(msg -> msg.getContent().length() / 4)
            .sum();
    }
}
```

#### Context-Aware Window Sizing
```java
@Service
public class ContextAwareMemoryService {
    
    public int calculateOptimalWindowSize(String conversationId, TaskComplexity complexity) {
        ConversationMetrics metrics = getConversationMetrics(conversationId);
        
        return switch (complexity) {
            case SIMPLE -> Math.min(5, metrics.getTypicalExchangeLength());
            case MEDIUM -> Math.min(15, metrics.getTypicalExchangeLength() * 2);
            case COMPLEX -> Math.min(30, metrics.getTypicalExchangeLength() * 3);
            case RESEARCH -> Math.min(50, metrics.getMaxEffectiveWindow());
        };
    }
    
    public enum TaskComplexity {
        SIMPLE,    // Q&A, translations
        MEDIUM,    // Analysis, explanations
        COMPLEX,   // Multi-step reasoning
        RESEARCH   // Long-form analysis
    }
}
```

### 2. Prompt Optimization

#### Template-Based Compression
```java
@Component
public class PromptOptimizer {
    
    public String optimizePrompt(String originalPrompt, OptimizationLevel level) {
        return switch (level) {
            case MINIMAL -> compressToEssentials(originalPrompt);
            case BALANCED -> preserveKeyElements(originalPrompt);
            case FULL -> originalPrompt; // No optimization
        };
    }
    
    private String compressToEssentials(String prompt) {
        // Remove examples, verbose explanations, keep core instructions
        return prompt.replaceAll("(?i)for example[^.]*\\.", "")
                    .replaceAll("(?i)in other words[^.]*\\.", "")
                    .replaceAll("\\s+", " ")
                    .trim();
    }
    
    private String preserveKeyElements(String prompt) {
        // Keep structure but remove redundancy
        String[] sentences = prompt.split("\\.");
        List<String> essential = Arrays.stream(sentences)
            .filter(s -> isEssential(s))
            .collect(Collectors.toList());
        
        return String.join(". ", essential) + ".";
    }
    
    private boolean isEssential(String sentence) {
        String lower = sentence.toLowerCase();
        return lower.contains("must") || 
               lower.contains("should") || 
               lower.contains("format") ||
               lower.contains("return") ||
               lower.contains("include");
    }
}
```

## Response Time Optimization

### 1. Streaming for Perceived Performance

#### Intelligent Streaming with Buffering
```java
@Service
public class OptimizedStreamingService {
    
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamWithBuffering(@RequestParam String message) {
        return chatClient.prompt()
            .user(message)
            .stream()
            .content()
            .buffer(Duration.ofMillis(100)) // Collect chunks for smooth delivery
            .map(chunks -> String.join("", chunks))
            .filter(chunk -> !chunk.trim().isEmpty())
            .map(chunk -> ServerSentEvent.<String>builder()
                .data(chunk)
                .event("content")
                .build())
            .doOnSubscribe(s -> sendInitialEvent())
            .doOnComplete(() -> sendCompletionEvent());
    }
    
    private void sendInitialEvent() {
        // Send typing indicator or initial response
    }
    
    private void sendCompletionEvent() {
        // Send completion signal to client
    }
}
```

#### Progressive Enhancement
```java
@Service
public class ProgressiveResponseService {
    
    public Flux<ResponseChunk> generateProgressiveResponse(String query) {
        return Flux.concat(
            // Quick initial response
            Mono.fromCallable(() -> generateQuickResponse(query))
                .map(response -> new ResponseChunk("initial", response, false)),
            
            // Detailed follow-up
            Mono.fromCallable(() -> generateDetailedResponse(query))
                .map(response -> new ResponseChunk("detailed", response, true))
        );
    }
    
    private String generateQuickResponse(String query) {
        return chatClient.prompt()
            .system("Provide a brief, immediate answer. Be concise.")
            .user(query)
            .options(OpenAiChatOptions.builder()
                .withModel("gpt-3.5-turbo")
                .withMaxTokens(100)
                .build())
            .call()
            .content();
    }
    
    private String generateDetailedResponse(String query) {
        return chatClient.prompt()
            .system("Provide a comprehensive, detailed analysis.")
            .user(query)
            .options(OpenAiChatOptions.builder()
                .withModel("gpt-4")
                .withMaxTokens(1000)
                .build())
            .call()
            .content();
    }
    
    public record ResponseChunk(String type, String content, boolean isFinal) {}
}
```

### 2. Caching Strategies

#### Intelligent Response Caching
```java
@Service
public class ChatCacheService {
    private final Cache<String, String> responseCache;
    private final ChatClient chatClient;
    
    public ChatCacheService() {
        this.responseCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofHours(24))
            .recordStats()
            .build();
    }
    
    public String getCachedResponse(String message, String context) {
        String cacheKey = createCacheKey(message, context);
        
        return responseCache.get(cacheKey, key -> {
            return chatClient.prompt()
                .user(message)
                .call()
                .content();
        });
    }
    
    private String createCacheKey(String message, String context) {
        // Normalize message for better cache hits
        String normalized = normalizeMessage(message);
        return DigestUtils.sha256Hex(normalized + "|" + context);
    }
    
    private String normalizeMessage(String message) {
        return message.toLowerCase()
            .replaceAll("\\s+", " ")
            .replaceAll("[^a-zA-Z0-9\\s]", "")
            .trim();
    }
    
    @EventListener
    public void onCacheStats(CacheStatsEvent event) {
        CacheStats stats = responseCache.stats();
        logger.info("Cache stats - Hit rate: {}, Evictions: {}", 
                   stats.hitRate(), stats.evictionCount());
    }
}
```

#### Semantic Caching
```java
@Service
public class SemanticCacheService {
    private final VectorStore vectorStore;
    private final EmbeddingClient embeddingClient;
    private static final double SIMILARITY_THRESHOLD = 0.95;
    
    public Optional<String> findSimilarResponse(String query) {
        List<float[]> queryEmbedding = embeddingClient.embed(List.of(query));
        
        List<Document> similar = vectorStore.similaritySearch(
            SearchRequest.query(query)
                .withSimilarityThreshold(SIMILARITY_THRESHOLD)
                .withTopK(1)
        );
        
        if (!similar.isEmpty()) {
            Document match = similar.get(0);
            logger.info("Cache hit for query: {} (similarity: {})", 
                       query, match.getMetadata().get("similarity"));
            return Optional.of(match.getContent());
        }
        
        return Optional.empty();
    }
    
    public void cacheResponse(String query, String response) {
        vectorStore.add(List.of(
            new Document(response, Map.of(
                "query", query,
                "timestamp", Instant.now().toString(),
                "type", "cached_response"
            ))
        ));
    }
}
```

## Memory and Resource Management

### 1. JVM Optimization

#### Memory Configuration
```yaml
# application-performance.yml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
        
server:
  tomcat:
    threads:
      max: 200
      min-spare: 10
    connection-timeout: 20000
    
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

```bash
# JVM optimization flags
JAVA_OPTS="-Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/tmp/heap-dumps"
```

#### Connection Pool Optimization
```java
@Configuration
public class DatabaseOptimizationConfig {
    
    @Bean
    public HikariConfig hikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        return config;
    }
}
```

### 2. Conversation Memory Optimization

#### Adaptive Memory Sizing
```java
@Service
public class AdaptiveMemoryService {
    private final Map<String, ConversationProfile> profiles = new ConcurrentHashMap<>();
    
    public int getOptimalWindowSize(String conversationId) {
        ConversationProfile profile = profiles.computeIfAbsent(conversationId, 
            id -> new ConversationProfile());
        
        // Adapt based on conversation patterns
        if (profile.hasLongMessages()) {
            return Math.min(10, profile.getAverageResponseLength() > 500 ? 8 : 10);
        } else if (profile.hasComplexQueries()) {
            return 20;
        } else {
            return 15; // Default
        }
    }
    
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void optimizeMemoryUsage() {
        long threshold = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(6);
        
        profiles.entrySet().removeIf(entry -> {
            ConversationProfile profile = entry.getValue();
            return profile.getLastActivity() < threshold;
        });
        
        logger.info("Memory optimization completed. Active conversations: {}", 
                   profiles.size());
    }
    
    private static class ConversationProfile {
        private long lastActivity = System.currentTimeMillis();
        private List<Integer> messageLengths = new ArrayList<>();
        private int complexQueryCount = 0;
        
        public boolean hasLongMessages() {
            return messageLengths.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0) > 200;
        }
        
        public boolean hasComplexQueries() {
            return complexQueryCount > 3;
        }
        
        public double getAverageResponseLength() {
            return messageLengths.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
        }
        
        // Other methods...
    }
}
```

## Scaling Strategies

### 1. Horizontal Scaling

#### Load Balancing Configuration
```yaml
# nginx.conf for AI application load balancing
upstream ai_backend {
    least_conn;
    server app1:8080 max_fails=3 fail_timeout=30s;
    server app2:8080 max_fails=3 fail_timeout=30s;
    server app3:8080 max_fails=3 fail_timeout=30s;
}

server {
    location /api/chat {
        proxy_pass http://ai_backend;
        proxy_set_header X-Conversation-ID $http_x_conversation_id;
        proxy_read_timeout 300s;  # Long timeout for AI responses
        proxy_connect_timeout 60s;
    }
}
```

#### Sticky Sessions for Stateful Conversations
```java
@Configuration
public class SessionConfiguration {
    
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("CHAT_SESSION");
        serializer.setCookiePath("/api/chat");
        serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        return serializer;
    }
    
    @Bean
    public SessionRepository<MapSession> sessionRepository() {
        return new MapSessionRepository(new ConcurrentHashMap<>());
    }
}
```

### 2. Asynchronous Processing

#### Queue-Based Chat Processing
```java
@Service
public class AsyncChatService {
    private final RabbitTemplate rabbitTemplate;
    private final ChatResponseRepository responseRepository;
    
    public String submitChatRequest(String conversationId, String message) {
        String requestId = UUID.randomUUID().toString();
        
        ChatRequest request = new ChatRequest(requestId, conversationId, message);
        rabbitTemplate.convertAndSend("chat.requests", request);
        
        return requestId; // Return immediately
    }
    
    @RabbitListener(queues = "chat.requests")
    public void processChatRequest(ChatRequest request) {
        try {
            String response = chatClient.prompt()
                .user(request.getMessage())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, request.getConversationId()))
                .call()
                .content();
            
            ChatResponse chatResponse = new ChatResponse(
                request.getRequestId(), 
                response, 
                ChatResponseStatus.COMPLETED
            );
            
            responseRepository.save(chatResponse);
            notifyClient(request.getRequestId(), response);
            
        } catch (Exception e) {
            handleProcessingError(request, e);
        }
    }
    
    private void notifyClient(String requestId, String response) {
        // WebSocket or Server-Sent Events notification
        messagingTemplate.convertAndSend("/topic/chat/" + requestId, response);
    }
}
```

### 3. Circuit Breaker Pattern

#### AI Provider Circuit Breaker
```java
@Component
public class ResilientChatService {
    private final CircuitBreaker primaryCircuitBreaker;
    private final CircuitBreaker fallbackCircuitBreaker;
    private final ChatClient primaryClient;
    private final ChatClient fallbackClient;
    
    public ResilientChatService() {
        this.primaryCircuitBreaker = CircuitBreaker.ofDefaults("primary-ai");
        this.fallbackCircuitBreaker = CircuitBreaker.ofDefaults("fallback-ai");
        
        primaryCircuitBreaker.getEventPublisher()
            .onStateTransition(event -> 
                logger.warn("Circuit breaker state transition: {}", event));
    }
    
    public String processWithFallback(String message) {
        return primaryCircuitBreaker.executeSupplier(() -> {
            return primaryClient.prompt()
                .user(message)
                .options(OpenAiChatOptions.builder()
                    .withTimeout(Duration.ofSeconds(30))
                    .build())
                .call()
                .content();
        }).recover(throwable -> {
            logger.warn("Primary AI failed, using fallback: {}", throwable.getMessage());
            
            return fallbackCircuitBreaker.executeSupplier(() -> {
                return fallbackClient.prompt()
                    .user(message)
                    .options(OpenAiChatOptions.builder()
                        .withModel("gpt-3.5-turbo") // Faster, more reliable
                        .withTimeout(Duration.ofSeconds(15))
                        .build())
                    .call()
                    .content();
            });
        });
    }
}
```

## Cost Optimization

### 1. Model Selection Strategy

#### Cost-Aware Model Router
```java
@Service
public class CostOptimizedModelRouter {
    private final Map<String, ModelCostProfile> costProfiles;
    
    public CostOptimizedModelRouter() {
        this.costProfiles = Map.of(
            "gpt-4", new ModelCostProfile(0.06, 0.12, 95), // input, output, quality
            "gpt-4-turbo", new ModelCostProfile(0.03, 0.06, 90),
            "gpt-3.5-turbo", new ModelCostProfile(0.002, 0.002, 75)
        );
    }
    
    public String selectOptimalModel(TaskRequirements requirements) {
        if (requirements.getQualityThreshold() < 80) {
            return "gpt-3.5-turbo";
        }
        
        if (requirements.getBudgetConstraint() == BudgetConstraint.STRICT) {
            return requirements.getQualityThreshold() > 85 ? "gpt-4-turbo" : "gpt-3.5-turbo";
        }
        
        return "gpt-4";
    }
    
    public double estimateCost(String model, int inputTokens, int outputTokens) {
        ModelCostProfile profile = costProfiles.get(model);
        return (inputTokens * profile.inputCostPer1K() / 1000.0) +
               (outputTokens * profile.outputCostPer1K() / 1000.0);
    }
    
    record ModelCostProfile(double inputCostPer1K, double outputCostPer1K, int qualityScore) {}
}
```

### 2. Token Budget Management

#### Request Budgeting
```java
@Service
public class TokenBudgetService {
    private final Map<String, TokenBudget> userBudgets = new ConcurrentHashMap<>();
    
    public boolean checkBudget(String userId, int estimatedTokens) {
        TokenBudget budget = userBudgets.computeIfAbsent(userId, 
            id -> new TokenBudget(10000, Period.ofDays(1))); // 10K tokens per day
        
        return budget.canSpend(estimatedTokens);
    }
    
    public void recordUsage(String userId, int actualTokens, double cost) {
        TokenBudget budget = userBudgets.get(userId);
        if (budget != null) {
            budget.recordUsage(actualTokens, cost);
        }
    }
    
    @Scheduled(cron = "0 0 0 * * *") // Daily reset
    public void resetDailyBudgets() {
        userBudgets.values().forEach(TokenBudget::resetIfExpired);
    }
    
    private static class TokenBudget {
        private int remainingTokens;
        private double remainingCost;
        private final int dailyTokenLimit;
        private final double dailyCostLimit;
        private LocalDate lastReset;
        
        public TokenBudget(int dailyTokenLimit, Period resetPeriod) {
            this.dailyTokenLimit = dailyTokenLimit;
            this.dailyCostLimit = 10.0; // $10 per day
            this.remainingTokens = dailyTokenLimit;
            this.remainingCost = dailyCostLimit;
            this.lastReset = LocalDate.now();
        }
        
        public boolean canSpend(int tokens) {
            resetIfExpired();
            return remainingTokens >= tokens;
        }
        
        public void recordUsage(int tokens, double cost) {
            this.remainingTokens -= tokens;
            this.remainingCost -= cost;
        }
        
        public void resetIfExpired() {
            if (LocalDate.now().isAfter(lastReset)) {
                this.remainingTokens = dailyTokenLimit;
                this.remainingCost = dailyCostLimit;
                this.lastReset = LocalDate.now();
            }
        }
    }
}
```

## Monitoring and Metrics

### 1. Performance Dashboards

#### Key Metrics Collection
```java
@Component
public class AIPerformanceDashboard {
    private final MeterRegistry meterRegistry;
    
    @EventListener
    public void recordChatMetrics(ChatCompletedEvent event) {
        // Response time percentiles
        meterRegistry.timer("ai.chat.response_time",
            "model", event.getModel(),
            "complexity", event.getComplexity())
            .record(event.getDuration());
        
        // Token efficiency
        meterRegistry.gauge("ai.chat.tokens_per_character",
            Tags.of("model", event.getModel()),
            event.getTokenCount() / (double) event.getCharacterCount());
        
        // Cost efficiency
        meterRegistry.gauge("ai.chat.cost_per_request",
            Tags.of("model", event.getModel()),
            event.getEstimatedCost());
        
        // Quality metrics (if available)
        if (event.getQualityScore() != null) {
            meterRegistry.gauge("ai.chat.quality_score",
                Tags.of("model", event.getModel()),
                event.getQualityScore());
        }
    }
    
    @Scheduled(fixedDelay = 60000) // Every minute
    public void updateDashboardMetrics() {
        // System resource usage
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        meterRegistry.gauge("jvm.memory.used", usedMemory);
        meterRegistry.gauge("jvm.memory.free", freeMemory);
        
        // Active conversation count
        meterRegistry.gauge("ai.conversations.active", getActiveConversationCount());
        
        // Cache statistics
        meterRegistry.gauge("ai.cache.hit_rate", getCacheHitRate());
    }
}
```

### 2. Performance Alerting

#### Alert Configuration
```java
@Component
public class PerformanceAlerting {
    private final NotificationService notificationService;
    
    @EventListener
    public void checkPerformanceThresholds(ChatMetricsEvent event) {
        // High latency alert
        if (event.getResponseTime().toSeconds() > 30) {
            notificationService.sendAlert(Alert.builder()
                .severity(Severity.WARNING)
                .message("High AI response latency: " + event.getResponseTime())
                .context(Map.of(
                    "model", event.getModel(),
                    "conversationId", event.getConversationId(),
                    "responseTime", event.getResponseTime().toString()
                ))
                .build());
        }
        
        // High cost alert
        if (event.getEstimatedCost() > 1.0) {
            notificationService.sendAlert(Alert.builder()
                .severity(Severity.HIGH)
                .message("High cost request: $" + event.getEstimatedCost())
                .context(Map.of(
                    "model", event.getModel(),
                    "tokenCount", event.getTokenCount(),
                    "cost", event.getEstimatedCost()
                ))
                .build());
        }
    }
    
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void checkSystemHealth() {
        double errorRate = getErrorRate();
        if (errorRate > 0.05) { // 5% error rate threshold
            notificationService.sendAlert(Alert.builder()
                .severity(Severity.CRITICAL)
                .message("High AI error rate: " + (errorRate * 100) + "%")
                .build());
        }
    }
}
```

## Performance Testing

### 1. Load Testing
```java
@SpringBootTest
class AIPerformanceTest {
    
    @Test
    void loadTestChatEndpoint() throws Exception {
        int numberOfThreads = 10;
        int requestsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (int i = 0; i < numberOfThreads; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        long start = System.currentTimeMillis();
                        
                        mockMvc.perform(post("/api/chat/new")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"message\": \"Test message " + j + "\"}"))
                                .andExpect(status().isOk());
                        
                        long duration = System.currentTimeMillis() - start;
                        assertThat(duration).isLessThan(5000); // 5 second SLA
                    }
                } catch (Exception e) {
                    fail("Load test failed", e);
                } finally {
                    latch.countDown();
                }
            }, executor);
            
            futures.add(future);
        }
        
        latch.await(10, TimeUnit.MINUTES);
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
```

## Key Takeaways

1. **Token optimization** is crucial for both performance and cost
2. **Streaming responses** dramatically improve perceived performance
3. **Intelligent caching** can reduce API calls by 30-70%
4. **Model selection** significantly impacts both speed and cost
5. **Memory management** prevents resource leaks in long-running applications
6. **Circuit breakers** ensure system resilience
7. **Comprehensive monitoring** is essential for optimization

## Next Steps

- 📄 [Production Guide](../guides/production.md) - Deployment strategies
- 📄 [Testing Guide](../guides/testing.md) - Testing strategies
- 🏗️ [Architecture Patterns](patterns.md) - Design decisions

---

[← Technology Choices](technology-choices.md) | [Back to Architecture](../README.md#architecture--design) | [Production Guide →](../guides/production.md)