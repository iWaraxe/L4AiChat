# 🧠 Tutorial S6: Advanced Chat Features

> **⏱️ Duration**: 60 minutes  
> **🎯 Difficulty**: 🔴 Advanced  
> **📋 Prerequisites**: Complete [Tutorial S5](./S5-production-apis.md)

## 🎯 Learning Objectives

By the end of this tutorial, you will:
- ✅ Generate structured output with complex entity mapping
- ✅ Implement multi-step conversation flows and workflows
- ✅ Build advanced prompt engineering techniques
- ✅ Create intelligent data analysis and extraction systems
- ✅ Design sophisticated reasoning and decision-making patterns

## 🛠️ Hands-On Exercise: Build an AI-Powered Code Analysis System

### Step 1: Explore the S6 Advanced Features

Let's examine the sophisticated AI capabilities:

```bash
# Navigate to the S6 module
cd src/main/java/com/coherentsolutions/l4aichat/s6advanced

# Check the advanced implementations
find . -name "*.java" -type f | head -10
```

**🤔 Question**: What makes S6 "advanced" compared to previous modules?

<details>
<summary>💡 Click to reveal the answer</summary>

**S6 Advanced Capabilities**:

1. **Structured Output Generation**: Complex entity mapping with nested objects
2. **Multi-Step Analysis Workflows**: Chained AI operations for complex tasks
3. **Advanced Prompt Engineering**: Context-aware, dynamic prompt construction
4. **Intelligent Data Extraction**: Parse and structure unstructured content
5. **Reasoning Patterns**: Implement step-by-step problem solving
6. **Domain-Specific Analysis**: Specialized AI for code, documents, data analysis

**Key Differences**:
- **Beyond Simple Chat**: Complex data processing and analysis
- **Structured Intelligence**: AI that returns formatted, usable data structures
- **Workflow Orchestration**: Multiple AI calls working together
- **Domain Expertise**: Specialized prompts for specific use cases
</details>

### Step 2: Start the S6 Application

```bash
# Start S6 application with advanced features
./mvnw spring-boot:run -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s6advanced.ChatbotApplication
```

### Step 3: Test Structured Code Analysis

Let's test sophisticated code analysis capabilities:

```bash
# Test comprehensive code analysis
curl -X POST http://localhost:8080/api/s6/analyze/code \
  -H "Content-Type: application/json" \
  -d '{
    "code": "public class UserService {\n    private final UserRepository userRepository;\n    \n    public User findById(Long id) {\n        return userRepository.findById(id).orElse(null);\n    }\n    \n    public void deleteUser(Long id) {\n        userRepository.deleteById(id);\n    }\n}",
    "analysisType": "COMPREHENSIVE"
  }'
```

**Expected Response**:
```json
{
  "codeQuality": {
    "overallScore": 6.5,
    "maintainabilityScore": 7.0,
    "reliabilityScore": 5.0,
    "securityScore": 7.0
  },
  "issues": [
    {
      "type": "RELIABILITY",
      "severity": "MEDIUM",
      "line": 5,
      "description": "Returning null instead of throwing exception",
      "suggestion": "Use orElseThrow() or Optional<User> return type"
    },
    {
      "type": "DESIGN",
      "severity": "LOW", 
      "description": "Missing input validation",
      "suggestion": "Add null checks for input parameters"
    }
  ],
  "patterns": [
    {
      "name": "Repository Pattern",
      "confidence": 0.95,
      "description": "Uses repository abstraction for data access"
    }
  ],
  "recommendations": [
    "Add comprehensive error handling",
    "Implement input validation",
    "Consider returning Optional<User> instead of null",
    "Add logging for debugging purposes"
  ],
  "complexity": {
    "cyclomaticComplexity": 2,
    "cognitiveComplexity": 1,
    "linesOfCode": 8
  }
}
```

### Step 4: Test Multi-Step Analysis Workflow

Test complex multi-step reasoning:

```bash
# Test architecture analysis workflow
curl -X POST http://localhost:8080/api/s6/analyze/architecture \
  -H "Content-Type: application/json" \
  -d '{
    "description": "We have a microservices system with 15 services, using REST APIs, MySQL databases, and Redis for caching. We are experiencing high latency and want to improve performance.",
    "includeRecommendations": true,
    "analysisDepth": "DEEP"
  }'
```

