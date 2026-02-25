# FinSight - Design Specification

## Architecture Overview

### Technology Stack
- **Backend**: Java 17 + Spring Boot 4.0.3
- **Database**: H2 (in-memory)
- **ORM**: Spring Data JPA + Hibernate
- **Testing**: JUnit 5, Mockito, AssertJ, jqwik
- **Build**: Maven
- **Containerization**: Docker + Docker Compose
- **Frontend**: React (or existing frontend tech)

### Architectural Layers
```
┌─────────────────────────────────────┐
│         Frontend (React)            │
│  - Pages: Login, Transactions,      │
│    Dashboard, Alerts, Subscriptions │
└─────────────────────────────────────┘
                 ↓ HTTP/REST
┌─────────────────────────────────────┐
│      Controller Layer               │
│  - TransactionController            │
│  - DashboardController (NEW)        │
│  - FraudAlertController (NEW)       │
│  - SubscriptionController (NEW)     │
│  - AuthController (NEW)             │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│       Service Layer                 │
│  - TransactionService               │
│  - FraudDetectionService            │
│  - DashboardService                 │
│  - FraudAlertService                │
│  - SubscriptionDetectorService      │
│  - DemoDataService (NEW)            │
│  - AuditLogService                  │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│      Repository Layer               │
│  - UserRepository                   │
│  - TransactionRepository            │
│  - FraudAlertRepository             │
│  - SubscriptionRepository           │
│  - AuditLogRepository               │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│         H2 Database                 │
└─────────────────────────────────────┘
```

## Database Schema

### Existing Tables (DO NOT BREAK)


#### users
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);
```

#### transactions
```sql
CREATE TABLE transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    type VARCHAR(20) NOT NULL,  -- INCOME, EXPENSE
    category VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    location VARCHAR(100),
    transaction_date TIMESTAMP NOT NULL,
    fraudulent BOOLEAN DEFAULT FALSE,
    fraud_score DOUBLE,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_transactions_user_date (user_id, transaction_date DESC),
    INDEX idx_transactions_fraudulent (user_id, fraudulent),
    INDEX idx_transactions_category (user_id, category)
);
```

#### fraud_alerts
```sql
CREATE TABLE fraud_alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    transaction_id BIGINT UNIQUE,
    message TEXT,
    severity VARCHAR(20),  -- LOW, MEDIUM, HIGH
    resolved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);
```

#### subscriptions
```sql
CREATE TABLE subscriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    merchant VARCHAR(100) NOT NULL,
    avg_amount DECIMAL(19,2) NOT NULL,
    last_paid_date DATE NOT NULL,
    next_due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,  -- ACTIVE, IGNORED
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_subscriptions_user (user_id),
    INDEX idx_subscriptions_due_date (user_id, next_due_date)
);
```

#### audit_logs
```sql
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    details TEXT,
    timestamp TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_audit_logs_user_time (user_id, timestamp DESC),
    INDEX idx_audit_logs_entity (entity_type, entity_id)
);
```

## Domain Models

### Enums

#### RiskLevel
```java
public enum RiskLevel {
    LOW,    // Score 0-39
    MEDIUM, // Score 40-69
    HIGH    // Score 70-100
}
```

#### SubscriptionStatus
```java
public enum SubscriptionStatus {
    ACTIVE,
    IGNORED
}
```

## API Endpoints

### Authentication (NEW)


```
POST /api/auth/register
Request: { username, email, password, fullName }
Response: { id, username, email, fullName, createdAt }

POST /api/auth/login
Request: { username, password }
Response: { token, user: { id, username, email, fullName } }
Note: Triggers demo data generation if user has 0 transactions

GET /api/auth/me
Response: { id, username, email, fullName, transactionCount, demoSeeded }
```

### Transactions (EXISTING + ENHANCED)

```
POST /api/transactions
Request: {
    userId, amount, type, category, 
    description?, location?, transactionDate
}
Response: TransactionResponse with fraud analysis
Note: Runs fraud detection, creates alert if needed, logs audit

GET /api/transactions/user/{userId}
Response: List<TransactionResponse>
Note: Returns all transactions ordered by date desc

