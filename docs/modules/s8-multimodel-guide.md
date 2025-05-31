# 📄 Module S8: Multi-Model Architecture Guide

## Overview

This module demonstrates how to work with multiple AI models within a single application, comparing their capabilities, routing requests intelligently, and building resilient multi-provider architectures. You'll learn to leverage the strengths of different models for optimal results.

## Learning Objectives

By completing this module, you will:
- ✅ Configure multiple ChatClient instances with different models
- ✅ Implement intelligent model routing based on task requirements
- ✅ Build model comparison and A/B testing systems
- ✅ Design fallback strategies for resilience
- ✅ Optimize costs through strategic model selection

## Why Multi-Model Architecture?

Different models excel at different tasks:

| Model | Best For | Strengths | Cost |
|-------|----------|-----------|------|
| GPT-4 Turbo | Complex analysis, reasoning | Accuracy, depth | High |
| GPT-3.5 Turbo | Quick responses, simple tasks | Speed, cost-effective | Low |
| GPT-4 | Creative writing, detailed work | Quality, creativity | Very High |
| Claude-3 | Code analysis, structured data | Precision, reliability | Medium |

**Single Model Approach** (limited):
```java
// Always uses the same model regardless of task
String response = chatClient.prompt().user(message).call().content();
```

**Multi-Model Approach** (optimized):
```java
// Choose the best model for each task
ChatClient model = modelRouter.selectOptimalModel(task);
String response = model.prompt().user(message).call().content();
```

## Module Structure

```
s8multimodel/
├── MultiModelApplication.java           # Main application
├── config/
│   └── MultiModelConfiguration.java    # Model configurations
├── controller/
│   └── MultiModelController.java       # Comparison endpoints
├── service/
│   ├── MultiModelService.java          # Core multi-model logic
│   ├── ModelRouter.java                # Intelligent routing
│   └── ModelComparator.java            # A/B testing service
├── model/
│   ├── ModelCapability.java            # Model capability definitions
│   ├── TaskType.java                   # Task classification
│   └── ComparisonResult.java           # Comparison results
└── strategy/
    ├── CostOptimizedStrategy.java      # Cost-focused routing
    ├── QualityFirstStrategy.java       # Quality-focused routing
    └── BalancedStrategy.java            # Balanced approach
```

## Multi-Model Configuration

### 1. Model Setup and Configuration
```java
@Configuration
public class MultiModelConfiguration {
    
    @Bean
    @Qualifier("gpt4")
    public ChatClient gpt4Client(ChatClient.Builder builder) {
        return builder.clone()
            .defaultOptions(OpenAiChatOptions.builder()
                .withModel("gpt-4")
                .withTemperature(0.7)
                .withMaxTokens(2000)
                .build())
            .build();
    }
    
    @Bean
    @Qualifier("gpt4turbo")
    public ChatClient gpt4TurboClient(ChatClient.Builder builder) {
        return builder.clone()
            .defaultOptions(OpenAiChatOptions.builder()
                .withModel("gpt-4-turbo")
                .withTemperature(0.7)
                .withMaxTokens(4000)
                .build())
            .build();
    }
    
    @Bean
    @Qualifier("gpt35turbo")
    public ChatClient gpt35TurboClient(ChatClient.Builder builder) {
        return builder.clone()
            .defaultOptions(OpenAiChatOptions.builder()
                .withModel("gpt-3.5-turbo")
                .withTemperature(0.7)
                .withMaxTokens(1000)
                .build())
            .build();
    }
    
    @Bean
    @Qualifier("claude3")
    public ChatClient claude3Client(ChatClient.Builder builder) {
        // Note: This would require Anthropic configuration
        return builder.clone()
            .defaultOptions(OpenAiChatOptions.builder()
                .withModel("claude-3-sonnet")
                .withTemperature(0.7)
                .withMaxTokens(2000)
                .build())
            .build();
    }
    
    @Bean
    public ModelCapabilityRegistry modelCapabilityRegistry() {
        return ModelCapabilityRegistry.builder()
            .register("gpt-4", ModelCapability.builder()
                .strength(TaskType.ANALYSIS, 95)
                .strength(TaskType.CREATIVE_WRITING, 90)
                .strength(TaskType.CODE_GENERATION, 85)
                .strength(TaskType.SIMPLE_QA, 80)
                .costPerToken(0.06)
                .responseTime(Duration.ofSeconds(15))
                .build())
            .register("gpt-4-turbo", ModelCapability.builder()
                .strength(TaskType.ANALYSIS, 92)
                .strength(TaskType.CREATIVE_WRITING, 88)
                .strength(TaskType.CODE_GENERATION, 90)
                .strength(TaskType.SIMPLE_QA, 85)
                .costPerToken(0.03)
                .responseTime(Duration.ofSeconds(8))
                .build())
            .register("gpt-3.5-turbo", ModelCapability.builder()
                .strength(TaskType.ANALYSIS, 75)
                .strength(TaskType.CREATIVE_WRITING, 70)
                .strength(TaskType.CODE_GENERATION, 80)
                .strength(TaskType.SIMPLE_QA, 90)
                .costPerToken(0.002)
                .responseTime(Duration.ofSeconds(3))
                .build())
            .build();
    }
}
```