**Expected Response**:
```json
{
  "architectureAssessment": {
    "currentArchitecture": "Microservices with REST APIs",
    "complexity": "HIGH",
    "scalabilityRating": 7,
    "performanceRating": 5,
    "maintainabilityRating": 6
  },
  "performanceAnalysis": {
    "identifiedBottlenecks": [
      "Database query optimization",
      "Network latency between services", 
      "Cache hit ratio optimization",
      "Service mesh communication overhead"
    ],
    "estimatedImprovements": {
      "queryOptimization": "30-50% latency reduction",
      "cacheStrategy": "20-40% response time improvement",
      "serviceConsolidation": "15-25% network overhead reduction"
    }
  },
  "actionPlan": [
    {
      "priority": 1,
      "action": "Implement database query optimization",
      "effort": "Medium",
      "timeline": "2-4 weeks",
      "expectedImpact": "High"
    },
    {
      "priority": 2,
      "action": "Optimize Redis caching strategy",
      "effort": "Low",
      "timeline": "1-2 weeks", 
      "expectedImpact": "Medium"
    }
  ]
}
```

### Step 5: Test Document Analysis

Test intelligent document processing:

```bash
# Test document structure analysis
curl -X POST http://localhost:8080/api/s6/analyze/document \
  -H "Content-Type: application/json" \
  -d '{
    "content": "# API Documentation\n\n## Authentication\nUse Bearer tokens in the Authorization header.\n\n## Endpoints\n\n### GET /users\nReturns list of users.\n\n**Parameters:**\n- limit: Maximum number of users (default: 10)\n- offset: Pagination offset (default: 0)\n\n**Response:**\n```json\n{\n  \"users\": [],\n  \"total\": 0\n}\n```",
    "documentType": "API_DOCUMENTATION"
  }'
```

### Step 6: Test Intelligent Data Extraction

```bash
# Test entity extraction from unstructured text
curl -X POST http://localhost:8080/api/s6/extract/entities \
  -H "Content-Type: application/json" \
  -d '{
    "text": "John Smith from Acme Corp called about the Spring Boot project. The deadline is March 15th, 2024. Budget is $50,000. Technical contact: jane.doe@acme.com. Priority: High.",
    "extractionTypes": ["PERSON", "ORGANIZATION", "DATE", "MONEY", "EMAIL", "PROJECT"]
  }'
```

**Expected Response**:
```json
{
  "extractedEntities": {
    "persons": [
      {"name": "John Smith", "confidence": 0.98},
      {"name": "Jane Doe", "confidence": 0.95}
    ],
    "organizations": [
      {"name": "Acme Corp", "confidence": 0.97}
    ],
    "dates": [
      {"date": "2024-03-15", "originalText": "March 15th, 2024", "confidence": 0.99}
    ],
    "monetary": [
      {"amount": 50000, "currency": "USD", "originalText": "$50,000", "confidence": 0.98}
    ],
    "emails": [
      {"email": "jane.doe@acme.com", "confidence": 0.99}
    ],
    "projects": [
      {"name": "Spring Boot project", "confidence": 0.85}
    ]
  },
  "summary": {
    "project": "Spring Boot project",
    "client": "Acme Corp",
    "budget": "$50,000",
    "deadline": "March 15th, 2024",
    "priority": "High",
    "contacts": ["John Smith", "jane.doe@acme.com"]
  }
}
```

## 💡 Concept Deep-Dive: Advanced AI Patterns

### **1. Structured Output with Complex Entity Mapping**

Moving beyond simple text responses to complex data structures:

```java
// Complex nested entity structure
public record CodeAnalysisResult(
    CodeQuality codeQuality,
    List<CodeIssue> issues,
    List<DetectedPattern> patterns,
    List<String> recommendations,
    CodeComplexity complexity,
    SecurityAssessment security
) {}

public record CodeQuality(
    double overallScore,
    double maintainabilityScore,
    double reliabilityScore,
    double securityScore,
    Map<String, Double> detailedMetrics
) {}

public record CodeIssue(
    IssueType type,
    IssueSeverity severity,
    int line,
    String description,
    String suggestion,
    List<String> codeExamples,
    double confidence
) {}

// Advanced prompt for structured analysis
@Service
public class AdvancedCodeAnalysisService {
    
