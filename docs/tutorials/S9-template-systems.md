# 📝 Tutorial S9: Template Systems & Dynamic Prompt Engineering

> **⏱️ Duration**: 60 minutes  
> **🎯 Difficulty**: 🔴 Advanced  
> **📋 Prerequisites**: Complete [Tutorial S8](./S8-multi-model.md)

## 🎯 Learning Objectives

By the end of this tutorial, you will:
- ✅ Design sophisticated prompt template systems
- ✅ Implement dynamic variable substitution and composition
- ✅ Build context-aware template selection mechanisms
- ✅ Create reusable prompt engineering patterns
- ✅ Master advanced template optimization techniques

## 🛠️ Hands-On Exercise: Build an Intelligent Template Engine

### Step 1: Explore the S9 Template Architecture

Let's examine the advanced template system implementation:

```bash
# Navigate to the S9 module
cd src/main/java/com/coherentsolutions/l4aichat/s9templates

# Check the template implementations
find . -name "*Template*.java" -type f
```

**🤔 Question**: Why are template systems crucial for scalable AI applications?

<details>
<summary>💡 Click to reveal the answer</summary>

**Template System Benefits**:

1. **Consistency**: Standardized prompt structures across the application
2. **Maintainability**: Centralized prompt management and versioning
3. **Reusability**: Share common prompt patterns across different use cases
4. **Flexibility**: Dynamic content based on context and user data
5. **A/B Testing**: Easy comparison of different prompt variations
6. **Localization**: Multi-language support with template variants
7. **Performance**: Optimized prompts for specific models and use cases

**Template Patterns**:
- **Variable Substitution**: `{{user_name}}` → "John Smith"
- **Conditional Blocks**: Include/exclude content based on conditions
- **Template Inheritance**: Base templates with specialized extensions
- **Context Injection**: Dynamic addition of relevant information
- **Multi-Modal Templates**: Support for text, images, and structured data
</details>

### Step 2: Start the S9 Application

```bash
# Start S9 application with template features
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s9templates.TemplateApplication
```

### Step 3: Test Basic Template Functionality

Let's test the template system capabilities:

```bash
# Test simple variable substitution
curl -X POST http://localhost:8080/api/s9/template/simple \
  -H "Content-Type: application/json" \
  -d '{
    "templateName": "code_review",
    "variables": {
      "language": "Java",
      "code": "public class Calculator { public int add(int a, int b) { return a + b; } }",
      "focus_areas": ["performance", "security", "maintainability"]
    }
  }'
```

**Expected Response**:
```json
{
  "generatedPrompt": "Please review this Java code focusing on performance, security, and maintainability:\n\n```java\npublic class Calculator { public int add(int a, int b) { return a + b; } }\n```\n\nProvide specific recommendations for improvement.",
  "templateUsed": "code_review",
  "variablesApplied": ["language", "code", "focus_areas"],
  "aiResponse": "This Java code is well-structured but could benefit from input validation..."
}
```

### Step 4: Test Dynamic Template Selection

Test intelligent template selection based on context:

```bash
# Test context-aware template selection
curl -X POST http://localhost:8080/api/s9/template/dynamic \
  -H "Content-Type: application/json" \
  -d '{
    "userInput": "I need help debugging a performance issue in my Spring Boot application",
    "userContext": {
      "experience_level": "intermediate",
      "preferred_style": "step_by_step",
      "domain": "backend_development"
    }
  }'
```

**Expected Response**:
```json
{
  "selectedTemplate": "debugging_guide_intermediate",
  "selectionReason": "Performance debugging question from intermediate backend developer",
  "generatedPrompt": "I'll help you debug the performance issue in your Spring Boot application. As an intermediate developer, here's a systematic approach:\n\n1. **Identify the bottleneck**...",
  "templateVariables": {
    "experience_level": "intermediate",
    "problem_type": "performance",
    "technology": "Spring Boot"
  }
}
```

### Step 5: Test Template Composition

Test advanced template composition patterns:

```bash
# Test template composition and inheritance
curl -X POST http://localhost:8080/api/s9/template/compose \
  -H "Content-Type: application/json" \
  -d '{
    "baseTemplate": "technical_analysis",
    "components": [
      {
        "type": "introduction",
        "template": "formal_intro"
      },
      {
        "type": "analysis",
        "template": "deep_technical_analysis"
      },
      {
        "type": "recommendations",
        "template": "actionable_recommendations"
      }
    ],
    "context": {
      "topic": "microservices architecture",
      "audience": "senior_developers",
      "depth": "comprehensive"
    }
  }'
```

### Step 6: Test Multi-Language Templates

