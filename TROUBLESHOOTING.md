# FinSight Troubleshooting Guide

Common issues and their solutions.

## Frontend Issues

### Dashboard: "TypeError: e.spendingByCategory.map is not a function"

**Problem**: Backend returns Maps as JSON objects, but frontend tries to use `.map()` which only works on arrays.

**Solution**: ✅ FIXED - Dashboard component now converts objects to arrays using `Object.entries()`.

**Backend JSON format**:
```json
{
  "spendingByCategory": {
    "Food": 100.50,
    "Entertainment": 50.25
  },
  "fraudByCategory": {
    "Food": 2,
    "Transport": 1
  }
}
```

**Frontend conversion**:
```javascript
// Convert Map objects to arrays
const spendingCategories = summary.spendingByCategory 
  ? Object.entries(summary.spendingByCategory) 
  : []

// Now you can use .map()
spendingCategories.map(([category, amount]) => ...)
```

### Property Name Mismatches

**Problem**: Frontend uses different property names than backend DTO.

**Backend DTO** (`DashboardSummary.java`):
- `totalIncome` ✅
- `totalExpenses` ✅
- `currentBalance` ✅
- `totalFlaggedTransactions` ✅
- `averageFraudScore` ✅

**Frontend** (must match):
```javascript
summary.totalIncome      // NOT summary.income
summary.totalExpenses    // NOT summary.totalExpense
summary.currentBalance   // NOT summary.balance
```

### Add Transaction: 400 Bad Request

**Problem**: Backend expects specific data types and date format.

**Backend expects** (`TransactionRequest.java`):
- `userId`: Long (number)
- `amount`: BigDecimal (number, not string)
- `type`: String ("INCOME" or "EXPENSE")
- `category`: String
- `description`: String
- `location`: String
- `transactionDate`: LocalDateTime (ISO 8601 format with time: "2026-02-25T12:00:00")

**Frontend must send**:
```javascript
{
  userId: 1,                              // number
  amount: 100.50,                         // number, not "100.50"
  type: "EXPENSE",                        // string
  category: "groceries",                  // string
  description: "Weekly shopping",         // string
  location: "New York",                   // string
  transactionDate: "2026-02-25T12:00:00"  // ISO 8601 with time
}
```

**Solution**: ✅ FIXED - Convert types before sending:
```javascript
const payload = {
  userId: formData.userId,
  amount: parseFloat(formData.amount),           // Convert to number
  type: formData.type,
  category: formData.category,
  description: formData.description,
  location: formData.location,
  transactionDate: formData.transactionDate + 'T12:00:00'  // Add time
}
```

### Null/Undefined Values

**Problem**: Dashboard crashes when values are null or undefined.

**Solution**: Use optional chaining and default values:
```javascript
${summary.totalIncome?.toFixed(2) || '0.00'}
{summary.totalFlaggedTransactions || 0}
```

## Backend Issues

### 500 Internal Server Error on Fraudulent Transaction

**Problem**: Creating a transaction that triggers fraud detection causes a 500 error.

**Common causes**:
1. Null values in fraud detection calculations
2. Empty reasons list causing String.join to fail
3. Message exceeding database column length
4. FraudAlert save failing due to constraints

**Solution**: ✅ FIXED - Added comprehensive null safety:

**In TransactionService**:
```java
// Check for null/empty reasons
if (result.isFraudulent() && result.getReasons() != null && !result.getReasons().isEmpty()) {
    // Build message safely
    String reasonsText = String.join(", ", result.getReasons());
    String message = "Fraud detected: " + reasonsText;
    
    // Ensure message doesn't exceed database column length
    if (message.length() > 255) {
        message = message.substring(0, 252) + "...";
    }
    
    // Try-catch around fraud alert save
    try {
        fraudAlertRepository.save(alert);
    } catch (Exception e) {
        // Log error but don't fail transaction creation
        System.err.println("Failed to create fraud alert: " + e.getMessage());
    }
}
```

**In FraudDetectionService**:
```java
// Null check at start
if (transaction == null || transaction.getUser() == null) {
    return new FraudDetectionResult(false, 0.0, RiskLevel.LOW, reasons);
}

// Try-catch around each rule
try {
    if (hasHighAmountAnomaly(user, transaction.getAmount())) {
        score += 30;
        reasons.add("Amount exceeds 3x user average");
    }
} catch (Exception e) {
    System.err.println("Error in high amount anomaly check: " + e.getMessage());
}
```

**Debug steps**:
1. Check backend logs for stack trace
2. Verify all transaction fields are set
3. Check database constraints on fraud_alerts table
4. Test with simple transaction first, then add complexity

### Port 8080 Already in Use