    public CodeAnalysisResult analyzeCode(String code, AnalysisType type) {
        String analysisPrompt = buildAdvancedPrompt(code, type);
        
        return chatClient.prompt()
            .system("""
                You are a senior software architect and code reviewer with expertise in:
                - Clean Code principles and SOLID design patterns
                - Security vulnerability assessment
                - Performance optimization techniques
                - Modern Java best practices
                
                Analyze the provided code comprehensively and return structured insights.
                Focus on actionable recommendations with specific examples.
                
                Rate each aspect from 1-10 where:
                - 1-3: Poor (immediate attention required)
                - 4-6: Fair (improvement recommended) 
                - 7-8: Good (minor improvements possible)
                - 9-10: Excellent (minimal changes needed)
                """)
            .user(analysisPrompt)
            .call()
            .entity(CodeAnalysisResult.class);
    }
    
    private String buildAdvancedPrompt(String code, AnalysisType type) {
        return switch (type) {
            case SECURITY_FOCUSED -> buildSecurityPrompt(code);
            case PERFORMANCE_FOCUSED -> buildPerformancePrompt(code);
            case ARCHITECTURE_FOCUSED -> buildArchitecturePrompt(code);
            case COMPREHENSIVE -> buildComprehensivePrompt(code);
        };
    }
    
    private String buildComprehensivePrompt(String code) {
        return String.format("""
            Analyze this code for:
            
            1. CODE QUALITY:
               - Readability and maintainability
               - Adherence to best practices
               - Design patterns usage
               
            2. POTENTIAL ISSUES:
               - Security vulnerabilities
               - Performance problems
               - Logic errors
               - Exception handling gaps
               
            3. ARCHITECTURAL PATTERNS:
               - Identify design patterns
               - Assess architectural decisions
               - Evaluate separation of concerns
               
            4. SPECIFIC RECOMMENDATIONS:
               - Concrete improvement suggestions
               - Code examples for fixes
               - Priority levels for changes
            
            Code to analyze:
            ```java
            %s
            ```
            
            Provide detailed, actionable insights with confidence scores.
            """, code);
    }
}
```

### **2. Multi-Step Reasoning Workflows**

Implement complex analysis through orchestrated AI interactions:

```java
@Service
public class MultiStepAnalysisService {
    
    public ArchitectureAnalysisResult analyzeArchitecture(String description) {
        // Step 1: Extract key components and technologies
        ComponentsAnalysis components = extractComponents(description);
        
        // Step 2: Identify potential bottlenecks
        BottleneckAnalysis bottlenecks = analyzeBottlenecks(components, description);
        
        // Step 3: Generate optimization recommendations
        OptimizationPlan optimizations = generateOptimizations(components, bottlenecks);
        
        // Step 4: Create implementation roadmap
        ImplementationRoadmap roadmap = createRoadmap(optimizations);
        
        // Step 5: Synthesize final analysis
        return synthesizeAnalysis(components, bottlenecks, optimizations, roadmap);
    }
    
    private ComponentsAnalysis extractComponents(String description) {
        return componentAnalysisClient.prompt()
            .system("""
                Extract and categorize technical components from the architecture description.
                Identify: services, databases, communication patterns, infrastructure components.
                """)
            .user("Analyze this architecture: " + description)
            .call()
            .entity(ComponentsAnalysis.class);
    }
    
    private BottleneckAnalysis analyzeBottlenecks(ComponentsAnalysis components, String description) {
        String bottleneckPrompt = String.format("""
            Given this system architecture:
            - Services: %s
            - Databases: %s
            - Communication: %s
            
            And this problem description: %s
            
            Identify likely performance bottlenecks and their root causes.
            """, 
            components.services(), 
            components.databases(), 
            components.communicationPatterns(),
            description
        );
        
        return bottleneckAnalysisClient.prompt()
            .system("""
                You are a performance engineering expert. Analyze the architecture 
                for potential bottlenecks, considering:
                - Database query patterns
                - Network latency
                - Service communication overhead
                - Resource utilization patterns
                """)
            .user(bottleneckPrompt)
            .call()
            .entity(BottleneckAnalysis.class);
    }
    
