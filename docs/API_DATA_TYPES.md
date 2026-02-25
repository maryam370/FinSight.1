# API Data Types Reference

Quick reference for frontend-backend data type mappings.

## Transaction Request

### Backend DTO (`TransactionRequest.java`)
```java
public class TransactionRequest {
    private Long userId;              // Java Long
    private BigDecimal amount;        // Java BigDecimal
    private String type;              // String enum: "INCOME" or "EXPENSE"
    private String category;          // String
    private String description;       // String
    private String location;          // String
    private LocalDateTime transactionDate;  // ISO 8601 with time
}
```

### Frontend Payload (JavaScript)
```javascript
{
  userId: 1,                              // number
  amount: 100.50,                         // number (not string "100.50")
  type: "EXPENSE",                        // string: "INCOME" or "EXPENSE"
  category: "groceries",                  // string
  description: "Weekly shopping",         // string
  location: "New York",                   // string
  transactionDate: "2026-02-25T12:00:00"  // ISO 8601 string with time
}
```

### Conversion Example
```javascript
// From form data (strings) to API payload (correct types)
const formData = {
  userId: 1,
  amount: "100.50",                    // string from input
  type: "EXPENSE",
  category: "groceries",
  description: "Weekly shopping",
  location: "New York",
  transactionDate: "2026-02-25"        // date from input (no time)
}

// Convert to correct types
const payload = {
  userId: formData.userId,             // already number
  amount: parseFloat(formData.amount), // string → number
  type: formData.type,                 // already string
  category: formData.category,         // already string
  description: formData.description,   // already string
  location: formData.location,         // already string
  transactionDate: formData.transactionDate + 'T12:00:00'  // add time
}
```

## Dashboard Summary Response

### Backend DTO (`DashboardSummary.java`)
```java
public class DashboardSummary {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal currentBalance;
    private Long totalFlaggedTransactions;
    private Double averageFraudScore;
    private Map<String, BigDecimal> spendingByCategory;  // Map → Object
    private Map<String, Long> fraudByCategory;           // Map → Object
    private List<TimeSeriesPoint> spendingTrends;        // List → Array
}
```

### Frontend Response (JavaScript)
```javascript
{
  totalIncome: 5000.00,                // number
  totalExpenses: 3500.50,              // number
  currentBalance: 1499.50,             // number
  totalFlaggedTransactions: 5,         // number
  averageFraudScore: 45.2,             // number
  spendingByCategory: {                // object (not array!)
    "Food": 500.00,
    "Transport": 200.00
  },
  fraudByCategory: {                   // object (not array!)
    "Food": 2,
    "Transport": 1
  },
  spendingTrends: [                    // array
    { date: "2026-02-01", amount: 100 },
    { date: "2026-02-02", amount: 150 }
  ]
}
```

### Conversion Example
```javascript
// Convert Map objects to arrays for iteration
const spendingCategories = Object.entries(summary.spendingByCategory)
// Result: [["Food", 500.00], ["Transport", 200.00]]

// Now you can use .map()
spendingCategories.map(([category, amount]) => {
  console.log(`${category}: $${amount}`)
})
```

## User Authentication

### Register Request
```javascript
{
  username: "demo",        // string
  email: "demo@test.com",  // string
  password: "password123", // string
  fullName: "Demo User"    // string
}
```

### Login Request
```javascript
{
  username: "demo",        // string
  password: "password123"  // string
}
```

### Login Response
```javascript
{
  token: "mock-token-123",           // string
  user: {
    id: 1,                           // number
    username: "demo",                // string
    email: "demo@test.com",          // string
    fullName: "Demo User"            // string
  },
  demoSeeded: true                   // boolean
}
```

## Transaction Response

### Backend DTO (`TransactionResponse.java`)
```java
public class TransactionResponse {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String type;
    private String category;
    private String description;
    private String location;
    private LocalDateTime transactionDate;
    private Integer fraudScore;
    private Boolean fraudulent;
    private String riskLevel;
    private LocalDateTime createdAt;
}
```

### Frontend Response (JavaScript)
```javascript
{
  id: 1,                                    // number
  userId: 1,                                // number
  amount: 100.50,                           // number
  type: "EXPENSE",                          // string
  category: "groceries",                    // string
  description: "Weekly shopping",           // string
  location: "New York",                     // string
  transactionDate: "2026-02-25T12:00:00",   // ISO 8601 string
  fraudScore: 45,                           // number
  fraudulent: false,                        // boolean
  riskLevel: "MEDIUM",                      // string: "LOW", "MEDIUM", "HIGH"
  createdAt: "2026-02-25T10:30:00"          // ISO 8601 string
}
```

## Fraud Alert