```bash
# Test localized templates
curl -X POST http://localhost:8080/api/s9/template/localized \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": "welcome_message",
    "language": "es",
    "variables": {
      "user_name": "María",
      "feature": "análisis de código"
    }
  }'
```

**Expected Response**:
```json
{
  "generatedPrompt": "¡Hola María! Te doy la bienvenida al sistema de análisis de código. ¿En qué puedo ayudarte hoy?",
  "language": "es",
  "templateVersion": "1.2",
  "localizationQuality": "native"
}
```

## 💡 Concept Deep-Dive: Advanced Template Systems

### **1. Sophisticated Template Engine Architecture**

Building a flexible, powerful template system:

```java
// Advanced template engine
@Component
public class AdvancedTemplateEngine {
    
    private final TemplateRepository templateRepository;
    private final VariableResolver variableResolver;
    private final ConditionalProcessor conditionalProcessor;
    private final TemplateCache templateCache;
    
    public TemplateRenderResult renderTemplate(TemplateRenderRequest request) {
        // Load and parse template
        Template template = loadTemplate(request.getTemplateName());
        
        // Resolve all variables
        Map<String, Object> resolvedVariables = variableResolver.resolveVariables(
            template.getVariables(), 
            request.getContext()
        );
        
        // Process conditional blocks
        Template processedTemplate = conditionalProcessor.processConditionals(
            template, 
            resolvedVariables
        );
        
        // Apply variable substitution
        String renderedContent = substituteVariables(processedTemplate, resolvedVariables);
        
        // Post-process (formatting, optimization)
        String finalContent = postProcess(renderedContent, request.getOptions());
        
        return TemplateRenderResult.builder()
            .renderedContent(finalContent)
            .templateUsed(template.getName())
            .variablesApplied(resolvedVariables.keySet())
            .processingTime(calculateProcessingTime())
            .build();
    }
    
    private String substituteVariables(Template template, Map<String, Object> variables) {
        String content = template.getContent();
        
        // Handle different variable types
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = formatVariable(entry.getValue(), template.getVariableFormat(entry.getKey()));
            
            content = content.replace(placeholder, value);
        }
        
        // Handle complex expressions
        content = processComplexExpressions(content, variables);
        
        // Handle loops and iterations
        content = processLoops(content, variables);
        
        return content;
    }
    
    private String processComplexExpressions(String content, Map<String, Object> variables) {
        Pattern expressionPattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
        Matcher matcher = expressionPattern.matcher(content);
        
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String expression = matcher.group(1);
            String value = evaluateExpression(expression, variables);
            matcher.appendReplacement(result, value);
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    private String evaluateExpression(String expression, Map<String, Object> variables) {
        // Handle conditional expressions: {{if condition}}content{{/if}}
        if (expression.startsWith("if ")) {
            return evaluateConditionalExpression(expression, variables);
        }
        
        // Handle list iterations: {{for item in list}}{{item}}{{/for}}
        if (expression.startsWith("for ")) {
            return evaluateLoopExpression(expression, variables);
        }
        
        // Handle function calls: {{uppercase(variable_name)}}
        if (expression.contains("(")) {
            return evaluateFunctionCall(expression, variables);
        }
        
        // Simple variable reference
        return String.valueOf(variables.getOrDefault(expression, ""));
    }
}

// Template composition system
@Service
public class TemplateCompositionService {
    
    public ComposedTemplate composeTemplate(CompositionRequest request) {
        Template baseTemplate = loadTemplate(request.getBaseTemplate());
        List<TemplateComponent> components = request.getComponents();
        
        // Build composition hierarchy
        TemplateHierarchy hierarchy = buildHierarchy(baseTemplate, components);
        
        // Resolve inheritance and overrides
        Template resolvedTemplate = resolveInheritance(hierarchy);
        
        // Merge component templates
        Template composedTemplate = mergeComponents(resolvedTemplate, components);
        
        // Validate composition
        ValidationResult validation = validateComposition(composedTemplate);
        if (!validation.isValid()) {
            throw new TemplateCompositionException(validation.getErrors());
        }
        
        return new ComposedTemplate(composedTemplate, hierarchy, validation);
    }
    
    private Template mergeComponents(Template baseTemplate, List<TemplateComponent> components) {
        String content = baseTemplate.getContent();
        
        for (TemplateComponent component : components) {
            String placeholder = "{{component:" + component.getType() + "}}";
            
            if (content.contains(placeholder)) {
                Template componentTemplate = loadTemplate(component.getTemplateName());
                String componentContent = componentTemplate.getContent();
                
                content = content.replace(placeholder, componentContent);
            }
        }
        
        return Template.builder()
            .name(baseTemplate.getName() + "_composed")
            .content(content)
            .variables(mergeVariables(baseTemplate, components))
            .metadata(createCompositionMetadata(baseTemplate, components))
            .build();
    }
}
```