**Windows**:
```bash
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Linux/Mac**:
```bash
lsof -ti:8080 | xargs kill -9
```

### Tests Failing

**Clean and rebuild**:
```bash
./mvnw clean install
```

**Run specific test**:
```bash
./mvnw test -Dtest=DashboardServiceTest
```

### H2 Console Not Accessible

**Check application.properties**:
```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

**Access**:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:finsight`
- Username: `sa`
- Password: (empty)

## Docker Issues

### Container Won't Start

**View logs**:
```bash
docker compose logs -f backend
docker compose logs -f frontend
```

**Clean everything**:
```bash
docker compose down -v
docker system prune -a
docker compose up --build
```

### Port Conflicts

**Check what's using ports**:
```bash
# Windows
netstat -ano | findstr :8080
netstat -ano | findstr :3000

# Linux/Mac
lsof -i :8080
lsof -i :3000
```

### Build Fails

**Backend build fails**:
```bash
# Clean Maven cache
./mvnw clean
rm -rf target/
./mvnw install
```

**Frontend build fails**:
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run build
```

## API Issues

### CORS Errors

**Check backend application.properties**:
```properties
spring.web.cors.allowed-origins=http://localhost:3000,http://localhost:5173
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.web.cors.allowed-headers=*
```

**Check frontend proxy** (vite.config.js):
```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### 401 Unauthorized

**Problem**: Token not being sent or invalid.

**Check**:
1. Token stored in localStorage: `localStorage.getItem('token')`
2. Token in Authorization header: Check browser DevTools Network tab
3. Backend expects token format: `Bearer <token>`

**Fix**:
```javascript
// In AuthContext.jsx
axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
```

### 404 Not Found

**Check endpoint URLs**:
- Backend: `http://localhost:8080/api/...`
- Frontend proxy: `/api/...` (proxied to backend)

**Verify controller mappings**:
```java
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    @GetMapping("/summary")  // Full path: /api/dashboard/summary
}
```

### 400 Bad Request / 422 Unprocessable Entity

**Problem**: Request payload doesn't match backend expectations, OR transaction saves successfully but returns wrong status code.

**Common causes**:
1. Wrong data types (string instead of number)
2. Missing required fields
3. Wrong date format
4. Invalid enum values
5. Exception thrown after successful save
6. Missing error handling in controller

**Debug steps**:
1. Check browser Network tab → Request Payload
2. Compare with backend DTO requirements
3. Check backend logs for validation errors
4. Verify field names match exactly (case-sensitive)
5. Check if transaction was actually saved in database (H2 console)

**Solution**: ✅ FIXED - Added proper error handling in controller:
```java
@PostMapping
public ResponseEntity<?> createTransaction(@RequestBody TransactionRequest request) {
    try {
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (RuntimeException e) {
        System.err.println("Error creating transaction: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("Failed to create transaction: " + e.getMessage()));
    } catch (Exception e) {
        System.err.println("Unexpected error: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("An unexpected error occurred"));
    }
}
```

**Example - Transaction creation**:
```javascript
// ❌ Wrong
{
  amount: "100.50",                    // String instead of number
  transactionDate: "2026-02-25"        // Missing time component
}

// ✅ Correct
{
  amount: 100.50,                      // Number
  transactionDate: "2026-02-25T12:00:00"  // ISO 8601 with time
}
```

**If transaction saves but returns 422**:
1. Check backend logs for exceptions after save
2. Verify response DTO can be serialized
3. Check for validation errors in response mapping
4. Ensure no exceptions in fraud detection after save

## Data Issues

### No Demo Data Generated

**Check**:
1. User has 0 transactions before login
2. Look for "Demo data generated!" message
3. Check backend logs for errors

**Manual trigger**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"password"}'
```

**Verify in H2 console**:
```sql
SELECT COUNT(*) FROM transactions WHERE user_id = 1;
```

### Transactions Not Showing

**Check filters**:
- Clear all filters
- Check date range
- Verify userId matches logged-in user

**Check API response**:
```bash
curl http://localhost:8080/api/transactions?userId=1
```

### Charts Not Rendering

**Check data format**:
```javascript
// Correct format for Chart.js
{
  labels: ['Food', 'Transport'],
  datasets: [{
    data: [100, 50],
    backgroundColor: ['#FF6384', '#36A2EB']
  }]
}
```

**Check Chart.js registration**:
```javascript
import { Chart as ChartJS, ArcElement, ... } from 'chart.js'
ChartJS.register(ArcElement, ...)
```

## Performance Issues

### Slow Dashboard Load

**Check**:
1. Number of transactions (H2 console)
2. Backend logs for slow queries
3. Network tab in browser DevTools

**Optimize**:
- Add pagination to transactions
- Cache dashboard summary
- Use database indexes

### High Memory Usage

**Backend**:
```properties
# Limit JPA batch size
spring.jpa.properties.hibernate.jdbc.batch_size=20