    private OptimizationPlan generateOptimizations(ComponentsAnalysis components, 
                                                  BottleneckAnalysis bottlenecks) {
        String optimizationPrompt = String.format("""
            Based on these identified bottlenecks:
            %s
            
            And this system architecture:
            %s
            
            Generate specific, actionable optimization recommendations with:
            - Priority levels (High/Medium/Low)
            - Estimated effort (Person-weeks)
            - Expected performance impact
            - Implementation complexity
            """,
            bottlenecks.identifiedBottlenecks(),
            components
        );
        
        return optimizationClient.prompt()
            .system("""
                You are a solutions architect specializing in performance optimization.
                Provide practical, implementable recommendations that balance:
                - Performance impact vs implementation effort
                - Short-term wins vs long-term architectural improvements
                - Resource constraints and team capabilities
                """)
            .user(optimizationPrompt)
            .call()
            .entity(OptimizationPlan.class);
    }
}
```

### **3. Advanced Prompt Engineering Techniques**

Sophisticated prompt construction for consistent, high-quality results:

```java
@Component
public class AdvancedPromptBuilder {
    
    public String buildAnalysisPrompt(AnalysisContext context) {
        return PromptTemplate.builder()
            .systemContext(buildSystemContext(context))
            .taskDefinition(buildTaskDefinition(context))
            .examplePatterns(buildExamples(context))
            .outputFormat(buildOutputFormat(context))
            .qualityConstraints(buildQualityConstraints(context))
            .build()
            .render(context.getVariables());
    }
    
    private String buildSystemContext(AnalysisContext context) {
        return switch (context.getDomain()) {
            case SOFTWARE_ARCHITECTURE -> """
                You are a principal software architect with 15+ years of experience in:
                - Distributed systems design and microservices architecture
                - Performance optimization and scalability patterns  
                - Cloud-native application development
                - Security and reliability engineering
                
                Your analysis should be:
                - Technically accurate and detailed
                - Practically implementable
                - Aligned with industry best practices
                - Focused on business impact
                """;
                
            case CODE_REVIEW -> """
                You are a senior software engineer and code reviewer specializing in:
                - Clean code principles and refactoring techniques
                - Security vulnerability identification
                - Performance optimization strategies
                - Modern development best practices
                
                Your reviews should be:
                - Constructive and actionable
                - Prioritized by impact and effort
                - Supported by concrete examples
                - Educational for the development team
                """;
                
            case DATA_ANALYSIS -> """
                You are a data scientist and analytics expert with expertise in:
                - Statistical analysis and data interpretation
                - Machine learning model evaluation
                - Data quality assessment and validation
                - Business intelligence and reporting
                
                Your analysis should be:
                - Statistically sound and rigorous
                - Business-focused with clear insights
                - Actionable with specific recommendations
                - Transparent about limitations and assumptions
                """;
        };
    }
    
    private String buildOutputFormat(AnalysisContext context) {
        return """
            Return your analysis in this structured format:
            
            {
              "summary": {
                "overallAssessment": "brief 2-3 sentence summary",
                "keyFindings": ["finding1", "finding2", "finding3"],
                "recommendedActions": ["action1", "action2"]
              },
              "detailedAnalysis": {
                // Domain-specific analysis structure
              },
              "actionPlan": [
                {
                  "priority": 1-5,
                  "action": "specific action description",
                  "rationale": "why this is important",
                  "effort": "estimated effort level",
                  "impact": "expected impact description",
                  "timeline": "suggested timeline"
                }
              ],
              "confidence": {
                "overallConfidence": 0.0-1.0,
                "uncertaintyAreas": ["area1", "area2"],
                "additionalDataNeeded": ["data1", "data2"]
              }
            }
            """;
    }
    
    // Chain-of-thought prompting for complex reasoning
    public String buildReasoningPrompt(String problem) {
        return String.format("""
            Problem: %s
            
            Let's solve this step by step:
            
            Step 1: Problem Understanding
            - What is the core issue?
            - What are the constraints?
            - What is the desired outcome?
            
            Step 2: Analysis
            - What factors contribute to this problem?
            - What are the potential solutions?
            - What are the trade-offs for each option?
            
            Step 3: Recommendation
            - Which solution is optimal and why?
            - What are the implementation steps?
            - What risks should be considered?
            
            Step 4: Validation
            - How can we measure success?
            - What could go wrong?
            - What contingency plans are needed?
            
            Please work through each step methodically.
            """, problem);
    }
}
```

## 🧪 Live Experiment: Advanced AI Capabilities

Let's experiment with sophisticated AI features:

### Experiment 1: Custom Domain Analysis

Create a specialized legal document analyzer:

```java
@Service
public class LegalDocumentAnalyzer {
    
