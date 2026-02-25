# FinSight - Implementation Tasks

## Task Breakdown

This document outlines incremental tasks to enhance the existing FinSight codebase. Each task builds on existing code without breaking functionality.

---

## Phase 1: Core Infrastructure (COMPLETED ✓)

### Task 1.1: Database Schema & Models ✓
**Status**: COMPLETED
**Description**: All entity models exist and are properly configured.

**Completed**:
- ✓ User entity with authentication fields
- ✓ Transaction entity with fraud fields
- ✓ FraudAlert entity
- ✓ Subscription entity
- ✓ RiskLevel enum
- ✓ SubscriptionStatus enum

---

### Task 1.2: Repository Layer ✓
**Status**: COMPLETED
**Description**: All repositories exist with required query methods.

**Completed**:
- ✓ UserRepository
- ✓ TransactionRepository with custom queries
- ✓ FraudAlertRepository
- ✓ SubscriptionRepository
- ✓ JpaSpecificationExecutor for filtering

---

### Task 1.3: Core Services ✓
**Status**: COMPLETED
**Description**: Core business logic services are implemented.

**Completed**:
- ✓ TransactionService
- ✓ FraudDetectionService with 4 rules
- ✓ DashboardService
- ✓ FraudAlertService
- ✓ SubscriptionDetectorService

---

## Phase 2: Missing Components (TO IMPLEMENT)

### Task 2.1: Demo Data Service
**Priority**: HIGH
**Estimated Time**: 2-3 hours
**Dependencies**: None

**Description**: Create DemoDataService to generate realistic demo transactions for first-time users.

**Implementation Steps**:
1. Create `DemoDataService.java` in service package
2. Implement `seedUserIfEmpty(Long userId)` method
3. Use deterministic Random with userId as seed
4. Generate 25-50 transactions over 60-90 days
5. Mix income (20-30%) and expense (70-80%)
6. Include categories: food, groceries, transport, bills, entertainment, subscriptions, clothes
7. Inject 10-15% suspicious transactions:
   - High amounts (>3x average)
   - Rapid-fire (5-7 in 5 minutes)
   - Unusual categories
   - Location anomalies
8. Run fraud detection on each generated transaction

**Files to Create**:
- `src/main/java/com/example/FinSight/service/DemoDataService.java`

**Definition of Done**:
- [ ] Service generates 25-50 transactions deterministically
- [ ] Transactions span 60-90 days
- [ ] 10-15% are flagged as fraudulent
- [ ] Only seeds if user has 0 transactions
- [ ] Unit tests pass with 100% coverage
- [ ] Integration test verifies end-to-end seeding

**Test Cases**:
- Generate demo data for new user
- Verify transaction count in range
- Verify date distribution
- Verify type distribution (income/expense)
- Verify fraud detection triggers
- Verify determinism (same userId = same data)
- Verify no seeding if transactions exist

---

### Task 2.2: Authentication Controller
**Priority**: HIGH
**Estimated Time**: 2 hours
**Dependencies**: Task 2.1

**Description**: Create AuthController for user registration, login, and profile.

**Implementation Steps**:
1. Create `AuthController.java` in controller package
2. Create DTOs: `RegisterRequest`, `LoginRequest`, `LoginResponse`, `UserDto`
3. Implement POST `/api/auth/register`
4. Implement POST `/api/auth/login` with demo data trigger
5. Implement GET `/api/auth/me`
6. Add password hashing (BCrypt)
7. Add basic session/token management (simplified for hackathon)

**Files to Create**:
- `src/main/java/com/example/FinSight/controller/AuthController.java`
- `src/main/java/com/example/FinSight/dto/RegisterRequest.java`
- `src/main/java/com/example/FinSight/dto/LoginRequest.java`
- `src/main/java/com/example/FinSight/dto/LoginResponse.java`
- DTOs already exist: `UserDto.java`

**Definition of Done**:
- [ ] Register endpoint creates user with hashed password
- [ ] Login endpoint authenticates and returns token
- [ ] Login triggers demo data if user has 0 transactions
- [ ] /me endpoint returns current user info
- [ ] Integration tests pass
- [ ] Invalid credentials return 401

**Test Cases**:
- Register new user successfully
- Register with duplicate username fails
- Login with valid credentials
- Login with invalid credentials fails
- Login triggers demo data for first-time user
- Get current user info

---

### Task 2.3: Dashboard Controller
**Priority**: MEDIUM
**Estimated Time**: 1 hour
**Dependencies**: None (DashboardService exists)

**Description**: Create DashboardController to expose dashboard metrics API.

