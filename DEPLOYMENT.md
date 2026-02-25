# FinSight Deployment Guide

Complete guide for deploying FinSight locally and with Docker.

## Prerequisites

### Local Development
- Java 17+
- Maven 3.8+
- Node.js 18+
- npm or yarn

### Docker Deployment
- Docker 20.10+
- Docker Compose 2.0+

## Local Development Setup

### Backend

1. Navigate to project root:
```bash
cd FinSight
```

2. Build the project:
```bash
./mvnw clean install
```

3. Run tests:
```bash
./mvnw test
```

4. Start the backend:
```bash
./mvnw spring-boot:run
```

Backend runs on `http://localhost:8080`

### Frontend

1. Navigate to frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start development server:
```bash
npm run dev
```

Frontend runs on `http://localhost:3000`

### Access H2 Console

When backend is running:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:finsight`
- Username: `sa`
- Password: (leave empty)

## Docker Deployment

### Quick Start

From project root:

```bash
docker compose up --build
```

This will:
1. Build backend Docker image
2. Build frontend Docker image
3. Start both services
4. Backend available at `http://localhost:8080`
5. Frontend available at `http://localhost:3000`

### Stop Services

```bash
docker compose down
```

### View Logs

```bash
# All services
docker compose logs -f

# Backend only
docker compose logs -f backend

# Frontend only
docker compose logs -f frontend
```

### Rebuild Specific Service

```bash
# Rebuild backend
docker compose up --build backend

# Rebuild frontend
docker compose up --build frontend
```

## Multi-Architecture Support (Apple Silicon)

The Dockerfiles use multi-arch base images that work on:
- x86_64 (Intel/AMD)
- arm64 (Apple Silicon M1/M2/M3)

No special configuration needed.

## Production Deployment

### Backend Configuration

For production, update `src/main/resources/application.properties`:

```properties
# Use external database instead of H2
spring.datasource.url=jdbc:postgresql://localhost:5432/finsight
spring.datasource.username=finsight_user
spring.datasource.password=secure_password

# Disable H2 console
spring.h2.console.enabled=false

# Production logging
logging.level.root=WARN
logging.level.com.example.FinSight=INFO
```

### Environment Variables

Set via Docker Compose or environment:

```yaml
services:
  backend:
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/finsight
      - SPRING_DATASOURCE_USERNAME=finsight_user
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - SPRING_PROFILES_ACTIVE=prod
```

### Security Considerations

1. **Authentication**: Current implementation is simplified for hackathon
   - Add proper JWT token generation
   - Implement password hashing (BCrypt)
   - Add token expiration and refresh

2. **CORS**: Configure allowed origins in production
   ```properties
   spring.web.cors.allowed-origins=https://yourdomain.com
   ```

3. **HTTPS**: Use reverse proxy (nginx/traefik) with SSL certificates

4. **Database**: Use persistent database (PostgreSQL/MySQL) instead of H2

## Testing Deployment

### 1. Register a User

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

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

Response includes:
- `token`: Authentication token
- `user`: User details
- `demoSeeded`: true if demo data was generated

### 3. Get Transactions

```bash
curl http://localhost:8080/api/transactions?userId=1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. Get Dashboard

```bash
curl http://localhost:8080/api/dashboard/summary?userId=1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Troubleshooting

### Backend Won't Start

1. Check Java version:
```bash
java -version
```

2. Check port 8080 is available:
```bash
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080
```

3. Check logs:
```bash
./mvnw spring-boot:run
```

### Frontend Won't Start

1. Check Node version:
```bash
node --version
```

2. Clear node_modules and reinstall:
```bash
rm -rf node_modules package-lock.json
npm install
```

3. Check port 3000 is available

### Docker Build Fails

1. Check Docker is running:
```bash
docker --version
docker compose version
```

2. Clean Docker cache:
```bash
docker system prune -a
```

3. Rebuild from scratch:
```bash
docker compose down -v
docker compose up --build
```

### Database Connection Issues

1. Check H2 console is enabled in application.properties
2. Verify JDBC URL is correct
3. Check logs for database errors

### CORS Errors

1. Verify backend CORS configuration in application.properties
2. Check frontend is using correct API base URL
3. Ensure proxy is configured in vite.config.js (dev) or nginx.conf (prod)

## Performance Optimization

### Backend

1. Enable caching:
```properties
spring.cache.type=caffeine
```

2. Optimize JPA queries:
```properties
spring.jpa.properties.hibernate.jdbc.batch_size=20
```

3. Connection pooling:
```properties
spring.datasource.hikari.maximum-pool-size=10
```

### Frontend

1. Build for production:
```bash
npm run build
```

2. Enable gzip in nginx (already configured)

3. Use CDN for static assets

## Monitoring

### Health Checks

Backend health endpoint:
```bash
curl http://localhost:8080/actuator/health
```

Frontend health (Docker):
```bash
curl http://localhost:3000/
```

### Logs

Backend logs location:
- Console output (default)
- Configure file logging in application.properties

Frontend logs:
- Browser console (dev)
- Nginx access/error logs (prod)

## Backup and Recovery

### H2 Database (Development)

Data is in-memory and lost on restart. For persistence:

```properties
spring.datasource.url=jdbc:h2:file:./data/finsight
```

### PostgreSQL (Production)

Regular backups:
```bash
pg_dump -U finsight_user finsight > backup.sql
```

Restore:
```bash
psql -U finsight_user finsight < backup.sql
```

## Scaling

### Horizontal Scaling

1. Use external database (PostgreSQL/MySQL)
2. Deploy multiple backend instances behind load balancer
3. Use Redis for session management
4. Serve frontend from CDN

### Vertical Scaling

Increase Docker container resources:

```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
```

## Support

For issues:
1. Check logs
2. Review this deployment guide
3. Check GitHub issues
4. Consult documentation in `docs/` directory

## Next Steps

After successful deployment:
1. Test all features through UI
2. Run integration tests
3. Configure monitoring
4. Set up CI/CD pipeline
5. Plan production database migration
