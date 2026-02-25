# FinSight - Financial Transaction Tracker & Fraud Detection

A hackathon-style financial monitoring system with real-time transaction tracking, rule-based fraud detection, actionable insights, and subscription management.

## 🎯 Features

### Core Capabilities
- **Real-Time Transaction Tracking**: Monitor financial transactions with live refresh
- **Fraud Detection**: Rule-based scoring system with explainable results
- **Actionable Insights**: Dashboard with spending analytics and trends
- **Advanced Filtering**: Filter and sort transactions by multiple criteria
- **Subscription Detection**: Automatically identify recurring payments
- **Demo Data**: Auto-generate realistic transactions for first-time users

### Fraud Detection Rules
1. **High Amount Anomaly** (+30 points): Amount > 3x user average
2. **Rapid-Fire Activity** (+25 points): 5+ transactions in 10 minutes
3. **Geographical Anomaly** (+25 points): Different location within 2 hours
4. **Unusual Category** (+20 points): New category for user

**Risk Levels**:
- LOW: 0-39 points
- MEDIUM: 40-69 points
- HIGH: 70-100 points (flagged as fraudulent)

## 🏗️ Architecture

### Technology Stack
- **Backend**: Java 17 + Spring Boot 4.0.3
- **Frontend**: React 18 + Vite
- **Database**: H2 (in-memory)
- **ORM**: Spring Data JPA
- **Charts**: Chart.js + react-chartjs-2
- **Testing**: JUnit 5, Mockito, AssertJ, jqwik
- **Build**: Maven (backend), npm (frontend)
- **Containerization**: Docker + Docker Compose

### Project Structure
```
FinSight/
├── src/
│   ├── main/
│   │   ├── java/com/example/FinSight/
│   │   │   ├── controller/      # REST API endpoints
│   │   │   ├── service/         # Business logic
│   │   │   ├── repository/      # Data access
│   │   │   ├── model/           # JPA entities
│   │   │   ├── dto/             # Data transfer objects
│   │   │   └── specification/   # Query specifications
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/example/FinSight/
│           └── service/         # Unit & integration tests
├── frontend/                    # React frontend
│   ├── src/
│   │   ├── components/         # Reusable components
│   │   ├── pages/              # Page components
│   │   ├── context/            # React context
│   │   ├── services/           # API services
│   │   └── App.jsx
│   ├── Dockerfile
│   └── package.json
├── docs/                        # Documentation
├── specs/finsight/              # Specifications
├── Dockerfile                   # Backend Docker config
├── docker-compose.yml           # Multi-container setup
├── pom.xml
└── README.md
```

## 🚀 Quick Start

**New to FinSight?** See [QUICKSTART.md](QUICKSTART.md) for a 5-minute setup guide.

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+ and npm
- Docker & Docker Compose (for containerized deployment)

### Option 1: Docker Deployment (Recommended)

#### Build and Run Everything
```bash
docker compose up --build
```

This starts:
- Backend on `http://localhost:8080`
- Frontend on `http://localhost:3000`

#### Stop Services
```bash
docker compose down
```

### Option 2: Local Development

#### Backend
```bash
# Build and test
./mvnw clean install

# Run backend
./mvnw spring-boot:run
```

Backend runs on `http://localhost:8080`

#### Frontend
```bash
cd frontend

# Install dependencies
npm install

# Run development server
npm run dev
```

Frontend runs on `http://localhost:3000`

### First Time Setup

1. Open `http://localhost:3000` in browser
2. Click "Register" and create an account
3. Login with your credentials
4. Demo data (25-50 transactions) will be auto-generated
5. Explore Dashboard, Transactions, Fraud Alerts, and Subscriptions

See [QUICKSTART.md](QUICKSTART.md) for detailed walkthrough and [DEPLOYMENT.md](DEPLOYMENT.md) for advanced deployment options.

## 📊 Database Schema

### Core Entities
- **users**: User accounts with authentication
- **transactions**: Financial transactions with fraud analysis
- **fraud_alerts**: Fraud detection alerts
- **subscriptions**: Detected recurring payments

### H2 Console Access
When backend is running:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:finsight`
- Username: `sa`
- Password: (leave empty)

See [ER Diagram](docs/ER-Diagram.md) for detailed schema.

## 🔌 API Endpoints

### Authentication
```
POST   /api/auth/register    # Register new user
POST   /api/auth/login       # Login (triggers demo data for first-time users)
GET    /api/auth/me          # Get current user info
```

### Transactions
```
POST   /api/transactions                    # Create transaction
GET    /api/transactions/user/{userId}      # Get user transactions
GET    /api/transactions/fraud/{userId}     # Get fraudulent transactions
GET    /api/transactions                    # Advanced filtering & sorting
       Query params: userId, type, category, startDate, endDate, 
                     fraudulent, sortBy, sortDir, page, size
```

### Dashboard
```
GET    /api/dashboard/summary               # Get financial summary
       Query params: userId, startDate, endDate
```

### Fraud Alerts
```
GET    /api/fraud/alerts                    # Get fraud alerts
       Query params: userId, resolved, severity
PUT    /api/fraud/alerts/{id}/resolve       # Resolve alert
```

### Subscriptions
```
POST   /api/subscriptions/detect            # Detect subscriptions
GET    /api/subscriptions                   # Get subscriptions
       Query params: userId, status
PUT    /api/subscriptions/{id}/ignore       # Ignore subscription
GET    /api/subscriptions/due-soon          # Get due-soon subscriptions
       Query params: userId, days