    public LegalAnalysisResult analyzeLegalDocument(String document, DocumentType type) {
        String specializedPrompt = buildLegalPrompt(document, type);
        
        return legalAnalysisClient.prompt()
            .system("""
                You are a legal AI assistant with expertise in contract analysis.
                Focus on identifying:
                - Key legal terms and obligations
                - Potential risks and liabilities
                - Missing clauses or protections
                - Compliance requirements
                
                Provide analysis that is:
                - Legally accurate but not legal advice
                - Risk-focused and practical
                - Clear for non-legal professionals
                - Actionable with specific recommendations
                """)
            .user(specializedPrompt)
            .call()
            .entity(LegalAnalysisResult.class);
    }
    
    private String buildLegalPrompt(String document, DocumentType type) {
        return switch (type) {
            case SERVICE_AGREEMENT -> String.format("""
                Analyze this service agreement for:
                1. Service scope and deliverables
                2. Payment terms and penalties
                3. Liability limitations and indemnification
                4. Termination clauses and consequences
                5. Intellectual property rights
                6. Data protection and confidentiality
                
                Document:
                %s
                """, document);
                
            case EMPLOYMENT_CONTRACT -> String.format("""
                Review this employment contract for:
                1. Compensation and benefits structure
                2. Job responsibilities and expectations
                3. Non-compete and confidentiality clauses
                4. Termination procedures and severance
                5. Stock options or equity provisions
                6. Dispute resolution mechanisms
                
                Document:
                %s
                """, document);
        };
    }
}
```

### Experiment 2: Intelligent Code Generation

Build an AI system that generates complete, working code:

```java
@Service
public class IntelligentCodeGenerator {
    
    public GeneratedCodeResult generateCode(CodeGenerationRequest request) {
        // Step 1: Analyze requirements
        RequirementsAnalysis analysis = analyzeRequirements(request.getRequirements());
        
        // Step 2: Design architecture
        ArchitectureDesign design = designArchitecture(analysis);
        
        // Step 3: Generate code components
        List<CodeComponent> components = generateComponents(design);
        
        // Step 4: Generate tests
        List<TestComponent> tests = generateTests(components);
        
        // Step 5: Create documentation
        String documentation = generateDocumentation(design, components);
        
        return new GeneratedCodeResult(components, tests, documentation, design);
    }
    
    private RequirementsAnalysis analyzeRequirements(String requirements) {
        return codeAnalysisClient.prompt()
            .system("""
                Analyze software requirements and extract:
                - Core functionality needed
                - Data models and entities
                - API endpoints or interfaces
                - Non-functional requirements (performance, security)
                - Technology constraints
                """)
            .user("Analyze these requirements: " + requirements)
            .call()
            .entity(RequirementsAnalysis.class);
    }
    
    private List<CodeComponent> generateComponents(ArchitectureDesign design) {
        return design.getComponents().stream()
            .map(this::generateComponent)
            .collect(toList());
    }
    
    private CodeComponent generateComponent(ComponentSpec spec) {
        String generationPrompt = String.format("""
            Generate production-ready Java code for:
            
            Component: %s
            Type: %s
            Dependencies: %s
            Interface: %s
            
            Requirements:
            - Follow Spring Boot best practices
            - Include proper error handling
            - Add input validation
            - Include JavaDoc comments
            - Use modern Java features appropriately
            - Ensure thread safety where needed
            
            Generate complete, compilable code.
            """, 
            spec.getName(),
            spec.getType(),
            spec.getDependencies(),
            spec.getInterface()
        );
        
        String generatedCode = codeGenerationClient.prompt()
            .system("""
                You are an expert Java developer generating production-quality code.
                The code must be:
                - Syntactically correct and compilable
                - Following best practices and conventions
                - Well-documented and maintainable
                - Properly structured with separation of concerns
                - Include appropriate error handling
                """)
            .user(generationPrompt)
            .call()
            .content();
            
        return new CodeComponent(spec.getName(), generatedCode, spec.getType());
    }
}
```

### Experiment 3: Multi-Modal Analysis

Combine different types of input for comprehensive analysis:

```java
@Service
public class MultiModalAnalysisService {
    
