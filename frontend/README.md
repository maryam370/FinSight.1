# FinSight Frontend

React-based frontend for the FinSight financial tracking and fraud detection system.

## Technology Stack

- React 18
- React Router 6
- Axios for API calls
- Chart.js + react-chartjs-2 for visualizations
- Vite for build tooling

## Getting Started

### Prerequisites

- Node.js 18+ and npm

### Installation

```bash
cd frontend
npm install
```

### Development

```bash
npm run dev
```

Application runs on `http://localhost:3000`

### Build

```bash
npm run build
```

Outputs to `dist/` directory.

### Preview Production Build

```bash
npm run preview
```

## Features

### Authentication
- Login with username/password
- Registration for new users
- Demo data auto-generation on first login
- Token-based authentication

### Dashboard
- Financial metrics (income, expenses, balance)
- Fraud statistics
- Spending by category (pie chart)
- Fraud by category (bar chart)

### Transactions
- Filterable transaction list
- Sort by date, amount, or fraud score
- Live refresh toggle (polls every 5 seconds)
- Add manual transactions
- Risk level badges
- Fraudulent transaction highlighting

### Fraud Alerts
- View all fraud alerts
- Filter by status (resolved/unresolved)
- Filter by severity (low/medium/high)
- Resolve alerts with one click
- Transaction details in each alert

### Subscriptions
- Auto-detect recurring payments
- Due soon banner
- Ignore unwanted subscriptions
- Next payment date tracking

## Project Structure

```
frontend/
├── public/              # Static assets
├── src/
│   ├── components/      # Reusable components
│   │   └── Layout.jsx   # Main layout with navigation
│   ├── context/         # React context providers
│   │   └── AuthContext.jsx
│   ├── pages/           # Page components
│   │   ├── Login.jsx
│   │   ├── Register.jsx
│   │   ├── Dashboard.jsx
│   │   ├── Transactions.jsx
│   │   ├── FraudAlerts.jsx
│   │   └── Subscriptions.jsx
│   ├── services/        # API service layer
│   │   └── api.js
│   ├── App.jsx          # Root component
│   ├── main.jsx         # Entry point
│   └── index.css        # Global styles
├── Dockerfile           # Docker build configuration
├── nginx.conf           # Nginx configuration for production
├── vite.config.js       # Vite configuration
└── package.json
```

## API Integration

The frontend communicates with the backend via REST API:

- Base URL: `/api` (proxied to `http://localhost:8080` in dev)
- Authentication: Bearer token in Authorization header
- All API calls in `src/services/api.js`

## Styling

- Minimal custom CSS
- Responsive design
- Color-coded badges for risk levels and transaction types
- Clean card-based layout

## Docker Deployment

Build and run with Docker:

```bash
docker build -t finsight-frontend .
docker run -p 3000:80 finsight-frontend
```

Or use docker-compose from project root:

```bash
docker compose up --build
```

## Environment Variables

No environment variables required. API proxy configured in `vite.config.js` for development and `nginx.conf` for production.

## Browser Support

- Chrome/Edge (latest)
- Firefox (latest)
- Safari (latest)

## Contributing

1. Follow React best practices
2. Use functional components with hooks
3. Keep components focused and small
4. Add CSS modules for component-specific styles
5. Test in multiple browsers

## License

Educational/Hackathon project
