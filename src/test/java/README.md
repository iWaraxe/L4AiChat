# Unit Testing Documentation

## Overview

This project implements a comprehensive unit testing strategy that focuses on **business logic validation** rather than complex framework mocking. This approach provides better test reliability, maintainability, and faster execution.

## Testing Philosophy

### ✅ What We Test
- **Business Logic**: Core algorithms, validation rules, and decision-making logic
- **Data Processing**: Message validation, conversation ID generation, state management
- **Edge Cases**: Null handling, empty inputs, boundary conditions
- **Integration Points**: Health endpoints, REST API contracts

### ❌ What We Don't Test
- **Spring AI Internal Logic**: Complex ChatClient/ChatModel mocking
- **Framework Internals**: Spring Boot auto-configuration, dependency injection
- **External Dependencies**: OpenAI API calls, network operations

## Test Structure

### Business Logic Tests (`/businesslogic/`)

Pure unit tests that focus on core functionality without framework dependencies:

#### 1. **ConversationIdValidationTest**
```java
// Tests UUID generation, validation, and conversation ID business rules
@Test
void shouldGenerateValidUuidFormat() {
    String uuid = UUID.randomUUID().toString();
    assertThat(uuid).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
}
```

**Coverage:**
- UUID format validation
- Conversation ID generation uniqueness
- Invalid ID handling
- Edge case management

#### 2. **MessageValidationTest**
```java
// Tests message content validation, sanitization, and classification
@ParameterizedTest
@ValueSource(strings = {"Hello", "How are you?", "Tell me about Spring AI"})
void shouldAcceptValidMessages(String validMessage) {
    boolean isValid = isValidMessage(validMessage);
    assertThat(isValid).isTrue();
}
```

**Coverage:**
- Message content validation
- Input sanitization
- Message type classification (questions, commands, greetings)
- Length validation
- Special character handling

#### 3. **ConversationStateTest**
```java
// Tests conversation lifecycle management and cleanup logic
@Test
void shouldTrackConversationLastInteractionTime() {
    String conversationId = stateManager.createConversation();
    stateManager.updateLastInteraction(conversationId);
    
    LocalDateTime lastInteraction = stateManager.getLastInteractionTime(conversationId);
    assertThat(lastInteraction).isNotNull();
}
```

**Coverage:**
- Conversation session management
- State tracking and cleanup
- Inactive conversation identification
- Statistics calculation

### Integration Tests (`/integration/`)

Integration tests that verify component interactions with proper Spring context:

- **Health endpoint functionality**
- **REST API contract validation**
- **Spring context loading**
- **Database connectivity (JDBC profile)**

## Test Execution

### Running Tests

```bash
# Run all tests
./mvnw test

# Run only unit tests
./mvnw test -Dtest="*ValidationTest,*StateTest"

# Run integration tests
./mvnw test -Dtest="*IntegrationTest"

# Run specific test class
./mvnw test -Dtest="ConversationIdValidationTest"
```

### Test Reports

Test results are available in:
- `target/surefire-reports/` - JUnit XML reports
- `target/site/jacoco/` - Code coverage reports (if configured)

## Best Practices

### 1. **Test Organization**
- Use `@Nested` classes to group related tests
- Use `@DisplayName` for clear test descriptions
- Follow Given-When-Then pattern

### 2. **Assertions**
- Use AssertJ for fluent assertions
- Test both positive and negative cases
- Include edge cases and boundary conditions

### 3. **Test Data**
- Use `@ParameterizedTest` for data-driven tests
- Use `@ValueSource`, `@NullAndEmptySource` for input variations
- Create realistic test data that represents actual usage

### 4. **Maintainability**
- Keep tests focused on single responsibility
- Avoid complex setup and teardown
- Make tests independent and repeatable

## Benefits of This Approach

### ✅ Advantages
- **Fast Execution**: No complex mocking or Spring context loading
- **Reliable**: Tests focus on actual business logic, not framework behavior
- **Maintainable**: Simple test structure that's easy to understand and modify
- **Comprehensive**: Covers edge cases and validation rules thoroughly

### 🔧 Integration Coverage
- **Health Endpoints**: Verified through existing integration tests
- **API Contracts**: REST endpoint validation
- **Spring Context**: Context loading verification
- **Database**: JDBC profile testing

## Testing Tools Used

- **JUnit 5**: Test framework with modern annotations and features
- **AssertJ**: Fluent assertion library for readable test code
- **Mockito**: Minimal mocking for unavoidable dependencies
- **Spring Boot Test**: Integration test support

## Future Enhancements

### Potential Additions
1. **Performance Tests**: Validate response times and throughput
2. **Contract Tests**: API contract validation with consumer-driven contracts
3. **Property-Based Testing**: Generate test cases automatically
4. **Mutation Testing**: Verify test quality through mutation analysis

### Metrics to Track
- **Code Coverage**: Aim for >80% on business logic
- **Test Execution Time**: Keep unit tests under 100ms each
- **Test Reliability**: Zero flaky tests
- **Test Maintenance**: Low effort to maintain when code changes

## Examples

### Testing Message Validation
```java
@ParameterizedTest
@NullAndEmptySource
@ValueSource(strings = {"   ", "\t", "\n"})
void shouldIdentifyInvalidMessages(String invalidMessage) {
    boolean isValid = isValidMessage(invalidMessage);
    assertThat(isValid).isFalse();
}
```

### Testing Business Logic
```java
@Test
void shouldHandleMultipleConversationsIndependently() {
    String conv1 = stateManager.createConversation();
    String conv2 = stateManager.createConversation();
    
    assertThat(conv1).isNotEqualTo(conv2);
    assertThat(stateManager.conversationExists(conv1)).isTrue();
    assertThat(stateManager.conversationExists(conv2)).isTrue();
}
```

This testing approach ensures high-quality, maintainable tests that provide confidence in the application's core functionality while avoiding the complexity and fragility of extensive framework mocking.