### **2. Context-Aware Template Selection**

Intelligent template selection based on user context and request characteristics:

```java
// Smart template selector
@Component
public class SmartTemplateSelector {
    
    private final TemplateAnalyzer templateAnalyzer;
    private final ContextMatcher contextMatcher;
    private final PerformanceTracker performanceTracker;
    
    public TemplateSelectionResult selectOptimalTemplate(SelectionRequest request) {
        // Analyze the request
        RequestAnalysis analysis = analyzeRequest(request);
        
        // Find candidate templates
        List<Template> candidates = findCandidateTemplates(analysis);
        
        // Score each candidate
        List<TemplateScore> scores = candidates.stream()
            .map(template -> scoreTemplate(template, analysis, request.getContext()))
            .sorted(Comparator.comparing(TemplateScore::getTotalScore).reversed())
            .collect(toList());
        
        // Select the best template
        TemplateScore bestScore = scores.get(0);
        Template selectedTemplate = bestScore.getTemplate();
        
        // Generate selection rationale
        String rationale = generateSelectionRationale(bestScore, analysis);
        
        return TemplateSelectionResult.builder()
            .selectedTemplate(selectedTemplate)
            .selectionScore(bestScore.getTotalScore())
            .rationale(rationale)
            .alternatives(scores.subList(1, Math.min(4, scores.size())))
            .build();
    }
    
    private TemplateScore scoreTemplate(Template template, RequestAnalysis analysis, UserContext context) {
        double contextMatch = contextMatcher.calculateContextMatch(template, context);
        double purposeAlignment = calculatePurposeAlignment(template, analysis.getPurpose());
        double complexityMatch = calculateComplexityMatch(template, analysis.getComplexity());
        double performanceScore = calculatePerformanceScore(template);
        double userPreferenceScore = calculateUserPreferenceScore(template, context);
        
        // Weight the scores
        double totalScore = 
            contextMatch * 0.3 +
            purposeAlignment * 0.25 +
            complexityMatch * 0.2 +
            performanceScore * 0.15 +
            userPreferenceScore * 0.1;
        
        return TemplateScore.builder()
            .template(template)
            .contextMatch(contextMatch)
            .purposeAlignment(purposeAlignment)
            .complexityMatch(complexityMatch)
            .performanceScore(performanceScore)
            .userPreferenceScore(userPreferenceScore)
            .totalScore(totalScore)
            .build();
    }
    
    private double calculatePurposeAlignment(Template template, RequestPurpose purpose) {
        Set<String> templateTags = template.getTags();
        Set<String> purposeTags = purpose.getTags();
        
        // Calculate Jaccard similarity
        Set<String> intersection = new HashSet<>(templateTags);
        intersection.retainAll(purposeTags);
        
        Set<String> union = new HashSet<>(templateTags);
        union.addAll(purposeTags);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
}

// Dynamic template adaptation
@Service
public class TemplateAdaptationService {
    
    public AdaptedTemplate adaptTemplateForUser(Template template, UserProfile userProfile) {
        // Analyze user preferences and history
        UserTemplatePreferences preferences = analyzeUserPreferences(userProfile);
        
        // Adapt template style
        Template styleAdapted = adaptTemplateStyle(template, preferences.getPreferredStyle());
        
        // Adapt complexity level
        Template complexityAdapted = adaptComplexity(styleAdapted, preferences.getComplexityLevel());
        
        // Adapt language and tone
        Template toneAdapted = adaptTone(complexityAdapted, preferences.getPreferredTone());
        
        // Add personalization elements
        Template personalized = addPersonalization(toneAdapted, userProfile);
        
        return new AdaptedTemplate(
            personalized,
            calculateAdaptationScore(template, personalized),
            generateAdaptationSummary(template, personalized)
        );
    }
    
    private Template adaptTemplateStyle(Template template, PreferredStyle style) {
        String content = template.getContent();
        
        return switch (style) {
            case STEP_BY_STEP -> addStepByStepStructure(content);
            case BULLET_POINTS -> convertToBulletPoints(content);
            case NARRATIVE -> convertToNarrative(content);
            case FORMAL -> makeFormal(content);
            case CONVERSATIONAL -> makeConversational(content);
        };
    }
    
    private String addStepByStepStructure(String content) {
        // Use AI to restructure content into step-by-step format
        return aiTemplateProcessor.prompt()
            .system("""
                Convert the following content into a clear step-by-step format.
                Use numbered steps and make each step actionable.
                Maintain all the original information but structure it sequentially.
                """)
            .user("Content to restructure: " + content)
            .call()
            .content();
    }
}
```

### **3. Advanced Template Optimization**

Performance optimization and intelligent caching for template systems:

