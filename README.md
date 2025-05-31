# 🤖 Spring AI Chat Applications - Lecture 4

Welcome to **Lecture 4** of the Spring AI course! This comprehensive repository demonstrates the evolution of AI chat applications using Spring AI 1.0.0, from basic single-turn conversations to advanced multi-model architectures.

## 📚 Course Navigation

This is part of a comprehensive Spring AI course:
- Lecture 1: Introduction to Spring AI
- Lecture 2: Working with Models
- Lecture 3: Prompt Engineering
- **→ Lecture 4: AI Chat Applications (You are here)**
- Lecture 5: RAG (Retrieval Augmented Generation)
- [... more lectures]

## 🎯 Learning Objectives

By completing this lecture, you will:
1. **Master** the Spring AI ChatClient fluent API
2. **Understand** conversation memory and state management
3. **Implement** production-ready AI chat applications
4. **Learn** when and why to use different architectural patterns
5. **Explore** advanced features like advisors, multi-model support, and templates

## 📖 Documentation Structure

### Core Concepts
- 📘 [Spring AI Fundamentals](docs/01-spring-ai-fundamentals.md) - Core concepts and architecture
- 📗 [ChatClient Deep Dive](docs/02-chatclient-deep-dive.md) - Understanding the ChatClient API
- 📙 [Memory and State Management](docs/03-memory-and-state.md) - How and why to manage conversation state

### Module Guides (Progressive Learning Path)
1. 📄 [S1: Multi-turn Conversations](docs/modules/s1-multiturn-guide.md) - Basic conversation patterns
2. 📄 [S2: ChatClient Components](docs/modules/s2-components-guide.md) - Building blocks exploration
3. 📄 [S3: Context Management](docs/modules/s3-context-guide.md) - Memory patterns and persistence
4. 📄 [S4: State Management](docs/modules/s4-state-guide.md) - Advanced state patterns
5. 📄 [S5: Production Chatbot](docs/modules/s5-chatbot-guide.md) - REST API best practices
6. 📄 [S6: Advanced Features](docs/modules/s6-advanced-guide.md) - Structured output and streaming
7. 📄 [S7: Advisor Patterns](docs/modules/s7-advisors-guide.md) - Interceptors and enhancers
8. 📄 [S8: Multi-Model Architecture](docs/modules/s8-multimodel-guide.md) - Model comparison strategies
9. 📄 [S9: Prompt Templates](docs/modules/s9-templates-guide.md) - Reusable prompt patterns

### Architecture & Design Decisions
- 🏗️ [Architectural Patterns](docs/architecture/patterns.md) - When to use which pattern
- 🔧 [Technology Choices](docs/architecture/technology-choices.md) - Why Spring AI?
- 📊 [Performance Considerations](docs/architecture/performance.md) - Optimization strategies

### Practical Guides
- 🚀 [Quick Start Guide](docs/guides/quick-start.md) - Get running in 5 minutes
- 🔄 [Migration Guide](docs/guides/migration-m7-to-1.0.md) - Upgrading from M7 to 1.0.0
- 🧪 [Testing Strategies](docs/guides/testing.md) - How to test AI applications
- 🛠️ [Troubleshooting](docs/guides/troubleshooting.md) - Common issues and solutions

## 🗂️ Repository Structure

```
L4AiChat/
├── 📁 src/main/java/com/coherentsolutions/l4aichat/
│   ├── 📂 s1multiturn/          # Basic multi-turn conversations
│   ├── 📂 s2components/         # ChatClient components exploration
│   ├── 📂 s3context/            # Context management with repositories
│   ├── 📂 s4statemanagement/    # Advanced state patterns
│   ├── 📂 s5chatbot/            # Production REST API chatbot
│   ├── 📂 s6advanced/           # Advanced features & structured output
│   ├── 📂 s7advisors/           # Advisor patterns & interceptors
│   ├── 📂 s8multimodel/         # Multi-model support & comparison
│   └── 📂 s9templates/          # Advanced prompt templates
├── 📁 docs/                     # Comprehensive documentation
├── 📁 examples/                 # Code examples and snippets
└── 📄 pom.xml                   # Maven configuration

```

## 🚦 Progressive Learning Path

### 🟢 Beginner Path
Start here if you're new to Spring AI:
1. Read [Spring AI Fundamentals](docs/01-spring-ai-fundamentals.md)
2. Complete [S1: Multi-turn Conversations](docs/modules/s1-multiturn-guide.md)
3. Explore [S2: Components](docs/modules/s2-components-guide.md)

### 🟡 Intermediate Path
For those comfortable with basics:
1. Study [S3: Context Management](docs/modules/s3-context-guide.md)
2. Implement [S5: Production Chatbot](docs/modules/s5-chatbot-guide.md)
3. Learn [S6: Advanced Features](docs/modules/s6-advanced-guide.md)

### 🔴 Advanced Path
For experienced developers:
1. Master [S7: Advisor Patterns](docs/modules/s7-advisors-guide.md)
2. Implement [S8: Multi-Model Architecture](docs/modules/s8-multimodel-guide.md)
3. Create [S9: Custom Templates](docs/modules/s9-templates-guide.md)

## 🛠️ Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **OpenAI API Key** (set as `OPENAI_API_KEY` environment variable)
- **Spring Boot 3.4.1**
- **Spring AI 1.0.0**

## 🚀 Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/iWaraxe/L4AiChat.git
   cd L4AiChat
   ```

2. **Set your OpenAI API key**
   ```bash
   export OPENAI_API_KEY=your-api-key-here
   ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run a specific module** (example: s5chatbot)
   ```bash
   mvn spring-boot:run -pl :L4AiChat -Dspring-boot.run.mainClass=com.coherentsolutions.l4aichat.s5chatbot.SpringAiChatbotApplication
   ```

## 📊 Key Learning Outcomes

### Technical Skills
- ✅ Master Spring AI ChatClient API
- ✅ Implement conversation memory patterns
- ✅ Build production-ready REST APIs
- ✅ Handle streaming responses
- ✅ Create structured outputs
- ✅ Compare multiple AI models
- ✅ Design reusable prompt templates

### Architectural Understanding
- 🎯 When to use stateless vs stateful conversations
- 🎯 How to choose between in-memory and persistent storage
- 🎯 Why advisor patterns improve maintainability
- 🎯 When multi-model architectures make sense
- 🎯 How to optimize for performance vs cost

## 🔑 Key Concepts Covered

1. **ChatClient API** - The foundation of Spring AI conversations
2. **Memory Management** - MessageWindowChatMemory, repositories
3. **Advisors** - Intercepting and enhancing AI interactions
4. **Streaming** - Real-time response handling
5. **Structured Output** - Type-safe AI responses
6. **Multi-Model Support** - Comparing and selecting models
7. **Prompt Templates** - Reusable, maintainable prompts

## 📚 Additional Resources

- 📖 [Official Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- 🎥 [Spring AI Video Tutorials](https://www.youtube.com/spring-ai)
- 💬 [Spring AI Community](https://github.com/spring-projects/spring-ai/discussions)
- 🐛 [Report Issues](https://github.com/iWaraxe/L4AiChat/issues)

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring AI Team for the excellent framework
- OpenAI for providing the AI models
- All contributors and students of this course

---

**Ready to start learning?** Head to the [Quick Start Guide](docs/guides/quick-start.md) or dive into [Module S1](docs/modules/s1-multiturn-guide.md)!

🎓 Happy Learning with Spring AI!