GET /api/transactions/fraud/{userId}
Response: List<TransactionResponse>
Note: Returns only fraudulent transactions

GET /api/transactions (NEW - ENHANCED)
Query Params:
  - userId (required or from auth token)
  - type (INCOME|EXPENSE)
  - category
  - startDate (ISO 8601)
  - endDate (ISO 8601)
  - fraudulent (true|false)
  - sortBy (transactionDate|amount|fraudScore)
  - sortDir (asc|desc)
  - page (default: 0)
  - size (default: 20)
Response: Page<TransactionResponse> {
    content: [...],
    totalElements,
    totalPages,
    number,
    size
}
```

### Dashboard (NEW)

```
GET /api/dashboard/summary
Query Params:
  - userId (required or from auth token)
  - startDate? (optional date filter)
  - endDate? (optional date filter)
Response: {
    totalIncome: BigDecimal,
    totalExpenses: BigDecimal,
    currentBalance: BigDecimal,
    totalFlaggedTransactions: Long,
    averageFraudScore: Double,
    spendingByCategory: Map<String, BigDecimal>,
    fraudByCategory: Map<String, Long>,
    spendingTrends: List<TimeSeriesPoint>
}

TimeSeriesPoint: {
    date: LocalDate,
    amount: BigDecimal
}
```

### Fraud Alerts (NEW)

```
GET /api/fraud/alerts
Query Params:
  - userId (required or from auth token)
  - resolved? (true|false)
  - severity? (LOW|MEDIUM|HIGH)
Response: List<FraudAlertDto> {
    id, userId, message, severity, resolved, createdAt,
    transaction: TransactionResponse
}

PUT /api/fraud/alerts/{id}/resolve
Response: FraudAlertDto
Note: Sets resolved=true, logs audit entry
```

### Subscriptions (NEW)

```
POST /api/subscriptions/detect
Request: { userId }
Response: List<Subscription>
Note: Analyzes transactions, detects patterns, saves subscriptions

GET /api/subscriptions
Query Params:
  - userId (required or from auth token)
  - status? (ACTIVE|IGNORED)
Response: List<Subscription>

PUT /api/subscriptions/{id}/ignore
Response: Subscription
Note: Sets status=IGNORED

GET /api/subscriptions/due-soon
Query Params:
  - userId (required or from auth token)
  - days (default: 7)
Response: List<Subscription>
Note: Returns subscriptions with nextDueDate within N days
```

### Demo Data (NEW)

```
POST /api/demo/seed
Request: { userId }
Response: {
    transactionsCreated: Integer,
    fraudulentCount: Integer,
    message: String
}
Note: Only seeds if user has 0 transactions
```

## Fraud Detection Algorithm

### Rule-Based Scoring System

#### Rule 1: High Amount Anomaly (+30 points)
```
IF transaction.amount > (userAverage * 3)
THEN score += 30
     reasons.add("Amount exceeds 3x user average")
```

**Implementation**:
- Calculate user's average transaction amount for same type
- Compare current transaction amount
- Threshold: 3x multiplier

#### Rule 2: Rapid-Fire Activity (+25 points)
```
IF count(transactions in last 10 minutes) >= 5
THEN score += 25
     reasons.add("5+ transactions in 10 minutes")
```

**Implementation**:
- Query transactions in window: [now - 10 min, now]
- Count transactions
- Threshold: 5 transactions

#### Rule 3: Geographical Anomaly (+25 points)
```
IF lastTransaction.location != currentTransaction.location
   AND hoursBetween(lastTransaction, currentTransaction) < 2
THEN score += 25
     reasons.add("Different location within 2 hours")
```

**Implementation**:
- Get user's most recent transaction
- Compare locations (case-insensitive)
- Calculate time difference
- Threshold: 2 hours

#### Rule 4: Unusual Category (+20 points)
```
IF category NOT IN userCategories
THEN score += 20
     reasons.add("New category for user")