    public ComprehensiveAnalysisResult analyzeProject(ProjectAnalysisRequest request) {
        // Analyze different aspects in parallel
        CompletableFuture<CodeAnalysis> codeAnalysis = 
            CompletableFuture.supplyAsync(() -> analyzeCodebase(request.getCodebase()));
            
        CompletableFuture<DocumentationAnalysis> docAnalysis = 
            CompletableFuture.supplyAsync(() -> analyzeDocumentation(request.getDocumentation()));
            
        CompletableFuture<ArchitectureAnalysis> archAnalysis = 
            CompletableFuture.supplyAsync(() -> analyzeArchitecture(request.getArchitectureDiagram()));
            
        CompletableFuture<TestAnalysis> testAnalysis = 
            CompletableFuture.supplyAsync(() -> analyzeTests(request.getTestSuite()));
        
        // Wait for all analyses to complete
        CompletableFuture.allOf(codeAnalysis, docAnalysis, archAnalysis, testAnalysis).join();
        
        // Synthesize comprehensive insights
        return synthesizeAnalysis(
            codeAnalysis.join(),
            docAnalysis.join(), 
            archAnalysis.join(),
            testAnalysis.join()
        );
    }
    
    private ComprehensiveAnalysisResult synthesizeAnalysis(CodeAnalysis code,
                                                          DocumentationAnalysis docs,
                                                          ArchitectureAnalysis arch,
                                                          TestAnalysis tests) {
        String synthesisPrompt = String.format("""
            Synthesize insights from multiple analysis perspectives:
            
            CODE ANALYSIS:
            Quality Score: %s
            Key Issues: %s
            Recommendations: %s
            
            DOCUMENTATION ANALYSIS:
            Coverage: %s
            Quality: %s
            Gaps: %s
            
            ARCHITECTURE ANALYSIS:
            Design Score: %s
            Patterns: %s
            Concerns: %s
            
            TEST ANALYSIS:
            Coverage: %s
            Quality: %s
            Missing Areas: %s
            
            Provide a holistic assessment with:
            1. Overall project health score
            2. Top 5 priority improvements
            3. Risk assessment
            4. Recommended next steps
            """,
            code.getQualityScore(), code.getKeyIssues(), code.getRecommendations(),
            docs.getCoverage(), docs.getQuality(), docs.getGaps(),
            arch.getDesignScore(), arch.getPatterns(), arch.getConcerns(),
            tests.getCoverage(), tests.getQuality(), tests.getMissingAreas()
        );
        
        return synthesisClient.prompt()
            .system("""
                You are a technical lead conducting a comprehensive project review.
                Provide balanced, actionable insights that help prioritize improvements
                across code quality, documentation, architecture, and testing.
                """)
            .user(synthesisPrompt)
            .call()
            .entity(ComprehensiveAnalysisResult.class);
    }
}
```

## ✅ Check Your Understanding

### Quick Quiz

1. **What is the main advantage of structured output over simple text responses?**
   - A) Faster processing
   - B) Better accuracy
   - C) Programmatic data usage and integration
   - D) Lower costs

<details>
<summary>Answer</summary>
**C) Programmatic data usage and integration** - Structured output allows applications to directly use AI responses as data objects, enabling automation, API integration, and complex workflows.
</details>

2. **Why is multi-step reasoning important for complex AI tasks?**
   - A) It reduces API calls
   - B) It breaks complex problems into manageable, logical steps
   - C) It improves response speed
   - D) It reduces model hallucinations

<details>
<summary>Answer</summary>
**B) It breaks complex problems into manageable, logical steps** - Multi-step reasoning improves accuracy and reliability by allowing the AI to work through problems systematically, just like human experts do.
</details>

3. **What is the purpose of chain-of-thought prompting?**
   - A) To reduce prompt length
   - B) To guide AI through step-by-step reasoning processes
   - C) To improve response speed
   - D) To reduce token usage

<details>
<summary>Answer</summary>
**B) To guide AI through step-by-step reasoning processes** - Chain-of-thought prompting explicitly guides the AI to show its reasoning steps, leading to more accurate and explainable results.
</details>

### Coding Challenge 🏆

**Challenge**: Create an "AI Technical Interview Assistant" that:
1. Analyzes candidate code submissions
2. Generates follow-up questions based on the code
3. Evaluates technical explanations
4. Provides hiring recommendations with justification

**Requirements**:
```java
@RestController
public class TechnicalInterviewController {
    