```java
// Template performance optimizer
@Component
public class TemplatePerformanceOptimizer {
    
    private final TemplateMetricsCollector metricsCollector;
    private final TemplateCache intelligentCache;
    
    @EventListener
    public void onTemplateUsage(TemplateUsageEvent event) {
        // Collect performance metrics
        TemplateMetrics metrics = TemplateMetrics.builder()
            .templateName(event.getTemplateName())
            .renderTime(event.getRenderTime())
            .contentLength(event.getGeneratedContent().length())
            .variableCount(event.getVariablesUsed().size())
            .complexityScore(calculateComplexityScore(event))
            .build();
        
        metricsCollector.recordMetrics(metrics);
        
        // Analyze for optimization opportunities
        analyzeOptimizationOpportunities(metrics);
    }
    
    @Scheduled(fixedRate = 3600000)  // Every hour
    public void optimizeTemplatePerformance() {
        // Identify slow templates
        List<String> slowTemplates = metricsCollector.getSlowTemplates();
        
        for (String templateName : slowTemplates) {
            optimizeTemplate(templateName);
        }
        
        // Update cache strategies
        updateCacheStrategies();
        
        // Precompile frequently used templates
        precompileFrequentTemplates();
    }
    
    private void optimizeTemplate(String templateName) {
        Template template = templateRepository.findByName(templateName);
        TemplateMetrics metrics = metricsCollector.getMetrics(templateName);
        
        // Identify bottlenecks
        List<OptimizationOpportunity> opportunities = identifyBottlenecks(template, metrics);
        
        for (OptimizationOpportunity opportunity : opportunities) {
            Template optimizedTemplate = applyOptimization(template, opportunity);
            
            // Test optimization effectiveness
            if (testOptimization(template, optimizedTemplate)) {
                templateRepository.save(optimizedTemplate);
                log.info("Successfully optimized template: {}", templateName);
            }
        }
    }
    
    private List<OptimizationOpportunity> identifyBottlenecks(Template template, TemplateMetrics metrics) {
        List<OptimizationOpportunity> opportunities = new ArrayList<>();
        
        // Complex variable resolution
        if (metrics.getVariableResolutionTime() > Duration.ofMillis(100)) {
            opportunities.add(new OptimizationOpportunity(
                OptimizationType.VARIABLE_RESOLUTION,
                "Slow variable resolution detected",
                EstimatedImprovement.HIGH
            ));
        }
        
        // Large template size
        if (template.getContent().length() > 10000) {
            opportunities.add(new OptimizationOpportunity(
                OptimizationType.TEMPLATE_SIZE,
                "Large template may benefit from chunking",
                EstimatedImprovement.MEDIUM
            ));
        }
        
        // Expensive expressions
        if (containsExpensiveExpressions(template)) {
            opportunities.add(new OptimizationOpportunity(
                OptimizationType.EXPRESSION_OPTIMIZATION,
                "Template contains expensive expressions",
                EstimatedImprovement.HIGH
            ));
        }
        
        return opportunities;
    }
}

// Intelligent template caching
@Component
public class IntelligentTemplateCache {
    
    private final Cache<String, CachedTemplate> templateCache;
    private final Cache<String, String> renderedContentCache;
    
    public Optional<String> getCachedRender(TemplateRenderRequest request) {
        String cacheKey = generateCacheKey(request);
        
        // Check if we have a cached render
        String cachedContent = renderedContentCache.getIfPresent(cacheKey);
        if (cachedContent != null) {
            recordCacheHit(request.getTemplateName());
            return Optional.of(cachedContent);
        }
        
        recordCacheMiss(request.getTemplateName());
        return Optional.empty();
    }
    
    public void cacheRenderedContent(TemplateRenderRequest request, String renderedContent) {
        String cacheKey = generateCacheKey(request);
        
        // Determine cache TTL based on template characteristics
        Duration ttl = determineCacheTTL(request);
        
        // Cache with expiry
        renderedContentCache.put(cacheKey, renderedContent);
        
        // Schedule cache invalidation if template is dynamic
        if (isDynamicTemplate(request.getTemplateName())) {
            scheduleInvalidation(cacheKey, ttl);
        }
    }
    
    private String generateCacheKey(TemplateRenderRequest request) {
        // Create semantic cache key that accounts for:
        // - Template name and version
        // - Variable values (sorted for consistency)
        // - User context relevant to template output
        
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(request.getTemplateName());
        keyBuilder.append(":");
        keyBuilder.append(request.getTemplateVersion());
        keyBuilder.append(":");
        
        // Add sorted variables
        Map<String, Object> sortedVars = new TreeMap<>(request.getVariables());
        for (Map.Entry<String, Object> entry : sortedVars.entrySet()) {
            keyBuilder.append(entry.getKey())
                     .append("=")
                     .append(hashValue(entry.getValue()))
                     .append(";");
        }
        
        return DigestUtils.sha256Hex(keyBuilder.toString());
    }
    
    private Duration determineCacheTTL(TemplateRenderRequest request) {
        Template template = getTemplate(request.getTemplateName());
        
        // Static templates can be cached longer
        if (template.isStatic()) {
            return Duration.ofHours(24);
        }
        
        // Dynamic templates with time-sensitive data
        if (template.hasTimeBasedVariables()) {
            return Duration.ofMinutes(15);
        }
        
        // User-specific templates
        if (template.isUserSpecific()) {
            return Duration.ofHours(1);
        }
        
        // Default TTL
        return Duration.ofHours(4);
    }
}
```

