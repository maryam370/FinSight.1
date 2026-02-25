# Test Driven Development (TDD) - FinSight Application

## Overview
This document outlines the comprehensive Test-Driven Development approach for the FinSight financial transaction monitoring and fraud detection system. The application follows strict TDD principles where tests are written before implementation code.

## TDD Philosophy

### Core Principles
1. **Red-Green-Refactor Cycle**
   - Write a failing test (Red)
   - Write minimal code to pass the test (Green)
   - Refactor while keeping tests green (Refactor)
   - Repeat

2. **Test First, Always**
   - No production code without a failing test
   - Tests define the specification
   - Tests serve as living documentation

3. **Small Incremental Steps**
   - One test at a time
   - Simplest implementation first
   - Gradual complexity increase

## Testing Framework & Tools

### Technology Stack
- **Testing Framework**: JUnit 5 (Jupiter)
- **Mocking Framework**: Mockito
- **Assertion Library**: AssertJ
- **Property-Based Testing**: jqwik 1.7.4
- **Spring Testing**: Spring Boot Test (4.0.3)
- **Database**: H2 (in-memory for tests)
- **Build Tool**: Maven

### Dependencies
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>net.jqwik</groupId>
    <artifactId>jqwik</artifactId>
    <version>1.7.4</version>
    <scope>test</scope>
</dependency>
```

## Test Categories

### 1. Unit Tests
**Purpose**: Test individual components in isolation with mocked dependencies.

**Characteristics**:
- Fast execution (< 100ms per test)
- No external dependencies
- Use Mockito for mocking
- Focus on business logic

**Examples**:
- `FraudDetectionServiceTest`: Tests fraud detection rules in isolation
- `DashboardServiceTest`: Tests dashboard metric calculations
- `SubscriptionDetectorServiceTest`: Tests subscription pattern detection

**Naming Convention**: `{ClassName}Test.java`

**Structure**:
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Service Unit Tests")
class ServiceTest {
    @Mock
    private Repository repository;
    
    @InjectMocks
    private Service service;
    
    @Nested
    @DisplayName("Feature Tests")
    class FeatureTests {
        @Test
        @DisplayName("Should do something when condition")
        void shouldDoSomethingWhenCondition() {
            // Arrange
            // Act
            // Assert
        }
    }
}
```

### 2. Integration Tests
**Purpose**: Test component interactions with real dependencies (database, Spring context).

**Characteristics**:
- Slower execution (< 5s per test)
- Real database (H2 in-memory)
- Full Spring context
- Transactional rollback

**Examples**:
- `TransactionServiceTest`: Tests transaction creation with real database
- `FraudAlertServiceTest`: Tests alert management with database

**Annotations**:
```java
@SpringBootTest
@Transactional
class ServiceIntegrationTest {
    @Autowired
    private Service service;
    
    @Autowired
    private Repository repository;
}
```

### 3. Property-Based Tests
**Purpose**: Test properties that should hold for all inputs using jqwik.

**Characteristics**:
- Generates random test data
- Discovers edge cases
- Validates invariants
- 100+ test cases per property

**Examples**:
- Fraud score always between 0-100
- Risk level mapping consistency
- Transaction amount precision
- Date range validations

**Structure**:
```java
@Property
void propertyName(@ForAll("generator") Type input) {
    // Test property holds for all inputs
}

@Provide
Arbitrary<Type> generator() {
    return Arbitraries.of(...);
}
```

### 4. End-to-End Tests
**Purpose**: Test complete user workflows through the API layer.

**Characteristics**:
- Full application context
- HTTP requests/responses
- Complete data flow
- Real-world scenarios

**Examples**:
- Create transaction → Detect fraud → Generate alert
- User registration → Login → Transaction creation
- Dashboard data aggregation workflow

## Test Organization