**Implementation Steps**:
1. Create `DashboardController.java` in controller package
2. Implement GET `/api/dashboard/summary`
3. Add query params: userId, startDate, endDate
4. Return DashboardSummary DTO
5. Add error handling

**Files to Create**:
- `src/main/java/com/example/FinSight/controller/DashboardController.java`

**Definition of Done**:
- [ ] Endpoint returns dashboard summary
- [ ] Date range filtering works
- [ ] Returns 404 if user not found
- [ ] Integration test passes

**Test Cases**:
- Get summary for user with transactions
- Get summary with date range filter
- Get summary for user with no transactions
- Invalid user ID returns 404

---

### Task 2.4: Fraud Alert Controller
**Priority**: MEDIUM
**Estimated Time**: 1 hour
**Dependencies**: None (FraudAlertService exists)

**Description**: Create FraudAlertController to manage fraud alerts.

**Implementation Steps**:
1. Create `FraudAlertController.java` in controller package
2. Implement GET `/api/fraud/alerts` with filters
3. Implement PUT `/api/fraud/alerts/{id}/resolve`
4. Add query params: userId, resolved, severity
5. Return FraudAlertDto

**Files to Create**:
- `src/main/java/com/example/FinSight/controller/FraudAlertController.java`

**Definition of Done**:
- [ ] Get alerts endpoint with filtering
- [ ] Resolve alert endpoint works
- [ ] Integration tests pass

**Test Cases**:
- Get all alerts for user
- Filter by resolved status
- Filter by severity
- Resolve alert successfully
- Resolve non-existent alert returns 404

---

### Task 2.5: Subscription Controller
**Priority**: MEDIUM
**Estimated Time**: 1.5 hours
**Dependencies**: None (SubscriptionDetectorService exists)

**Description**: Create SubscriptionController to manage subscriptions.

**Implementation Steps**:
1. Create `SubscriptionController.java` in controller package
2. Implement POST `/api/subscriptions/detect`
3. Implement GET `/api/subscriptions`
4. Implement PUT `/api/subscriptions/{id}/ignore`
5. Implement GET `/api/subscriptions/due-soon`
6. Add query params: userId, status, days

**Files to Create**:
- `src/main/java/com/example/FinSight/controller/SubscriptionController.java`

**Definition of Done**:
- [ ] Detect subscriptions endpoint works
- [ ] Get subscriptions with status filter
- [ ] Ignore subscription endpoint works
- [ ] Due-soon endpoint returns correct results
- [ ] Integration tests pass

**Test Cases**:
- Detect subscriptions for user
- Get all subscriptions
- Filter by status (ACTIVE/IGNORED)
- Ignore subscription
- Get subscriptions due within 7 days

---

### Task 2.6: Enhanced Transaction Controller
**Priority**: HIGH
**Estimated Time**: 2 hours
**Dependencies**: None (TransactionController exists)

**Description**: Enhance existing TransactionController with advanced filtering/sorting.

**Implementation Steps**:
1. Add GET `/api/transactions` endpoint with full filtering
2. Add query params: type, category, startDate, endDate, fraudulent, sortBy, sortDir, page, size
3. Use TransactionSpecification for filtering
4. Return Page<TransactionResponse>
5. Keep existing endpoints unchanged

**Files to Modify**:
- `src/main/java/com/example/FinSight/controller/TransactionController.java`

**Definition of Done**:
- [ ] New endpoint supports all filters
- [ ] Sorting works (date, amount, fraud score)
- [ ] Pagination works correctly
- [ ] Existing endpoints still work
- [ ] Integration tests pass

**Test Cases**:
- Filter by type
- Filter by category
- Filter by date range
- Filter by fraudulent status
- Sort by date ascending/descending
- Sort by amount
- Pagination with page/size

---

## Phase 3: Frontend Implementation

### Task 3.1: Authentication Pages
**Priority**: HIGH
**Estimated Time**: 3 hours
**Dependencies**: Task 2.2

**Description**: Create login and register pages.

**Implementation Steps**:
1. Create Login component with form
2. Create Register component with form
3. Implement authentication context/state
4. Store token in localStorage
5. Add "Preparing demo data..." toast on first login
6. Redirect to dashboard after login

**Files to Create**:
- `frontend/src/pages/Login.jsx`
- `frontend/src/pages/Register.jsx`
- `frontend/src/context/AuthContext.jsx`
- `frontend/src/services/authService.js`

**Definition of Done**:
- [ ] Login form works
- [ ] Register form works
- [ ] Token stored and used in API calls
- [ ] Demo data toast shows on first login
- [ ] Redirect to dashboard after login
- [ ] Error messages display correctly

---

### Task 3.2: Dashboard Page
**Priority**: HIGH
**Estimated Time**: 4 hours
**Dependencies**: Task 2.3