## 🧪 Live Experiment: Advanced Template Patterns

Let's experiment with sophisticated template features:

### Experiment 1: Template A/B Testing

Create a system for testing different template variations:

```java
// Template A/B testing framework
@Component
public class TemplateABTestingFramework {
    
    private final ABTestConfigurationService configService;
    private final TemplatePerformanceTracker performanceTracker;
    
    public TemplateVariant selectTemplateVariant(String templateName, UserContext context) {
        // Get active A/B tests for this template
        List<ABTest> activeTests = configService.getActiveTests(templateName);
        
        if (activeTests.isEmpty()) {
            return new TemplateVariant(templateName, "control", 1.0);
        }
        
        // Select variant based on user assignment
        for (ABTest test : activeTests) {
            TemplateVariant variant = assignUserToVariant(test, context);
            if (variant != null) {
                return variant;
            }
        }
        
        return new TemplateVariant(templateName, "control", 1.0);
    }
    
    public void recordTemplateUsage(String templateName, String variant, 
                                  TemplateUsageMetrics metrics) {
        performanceTracker.recordUsage(templateName, variant, metrics);
        
        // Check if we have enough data to make decisions
        if (performanceTracker.hasEnoughData(templateName)) {
            analyzeABTestResults(templateName);
        }
    }
    
    private void analyzeABTestResults(String templateName) {
        ABTestResults results = performanceTracker.getResults(templateName);
        
        // Calculate statistical significance
        StatisticalSignificance significance = calculateSignificance(results);
        
        if (significance.isSignificant()) {
            String winningVariant = significance.getWinningVariant();
            
            // Automatically promote winning variant
            if (configService.isAutoPromotionEnabled()) {
                promoteWinningVariant(templateName, winningVariant);
            }
            
            // Send notification to administrators
            notifyABTestCompletion(templateName, results, significance);
        }
    }
}
```

### Experiment 2: Multi-Modal Template Support

Build templates that handle text, images, and structured data:

```java
@Service
public class MultiModalTemplateService {
    
    public MultiModalTemplate createMultiModalTemplate(MultiModalRequest request) {
        // Process different content types
        List<TemplateComponent> components = new ArrayList<>();
        
        // Text components
        for (TextContent textContent : request.getTextComponents()) {
            components.add(createTextComponent(textContent));
        }
        
        // Image components  
        for (ImageContent imageContent : request.getImageComponents()) {
            components.add(createImageComponent(imageContent));
        }
        
        // Data visualization components
        for (DataContent dataContent : request.getDataComponents()) {
            components.add(createDataVisualizationComponent(dataContent));
        }
        
        // Combine components into cohesive template
        return composeMultiModalTemplate(components, request.getLayout());
    }
    
    private TemplateComponent createImageComponent(ImageContent imageContent) {
        return TemplateComponent.builder()
            .type(ComponentType.IMAGE)
            .content(imageContent.getImageData())
            .metadata(Map.of(
                "alt_text", imageContent.getAltText(),
                "caption", imageContent.getCaption(),
                "position", imageContent.getPosition().toString()
            ))
            .template("""
                ![{{alt_text}}]({{image_url}})
                {{#if caption}}
                *{{caption}}*
                {{/if}}
                """)
            .build();
    }
}
```

### Experiment 3: Template Version Control and Migration

Implement sophisticated versioning and migration strategies:

