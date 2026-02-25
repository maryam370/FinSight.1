# FinSight - Requirements Specification

## Project Overview
FinSight is a financial transaction tracking and fraud detection system designed for hackathon-style rapid development. It provides real-time transaction monitoring, rule-based fraud detection, actionable insights, and subscription tracking.

## Core Objectives

### 1. Real-Time Transaction Tracking
- Track financial transactions with near real-time updates (polling/refresh mechanism)
- Support manual transaction entry by authenticated users
- Display transactions with comprehensive filtering and sorting capabilities
- Provide pagination for efficient data handling

### 2. Fraud Detection & Prevention
- Implement rule-based fraud detection with explainable heuristics
- Calculate fraud scores (0-100) and risk levels (LOW/MEDIUM/HIGH)
- Automatically flag suspicious transactions
- Generate fraud alerts with severity indicators
- Allow users to review and resolve fraud alerts

### 3. Actionable Insights Dashboard
- Display financial summary metrics (income, expenses, balance)
- Show fraud statistics (flagged count, average risk score)
- Visualize spending patterns by category
- Track fraud incidents by category
- Display spending trends over time

### 4. Advanced Transaction Filtering & Sorting
- Filter by: type (income/expense), category, date range, fraudulent status
- Sort by: transaction date, amount, fraud score
- Support ascending/descending order
- Efficient pagination for large datasets

### 5. Subscription Detection & Tracking
- Automatically detect recurring monthly payments
- Track subscription due dates
- Provide "due soon" notifications (configurable days ahead)
- Allow users to ignore/dismiss subscriptions

### 6. Demo Data Generation
- Auto-generate realistic demo transactions on first login
- Create 25-50 transactions spanning 60-90 days
- Include mix of income/expense across multiple categories
- Intentionally include suspicious transactions to trigger fraud detection
- Deterministic generation for testing purposes

### 7. Manual Transaction Entry
- Allow authenticated users to add transactions anytime
- Automatically run fraud detection on new transactions
- Create fraud alerts if thresholds are exceeded

## Functional Requirements

### FR-1: User Authentication
- **FR-1.1**: Users can register with username, email, and password
- **FR-1.2**: Users can login with credentials
- **FR-1.3**: System maintains user sessions
- **FR-1.4**: First-time login triggers demo data generation

### FR-2: Demo Data Generation
- **FR-2.1**: System detects first-time users (0 transactions)
- **FR-2.2**: Automatically generates 25-50 demo transactions
- **FR-2.3**: Transactions span 60-90 days in the past
- **FR-2.4**: Categories include: food, clothes, transport, bills, entertainment, subscriptions, transfers
- **FR-2.5**: Mix of income (20-30%) and expense (70-80%) transactions
- **FR-2.6**: 10-15% of transactions are intentionally suspicious
- **FR-2.7**: Generation is deterministic based on userId for testing
- **FR-2.8**: User sees "Preparing demo data..." message during generation

### FR-3: Transaction Management
- **FR-3.1**: Users can view all their transactions
- **FR-3.2**: Users can add manual transactions with:
  - Amount (required)
  - Type: INCOME or EXPENSE (required)
  - Category (required)
  - Description (optional)
  - Location (optional)
  - Transaction date (required)
- **FR-3.3**: System validates transaction data
- **FR-3.4**: System runs fraud detection on all new transactions
- **FR-3.5**: System stores fraud score and risk level with each transaction

### FR-4: Transaction Filtering & Sorting
- **FR-4.1**: Filter by transaction type (INCOME/EXPENSE)
- **FR-4.2**: Filter by category
- **FR-4.3**: Filter by date range (start date, end date)
- **FR-4.4**: Filter by fraudulent status (true/false)
- **FR-4.5**: Sort by transaction date (ascending/descending)
- **FR-4.6**: Sort by amount (ascending/descending)
- **FR-4.7**: Sort by fraud score (ascending/descending)
- **FR-4.8**: Support pagination (page number, page size)
- **FR-4.9**: Return total count and page metadata

