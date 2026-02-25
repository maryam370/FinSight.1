# FinSight - Quick Start Guide

Get FinSight up and running in 5 minutes!

## Prerequisites

Choose one option:

### Option A: Docker (Easiest)
- Docker Desktop installed
- Docker Compose available

### Option B: Local Development
- Java 17+
- Maven 3.8+
- Node.js 18+
- npm

## Option A: Docker Deployment (Recommended)

### 1. Start Everything
```bash
docker compose up --build
```

Wait for both services to start (~2-3 minutes first time).

### 2. Access the Application
- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`

### 3. Stop Services
```bash
docker compose down
```

## Option B: Local Development

### 1. Start Backend
```bash
# In project root
./mvnw spring-boot:run
```

Backend starts on `http://localhost:8080`

### 2. Start Frontend (New Terminal)
```bash
cd frontend
npm install
npm run dev
```

Frontend starts on `http://localhost:3000`

## First Time Usage

### 1. Register Account
- Open `http://localhost:3000`
- Click "Register"
- Fill in:
  - Full Name: Your Name
  - Username: demo
  - Email: demo@example.com
  - Password: password123
- Click "Register"

### 2. Login
- Use your credentials
- Watch for "Demo data generated!" message
- You'll be redirected to Dashboard

### 3. Explore Features

#### Dashboard
- View financial metrics
- See spending by category chart
- Check fraud statistics

#### Transactions
- Browse 25-50 auto-generated transactions
- Try filters:
  - Type: Income/Expense
  - Category: groceries, transport, etc.
  - Fraudulent: Yes/No
- Sort by date, amount, or fraud score
- Toggle "Live Refresh" for real-time updates

#### Add Transaction
- Click "Add Transaction"
- Fill in details:
  - Amount: 500
  - Type: Expense
  - Category: electronics
  - Description: New laptop
  - Location: New York
  - Date: Today
- Submit and watch fraud detection run

#### Fraud Alerts
- View flagged transactions
- Filter by severity (Low/Medium/High)
- Click "Resolve Alert" to mark as handled

#### Subscriptions
- Click "Detect Subscriptions"
- See recurring payments
- Check "Due Soon" banner
- Click "Ignore" on unwanted subscriptions

## Testing API Directly

### Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

Response includes:
```json
{
  "token": "mock-token-123",
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "fullName": "Test User"
  },
  "demoSeeded": true
}
```

### Get Transactions
```bash
curl http://localhost:8080/api/transactions?userId=1 \
  -H "Authorization: Bearer mock-token-123"
```

### Get Dashboard
```bash
curl http://localhost:8080/api/dashboard/summary?userId=1 \
  -H "Authorization: Bearer mock-token-123"
```

## H2 Database Console

Access the in-memory database:

1. Open `http://localhost:8080/h2-console`
2. Enter connection details:
   - JDBC URL: `jdbc:h2:mem:finsight`
   - Username: `sa`
   - Password: (leave empty)
3. Click "Connect"

### Useful Queries
```sql
-- View all users
SELECT * FROM users;

-- View all transactions
SELECT * FROM transactions;

-- View fraudulent transactions
SELECT * FROM transactions WHERE fraudulent = true;

-- View fraud alerts
SELECT * FROM fraud_alerts;

-- View subscriptions
SELECT * FROM subscriptions;
```

## Troubleshooting

### Port Already in Use

#### Backend (8080)
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

#### Frontend (3000)
```bash
# Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:3000 | xargs kill -9
```

### Docker Issues

#### Clean Everything
```bash
docker compose down -v
docker system prune -a
docker compose up --build
```

#### View Logs
```bash
# All services
docker compose logs -f

# Backend only
docker compose logs -f backend

# Frontend only
docker compose logs -f frontend
```

### Backend Won't Start

1. Check Java version:
```bash
java -version
```

2. Clean and rebuild:
```bash
./mvnw clean install
```

3. Check logs for errors

### Frontend Won't Start

1. Check Node version:
```bash
node --version
```

2. Clean and reinstall:
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

3. Check for port conflicts

### No Demo Data Generated

1. Check user has 0 transactions
2. Look for "Demo data generated!" message on login
3. Check backend logs for errors
4. Try logging out and back in

## Next Steps

### Learn More
- [README.md](README.md) - Full project overview
- [DEPLOYMENT.md](DEPLOYMENT.md) - Detailed deployment guide
- [BACKEND_COMPLETE.md](BACKEND_COMPLETE.md) - Backend documentation
- [specs/finsight/requirements.md](specs/finsight/requirements.md) - Requirements
- [specs/finsight/design.md](specs/finsight/design.md) - API design

### Customize
- Modify fraud detection rules in `FraudDetectionService.java`
- Adjust demo data generation in `DemoDataService.java`
- Change subscription detection logic in `SubscriptionDetectorService.java`
- Update frontend styling in CSS files

### Extend
- Add more transaction categories
- Implement email notifications
- Add export to CSV/PDF
- Create mobile app
- Integrate with real bank APIs

## Common Use Cases

### Demo for Stakeholders
1. Start with Docker: `docker compose up --build`
2. Register account with realistic name
3. Show dashboard with auto-generated data
4. Add a high-value transaction to trigger fraud alert
5. Demonstrate fraud alert resolution
6. Show subscription detection

### Development
1. Start backend: `./mvnw spring-boot:run`
2. Start frontend: `cd frontend && npm run dev`
3. Make changes to code
4. Frontend hot-reloads automatically
5. Backend requires restart

### Testing
1. Run backend tests: `./mvnw test`
2. Check coverage: `./mvnw test jacoco:report`
3. Test API with curl or Postman
4. Test frontend in browser

## Support

### Documentation
- Check [DEPLOYMENT.md](DEPLOYMENT.md) for detailed instructions
- Review [PROJECT_COMPLETE.md](PROJECT_COMPLETE.md) for feature list
- Read [docs/TDD.md](docs/TDD.md) for testing approach

### Issues
- Check logs for error messages
- Verify prerequisites are installed
- Try clean rebuild
- Check port availability

## Success Checklist

- [ ] Docker/Java/Node installed
- [ ] Services start without errors
- [ ] Can access frontend at localhost:3000
- [ ] Can register new account
- [ ] Demo data generates on login
- [ ] Dashboard displays metrics
- [ ] Can view and filter transactions
- [ ] Can add manual transaction
- [ ] Fraud detection triggers
- [ ] Can view and resolve fraud alerts
- [ ] Can detect and manage subscriptions

## Tips

1. **Use Docker** for quickest setup
2. **Check logs** if something doesn't work
3. **Clear browser cache** if frontend looks broken
4. **Restart services** if behavior is unexpected
5. **Use H2 console** to inspect database state

---

**Ready to go!** Start with `docker compose up --build` and open `http://localhost:3000`

Happy tracking! 🚀