```

**Implementation**:
- Query distinct categories used by user
- Check if current category exists
- First-time category triggers rule

### Risk Level Mapping
```
score >= 70  → HIGH (fraudulent = true)
score >= 40  → MEDIUM
score < 40   → LOW
```

### Fraud Detection Flow
```
1. Transaction created/submitted
2. FraudDetectionService.analyzeTransaction()
3. Apply all 4 rules, accumulate score
4. Calculate risk level
5. Set fraudulent flag if score >= 70
6. Return FraudDetectionResult
7. If fraudulent, create FraudAlert
8. Log audit entry
```

## Demo Data Generation Strategy

### DemoDataService Design

#### Deterministic Generation
```java
Random random = new Random(userId); // Seed with userId for determinism
```

#### Transaction Distribution
- **Total**: 25-50 transactions (random within range)
- **Time Range**: 60-90 days ago to today
- **Type Distribution**:
  - INCOME: 20-30%
  - EXPENSE: 70-80%

#### Categories
- **INCOME**: salary, freelance, investment, refund, gift
- **EXPENSE**: food, groceries, transport, bills, entertainment, subscriptions, clothes, health, travel

#### Amount Ranges (in USD)
- **INCOME**: $500 - $5000
- **EXPENSE**:
  - food/groceries: $10 - $200
  - transport: $5 - $100
  - bills: $50 - $500
  - entertainment: $20 - $300
  - subscriptions: $5 - $50
  - clothes: $30 - $500
  - health: $50 - $1000
  - travel: $100 - $2000

#### Suspicious Transaction Injection
Generate 10-15% intentionally suspicious:
1. **High Amount**: 3-5 transactions with amount > 3x average
2. **Rapid-Fire**: Create 5-7 transactions within 5-minute window
3. **Unusual Category**: Use rare categories (luxury, gambling)
4. **Location Anomaly**: Same user, different cities within 1 hour

#### Merchant/Description Examples
- food: "Starbucks", "McDonald's", "Local Restaurant"
- groceries: "Whole Foods", "Walmart", "Target"
- transport: "Uber", "Lyft", "Gas Station"
- bills: "Electric Bill", "Internet Bill", "Water Bill"
- entertainment: "Netflix", "Spotify", "Movie Theater"
- subscriptions: "Amazon Prime", "Gym Membership", "Adobe"

### Seeding Trigger
**Option 1 (Implemented)**: Automatic in login flow
```java
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    User user = authService.authenticate(request);
    
    // Check if demo data needed
    if (transactionRepository.countByUser(user) == 0) {
        demoDataService.seedUserIfEmpty(user.getId());
    }
    
    return ResponseEntity.ok(new LoginResponse(token, user));
}
```

**Option 2**: Explicit bootstrap endpoint
```java
@PostMapping("/bootstrap")
public ResponseEntity<BootstrapResponse> bootstrap() {
    User user = getCurrentUser();
    demoDataService.seedUserIfEmpty(user.getId());
    return ResponseEntity.ok(new BootstrapResponse(transactionCount, fraudCount));
}
```

## Subscription Detection Algorithm

### Pattern Recognition Logic

#### Step 1: Group by Merchant
```java
Map<String, List<Transaction>> byMerchant = 
    expenses.stream()
        .collect(Collectors.groupingBy(t -> normalizeMerchant(t.getDescription())));
```

#### Step 2: Normalize Merchant Names
```java
private String normalizeMerchant(String merchant) {
    return merchant.toLowerCase()
                   .replaceAll("[^a-z0-9]", "")
                   .trim();
}
```
Examples:
- "Netflix Subscription" → "netflixsubscription"
- "NETFLIX" → "netflix"
- "Net-flix!" → "netflix"

#### Step 3: Detect Recurring Patterns
```java
for (List<Transaction> txns : merchantGroups) {
    if (txns.size() < 2) continue;
    
    txns.sort(byDate);
    int recurringCount = 0;
    
    for (int i = 1; i < txns.size(); i++) {
        long daysBetween = daysBetween(txns[i-1], txns[i]);
        if (daysBetween >= 25 && daysBetween <= 35) {
            recurringCount++;
        }
    }
    
    if (recurringCount >= 2) {
        createSubscription(merchant, txns);
    }
}
```

#### Step 4: Calculate Subscription Metrics
```java
Subscription sub = new Subscription();
sub.setMerchant(originalMerchantName);
sub.setAvgAmount(calculateAverage(txns));
sub.setLastPaidDate(lastTxn.getTransactionDate().toLocalDate());
sub.setNextDueDate(lastPaidDate.plusDays(30));
sub.setStatus(SubscriptionStatus.ACTIVE);
```

### Due Soon Detection
```java
LocalDate today = LocalDate.now();
LocalDate endDate = today.plusDays(days);

