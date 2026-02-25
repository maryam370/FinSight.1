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
- **Database**: H2 (in-memory)
- **ORM**: Spring Data JPA
- **Testing**: JUnit 5, Mockito, AssertJ, jqwik
- **Build**: Maven
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
├── docs/                        # Documentation
│   ├── ER-Diagram.md
│   ├── TDD.md
│   ├── SDLC.md
│   └── TestCase.md
├── specs/finsight/              # Specifications
│   ├── requirements.md
│   ├── design.md
│   └── tasks.md
├── pom.xml
└── README.md
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for containerized deployment)

### Local Development

#### 1. Clone Repository
```bash
git clone <repository-url>
cd FinSight
```

#### 2. Build Project
```bash
mvn clean install
```

#### 3. Run Tests
```bash
mvn test
```

#### 4. Run Application
```bash
mvn spring-boot:run
```

Application will start on `http://localhost:8080`

### Docker Deployment

#### Build and Run
```bash
docker compose up --build
```

#### Stop Services
```bash
docker compose down
```

## 📊 Database Schema

### Core Entities
- **users**: User accounts with authentication
- **transactions**: Financial transactions with fraud analysis
- **fraud_alerts**: Fraud detection alerts
- **subscriptions**: Detected recurring payments

See [ER Diagram](docs/ER-Diagram.md) for detailed schema.

## 🔌 API Endpoints

### Authentication (TO IMPLEMENT)
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
       Query params: type, category, startDate, endDate, 
                     fraudulent, sortBy, sortDir, page, size
```

### Dashboard (TO IMPLEMENT)
```
GET    /api/dashboard/summary               # Get financial summary
       Query params: userId, startDate, endDate
```

### Fraud Alerts (TO IMPLEMENT)
```
GET    /api/fraud/alerts                    # Get fraud alerts
       Query params: userId, resolved, severity
PUT    /api/fraud/alerts/{id}/resolve       # Resolve alert
```

### Subscriptions (TO IMPLEMENT)
```
POST   /api/subscriptions/detect            # Detect subscriptions
GET    /api/subscriptions                   # Get subscriptions
       Query params: userId, status
PUT    /api/subscriptions/{id}/ignore       # Ignore subscription
GET    /api/subscriptions/due-soon          # Get due-soon subscriptions
       Query params: userId, days
```

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

### ✅ Completed
- [x] Database schema and JPA entities
- [x] Repository layer with custom queries
- [x] Core service layer:
  - [x] TransactionService
  - [x] FraudDetectionService (4 rules implemented)
  - [x] DashboardService
  - [x] FraudAlertService
  - [x] SubscriptionDetectorService
- [x] Transaction filtering with JPA Specifications
- [x] Comprehensive unit tests
- [x] Integration tests
- [x] Documentation (TDD, ER Diagram, SDLC)

### 🚧 In Progress / To Do
- [ ] DemoDataService (auto-generate demo transactions)
- [ ] Authentication controller & endpoints
- [ ] Dashboard controller
- [ ] Fraud alert controller
- [ ] Subscription controller
- [ ] Enhanced transaction controller with full filtering
- [ ] Frontend implementation (React)
- [ ] Docker configuration
- [ ] End-to-end tests

See [Tasks](specs/finsight/tasks.md) for detailed implementation plan.

## 🎮 Demo Walkthrough

### First-Time User Experience
1. **Register**: Create new account
2. **Login**: System detects 0 transactions
3. **Demo Data**: 25-50 transactions auto-generated (60-90 days)
4. **Dashboard**: View financial summary and insights
5. **Transactions**: Browse, filter, and sort transactions
6. **Fraud Alerts**: Review flagged transactions
7. **Subscriptions**: See detected recurring payments

### Manual Transaction Flow
1. Click "Add Transaction" button
2. Fill form: amount, type, category, description, location, date
3. Submit → Fraud detection runs automatically
4. If fraudulent (score ≥ 70), alert is created
5. Transaction appears in list with risk badge

### Fraud Alert Resolution
1. Navigate to Fraud Alerts page
2. Review alert details and transaction
3. Click "Resolve" button
4. Alert marked as resolved

### Subscription Management
1. System auto-detects recurring payments
2. View subscriptions list
3. See "Due Soon" banner for upcoming payments
4. Ignore unwanted subscriptions

## 🔧 Configuration

### Application Properties
```properties
# Application
spring.application.name=FinSight

# H2 Database (in-memory)
spring.datasource.url=jdbc:h2:mem:finsight
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

# H2 Console (optional)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### Environment Variables
```bash
# Server port
SERVER_PORT=8080

# Database (if using external DB)
DB_URL=jdbc:h2:mem:finsight
DB_USERNAME=sa
DB_PASSWORD=

# Logging
LOGGING_LEVEL=INFO
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