    @PostMapping("/api/s6/interview/analyze-submission")
    public ResponseEntity<CodeSubmissionAnalysis> analyzeSubmission(
            @RequestBody CodeSubmissionRequest request) {
        // Analyze candidate's code submission
    }
    
    @PostMapping("/api/s6/interview/generate-questions")
    public ResponseEntity<List<InterviewQuestion>> generateQuestions(
            @RequestBody CodeSubmissionAnalysis analysis) {
        // Generate targeted follow-up questions
    }
    
    @PostMapping("/api/s6/interview/evaluate-explanation")
    public ResponseEntity<ExplanationEvaluation> evaluateExplanation(
            @RequestBody CandidateExplanation explanation) {
        // Evaluate candidate's technical explanation
    }
    
    @PostMapping("/api/s6/interview/hiring-recommendation")
    public ResponseEntity<HiringRecommendation> generateRecommendation(
            @RequestBody InterviewData interviewData) {
        // Generate hiring recommendation
    }
}
```

<details>
<summary>💡 Solution</summary>

```java
// Data structures for technical interview
public record CodeSubmissionAnalysis(
    double technicalScore,
    CodeQuality codeQuality,
    List<TechnicalStrength> strengths,
    List<TechnicalGap> gaps,
    DifficultyAssessment difficulty,
    List<String> improvementAreas
) {}

public record InterviewQuestion(
    String question,
    QuestionType type,
    DifficultyLevel difficulty,
    List<String> expectedTopics,
    String rationale
) {}

public record HiringRecommendation(
    RecommendationType recommendation,  // STRONG_HIRE, HIRE, NO_HIRE, etc.
    double confidenceScore,
    List<String> strengths,
    List<String> concerns,
    String levelRecommendation,
    List<String> justification
) {}

// Technical interview service
@Service
public class TechnicalInterviewService {
    
    public CodeSubmissionAnalysis analyzeSubmission(String code, String problem, String timeSpent) {
        String analysisPrompt = String.format("""
            Analyze this coding interview submission:
            
            PROBLEM: %s
            TIME SPENT: %s
            
            CANDIDATE SOLUTION:
            ```java
            %s
            ```
            
            Evaluate:
            1. CORRECTNESS: Does it solve the problem correctly?
            2. EFFICIENCY: Time/space complexity analysis
            3. CODE QUALITY: Readability, structure, best practices
            4. PROBLEM-SOLVING: Approach and thought process
            5. EDGE CASES: Handling of boundary conditions
            
            Consider the time constraint and provide fair assessment.
            """, problem, timeSpent, code);
        
        return interviewClient.prompt()
            .system("""
                You are a senior software engineer conducting technical interviews.
                Assess candidates fairly considering:
                - Problem difficulty vs time constraint
                - Code correctness and efficiency
                - Communication and problem-solving approach
                - Industry best practices
                
                Be constructive and specific in feedback.
                """)
            .user(analysisPrompt)
            .call()
            .entity(CodeSubmissionAnalysis.class);
    }
    
    public List<InterviewQuestion> generateQuestions(CodeSubmissionAnalysis analysis) {
        String questionPrompt = String.format("""
            Based on this code submission analysis:
            
            Technical Score: %s
            Strengths: %s
            Gaps: %s
            
            Generate 5-7 targeted follow-up questions that:
            1. Explore the candidate's understanding of their solution
            2. Test knowledge of alternatives and optimizations
            3. Assess ability to handle edge cases
            4. Evaluate system design thinking
            5. Check understanding of trade-offs
            
            Questions should be:
            - Specific to their submitted code
            - Progressive in difficulty
            - Allow for multiple valid approaches
            - Test both technical depth and breadth
            """, 
            analysis.technicalScore(),
            analysis.strengths(),
            analysis.gaps()
        );
        
        QuestionGenerationResult result = questionClient.prompt()
            .system("""
                Generate thoughtful interview questions that reveal:
                - Technical depth and understanding
                - Problem-solving methodology
                - Ability to optimize and refactor
                - System design capabilities
                - Communication skills
                """)
            .user(questionPrompt)
            .call()
            .entity(QuestionGenerationResult.class);
            
        return result.questions();
    }
    
