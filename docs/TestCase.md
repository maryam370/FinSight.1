# Test Cases Document

## Unit Test Cases

### UserService Tests
1. TC-001: Create User - Valid Data
2. TC-002: Create User - Duplicate Email
3. TC-003: Authenticate User - Valid Credentials
4. TC-004: Authenticate User - Invalid Password

### TransactionService Tests
5. TC-005: Create Transaction - Valid Amount
6. TC-006: Create Transaction - Negative Amount
7. TC-007: Get User Transactions - Valid User
8. TC-008: Get User Transactions - No Transactions

### FraudDetectionService Tests
9. TC-009: Detect Fraud - Unusual Large Amount
10. TC-010: Detect Fraud - Multiple Transactions
11. TC-011: Detect Fraud - Unusual Location
12. TC-012: Detect Fraud - Normal Pattern

## Integration Test Cases
13. TC-013: Complete Transaction Flow
14. TC-014: Fraud Alert Generation
15. TC-015: Report Generation
16. TC-016: Concurrent Transactions

## Security Test Cases
17. TC-017: Unauthorized Access Attempt
18. TC-018: SQL Injection Prevention
19. TC-019: XSS Prevention
20. TC-020: Rate Limiting