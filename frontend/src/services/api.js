import axios from 'axios'

const api = axios.create({
  baseURL: '/api'
})

// Transactions
export const getTransactions = (params) => 
  api.get('/transactions', { params })

export const createTransaction = (data) => 
  api.post('/transactions', data)

// Dashboard
export const getDashboardSummary = (userId, startDate, endDate) => 
  api.get('/dashboard/summary', { params: { userId, startDate, endDate } })

// Fraud Alerts
export const getFraudAlerts = (params) => 
  api.get('/fraud/alerts', { params })

export const resolveFraudAlert = (id) => 
  api.put(`/fraud/alerts/${id}/resolve`)

// Subscriptions
export const detectSubscriptions = (userId) => 
  api.post('/subscriptions/detect', { userId })

export const getSubscriptions = (params) => 
  api.get('/subscriptions', { params })

export const ignoreSubscription = (id) => 
  api.put(`/subscriptions/${id}/ignore`)

export const getDueSoonSubscriptions = (userId, days = 7) => 
  api.get('/subscriptions/due-soon', { params: { userId, days } })

export default api