```

See [Design Specification](specs/finsight/design.md) for detailed API documentation.

## 🧪 Testing

### Test Coverage
- **Line Coverage**: > 85%
- **Branch Coverage**: > 80%
- **Method Coverage**: > 90%

### Run Tests
```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=FraudDetectionServiceTest

# With coverage report
mvn test jacoco:report
```

### Test Categories
1. **Unit Tests**: Service layer with mocked dependencies
2. **Integration Tests**: Full Spring context with H2 database
3. **Property-Based Tests**: jqwik for invariant testing

See [TDD Documentation](docs/TDD.md) for detailed testing strategy.

## 📝 Implementation Status

### ✅ Completed (100%)
- [x] Backend: All models, repositories, services, controllers
- [x] Backend: Fraud detection with 4 rules
- [x] Backend: Demo data generation (deterministic)
- [x] Backend: Subscription detection
- [x] Backend: Dashboard analytics
- [x] Backend: 95/95 tests passing
- [x] Frontend: React application with all pages
- [x] Frontend: Authentication (login/register)
- [x] Frontend: Dashboard with charts
- [x] Frontend: Transactions with filtering/sorting
- [x] Frontend: Fraud alerts management
- [x] Frontend: Subscriptions tracking
- [x] Docker: Multi-container setup
- [x] Documentation: Complete specs and guides

### 🎯 Ready for Demo
The application is fully functional and ready to use. All features are implemented and tested.

See [BACKEND_COMPLETE.md](BACKEND_COMPLETE.md) for backend details and [DEPLOYMENT.md](DEPLOYMENT.md) for deployment instructions.

## 🎮 Demo Walkthrough

### Quick Demo (5 minutes)

1. **Start Application**
   ```bash
   docker compose up --build
   ```

2. **Open Browser**
   - Navigate to `http://localhost:3000`

3. **Register Account**
   - Click "Register"
   - Fill in: username, email, password, full name
   - Submit

4. **Login**
   - Use your credentials
   - Demo data auto-generates (25-50 transactions)
   - You'll see "Demo data generated!" message

5. **Explore Dashboard**
   - View financial metrics
   - See spending by category chart
   - Check fraud statistics

6. **Browse Transactions**
   - Filter by type, category, fraudulent status
   - Sort by date, amount, or fraud score
   - Toggle "Live Refresh" for real-time updates
   - Notice risk badges (LOW/MEDIUM/HIGH)

7. **Check Fraud Alerts**
   - View flagged transactions
   - Filter by severity
   - Click "Resolve Alert" to mark as handled

8. **Manage Subscriptions**
   - Click "Detect Subscriptions"
   - See recurring payments
   - Check "Due Soon" banner
   - Ignore unwanted subscriptions

9. **Add Manual Transaction**
   - Click "Add Transaction"
   - Fill in details
   - Submit and watch fraud detection run

### Testing API Directly

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","email":"demo@example.com","password":"password","fullName":"Demo User"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"password"}'

# Get transactions (use token from login response)
curl http://localhost:8080/api/transactions?userId=1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 🔧 Configuration

### Backend (application.properties)
```properties
# Server
server.port=8080

# H2 Database
spring.datasource.url=jdbc:h2:mem:finsight
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

# CORS (for frontend)
spring.web.cors.allowed-origins=http://localhost:3000,http://localhost:5173
```

### Frontend (vite.config.js)
```javascript
export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

### Docker (docker-compose.yml)
```yaml
services:
  backend:
    ports:
      - "8080:8080"
  frontend:
    ports:
      - "3000:80"
```

## 📚 Documentation

- [Requirements Specification](specs/finsight/requirements.md)
- [Design Specification](specs/finsight/design.md)
- [Implementation Tasks](specs/finsight/tasks.md)
- [TDD Approach](docs/TDD.md)
- [ER Diagram](docs/ER-Diagram.md)
- [SDLC Process](docs/SDLC.md)
- [Test Cases](docs/TestCase.md)

## 🤝 Contributing

### Development Workflow
1. Create feature branch from `main`
2. Write failing tests (TDD approach)
3. Implement minimal code to pass tests
4. Refactor while keeping tests green
5. Ensure coverage > 85%
6. Submit pull request

### Code Standards
- Follow Java naming conventions
- Use Lombok for boilerplate reduction
- Write descriptive test names
- Document complex business logic
- Keep methods focused and small

## 🐛 Troubleshooting

### Common Issues

**Issue**: Tests fail with database errors
- **Solution**: Ensure H2 dependency is in classpath, check `@Transactional` on test classes

**Issue**: Fraud detection not triggering
- **Solution**: Verify user has baseline transactions for average calculation

**Issue**: Demo data not generating
- **Solution**: Check user has 0 transactions, verify DemoDataService is called

**Issue**: Docker build fails
- **Solution**: Run `mvn clean package` first, check Dockerfile paths

## 📄 License

This project is created for educational/hackathon purposes.

## 👥 Team

Developed as part of FinSight hackathon project.

## 🔮 Future Enhancements

- Machine learning fraud detection
- Real bank account integration (Plaid, Yodlee)
- Multi-currency support
- Budget planning and goals
- Bill payment reminders
- Export to CSV/PDF
- Email notifications
- Mobile native apps
- Two-factor authentication
- Social features (spending comparisons)

## 📞 Support

For issues and questions:
- Check [Documentation](docs/)
- Review [Tasks](specs/finsight/tasks.md)
- Open GitHub issue

---

**Built with ❤️ using Spring Boot, JPA, and TDD principles**