# Connection pooling
spring.datasource.hikari.maximum-pool-size=10
```

**Frontend**:
- Disable live refresh when not needed
- Limit transaction page size
- Clear old data from state

## Development Issues

### Hot Reload Not Working

**Frontend**:
```bash
# Restart Vite dev server
cd frontend
npm run dev
```

**Backend**:
```bash
# Spring Boot DevTools (if added)
./mvnw spring-boot:run
```

### Changes Not Reflected

**Frontend**:
1. Clear browser cache (Ctrl+Shift+R)
2. Check browser console for errors
3. Verify file saved

**Backend**:
1. Restart Spring Boot application
2. Check for compilation errors
3. Verify changes in target/ directory

## Testing Issues

### Tests Fail Locally But Pass in CI

**Check**:
1. Java version matches
2. Maven version matches
3. Clean build: `./mvnw clean test`

### Specific Test Fails

**Run with verbose output**:
```bash
./mvnw test -Dtest=DashboardServiceTest -X
```

**Check test isolation**:
- Tests should not depend on each other
- Use `@Transactional` for database tests
- Clean up test data in `@AfterEach`

## Browser Issues

### Blank Page

**Check browser console** (F12):
- Look for JavaScript errors
- Check network requests
- Verify API responses

**Common causes**:
1. Backend not running
2. CORS errors
3. JavaScript syntax errors
4. Missing dependencies

### Styling Issues

**Clear cache**:
- Hard refresh: Ctrl+Shift+R (Windows/Linux) or Cmd+Shift+R (Mac)
- Clear browser cache completely
- Try incognito/private mode

**Check CSS loading**:
- Browser DevTools → Network tab
- Look for 404 errors on CSS files
- Verify import statements in components

## Database Issues

### Data Lost on Restart

**Expected behavior**: H2 in-memory database loses data on restart.

**For persistence**:
```properties
spring.datasource.url=jdbc:h2:file:./data/finsight
```

### Cannot Connect to H2 Console

**Check**:
1. Backend is running
2. H2 console enabled in application.properties
3. Correct JDBC URL: `jdbc:h2:mem:finsight`
4. Username: `sa`, Password: (empty)

### Schema Errors

**Check**:
```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

**View generated schema**:
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

## Getting Help

### Collect Information

Before asking for help, collect:
1. Error message (full stack trace)
2. Browser console output (F12)
3. Backend logs
4. Steps to reproduce
5. Environment (OS, Java version, Node version)

### Check Logs

**Backend logs**:
```bash
# Console output when running
./mvnw spring-boot:run

# Docker logs
docker compose logs -f backend
```

**Frontend logs**:
- Browser console (F12)
- Vite dev server output
- Docker logs: `docker compose logs -f frontend`

### Useful Commands

**Check versions**:
```bash
java -version
mvn -version
node -version
npm -version
docker -version
```

**Check running processes**:
```bash
# Windows
netstat -ano | findstr :8080
netstat -ano | findstr :3000

# Linux/Mac
lsof -i :8080
lsof -i :3000
```

**Clean everything**:
```bash
# Backend
./mvnw clean
rm -rf target/

# Frontend
cd frontend
rm -rf node_modules package-lock.json dist/
npm install

# Docker
docker compose down -v
docker system prune -a
```

## Quick Fixes

### "It was working, now it's not"

1. Restart everything:
```bash
# Stop all
docker compose down
# Or Ctrl+C on running processes

# Start fresh
docker compose up --build
```

2. Clear caches:
```bash
# Browser: Ctrl+Shift+R
# Maven: ./mvnw clean
# npm: rm -rf node_modules && npm install
```

3. Check logs for errors

### "I changed code but nothing happens"

1. Verify file saved
2. Check for syntax errors
3. Restart the service
4. Clear browser cache
5. Check correct file is being edited

### "Everything is broken"

1. Check if backend is running: `curl http://localhost:8080/actuator/health`
2. Check if frontend is running: `curl http://localhost:3000`
3. Check browser console for errors
4. Check backend logs for errors
5. Try clean rebuild: `docker compose down -v && docker compose up --build`

## Still Having Issues?

1. Review [DEPLOYMENT.md](DEPLOYMENT.md)
2. Check [QUICKSTART.md](QUICKSTART.md)
3. Review [README.md](README.md)
4. Check GitHub issues
5. Review documentation in `docs/` directory

---

**Most issues can be resolved by restarting services and clearing caches!**