**Description**: Create dashboard with metrics and charts.

**Implementation Steps**:
1. Create Dashboard component
2. Add metric cards (income, expenses, balance, flagged, avg score)
3. Add charts (spending by category, fraud by category, trends)
4. Add date range filter
5. Fetch data from `/api/dashboard/summary`
6. Use chart library (Chart.js or Recharts)

**Files to Create**:
- `frontend/src/pages/Dashboard.jsx`
- `frontend/src/components/MetricCard.jsx`
- `frontend/src/components/SpendingChart.jsx`
- `frontend/src/services/dashboardService.js`

**Definition of Done**:
- [ ] Metrics display correctly
- [ ] Charts render with data
- [ ] Date range filter works
- [ ] Responsive layout
- [ ] Loading states
- [ ] Error handling

---

### Task 3.3: Transactions Page
**Priority**: HIGH
**Estimated Time**: 5 hours
**Dependencies**: Task 2.6

**Description**: Create transactions page with filtering, sorting, and live refresh.

**Implementation Steps**:
1. Create Transactions component
2. Add filter panel (type, category, date range, fraudulent)
3. Add sort controls (sortBy, sortDir)
4. Add transaction table with pagination
5. Add "Add Transaction" modal
6. Add "Live Refresh" toggle (poll every 5s)
7. Add risk level badges

**Files to Create**:
- `frontend/src/pages/Transactions.jsx`
- `frontend/src/components/TransactionTable.jsx`
- `frontend/src/components/TransactionFilters.jsx`
- `frontend/src/components/AddTransactionModal.jsx`
- `frontend/src/components/RiskBadge.jsx`
- `frontend/src/services/transactionService.js`

**Definition of Done**:
- [ ] Table displays transactions
- [ ] All filters work
- [ ] Sorting works
- [ ] Pagination works
- [ ] Add transaction modal works
- [ ] Live refresh polls correctly
- [ ] Risk badges color-coded
- [ ] Responsive design

---

### Task 3.4: Fraud Alerts Page
**Priority**: MEDIUM
**Estimated Time**: 3 hours
**Dependencies**: Task 2.4

**Description**: Create fraud alerts page with filtering and resolution.

**Implementation Steps**:
1. Create FraudAlerts component
2. Add filter controls (resolved, severity)
3. Display alert cards with transaction details
4. Add resolve button
5. Show severity badges
6. Fetch from `/api/fraud/alerts`

**Files to Create**:
- `frontend/src/pages/FraudAlerts.jsx`
- `frontend/src/components/AlertCard.jsx`
- `frontend/src/components/SeverityBadge.jsx`
- `frontend/src/services/fraudAlertService.js`

**Definition of Done**:
- [ ] Alerts display in cards
- [ ] Filters work (resolved, severity)
- [ ] Resolve button works
- [ ] Severity badges color-coded
- [ ] Transaction details shown
- [ ] Responsive design

---

### Task 3.5: Subscriptions Page
**Priority**: MEDIUM
**Estimated Time**: 3 hours
**Dependencies**: Task 2.5

**Description**: Create subscriptions page with due-soon banner.

**Implementation Steps**:
1. Create Subscriptions component
2. Add "Due Soon" banner at top
3. Display subscription list
4. Add ignore button
5. Show status badges
6. Fetch from `/api/subscriptions`

**Files to Create**:
- `frontend/src/pages/Subscriptions.jsx`
- `frontend/src/components/SubscriptionCard.jsx`
- `frontend/src/components/DueSoonBanner.jsx`
- `frontend/src/services/subscriptionService.js`

**Definition of Done**:
- [ ] Subscriptions display correctly
- [ ] Due-soon banner shows count
- [ ] Ignore button works
- [ ] Status badges display
- [ ] Responsive design

---

## Phase 4: Testing & Quality

### Task 4.1: Unit Tests for New Services
**Priority**: HIGH
**Estimated Time**: 3 hours
**Dependencies**: Task 2.1

**Description**: Write comprehensive unit tests for DemoDataService.

**Implementation Steps**:
1. Create `DemoDataServiceTest.java`
2. Test deterministic generation
3. Test transaction count range
4. Test date distribution
5. Test type distribution
6. Test fraud injection
7. Test no-seed if transactions exist

**Files to Create**:
- `src/test/java/com/example/FinSight/service/DemoDataServiceTest.java`

**Definition of Done**:
- [ ] All test cases pass
- [ ] Coverage > 90%
- [ ] Edge cases covered
- [ ] Determinism verified

---

### Task 4.2: Integration Tests for Controllers
**Priority**: HIGH
**Estimated Time**: 4 hours
**Dependencies**: Tasks 2.2-2.6