### Package Structure
```
src/test/java/com/example/FinSight/
├── FinSightApplicationTests.java
└── service/
    ├── DashboardServiceTest.java
    ├── DemoDataServiceTest.java
    ├── FraudAlertServiceTest.java
    ├── FraudDetectionServiceTest.java
    ├── SubscriptionDetectorServiceTest.java
    └── TransactionServiceTest.java
```

### Nested Test Classes
Use `@Nested` to group related tests:
```java
@Nested
@DisplayName("Rule 1: High Amount Anomaly Tests")
class HighAmountAnomalyTests {
    // All tests for this specific rule
}

@Nested
@DisplayName("Rule 2: Rapid-Fire Activity Tests")
class RapidFireActivityTests {
    // All tests for this specific rule
}
```

## Fraud Detection Testing Strategy

### Rule-Based Testing
Each fraud detection rule is tested independently:

#### Rule 1: High Amount Anomaly (>3x average adds 30 points)
**Test Cases**:
- Amount exactly 3.1x average → +30 points
- Amount exactly 3x average → 0 points
- User has no average (null) → 0 points
- User average is zero → 0 points

#### Rule 2: Rapid-Fire Activity (5+ in 10 min adds 25 points)
**Test Cases**:
- Exactly 5 transactions in 10 minutes → +25 points
- 8 transactions in 10 minutes → +25 points
- Only 4 transactions in 10 minutes → 0 points

#### Rule 3: Geographical Anomaly (different location < 2 hours adds 25 points)
**Test Cases**:
- Different location within 1 hour → +25 points
- Different location at 1h 59m → +25 points
- Different location at exactly 2 hours → 0 points
- Same location within 2 hours → 0 points
- Current location is null → 0 points
- Previous location is null → 0 points
- No previous transaction → 0 points
- Case-insensitive location comparison

#### Rule 4: Unusual Category (never used adds 20 points)
**Test Cases**:
- Category is new for user → +20 points
- Category exists for user → 0 points

### Risk Level Mapping Tests
**Test Cases**:
- Score 0 → LOW
- Score 39 → LOW
- Score 40 → MEDIUM
- Score 69 → MEDIUM
- Score 70 → HIGH
- Score 100 → HIGH

### Combined Rules Tests
**Test Cases**:
- Multiple rules triggered simultaneously
- Score accumulation accuracy
- Fraud flag threshold (≥70)

## Service Layer Testing

### TransactionService Tests
**Unit Tests** (with mocks):
- Transaction creation with valid data
- Fraud detection integration
- Invalid user ID handling

**Integration Tests** (with database):
- Transaction persistence
- Fraud alert creation for high-risk transactions
- Filtering by type, category, date range, fraud status
- Sorting by amount, date
- Pagination

### DashboardService Tests
**Test Cases**:
- Total income calculation
- Total expenses calculation
- Current balance calculation
- Fraud metrics aggregation
- Spending by category
- Fraud by category
- Time series trends
- Date range filtering

### SubscriptionDetectorService Tests
**Test Cases**:
- Detect recurring patterns (25-35 days apart)
- Require minimum 2 occurrences
- Calculate average amount
- Predict next due date
- Merchant name normalization
- Find subscriptions due soon

### FraudAlertService Tests
**Test Cases**:
- Find alerts by user
- Filter by resolved status
- Filter by severity level
- Resolve alert
- DTO conversion with transaction details

## Test Data Management

### Test User Setup
```java
@BeforeEach
void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser = userRepository.save(testUser);
}
```

### Helper Methods
```java
private Transaction createTransaction(
    BigDecimal amount, 
    String category, 
    String location, 
    LocalDateTime date
) {
    Transaction transaction = new Transaction();
    transaction.setUser(testUser);
    transaction.setAmount(amount);
    transaction.setType("EXPENSE");
    transaction.setCategory(category);
    transaction.setLocation(location);
    transaction.setTransactionDate(date);
    transaction.setCreatedAt(LocalDateTime.now());
    return transaction;
}
```

## Assertion Patterns