```java
@Service
public class TemplateVersionControlService {
    
    private final TemplateRepository templateRepository;
    private final TemplateValidator templateValidator;
    private final MigrationEngine migrationEngine;
    
    public TemplateVersion createNewVersion(String templateName, 
                                          String newContent, 
                                          VersionMetadata metadata) {
        // Validate new template
        ValidationResult validation = templateValidator.validate(newContent);
        if (!validation.isValid()) {
            throw new InvalidTemplateException(validation.getErrors());
        }
        
        // Get current version
        TemplateVersion currentVersion = templateRepository.getCurrentVersion(templateName);
        
        // Create new version
        TemplateVersion newVersion = TemplateVersion.builder()
            .templateName(templateName)
            .version(generateNextVersion(currentVersion.getVersion()))
            .content(newContent)
            .metadata(metadata)
            .parentVersion(currentVersion.getVersion())
            .createdAt(Instant.now())
            .status(VersionStatus.DRAFT)
            .build();
        
        // Save new version
        templateRepository.saveVersion(newVersion);
        
        return newVersion;
    }
    
    public void deployVersion(String templateName, String version) {
        TemplateVersion versionToDeploy = templateRepository.getVersion(templateName, version);
        
        // Run pre-deployment tests
        DeploymentTestResult testResult = runDeploymentTests(versionToDeploy);
        if (!testResult.isSuccessful()) {
            throw new DeploymentException("Pre-deployment tests failed", testResult.getErrors());
        }
        
        // Create rollback point
        TemplateVersion currentVersion = templateRepository.getCurrentVersion(templateName);
        createRollbackPoint(currentVersion);
        
        // Deploy new version
        templateRepository.setCurrentVersion(templateName, version);
        
        // Update cache
        templateCache.invalidate(templateName);
        
        // Monitor deployment
        scheduleDeploymentMonitoring(templateName, version);
    }
    
    public void migrateTemplates(String fromVersion, String toVersion) {
        List<Template> templatesToMigrate = templateRepository.getTemplatesByVersion(fromVersion);
        
        MigrationPlan plan = migrationEngine.createMigrationPlan(fromVersion, toVersion);
        
        for (Template template : templatesToMigrate) {
            try {
                Template migratedTemplate = migrationEngine.migrateTemplate(template, plan);
                templateRepository.save(migratedTemplate);
                
            } catch (MigrationException e) {
                log.error("Failed to migrate template: {}", template.getName(), e);
                // Continue with other templates
            }
        }
    }
}
```

## ✅ Check Your Understanding

### Quick Quiz

1. **What is the main advantage of using template composition over monolithic templates?**
   - A) Better performance
   - B) Modularity and reusability of template components
   - C) Easier debugging
   - D) Lower memory usage

<details>
<summary>Answer</summary>
**B) Modularity and reusability of template components** - Template composition allows you to build complex templates from reusable components, making maintenance easier and enabling consistent patterns across different use cases.
</details>

2. **Why is context-aware template selection important?**
   - A) To reduce storage requirements
   - B) To personalize responses based on user context and request characteristics
   - C) To improve security
   - D) To reduce network latency

<details>
<summary>Answer</summary>
**B) To personalize responses based on user context and request characteristics** - Context-aware selection ensures users get the most appropriate template variant based on their experience level, preferences, and the specific nature of their request.
</details>

3. **What is the benefit of template A/B testing?**
   - A) Faster template rendering
   - B) Data-driven optimization of prompt effectiveness
   - C) Reduced storage costs
   - D) Better security

<details>
<summary>Answer</summary>
**B) Data-driven optimization of prompt effectiveness** - A/B testing allows you to systematically compare different template variations and choose the ones that perform best based on actual usage metrics and user feedback.
</details>

### Coding Challenge 🏆

**Challenge**: Create a "Smart Template Recommendation System" that:
1. Analyzes user interaction patterns to recommend optimal templates
2. Automatically suggests template improvements based on usage data
3. Predicts template performance for new use cases
4. Provides template analytics and insights dashboard

**Requirements**:
```java
@RestController
public class TemplateRecommendationController {
    
    @GetMapping("/api/s9/recommendations/templates")
    public ResponseEntity<List<TemplateRecommendation>> getTemplateRecommendations(
            @RequestParam String useCase,
            @RequestParam String userProfile) {
        // Generate personalized template recommendations
    }
    
    @PostMapping("/api/s9/recommendations/improvements")
    public ResponseEntity<List<TemplateImprovement>> suggestImprovements(
            @RequestBody TemplateAnalysisRequest request) {
        // Analyze template and suggest improvements
    }
    
    @GetMapping("/api/s9/analytics/performance")
    public ResponseEntity<TemplatePerformanceAnalytics> getPerformanceAnalytics(
            @RequestParam String templateName,
            @RequestParam String timeRange) {
        // Provide detailed template performance analytics
    }
}
```

<details>
<summary>💡 Solution</summary>