### FR-5: Fraud Detection
- **FR-5.1**: Implement High Amount Anomaly rule
  - Trigger: Amount > 3x user's average for same type
  - Score: +30 points
- **FR-5.2**: Implement Rapid-Fire Activity rule
  - Trigger: 5+ transactions within 10 minutes
  - Score: +25 points
- **FR-5.3**: Implement Geographical Anomaly rule
  - Trigger: Different location within 2 hours
  - Score: +25 points
- **FR-5.4**: Implement Unusual Category rule
  - Trigger: Category never used by user before
  - Score: +20 points
- **FR-5.5**: Calculate total fraud score (0-100)
- **FR-5.6**: Map score to risk level:
  - LOW: 0-39
  - MEDIUM: 40-69
  - HIGH: 70-100
- **FR-5.7**: Flag transaction as fraudulent when score >= 70
- **FR-5.8**: Store fraud reasons for explainability

### FR-6: Fraud Alerts
- **FR-6.1**: Auto-create fraud alert when transaction is flagged
- **FR-6.2**: Alert includes: user, transaction, message, severity, resolved status
- **FR-6.3**: Users can view all their fraud alerts
- **FR-6.4**: Filter alerts by resolved status
- **FR-6.5**: Filter alerts by severity (LOW/MEDIUM/HIGH)
- **FR-6.6**: Users can resolve alerts

### FR-7: Dashboard & Insights
- **FR-7.1**: Calculate total income (sum of INCOME transactions)
- **FR-7.2**: Calculate total expenses (sum of EXPENSE transactions)
- **FR-7.3**: Calculate current balance (income - expenses)
- **FR-7.4**: Count total flagged transactions
- **FR-7.5**: Calculate average fraud score across all transactions
- **FR-7.6**: Aggregate spending by category
- **FR-7.7**: Aggregate fraud incidents by category
- **FR-7.8**: Generate spending trends over time (daily totals)
- **FR-7.9**: Support date range filtering for dashboard metrics

### FR-8: Subscription Detection
- **FR-8.1**: Analyze expense transactions for recurring patterns
- **FR-8.2**: Group transactions by normalized merchant name
- **FR-8.3**: Detect patterns: same merchant, 25-35 days apart, >= 2 occurrences
- **FR-8.4**: Calculate average subscription amount
- **FR-8.5**: Predict next due date (last payment + 30 days)
- **FR-8.6**: Store detected subscriptions with status (ACTIVE/IGNORED)
- **FR-8.7**: Users can view all detected subscriptions
- **FR-8.8**: Users can ignore subscriptions
- **FR-8.9**: Find subscriptions due within N days
- **FR-8.10**: Display "due soon" notifications

### FR-9: Real-Time Updates
- **FR-9.1**: Frontend polls backend every 5-10 seconds (configurable)
- **FR-9.2**: Provide "Live Refresh" toggle in UI
- **FR-9.3**: Backend supports efficient pagination for polling

## Non-Functional Requirements

### NFR-1: Performance
- **NFR-1.1**: Transaction list queries respond within 500ms
- **NFR-1.2**: Fraud detection completes within 200ms per transaction
- **NFR-1.3**: Dashboard metrics calculate within 1 second
- **NFR-1.4**: Demo data generation completes within 3 seconds

### NFR-2: Scalability
- **NFR-2.1**: Support 1000+ transactions per user
- **NFR-2.2**: Efficient pagination for large datasets
- **NFR-2.3**: Indexed database queries for performance

### NFR-3: Reliability
- **NFR-3.1**: Transactional integrity for all database operations
- **NFR-3.2**: Rollback on errors
- **NFR-3.3**: Deterministic fraud detection (same input = same output)

### NFR-4: Maintainability
- **NFR-4.1**: Clean separation of concerns (controller/service/repository)
- **NFR-4.2**: Comprehensive unit and integration tests
- **NFR-4.3**: Test coverage > 85%
- **NFR-4.4**: Clear code documentation