### 2. Task Classification System
```java
public enum TaskType {
    SIMPLE_QA("Simple question answering"),
    ANALYSIS("Complex analysis and reasoning"),
    CREATIVE_WRITING("Creative content generation"),
    CODE_GENERATION("Programming and code tasks"),
    TRANSLATION("Language translation"),
    SUMMARIZATION("Content summarization"),
    MATH_PROBLEM("Mathematical computations"),
    STRUCTURED_DATA("Data extraction and formatting");
    
    private final String description;
    
    TaskType(String description) {
        this.description = description;
    }
}

@Service
public class TaskClassificationService {
    
    public TaskType classifyTask(String userMessage) {
        String message = userMessage.toLowerCase();
        
        // Code-related keywords
        if (containsAny(message, "code", "program", "function", "class", "algorithm", "debug")) {
            return TaskType.CODE_GENERATION;
        }
        
        // Analysis keywords
        if (containsAny(message, "analyze", "compare", "evaluate", "assess", "examine", "strategy")) {
            return TaskType.ANALYSIS;
        }
        
        // Creative keywords
        if (containsAny(message, "write", "story", "poem", "creative", "imagine", "invent")) {
            return TaskType.CREATIVE_WRITING;
        }
        
        // Translation keywords
        if (containsAny(message, "translate", "convert to", "in spanish", "in french", "language")) {
            return TaskType.TRANSLATION;
        }
        
        // Math keywords
        if (containsAny(message, "calculate", "solve", "equation", "formula", "mathematics")) {
            return TaskType.MATH_PROBLEM;
        }
        
        // Summarization keywords
        if (containsAny(message, "summarize", "summary", "key points", "brief", "overview")) {
            return TaskType.SUMMARIZATION;
        }
        
        // Data extraction keywords
        if (containsAny(message, "extract", "format", "structure", "json", "table", "list")) {
            return TaskType.STRUCTURED_DATA;
        }
        
        // Default to simple Q&A
        return TaskType.SIMPLE_QA;
    }
    
    private boolean containsAny(String text, String... keywords) {
        return Arrays.stream(keywords).anyMatch(text::contains);
    }
}
```

## Intelligent Model Routing