```java
// Template recommendation data structures
public record TemplateRecommendation(
    String templateName,
    double relevanceScore,
    String rationale,
    List<String> strengths,
    List<String> considerations,
    UsageStatistics historicalUsage
) {}

public record TemplateImprovement(
    ImprovementType type,
    String description,
    double potentialImpact,
    String implementation,
    List<String> examples
) {}

// Smart recommendation service
@Service
public class SmartTemplateRecommendationService {
    
    private final UserBehaviorAnalyzer behaviorAnalyzer;
    private final TemplatePerformanceAnalyzer performanceAnalyzer;
    private final MLTemplatePredictor templatePredictor;
    
    public List<TemplateRecommendation> generateRecommendations(String useCase, String userProfile) {
        // Analyze user behavior patterns
        UserBehaviorProfile behavior = behaviorAnalyzer.analyzeUser(userProfile);
        
        // Find similar use cases
        List<String> similarUseCases = findSimilarUseCases(useCase);
        
        // Get candidate templates
        List<Template> candidates = getCandidateTemplates(useCase, similarUseCases);
        
        // Score each template
        List<TemplateRecommendation> recommendations = candidates.stream()
            .map(template -> scoreTemplate(template, useCase, behavior))
            .sorted(Comparator.comparing(TemplateRecommendation::relevanceScore).reversed())
            .limit(10)
            .collect(toList());
        
        return recommendations;
    }
    
    private TemplateRecommendation scoreTemplate(Template template, String useCase, UserBehaviorProfile behavior) {
        double relevanceScore = calculateRelevanceScore(template, useCase, behavior);
        String rationale = generateRationale(template, useCase, behavior);
        List<String> strengths = identifyTemplateStrengths(template);
        List<String> considerations = identifyConsiderations(template, behavior);
        UsageStatistics usage = performanceAnalyzer.getUsageStatistics(template.getName());
        
        return new TemplateRecommendation(
            template.getName(),
            relevanceScore,
            rationale,
            strengths,
            considerations,
            usage
        );
    }
    
    public List<TemplateImprovement> suggestImprovements(Template template) {
        List<TemplateImprovement> improvements = new ArrayList<>();
        
        // Analyze template structure
        TemplateStructureAnalysis structure = analyzeTemplateStructure(template);
        improvements.addAll(suggestStructuralImprovements(structure));
        
        // Analyze performance metrics
        TemplatePerformanceMetrics performance = performanceAnalyzer.getMetrics(template.getName());
        improvements.addAll(suggestPerformanceImprovements(performance));
        
        // Analyze user feedback
        UserFeedbackAnalysis feedback = analyzeFeedback(template.getName());
        improvements.addAll(suggestFeedbackBasedImprovements(feedback));
        
        // Use ML to predict potential improvements
        List<TemplateImprovement> mlSuggestions = templatePredictor.predictImprovements(template);
        improvements.addAll(mlSuggestions);
        
        return improvements.stream()
            .sorted(Comparator.comparing(TemplateImprovement::potentialImpact).reversed())
            .collect(toList());
    }
    
    private List<TemplateImprovement> suggestStructuralImprovements(TemplateStructureAnalysis analysis) {
        List<TemplateImprovement> improvements = new ArrayList<>();
        
        // Check for overly complex structures
        if (analysis.getComplexityScore() > 8.0) {
            improvements.add(new TemplateImprovement(
                ImprovementType.STRUCTURE_SIMPLIFICATION,
                "Template structure is overly complex and could be simplified",
                0.7,
                "Break down complex expressions into simpler components",
                List.of("Split long conditional blocks", "Reduce nesting levels")
            ));
        }
        
        // Check for missing error handling
        if (!analysis.hasErrorHandling()) {
            improvements.add(new TemplateImprovement(
                ImprovementType.ERROR_HANDLING,
                "Template lacks proper error handling for missing variables",
                0.8,
                "Add default values and conditional checks",
                List.of("{{variable|default:'N/A'}}", "{{#if variable}}...{{else}}...{{/if}}")
            ));
        }
        
        // Check for inconsistent formatting
        if (analysis.hasFormattingInconsistencies()) {
            improvements.add(new TemplateImprovement(
                ImprovementType.FORMATTING_CONSISTENCY,
                "Template has inconsistent formatting patterns",
                0.6,
                "Standardize variable naming and formatting",
                List.of("Use consistent variable naming", "Standardize indentation")
            ));
        }
        
        return improvements;
    }
    
    public TemplatePerformanceAnalytics getPerformanceAnalytics(String templateName, String timeRange) {
        Duration period = parseTimeRange(timeRange);
        Instant startTime = Instant.now().minus(period);
        
        // Collect performance data
        List<TemplateUsageEvent> events = getUsageEvents(templateName, startTime);
        
        // Calculate metrics
        PerformanceMetrics metrics = calculateMetrics(events);
        
        // Generate trends
        List<PerformanceTrend> trends = generateTrends(events, period);
        
        // Identify patterns
        List<UsagePattern> patterns = identifyUsagePatterns(events);
        
        // Generate insights
        List<PerformanceInsight> insights = generateInsights(metrics, trends, patterns);
        
        return new TemplatePerformanceAnalytics(
            templateName,
            timeRange,
            metrics,
            trends,
            patterns,
            insights,
            generateRecommendations(metrics, patterns)
        );
    }
}

// Machine learning template predictor
@Component
public class MLTemplatePredictor {
    
    private final ModelTrainingService trainingService;
    private final FeatureExtractor featureExtractor;
    
    public List<TemplateImprovement> predictImprovements(Template template) {
        // Extract features from template
        TemplateFeatures features = featureExtractor.extractFeatures(template);
        
        // Get trained models
        MLModel improvementModel = trainingService.getImprovementPredictionModel();
        MLModel impactModel = trainingService.getImpactPredictionModel();
        
        // Predict improvements
        List<PredictedImprovement> predictions = improvementModel.predict(features);
        
        // Estimate impact for each improvement
        List<TemplateImprovement> improvements = predictions.stream()
            .map(prediction -> {
                double impact = impactModel.predictImpact(features, prediction);
                return convertToTemplateImprovement(prediction, impact);
            })
            .filter(improvement -> improvement.potentialImpact() > 0.3)  // Filter low-impact
            .collect(toList());
        
        return improvements;
    }
}
```
</details>

