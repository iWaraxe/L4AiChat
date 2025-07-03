# S6 Advanced Module

## Overview

The S6 Advanced module showcases sophisticated chat patterns and structured responses using Spring AI 1.0.0's advanced features. This module demonstrates production-ready patterns including advisor chains, enhanced structured output, and complex conversation management.

## Features

- **Advanced Advisor Patterns**: Chained and configurable advisors
- **Enhanced Structured Output**: Complex entity mapping and validation
- **Production Chat Patterns**: Error handling, retry logic, fallbacks
- **Advanced Context Management**: Multi-layered conversation state
- **Custom Response Processing**: Specialized output formatters

## Endpoints

### Advanced Chat
- `POST /api/s6/chat/advanced` - Advanced conversation handling
- `POST /api/s6/chat/structured` - Complex structured responses
- `POST /api/s6/chat/guided` - AI-guided conversations

### Health Check
- `GET /api/s6/health` - Module health and advisor status

## Usage Examples

### Advanced Chat with Multiple Advisors
```bash
curl -X POST http://localhost:8080/api/s6/chat/advanced \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Help me plan a trip to Japan",
    "context": "travel_planning",
    "preferences": {"budget": "moderate", "duration": "2_weeks"}
  }'
```

### Complex Structured Output
```bash
curl -X POST http://localhost:8080/api/s6/chat/structured \
  -H "Content-Type: application/json" \
  -d '{
    "query": "Analyze market trends for tech stocks",
    "outputFormat": "detailed_analysis"
  }'
```

## Key Classes

- `AdvancedChatController` - Advanced conversation patterns
- `StructuredChatController` - Complex structured output handling  
- `ChatService` - Enhanced service layer with advisor management

## Advanced Patterns

### Advisor Chaining
The module demonstrates how to chain multiple advisors for complex workflows:
1. Memory advisor for context
2. Validation advisor for input checking
3. Formatting advisor for output structuring
4. Logging advisor for monitoring

### Error Handling
- Graceful degradation when AI services are unavailable
- Retry logic with exponential backoff
- Fallback responses for critical operations
- Comprehensive error logging and monitoring

### Performance Optimization
- Response caching for common queries
- Async processing for long-running operations
- Connection pooling for model interactions
- Resource usage monitoring

## Learning Objectives

1. **Advanced Architecture**: Master complex advisor patterns
2. **Production Patterns**: Implement robust error handling and monitoring
3. **Performance**: Optimize for scale and reliability
4. **Integration**: Combine multiple AI capabilities effectively

## Best Practices

- Implement comprehensive error handling and fallbacks
- Use caching strategically to reduce API costs
- Monitor token usage and response times
- Design for horizontal scaling
- Implement proper logging and observability

## Migration Notes

Fully updated for Spring AI 1.0.0:
- ✅ Advanced advisor pattern implementations
- ✅ Enhanced structured output capabilities
- ✅ Modern ChatClient integration patterns
- ✅ Production-ready error handling and monitoring