### 1. Model Router Implementation
```java
@Service
public class ModelRouter {
    private final Map<String, ChatClient> modelClients;
    private final ModelCapabilityRegistry capabilityRegistry;
    private final TaskClassificationService taskClassifier;
    private final ModelSelectionStrategy strategy;
    
    public ModelRouter(Map<String, ChatClient> modelClients,
                      ModelCapabilityRegistry capabilityRegistry,
                      TaskClassificationService taskClassifier,
                      @Qualifier("balanced") ModelSelectionStrategy strategy) {
        this.modelClients = modelClients;
        this.capabilityRegistry = capabilityRegistry;
        this.taskClassifier = taskClassifier;
        this.strategy = strategy;
    }
    
    public ChatClient selectOptimalModel(String userMessage) {
        TaskType taskType = taskClassifier.classifyTask(userMessage);
        return selectOptimalModel(taskType, new SelectionCriteria());
    }
    
    public ChatClient selectOptimalModel(TaskType taskType, SelectionCriteria criteria) {
        String selectedModel = strategy.selectModel(taskType, criteria, capabilityRegistry);
        
        ChatClient client = modelClients.get(selectedModel);
        if (client == null) {
            logger.warn("Model {} not available, falling back to default", selectedModel);
            return modelClients.get("gpt-3.5-turbo"); // Safe fallback
        }
        
        return client;
    }
    
    public CompletableFuture<ModelPerformanceResult> routeWithPerformanceTracking(
            String userMessage, TaskType taskType) {
        
        long startTime = System.currentTimeMillis();
        ChatClient selectedClient = selectOptimalModel(taskType, new SelectionCriteria());
        String selectedModel = getModelName(selectedClient);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                String response = selectedClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();
                
                long duration = System.currentTimeMillis() - startTime;
                
                return new ModelPerformanceResult(
                    selectedModel,
                    response,
                    duration,
                    taskType,
                    true,
                    null
                );
                
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                return new ModelPerformanceResult(
                    selectedModel,
                    null,
                    duration,
                    taskType,
                    false,
                    e.getMessage()
                );
            }
        });
    }
}
```

### 2. Model Selection Strategies

#### Cost-Optimized Strategy
```java
@Component
@Qualifier("cost-optimized")
public class CostOptimizedStrategy implements ModelSelectionStrategy {
    private static final Logger logger = LoggerFactory.getLogger(CostOptimizedStrategy.class);
    
    @Override
    public String selectModel(TaskType taskType, SelectionCriteria criteria, 
                             ModelCapabilityRegistry registry) {
        
        // Get all models that can handle this task adequately
        List<String> suitableModels = registry.getModelsWithMinimumCapability(taskType, 70);
        
        if (suitableModels.isEmpty()) {
            logger.warn("No suitable models found for task: {}", taskType);
            return "gpt-3.5-turbo"; // Cheapest fallback
        }
        
        // Sort by cost per token (ascending)
        return suitableModels.stream()
            .min(Comparator.comparing(model -> registry.getCostPerToken(model)))
            .orElse("gpt-3.5-turbo");
    }
}
```

#### Quality-First Strategy
```java
@Component
@Qualifier("quality-first")
public class QualityFirstStrategy implements ModelSelectionStrategy {
    
    @Override
    public String selectModel(TaskType taskType, SelectionCriteria criteria, 
                             ModelCapabilityRegistry registry) {
        
        // Always choose the model with highest capability for the task
        return registry.getAllModels().stream()
            .max(Comparator.comparing(model -> registry.getCapability(model, taskType)))
            .orElse("gpt-4"); // Best quality fallback
    }
}
```

#### Balanced Strategy
```java
@Component
@Qualifier("balanced")
public class BalancedStrategy implements ModelSelectionStrategy {
    
    @Override
    public String selectModel(TaskType taskType, SelectionCriteria criteria, 
                             ModelCapabilityRegistry registry) {
        
        // Calculate value score: capability / cost
        return registry.getAllModels().stream()
            .max(Comparator.comparing(model -> calculateValueScore(model, taskType, registry)))
            .orElse("gpt-4-turbo"); // Balanced fallback
    }
    
    private double calculateValueScore(String model, TaskType taskType, 
                                     ModelCapabilityRegistry registry) {
        double capability = registry.getCapability(model, taskType);
        double cost = registry.getCostPerToken(model);
        double responseTime = registry.getAverageResponseTime(model).toSeconds();
        
        // Value = capability / (cost weight * cost + time weight * time)
        double costWeight = 0.6;
        double timeWeight = 0.4;
        
        return capability / (costWeight * cost * 1000 + timeWeight * responseTime);
    }
}
```