return subscriptionRepository.findDueSoon(user, today, endDate);
```

## Service Layer Design

### TransactionService (EXISTING + ENHANCED)
**Responsibilities**:
- Create transactions with fraud detection
- Retrieve transactions with filtering/sorting/pagination
- Manage transaction lifecycle
- Integrate with FraudDetectionService
- Log audit entries

**Key Methods**:
```java
TransactionResponse createTransaction(TransactionRequest request)
List<TransactionResponse> getUserTransactions(Long userId)
List<TransactionResponse> getFraudulentTransactions(Long userId)
Page<TransactionResponse> findWithFilters(userId, filters, pageable)
```

### FraudDetectionService (EXISTING)
**Responsibilities**:
- Analyze transactions for fraud
- Apply rule-based scoring
- Calculate risk levels
- Return explainable results

**Key Methods**:
```java
FraudDetectionResult analyzeTransaction(Transaction transaction)
```

### DashboardService (EXISTING)
**Responsibilities**:
- Aggregate financial metrics
- Calculate spending patterns
- Generate time series data
- Support date range filtering

**Key Methods**:
```java
DashboardSummary getSummary(Long userId, LocalDate startDate, LocalDate endDate)
```

### FraudAlertService (EXISTING)
**Responsibilities**:
- Manage fraud alerts
- Filter by status and severity
- Resolve alerts
- Log resolutions

**Key Methods**:
```java
List<FraudAlertDto> findByUser(Long userId)
List<FraudAlertDto> findByUserAndResolved(Long userId, boolean resolved)
List<FraudAlertDto> findByUserAndSeverity(Long userId, String severity)
FraudAlertDto resolveAlert(Long alertId)
```

### SubscriptionDetectorService (EXISTING)
**Responsibilities**:
- Detect recurring payment patterns
- Calculate subscription metrics
- Predict due dates
- Find due-soon subscriptions

**Key Methods**:
```java
List<Subscription> detectSubscriptions(Long userId)
List<Subscription> findDueSoon(Long userId, int days)
```

### DemoDataService (NEW - TO IMPLEMENT)
**Responsibilities**:
- Generate realistic demo transactions
- Ensure deterministic generation
- Inject suspicious transactions
- Seed only first-time users

**Key Methods**:
```java
void seedUserIfEmpty(Long userId)
List<Transaction> generateDemoTransactions(User user, int count)
```

### AuditLogService (EXISTING)
**Responsibilities**:
- Log all important actions
- Store audit trail
- Support compliance tracking

**Key Methods**:
```java
void logAction(Long userId, String action, String entityType, Long entityId, String details)
```

## Frontend Design

### Pages

#### 1. Login/Register Page
- Username/email + password fields
- Register link
- After login: redirect to dashboard
- Show "Preparing demo data..." toast if seeding

#### 2. Dashboard Page
**Metrics Cards**:
- Total Income (green)
- Total Expenses (red)
- Current Balance (blue)
- Flagged Transactions (orange)
- Average Risk Score (yellow)

**Charts**:
- Spending by Category (pie chart)
- Fraud by Category (bar chart)
- Spending Over Time (line chart)

**Date Range Filter**: Last 7/30/90 days, Custom

#### 3. Transactions Page
**Filters Panel**:
- Type dropdown (All/Income/Expense)
- Category dropdown (All/specific)
- Date range picker
- Fraudulent only checkbox
- Sort by dropdown (Date/Amount/Risk Score)
- Sort direction toggle (Asc/Desc)

**Transaction Table**:
- Columns: Date, Description, Category, Amount, Type, Risk Level, Actions
- Risk level badges (LOW=green, MEDIUM=yellow, HIGH=red)
- Pagination controls
- "Live Refresh" toggle

**Add Transaction Button**: Opens modal with form

#### 4. Fraud Alerts Page
**Filters**:
- Resolved/Unresolved toggle
- Severity filter (All/Low/Medium/High)

**Alert Cards**:
- Transaction details
- Fraud message
- Severity badge
- Resolve button
- Timestamp

#### 5. Subscriptions Page
**Due Soon Banner**: Shows count of subscriptions due within 7 days

**Subscription List**:
- Merchant name
- Average amount
- Last paid date
- Next due date
- Status badge
- Ignore button

### UI Components

#### Risk Level Badge
```jsx
<Badge color={
    level === 'HIGH' ? 'red' :
    level === 'MEDIUM' ? 'yellow' : 'green'
}>
    {level}