### AssertJ Fluent Assertions
```java
// Basic assertions
assertThat(result.getFraudScore()).isEqualTo(30.0);
assertThat(result.isFraudulent()).isTrue();

// Collection assertions
assertThat(results.getContent()).hasSize(2);
assertThat(results.getContent()).allMatch(t -> t.getType().equals("EXPENSE"));

// Exception assertions
assertThatThrownBy(() -> service.method(invalidId))
    .isInstanceOf(RuntimeException.class)
    .hasMessageContaining("User not found");

// BigDecimal comparisons
assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));

// Date comparisons
assertThat(content.get(0).getTransactionDate())
    .isAfter(content.get(1).getTransactionDate());
```

## Test Coverage Goals

### Quantitative Targets
- **Line Coverage**: > 85%
- **Branch Coverage**: > 80%
- **Method Coverage**: > 90%
- **Class Coverage**: 100%

### Critical Path Coverage
- All fraud detection rules: 100%
- Transaction creation flow: 100%
- Alert generation: 100%

### Edge Case Coverage
- Null values
- Empty collections
- Boundary values (0, max, min)
- Invalid inputs
- Concurrent operations

## Mocking Strategy

### What to Mock
- External dependencies (repositories in unit tests)
- Time-dependent operations
- Random number generators
- External APIs (if any)

### What NOT to Mock
- Domain objects (entities, DTOs)
- Value objects
- Simple utilities
- Spring framework components (in integration tests)

### Mockito Patterns
```java
// Return value
when(repository.findById(1L)).thenReturn(Optional.of(user));

// Return different values on subsequent calls
when(repository.count())
    .thenReturn(5L)
    .thenReturn(6L);

// Throw exception
when(repository.findById(999L))
    .thenThrow(new RuntimeException("Not found"));

// Argument matchers
when(repository.countByUserAndTransactionDateBetween(
    eq(testUser), 
    any(LocalDateTime.class), 
    any(LocalDateTime.class)
)).thenReturn(5L);

// Verify interactions
verify(repository).save(any(Transaction.class));
verify(repository, times(2)).findById(1L);
verify(repository, never()).delete(any());
```

## Test Execution

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=FraudDetectionServiceTest

# Run specific test method
mvn test -Dtest=FraudDetectionServiceTest#shouldAdd30PointsWhenAmountIs3Point1xAverage

# Run with coverage
mvn test jacoco:report
```

### Test Lifecycle
```java
@BeforeAll
static void setupClass() {
    // Runs once before all tests in class
}

@BeforeEach
void setUp() {
    // Runs before each test method
}

@AfterEach
void tearDown() {
    // Runs after each test method
}