## Model Comparison and A/B Testing

### 1. Parallel Model Comparison
```java
@Service
public class ModelComparator {
    private final Map<String, ChatClient> modelClients;
    private final ExecutorService executorService;
    
    public ModelComparator(Map<String, ChatClient> modelClients) {
        this.modelClients = modelClients;
        this.executorService = Executors.newFixedThreadPool(5);
    }
    
    public CompletableFuture<ComparisonResult> compareModels(String userMessage, 
                                                           List<String> modelNames) {
        
        List<CompletableFuture<ModelResponse>> futures = modelNames.stream()
            .map(modelName -> processWithModel(userMessage, modelName))
            .collect(Collectors.toList());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                List<ModelResponse> responses = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
                
                return new ComparisonResult(userMessage, responses, analyzeResponses(responses));
            });
    }
    
    private CompletableFuture<ModelResponse> processWithModel(String message, String modelName) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                ChatClient client = modelClients.get(modelName);
                if (client == null) {
                    throw new IllegalArgumentException("Model not found: " + modelName);
                }
                
                String response = client.prompt()
                    .user(message)
                    .call()
                    .content();
                
                long duration = System.currentTimeMillis() - startTime;
                
                return new ModelResponse(
                    modelName,
                    response,
                    duration,
                    response.length(),
                    true,
                    null
                );
                
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                return new ModelResponse(
                    modelName,
                    null,
                    duration,
                    0,
                    false,
                    e.getMessage()
                );
            }
        }, executorService);
    }
    
    private ComparisonAnalysis analyzeResponses(List<ModelResponse> responses) {
        List<ModelResponse> successful = responses.stream()
            .filter(ModelResponse::isSuccessful)
            .collect(Collectors.toList());
        
        if (successful.isEmpty()) {
            return new ComparisonAnalysis("All models failed", null, null, null);
        }
        
        // Find fastest response
        ModelResponse fastest = successful.stream()
            .min(Comparator.comparing(ModelResponse::duration))
            .orElse(null);
        
        // Find most detailed response
        ModelResponse mostDetailed = successful.stream()
            .max(Comparator.comparing(ModelResponse::responseLength))
            .orElse(null);
        
        // Calculate average response time
        double avgResponseTime = successful.stream()
            .mapToLong(ModelResponse::duration)
            .average()
            .orElse(0.0);
        
        return new ComparisonAnalysis(
            "Comparison completed",
            fastest,
            mostDetailed,
            avgResponseTime
        );
    }
}
```