### Frontend Response
```javascript
{
  id: 1,                                    // number
  userId: 1,                                // number
  transactionId: 5,                         // number
  message: "High amount anomaly detected",  // string
  severity: "HIGH",                         // string: "LOW", "MEDIUM", "HIGH"
  resolved: false,                          // boolean
  createdAt: "2026-02-25T10:30:00",         // ISO 8601 string
  transactionDescription: "Laptop purchase", // string (from joined transaction)
  transactionAmount: 2500.00                // number (from joined transaction)
}
```

## Subscription

### Frontend Response
```javascript
{
  id: 1,                                    // number
  userId: 1,                                // number
  merchant: "Netflix",                      // string
  avgAmount: 15.99,                         // number
  lastPaidDate: "2026-02-01T00:00:00",      // ISO 8601 string
  nextDueDate: "2026-03-01T00:00:00",       // ISO 8601 string
  status: "ACTIVE",                         // string: "ACTIVE", "IGNORED"
  createdAt: "2026-02-25T10:30:00"          // ISO 8601 string
}
```

## Common Type Conversions

### String to Number
```javascript
// Input field value (always string)
const amountStr = "100.50"

// Convert to number
const amount = parseFloat(amountStr)  // 100.50
const userId = parseInt(userIdStr)    // 1
```

### Date to ISO 8601 with Time
```javascript
// Date input value (YYYY-MM-DD)
const dateStr = "2026-02-25"

// Add time component
const dateTime = dateStr + "T12:00:00"  // "2026-02-25T12:00:00"

// Or use current time
const dateTime = new Date(dateStr).toISOString()  // "2026-02-25T00:00:00.000Z"
```

### Object to Array (for Maps)
```javascript
// Backend sends Map as object
const spendingByCategory = {
  "Food": 500.00,
  "Transport": 200.00
}

// Convert to array of [key, value] pairs
const entries = Object.entries(spendingByCategory)
// [["Food", 500.00], ["Transport", 200.00]]

// Or get just keys
const categories = Object.keys(spendingByCategory)
// ["Food", "Transport"]

// Or get just values
const amounts = Object.values(spendingByCategory)
// [500.00, 200.00]
```

### Boolean Conversion
```javascript
// String to boolean
const fraudulentStr = "true"
const fraudulent = fraudulentStr === "true"  // true

// Or use Boolean()
const fraudulent = Boolean(fraudulentStr)  // true (any non-empty string)
```

## Validation Rules

### Amount
- Must be positive number
- Max 2 decimal places
- Backend: `BigDecimal`
- Frontend: `parseFloat(value)`

### Type
- Must be: "INCOME" or "EXPENSE"
- Case-sensitive
- Backend: String enum

### Date
- Must include time component
- Format: ISO 8601 (YYYY-MM-DDTHH:mm:ss)
- Backend: `LocalDateTime`
- Frontend: Add "T12:00:00" to date string

### Risk Level
- Values: "LOW", "MEDIUM", "HIGH"
- Read-only (calculated by backend)
- Case-sensitive

### Status
- Subscription: "ACTIVE", "IGNORED"
- Case-sensitive

## Common Mistakes

### ❌ Wrong
```javascript
// Sending string instead of number
{ amount: "100.50" }

// Missing time component
{ transactionDate: "2026-02-25" }

// Trying to use .map() on object
summary.spendingByCategory.map(...)

// Wrong property name
summary.balance  // Should be currentBalance
```

### ✅ Correct
```javascript
// Number type
{ amount: 100.50 }

// ISO 8601 with time
{ transactionDate: "2026-02-25T12:00:00" }

// Convert object to array first
Object.entries(summary.spendingByCategory).map(...)

// Correct property name
summary.currentBalance
```

## Testing Payloads

### Using curl
```bash
# Create transaction
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "amount": 100.50,
    "type": "EXPENSE",
    "category": "groceries",
    "description": "Weekly shopping",
    "location": "New York",
    "transactionDate": "2026-02-25T12:00:00"
  }'
```

### Using Browser Console
```javascript
// Test transaction creation
const payload = {
  userId: 1,
  amount: 100.50,
  type: "EXPENSE",
  category: "groceries",
  description: "Weekly shopping",
  location: "New York",
  transactionDate: "2026-02-25T12:00:00"
}

fetch('/api/transactions', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(payload)
})
.then(r => r.json())
.then(console.log)
```

## Debugging Tips

1. **Check Network Tab**: See actual request payload
2. **Console.log payload**: Before sending to API
3. **Check backend logs**: See validation errors
4. **Use typeof**: Verify data types
   ```javascript
   console.log(typeof payload.amount)  // Should be "number"
   ```
5. **Validate dates**: Ensure ISO 8601 format
   ```javascript
   console.log(new Date(payload.transactionDate))  // Should be valid date
   ```

## References

- [ISO 8601 Date Format](https://en.wikipedia.org/wiki/ISO_8601)
- [JavaScript Date](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date)
- [Object.entries()](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/entries)
- [parseFloat()](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/parseFloat)