## 🎯 Real-World Scenario: Enterprise Template Management Platform

**Scenario**: You're building a template management platform for a large enterprise with:
- 1000+ templates across different departments
- Multi-language support for global teams
- Compliance requirements for different industries
- A/B testing for continuous optimization
- Integration with existing business systems

**Your Task**: Design a comprehensive enterprise template platform.

### Solution Architecture

```java
// Enterprise template platform configuration
@Configuration
public class EnterpriseTemplatePlatformConfig {
    
    @Bean
    public TemplateGovernanceService governanceService() {
        return new TemplateGovernanceService();
    }
    
    @Bean
    public ComplianceTemplateValidator complianceValidator() {
        return new ComplianceTemplateValidator();
    }
    
    @Bean
    public EnterpriseTemplateOrchestrator orchestrator() {
        return new EnterpriseTemplateOrchestrator();
    }
}

// Enterprise template orchestrator
@Service
public class EnterpriseTemplateOrchestrator {
    
    public TemplateResponse processEnterpriseRequest(EnterpriseTemplateRequest request) {
        // Apply governance rules
        governanceService.validateRequest(request);
        
        // Check compliance requirements
        complianceValidator.validateCompliance(request);
        
        // Select appropriate template with enterprise constraints
        Template selectedTemplate = selectEnterpriseTemplate(request);
        
        // Render with audit trail
        return renderWithAuditTrail(selectedTemplate, request);
    }
}
```

## 🔗 Next Steps

🎉 **Congratulations!** You've completed the entire Spring AI advanced tutorial series!

**What you've mastered across all 9 tutorials**:
- ✅ **S1**: ChatClient fundamentals and multi-turn conversations
- ✅ **S2**: Core components, message types, and streaming
- ✅ **S3**: Memory management and persistence strategies
- ✅ **S4**: Advanced state management and enterprise patterns
- ✅ **S5**: Production-ready APIs with security and monitoring
- ✅ **S6**: Advanced features and structured output generation
- ✅ **S7**: Advisor patterns and sophisticated middleware
- ✅ **S8**: Multi-model architectures and intelligent routing
- ✅ **S9**: Template systems and dynamic prompt engineering

**🚀 You're now ready to:**
- Build enterprise-grade AI applications with Spring AI
- Implement sophisticated conversation management systems
- Design resilient multi-model architectures
- Create reusable advisor patterns for any use case
- Develop advanced template systems for dynamic content generation

**Continue your Spring AI journey:**
- 📚 [Complete Documentation](../README.md) - Full project reference
- 🏗️ [Architecture Guides](../architecture/) - Advanced patterns and strategies
- 🛠️ [Implementation Guides](../guides/) - Practical how-to guides
- 🧪 [Best Practices](../guides/best-practices.md) - Production recommendations

## 📚 Additional Resources

- 📖 [S9 Module Guide](../modules/S9-templates.md) - Complete template systems reference
- 🏗️ [Template Architecture](../architecture/template-patterns.md) - Advanced template patterns
- 📝 [Prompt Engineering Guide](../guides/prompt-engineering.md) - Professional prompting techniques

---

**🎉 Exceptional Achievement!** You've mastered the complete Spring AI ecosystem and can now build sophisticated, production-ready AI applications with confidence.

Your journey through these 9 comprehensive tutorials has equipped you with the knowledge and skills to tackle any AI application challenge using Spring AI's powerful framework.

[← Previous: S8 Multi-Model](./S8-multi-model.md) | [🏠 Tutorial Index](./README.md)