### 2. A/B Testing Framework
```java
@Service
public class ModelABTestingService {
    private final ModelComparator modelComparator;
    private final ABTestMetricsService metricsService;
    
    public ABTestingService(ModelComparator modelComparator, ABTestMetricsService metricsService) {
        this.modelComparator = modelComparator;
        this.metricsService = metricsService;
    }
    
    public CompletableFuture<ABTestResult> runABTest(String userMessage, 
                                                    String modelA, 
                                                    String modelB,
                                                    String testId) {
        
        return modelComparator.compareModels(userMessage, List.of(modelA, modelB))
            .thenApply(comparison -> {
                ABTestResult result = analyzeABTest(comparison, modelA, modelB, testId);
                metricsService.recordABTestResult(result);
                return result;
            });
    }
    
    private ABTestResult analyzeABTest(ComparisonResult comparison, 
                                      String modelA, String modelB, String testId) {
        
        ModelResponse responseA = findResponseByModel(comparison, modelA);
        ModelResponse responseB = findResponseByModel(comparison, modelB);
        
        if (responseA == null || responseB == null) {
            return new ABTestResult(testId, modelA, modelB, "INCONCLUSIVE", 
                                  "One or both models failed to respond");
        }
        
        // Compare based on multiple criteria
        ABTestScores scores = calculateABTestScores(responseA, responseB);
        String winner = determineWinner(scores);
        String reasoning = buildReasoning(scores);
        
        return new ABTestResult(testId, modelA, modelB, winner, reasoning, scores);
    }
    
    private ABTestScores calculateABTestScores(ModelResponse responseA, ModelResponse responseB) {
        // Speed score (faster is better)
        double speedScoreA = responseB.duration() / (double) responseA.duration();
        double speedScoreB = responseA.duration() / (double) responseB.duration();
        
        // Detail score (more comprehensive response is better)
        double detailScoreA = responseA.responseLength() / (double) Math.max(responseB.responseLength(), 1);
        double detailScoreB = responseB.responseLength() / (double) Math.max(responseA.responseLength(), 1);
        
        // Quality score (would need NLP analysis or user feedback)
        double qualityScoreA = calculateQualityScore(responseA.content());
        double qualityScoreB = calculateQualityScore(responseB.content());
        
        return new ABTestScores(
            speedScoreA, speedScoreB,
            detailScoreA, detailScoreB,
            qualityScoreA, qualityScoreB
        );
    }
    
    private double calculateQualityScore(String content) {
        // Simple quality heuristics
        if (content == null || content.trim().isEmpty()) {
            return 0.0;
        }
        
        double score = 0.5; // Base score
        
        // Bonus for structure
        if (content.contains("\n\n") || content.contains("1.") || content.contains("•")) {
            score += 0.2;
        }
        
        // Bonus for detailed explanations
        if (content.length() > 200) {
            score += 0.2;
        }
        
        // Bonus for examples
        if (content.toLowerCase().contains("example") || content.toLowerCase().contains("for instance")) {
            score += 0.1;
        }
        
        return Math.min(1.0, score);
    }
}
```

## Resilient Multi-Model Patterns

### 1. Fallback Chain Pattern
```java
@Service
public class ResilientMultiModelService {
    private final List<ChatClient> modelChain;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    
    public ResilientMultiModelService(List<ChatClient> modelChain, 
                                    CircuitBreakerRegistry circuitBreakerRegistry) {
        this.modelChain = modelChain;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }
    
    public String processWithFallback(String userMessage, Duration timeout) {
        Exception lastException = null;
        
        for (int i = 0; i < modelChain.size(); i++) {
            ChatClient client = modelChain.get(i);
            String modelName = getModelName(client);
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(modelName);
            
            try {
                return circuitBreaker.executeSupplier(() -> {
                    return processWithTimeout(client, userMessage, timeout);
                });
                
            } catch (Exception e) {
                lastException = e;
                logger.warn("Model {} failed, trying next in chain. Error: {}", 
                           modelName, e.getMessage());
                
                // If this is the last model, throw the exception
                if (i == modelChain.size() - 1) {
                    break;
                }
            }
        }
        
        // All models failed
        throw new AllModelsFailedException("All models in chain failed", lastException);
    }
    
    private String processWithTimeout(ChatClient client, String message, Duration timeout) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return client.prompt().user(message).call().content();
        });
        
        try {
            return future.get(timeout.toSeconds(), TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new ModelTimeoutException("Model response timeout", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new ModelExecutionException("Model execution failed", e);
        }
    }
}
```