@AfterAll
static void tearDownClass() {
    // Runs once after all tests in class
}
```

## Best Practices

### 1. Test Naming
- Use descriptive names: `shouldAddPointsWhenConditionMet`
- Use `@DisplayName` for readable descriptions
- Follow pattern: `should{ExpectedBehavior}When{Condition}`

### 2. Arrange-Act-Assert Pattern
```java
@Test
void testMethod() {
    // Arrange: Set up test data and mocks
    User user = createTestUser();
    when(repository.findById(1L)).thenReturn(Optional.of(user));
    
    // Act: Execute the method under test
    Result result = service.performAction(1L);
    
    // Assert: Verify the outcome
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(expected);
}
```

### 3. One Assertion Per Test (Guideline)
- Focus each test on one behavior
- Multiple assertions OK if testing same concept
- Use nested classes to group related tests

### 4. Test Independence
- Tests should not depend on execution order
- Use `@BeforeEach` for setup
- Use `@Transactional` for database rollback

### 5. Avoid Test Logic
- No conditionals in tests
- No loops in tests
- Keep tests simple and readable

### 6. Test Data Builders
```java
private TransactionRequest buildRequest() {
    TransactionRequest request = new TransactionRequest();
    request.setUserId(testUser.getId());
    request.setAmount(new BigDecimal("100.00"));
    request.setType("EXPENSE");
    request.setCategory("groceries");
    request.setTransactionDate(LocalDateTime.now());
    return request;
}
```

## Continuous Integration

### Pre-Commit Checks
- All tests must pass
- No test skipping allowed
- Coverage thresholds enforced

### Build Pipeline
1. Compile code
2. Run unit tests
3. Run integration tests
4. Generate coverage report
5. Fail build if coverage < threshold

## Common Testing Patterns

### Testing Exceptions
```java
@Test
void shouldThrowExceptionWhenUserNotFound() {
    assertThatThrownBy(() -> service.getUser(999L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("User not found");
}
```

### Testing Collections
```java
@Test
void shouldReturnFilteredResults() {
    List<Transaction> results = service.findByType("EXPENSE");
    
    assertThat(results)
        .isNotEmpty()
        .hasSize(3)
        .allMatch(t -> t.getType().equals("EXPENSE"))
        .extracting(Transaction::getCategory)
        .contains("groceries", "utilities");
}
```

### Testing Pagination
```java
@Test
void shouldPaginateResults() {
    Page<Transaction> page = service.findAll(PageRequest.of(0, 10));
    
    assertThat(page.getContent()).hasSize(10);
    assertThat(page.getTotalElements()).isEqualTo(25);
    assertThat(page.getTotalPages()).isEqualTo(3);
    assertThat(page.hasNext()).isTrue();
}
```

### Testing Time-Dependent Logic
```java
@Test
void shouldDetectGeographicalAnomaly() {
    LocalDateTime currentTime = LocalDateTime.now();
    LocalDateTime previousTime = currentTime.minusMinutes(30);
    
    Transaction current = createTransaction("New York", currentTime);
    Transaction previous = createTransaction("Los Angeles", previousTime);
    
    when(repository.findTopByUserOrderByTransactionDateDesc(testUser))
        .thenReturn(Optional.of(previous));
    
    FraudDetectionResult result = service.analyzeTransaction(current);
    
    assertThat(result.getReasons())
        .contains("Different location within 2 hours");
}
```

## Troubleshooting

### Common Issues

**Issue**: Tests pass individually but fail when run together
- **Cause**: Shared state between tests
- **Solution**: Use `@BeforeEach` to reset state, ensure test independence

**Issue**: Flaky tests (intermittent failures)
- **Cause**: Time-dependent logic, race conditions
- **Solution**: Mock time, use fixed timestamps, avoid Thread.sleep()

**Issue**: Slow test execution
- **Cause**: Too many integration tests, database operations
- **Solution**: Prefer unit tests, use in-memory database, optimize queries

**Issue**: Mock not working as expected
- **Cause**: Incorrect argument matchers, wrong mock setup
- **Solution**: Use `verify()` to check interactions, use `any()` matchers carefully

## Documentation

### Test Documentation
- Use `@DisplayName` for readable test names
- Add comments for complex test scenarios
- Document test data assumptions
- Explain non-obvious assertions

### Living Documentation
- Tests serve as usage examples
- Test names describe expected behavior
- Test structure shows API contracts

## Metrics & Reporting

### Coverage Reports
- Generated by JaCoCo (if configured)
- View in `target/site/jacoco/index.html`
- Track trends over time

### Test Execution Reports
- Surefire reports in `target/surefire-reports/`
- HTML reports for easy viewing
- Failed test details with stack traces

## Future Enhancements

### Planned Improvements
1. Add contract testing for API endpoints
2. Implement mutation testing
3. Add performance benchmarks
4. Expand property-based testing coverage
5. Add security testing
6. Implement chaos engineering tests

## Conclusion

This TDD approach ensures:
- High code quality through comprehensive testing
- Confidence in refactoring and changes
- Living documentation through tests
- Early bug detection
- Maintainable and reliable codebase

All developers must follow these TDD practices to maintain code quality and system reliability.