</Badge>
```

#### Live Refresh Toggle
```jsx
<Toggle 
    checked={liveRefresh}
    onChange={setLiveRefresh}
    label="Live Refresh (5s)"
/>
```

#### Transaction Form Modal
```jsx
<Modal>
    <Input label="Amount" type="number" required />
    <Select label="Type" options={['INCOME', 'EXPENSE']} required />
    <Select label="Category" options={categories} required />
    <Input label="Description" />
    <Input label="Location" />
    <DatePicker label="Date" required />
    <Button type="submit">Add Transaction</Button>
</Modal>
```

## Deployment Architecture

### Docker Setup

#### Backend Dockerfile
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/FinSight-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Frontend Dockerfile
```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
```

#### docker-compose.yml
```yaml
version: '3.8'
services:
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  frontend:
    build: ./frontend
    ports:
      - "3000:80"
    depends_on:
      - backend
    environment:
      - REACT_APP_API_URL=http://localhost:8080
```

## Testing Strategy

### Unit Tests
- All service methods
- Fraud detection rules individually
- Subscription detection logic
- Demo data generation

### Integration Tests
- Transaction creation with database
- Fraud alert generation
- Dashboard metrics calculation
- Subscription detection with real data

### Property-Based Tests (jqwik)
- Fraud score always 0-100
- Risk level mapping consistency
- Transaction amount precision

### End-to-End Tests
- Login → Demo data → View transactions
- Add transaction → Fraud detection → Alert
- Filter/sort transactions
- Resolve fraud alert
- Detect subscriptions

## Performance Considerations

### Database Indexes
- `idx_transactions_user_date`: Fast user transaction queries
- `idx_transactions_fraudulent`: Fast fraud filtering
- `idx_transactions_category`: Fast category filtering
- `idx_subscriptions_due_date`: Fast due-soon queries
- `idx_audit_logs_user_time`: Fast audit log retrieval

### Query Optimization
- Use pagination for large result sets
- Fetch only required fields in DTOs
- Use `@Transactional` for consistency
- Batch operations where possible

### Caching Strategy (Future)
- Cache user averages for fraud detection
- Cache dashboard metrics (5-minute TTL)
- Cache subscription list (1-hour TTL)

## Security Considerations

### Authentication
- Password hashing (BCrypt recommended)
- Session/JWT token management
- Token expiration

### Authorization
- User can only access own data
- Validate userId in all requests
- Prevent SQL injection (use JPA)

### Input Validation
- Validate all request DTOs
- Sanitize user inputs
- Enforce business rules (amount > 0, valid dates)

## Error Handling

### Standard Error Response
```json
{
    "timestamp": "2026-02-25T10:30:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Invalid transaction amount",
    "path": "/api/transactions"
}
```

### HTTP Status Codes
- 200: Success
- 201: Created
- 400: Bad Request (validation error)
- 401: Unauthorized
- 404: Not Found
- 500: Internal Server Error

## Monitoring & Logging

### Application Logs
- Transaction creation
- Fraud detection results
- Alert generation
- Subscription detection
- Demo data seeding

### Audit Logs
- All user actions
- Entity changes
- Alert resolutions

### Metrics (Future)
- Transaction count per user
- Fraud detection rate
- Average fraud score
- API response times