### 2. Load Balancing Pattern
```java
@Service
public class LoadBalancedModelService {
    private final List<ChatClient> modelPool;
    private final AtomicInteger roundRobinCounter;
    private final Map<String, AtomicInteger> modelLoadCounters;
    
    public LoadBalancedModelService(List<ChatClient> modelPool) {
        this.modelPool = modelPool;
        this.roundRobinCounter = new AtomicInteger(0);
        this.modelLoadCounters = modelPool.stream()
            .collect(Collectors.toMap(
                this::getModelName,
                client -> new AtomicInteger(0)
            ));
    }
    
    public String processWithLoadBalancing(String userMessage, LoadBalancingStrategy strategy) {
        ChatClient selectedClient = switch (strategy) {
            case ROUND_ROBIN -> selectRoundRobin();
            case LEAST_LOADED -> selectLeastLoaded();
            case RANDOM -> selectRandom();
        };
        
        String modelName = getModelName(selectedClient);
        AtomicInteger loadCounter = modelLoadCounters.get(modelName);
        
        try {
            loadCounter.incrementAndGet();
            return selectedClient.prompt().user(userMessage).call().content();
        } finally {
            loadCounter.decrementAndGet();
        }
    }
    
    private ChatClient selectRoundRobin() {
        int index = roundRobinCounter.getAndIncrement() % modelPool.size();
        return modelPool.get(index);
    }
    
    private ChatClient selectLeastLoaded() {
        return modelLoadCounters.entrySet().stream()
            .min(Comparator.comparing(entry -> entry.getValue().get()))
            .map(entry -> findClientByModelName(entry.getKey()))
            .orElse(modelPool.get(0));
    }
    
    private ChatClient selectRandom() {
        return modelPool.get(ThreadLocalRandom.current().nextInt(modelPool.size()));
    }
}
```

## Testing Multi-Model Systems

### 1. Model Comparison Testing
```java
@Test
void testModelComparison() {
    // Given
    String testMessage = "Explain quantum computing in simple terms";
    List<String> models = List.of("gpt-4", "gpt-3.5-turbo");
    
    // When
    ComparisonResult result = modelComparator.compareModels(testMessage, models)
        .join();
    
    // Then
    assertThat(result.getResponses()).hasSize(2);
    assertThat(result.getAnalysis()).isNotNull();
    
    // Verify all models responded
    result.getResponses().forEach(response -> {
        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.content()).isNotBlank();
    });
}
```

### 2. Fallback Testing
```java
@Test
void testFallbackChain() {
    // Given - first model will fail, second should succeed
    when(primaryClient.prompt()).thenThrow(new RuntimeException("API Error"));
    when(fallbackClient.prompt()).thenReturn(mockPromptSpec);
    when(mockPromptSpec.user(anyString())).thenReturn(mockPromptSpec);
    when(mockPromptSpec.call()).thenReturn(mockCallResponse);
    when(mockCallResponse.content()).thenReturn("Fallback response");
    
    List<ChatClient> chain = List.of(primaryClient, fallbackClient);
    ResilientMultiModelService service = new ResilientMultiModelService(chain, circuitBreakerRegistry);
    
    // When
    String result = service.processWithFallback("test message", Duration.ofSeconds(30));
    
    // Then
    assertThat(result).isEqualTo("Fallback response");
    verify(primaryClient).prompt();
    verify(fallbackClient).prompt();
}
```

## Key Takeaways

1. **Different models excel at different tasks** - route intelligently
2. **Cost optimization** is crucial for production deployments
3. **Fallback strategies** ensure system resilience
4. **A/B testing** helps validate model selection decisions
5. **Performance monitoring** across models guides optimization
6. **Load balancing** improves overall system performance

## What's Next?

Ready to master prompt templates? Continue to:
- 📄 [S9: Templates](s9-templates-guide.md) - Advanced prompt patterns
- 🏗️ [Performance Guide](../architecture/performance.md) - System optimization
- 📄 [Production Guide](../guides/production.md) - Deployment strategies

---

[← S7: Advisors](s7-advisors-guide.md) | [Back to Modules](../README.md#module-guides) | [S9: Templates →](s9-templates-guide.md)