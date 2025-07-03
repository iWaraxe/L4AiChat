# S2 Components Module

## Overview

The S2 Components module provides comprehensive demonstrations of Spring AI 1.0.0 core components, message types, and structured output capabilities. This module serves as a deep dive into the fundamental building blocks of Spring AI applications.

## Features

- **ChatClient vs ChatModel**: Side-by-side comparison of API approaches
- **Message Types**: Comprehensive message handling (User, System, Assistant)
- **Structured Output**: Entity mapping and output converters
- **Streaming Responses**: Real-time token-by-token responses
- **Multi-Modal Support**: Text and image processing capabilities
- **Option Configuration**: Runtime model parameter customization

## Endpoints

### ChatClient Demonstrations
- `POST /api/s2/client/basic` - Simple fluent API usage
- `POST /api/s2/client/system` - System message configuration
- `POST /api/s2/client/template` - Template-style prompts
- `POST /api/s2/client/metadata` - Response metadata access
- `POST /api/s2/client/stream` - Streaming responses
- `POST /api/s2/client/structured` - Entity mapping

### ChatModel Demonstrations
- `POST /api/s2/model/basic` - Direct model interaction
- `POST /api/s2/model/prompt` - Prompt object usage
- `POST /api/s2/model/options` - Custom options
- `POST /api/s2/model/conversation` - Multi-turn in single request
- `POST /api/s2/model/stream` - Model streaming

### Message Types
- `POST /api/s2/messages/types` - Message role demonstrations
- `POST /api/s2/messages/user-message` - UserMessage construction
- `POST /api/s2/messages/conversation` - Multi-turn conversation
- `POST /api/s2/messages/multimodal` - Image + text processing

### Structured Output
- `POST /api/structured/entity` - Direct entity mapping
- `POST /api/structured/bean-converter` - BeanOutputConverter usage
- `POST /api/structured/list-converter` - List output conversion
- `POST /api/structured/map-converter` - Map output conversion
- `POST /api/structured/complex-types` - Complex type handling

### Health Check
- `GET /api/s2/health` - Module health and capabilities

## Usage Examples

### Basic ChatClient Usage
```bash
curl -X POST http://localhost:8080/api/s2/client/basic \
  -H "Content-Type: application/json" \
  -d '{"message": "Explain Spring AI in one sentence"}'
```

### Structured Output with Entity Mapping
```bash
curl -X POST http://localhost:8080/api/structured/entity \
  -H "Content-Type: application/json" \
  -d '{"product": "iPhone 15"}'
```

### Streaming Response
```bash
curl -X POST http://localhost:8080/api/s2/client/stream \
  -H "Content-Type: application/json" \
  -d '{"message": "Write a short story"}' \
  --no-buffer
```

### Multi-Modal Processing
```bash
curl -X POST http://localhost:8080/api/s2/messages/multimodal \
  -F "file=@image.jpg" \
  -F "prompt=Describe this image in detail"
```

## Key Classes

- `ChatClientDemoController` - Fluent API demonstrations
- `ChatModelDemoController` - Direct model interaction examples
- `MessageTypesDemoController` - Message construction and handling
- `StructuredOutputDemoController` - Output conversion examples

## Data Models

### Response DTOs
- `WeatherReport` - Structured weather information
- `ProductInfo` - Product details with pricing
- `MovieRecommendation` - Movie suggestions with ratings

### Request DTOs
- Standard message requests with text content
- Multi-modal requests with file uploads
- Template requests with parameter substitution

## Configuration

The module demonstrates various configuration patterns:
- Runtime option overrides (temperature, max tokens)
- System message customization
- Output converter configuration
- Streaming response setup

## Learning Objectives

1. **API Mastery**: Compare ChatClient vs ChatModel approaches
2. **Message Handling**: Understand role-based message construction
3. **Structured Data**: Master entity mapping and output conversion
4. **Streaming**: Implement real-time response processing
5. **Multi-Modal**: Handle text and image inputs effectively

## Best Practices

- Use ChatClient for most use cases (higher-level, more convenient)
- Use ChatModel when you need fine-grained control
- Leverage structured output for data-driven applications
- Implement streaming for long-form content generation
- Validate and sanitize all user inputs

## Migration Notes

Updated for Spring AI 1.0.0:
- New fluent ChatClient API patterns
- Enhanced entity mapping capabilities
- Improved streaming response handling
- Better multi-modal support
- Refined output converter system