### NFR-5: Usability
- **NFR-5.1**: Intuitive UI with minimal learning curve
- **NFR-5.2**: Responsive design for mobile and desktop
- **NFR-5.3**: Clear visual indicators for fraud risk levels
- **NFR-5.4**: Toast notifications for important events

### NFR-6: Deployment
- **NFR-6.1**: Dockerized application (backend + frontend)
- **NFR-6.2**: Single command deployment: `docker compose up --build`
- **NFR-6.3**: Multi-architecture support (Apple Silicon compatible)
- **NFR-6.4**: In-memory H2 database for simplicity

### NFR-7: Security
- **NFR-7.1**: Password hashing (not plain text)
- **NFR-7.2**: User authentication required for all operations
- **NFR-7.3**: Users can only access their own data
- **NFR-7.4**: Input validation on all endpoints

## Acceptance Criteria

### AC-1: Demo Data Generation
- **Given** a new user logs in for the first time
- **When** the system detects 0 transactions
- **Then** 25-50 demo transactions are automatically created
- **And** transactions span 60-90 days
- **And** 10-15% are flagged as fraudulent
- **And** user sees confirmation message

### AC-2: Manual Transaction Entry
- **Given** an authenticated user
- **When** user submits a valid transaction
- **Then** transaction is saved to database
- **And** fraud detection runs automatically
- **And** fraud alert is created if score >= 70

### AC-3: Transaction Filtering
- **Given** a user with multiple transactions
- **When** user applies filters (type=EXPENSE, category=groceries, fraudulent=true)
- **Then** only matching transactions are returned
- **And** results are paginated
- **And** total count is accurate

### AC-4: Fraud Detection
- **Given** a transaction with amount > 3x user average
- **When** fraud detection runs
- **Then** fraud score includes +30 points
- **And** reason "Amount exceeds 3x user average" is recorded
- **And** risk level is calculated correctly
- **And** transaction is flagged if score >= 70

### AC-5: Dashboard Metrics
- **Given** a user with transaction history
- **When** user views dashboard
- **Then** total income is sum of INCOME transactions
- **And** total expenses is sum of EXPENSE transactions
- **And** balance = income - expenses
- **And** spending by category is accurate
- **And** fraud statistics are correct

### AC-6: Subscription Detection
- **Given** a user with recurring monthly payments
- **When** subscription detection runs
- **Then** subscriptions are identified (same merchant, 25-35 days apart)
- **And** average amount is calculated
- **And** next due date is predicted
- **And** subscriptions due within 7 days are flagged

### AC-7: Fraud Alert Management
- **Given** a fraudulent transaction
- **When** transaction is created
- **Then** fraud alert is auto-generated
- **And** alert includes severity based on risk level
- **And** user can view alert in alerts list
- **And** user can resolve alert

### AC-8: Real-Time Updates
- **Given** live refresh is enabled
- **When** new transactions are added
- **Then** UI polls backend every 5-10 seconds
- **And** new transactions appear without page reload
- **And** fraud alerts update automatically

## Out of Scope (V1)

- Real bank account integration
- Machine learning fraud detection
- Multi-currency support
- Transaction categories customization
- Budget planning features
- Bill payment reminders
- Export to CSV/PDF
- Mobile native apps
- Email notifications
- Two-factor authentication
- Social features (sharing, comparing)

## Success Metrics

- Demo data generation success rate: 100%
- Fraud detection accuracy: > 90% (based on intentional test cases)
- Dashboard load time: < 1 second
- Transaction filtering response time: < 500ms
- Test coverage: > 85%
- Zero critical bugs in core flows
- Successful Docker deployment on first try

## Constraints

- Hackathon-simple architecture (no microservices)
- In-memory H2 database (data lost on restart)
- No external API dependencies
- Deterministic logic for testing
- Must not break existing endpoints
- Extend existing schema, don't recreate