**Description**: Write integration tests for all new controllers.

**Implementation Steps**:
1. Create controller test classes
2. Use `@SpringBootTest` and `@Transactional`
3. Test all endpoints
4. Test error cases
5. Verify database state

**Files to Create**:
- `src/test/java/com/example/FinSight/controller/AuthControllerTest.java`
- `src/test/java/com/example/FinSight/controller/DashboardControllerTest.java`
- `src/test/java/com/example/FinSight/controller/FraudAlertControllerTest.java`
- `src/test/java/com/example/FinSight/controller/SubscriptionControllerTest.java`

**Definition of Done**:
- [ ] All endpoints tested
- [ ] Happy paths pass
- [ ] Error cases handled
- [ ] Coverage > 85%

---

### Task 4.3: End-to-End Tests
**Priority**: MEDIUM
**Estimated Time**: 3 hours
**Dependencies**: All Phase 2 & 3 tasks

**Description**: Write E2E tests for critical user flows.

**Implementation Steps**:
1. Test: Register → Login → Demo data → View transactions
2. Test: Add transaction → Fraud detection → Alert
3. Test: Filter/sort transactions
4. Test: Resolve fraud alert
5. Test: Detect subscriptions

**Files to Create**:
- `src/test/java/com/example/FinSight/e2e/UserFlowTest.java`

**Definition of Done**:
- [ ] All critical flows tested
- [ ] Tests pass consistently
- [ ] No flaky tests

---

## Phase 5: DevOps & Documentation

### Task 5.1: Docker Configuration
**Priority**: HIGH
**Estimated Time**: 2 hours
**Dependencies**: None

**Description**: Create Docker setup for easy deployment.

**Implementation Steps**:
1. Create backend Dockerfile
2. Create frontend Dockerfile
3. Create docker-compose.yml
4. Test multi-arch build (Apple Silicon)
5. Verify `docker compose up --build` works

**Files to Create**:
- `Dockerfile` (backend)
- `frontend/Dockerfile`
- `docker-compose.yml`
- `.dockerignore`

**Definition of Done**:
- [ ] Backend builds successfully
- [ ] Frontend builds successfully
- [ ] docker-compose starts both services
- [ ] Services communicate correctly
- [ ] Works on Apple Silicon

---

### Task 5.2: README & Documentation
**Priority**: MEDIUM
**Estimated Time**: 2 hours
**Dependencies**: All tasks

**Description**: Create comprehensive README with setup and demo instructions.

**Implementation Steps**:
1. Update README.md
2. Add setup instructions
3. Add demo walkthrough
4. Add API documentation
5. Add architecture diagram
6. Add screenshots

**Files to Modify**:
- `README.md`

**Definition of Done**:
- [ ] Setup instructions clear
- [ ] Demo steps documented
- [ ] API endpoints listed
- [ ] Architecture explained
- [ ] Screenshots included

---

### Task 5.3: Application Properties
**Priority**: LOW
**Estimated Time**: 30 minutes
**Dependencies**: None

**Description**: Configure application.properties for H2 and logging.

**Implementation Steps**:
1. Configure H2 console
2. Set logging levels
3. Configure JPA settings
4. Add demo data flag (optional)

**Files to Modify**:
- `src/main/resources/application.properties`

**Definition of Done**:
- [ ] H2 console accessible
- [ ] Logging configured
- [ ] JPA settings optimized

---

## Summary

### Total Estimated Time: 40-45 hours

### Critical Path:
1. Task 2.1: Demo Data Service (3h)
2. Task 2.2: Auth Controller (2h)
3. Task 2.6: Enhanced Transaction Controller (2h)
4. Task 3.1: Auth Pages (3h)
5. Task 3.3: Transactions Page (5h)
6. Task 4.1: Unit Tests (3h)
7. Task 5.1: Docker (2h)

**Minimum Viable Product**: Tasks 2.1, 2.2, 2.6, 3.1, 3.3, 5.1 (17 hours)

### Task Dependencies Graph:
```
Phase 1 (DONE) → Phase 2 → Phase 3 → Phase 4 → Phase 5
                    ↓         ↓         ↓         ↓
                  2.1-2.6   3.1-3.5   4.1-4.3   5.1-5.3
```

### Priority Levels:
- **HIGH**: Must have for MVP (Tasks 2.1, 2.2, 2.6, 3.1, 3.3, 4.1, 5.1)
- **MEDIUM**: Important for full feature set (Tasks 2.3-2.5, 3.2, 3.4-3.5, 4.2-4.3, 5.2)
- **LOW**: Nice to have (Task 5.3)