    public HiringRecommendation generateRecommendation(InterviewData data) {
        String recommendationPrompt = String.format("""
            Generate hiring recommendation based on complete interview:
            
            CODE SUBMISSION:
            Score: %s
            Quality: %s
            
            TECHNICAL QUESTIONS:
            Responses: %s
            Understanding: %s
            
            COMMUNICATION:
            Clarity: %s
            Problem-solving approach: %s
            
            EXPERIENCE LEVEL: %s
            ROLE REQUIREMENTS: %s
            
            Provide recommendation considering:
            - Technical competency for the role
            - Growth potential and learning ability
            - Team fit and communication skills
            - Overall problem-solving approach
            """,
            data.getCodeScore(),
            data.getCodeQuality(),
            data.getQuestionResponses(),
            data.getTechnicalUnderstanding(),
            data.getCommunicationClarity(),
            data.getProblemSolvingApproach(),
            data.getExperienceLevel(),
            data.getRoleRequirements()
        );
        
        return recommendationClient.prompt()
            .system("""
                You are an experienced engineering manager making hiring decisions.
                Consider:
                - Technical skills vs role requirements
                - Potential for growth and learning
                - Team dynamics and culture fit
                - Long-term value to the organization
                
                Be fair, unbiased, and provide clear justification.
                """)
            .user(recommendationPrompt)
            .call()
            .entity(HiringRecommendation.class);
    }
}
```
</details>

## 🎯 Real-World Scenario: AI-Powered Development Assistant

**Scenario**: You're building an AI assistant for a software development team that needs to:
- Automatically review pull requests and suggest improvements
- Generate comprehensive documentation from code
- Identify potential security vulnerabilities
- Suggest architectural improvements
- Create test cases automatically

**Your Task**: Design the complete AI-powered development workflow.

### Solution Architecture

```java
// Development assistant orchestrator
@Service
public class AIDevAssistantOrchestrator {
    
    @EventListener
    public void onPullRequestCreated(PullRequestEvent event) {
        CompletableFuture.runAsync(() -> {
            // Analyze PR in parallel
            PullRequestAnalysis analysis = analyzePullRequest(event.getPullRequest());
            
            // Post review comments
            postReviewComments(event.getPullRequestId(), analysis);
            
            // Update documentation if needed
            if (analysis.needsDocumentationUpdate()) {
                updateDocumentation(event.getChangedFiles());
            }
            
            // Run security scan
            SecurityScanResult security = scanForSecurity(event.getChangedFiles());
            if (!security.isClean()) {
                createSecurityIssues(security);
            }
        });
    }
    
    private PullRequestAnalysis analyzePullRequest(PullRequest pr) {
        // Multi-step analysis
        CodeChangeAnalysis changes = analyzeCodeChanges(pr.getChangedFiles());
        ImpactAssessment impact = assessImpact(changes);
        QualityAssessment quality = assessQuality(changes);
        TestCoverage coverage = analyzeCoverage(changes);
        
        return synthesizePRAnalysis(changes, impact, quality, coverage);
    }
}
```

## 🔗 Next Steps

Incredible work! You've mastered advanced AI features including structured output, multi-step reasoning, and sophisticated prompt engineering.

**What you've learned**:
- ✅ Complex entity mapping and structured data generation
- ✅ Multi-step analysis workflows and reasoning chains
- ✅ Advanced prompt engineering techniques
- ✅ Domain-specific AI applications
- ✅ Intelligent code analysis and generation

**Ready for advisor patterns mastery?** 

👉 **Continue to [Tutorial S7: Advisor Patterns Mastery](./S7-advisor-patterns.md)** to learn about:
- Custom advisor implementations and chaining
- Performance optimization with advisors
- Security and validation advisors
- Advanced advisor composition patterns

## 📚 Additional Resources

- 📖 [S6 Module Guide](../modules/S6-advanced.md) - Complete advanced features reference
- 🏗️ [Structured Output Patterns](../architecture/structured-output.md) - Entity mapping strategies
- 🔧 [Prompt Engineering Guide](../guides/prompt-engineering.md) - Advanced prompting techniques

---

**🎉 Exceptional achievement!** You now understand how to build sophisticated AI applications that go far beyond simple chat.

The next tutorial will show you how to create powerful, reusable advisor patterns that can transform and enhance any AI interaction.

[← Previous: S5 Production APIs](./S5-production-apis.md) | [Next: S7 Advisor Patterns →](./S7-advisor-patterns.md)