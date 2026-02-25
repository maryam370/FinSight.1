import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { getDashboardSummary } from '../services/api'
import { Chart as ChartJS, ArcElement, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js'
import { Pie, Bar } from 'react-chartjs-2'
import './Dashboard.css'

ChartJS.register(ArcElement, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

function Dashboard() {
  const { user } = useAuth()
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadDashboard()
  }, [])

  const loadDashboard = async () => {
    try {
      const response = await getDashboardSummary(user.id)
      setSummary(response.data)
    } catch (err) {
      setError('Failed to load dashboard')
    } finally {
      setLoading(false)
    }
  }

  if (loading) return <div className="loading">Loading dashboard...</div>
  if (error) return <div className="alert alert-error">{error}</div>
  if (!summary) return null

  const spendingData = {
    labels: summary.spendingByCategory.map(item => item.category),
    datasets: [{
      data: summary.spendingByCategory.map(item => item.amount),
      backgroundColor: [
        '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'
      ]
    }]
  }

  const fraudData = {
    labels: summary.fraudByCategory.map(item => item.category),
    datasets: [{
      label: 'Fraud Count',
      data: summary.fraudByCategory.map(item => item.count),
      backgroundColor: '#dc3545'
    }]
  }

  return (
    <div className="dashboard">
      <h1>Dashboard</h1>
      
      <div className="metrics-grid">
        <div className="metric-card">
          <h3>Total Income</h3>
          <p className="metric-value income">${summary.totalIncome.toFixed(2)}</p>
        </div>
        
        <div className="metric-card">
          <h3>Total Expenses</h3>
          <p className="metric-value expense">${summary.totalExpense.toFixed(2)}</p>
        </div>
        
        <div className="metric-card">
          <h3>Balance</h3>
          <p className="metric-value balance">${summary.balance.toFixed(2)}</p>
        </div>
        
        <div className="metric-card">
          <h3>Flagged Transactions</h3>
          <p className="metric-value flagged">{summary.totalFlaggedTransactions}</p>
        </div>
        
        <div className="metric-card">
          <h3>Avg Fraud Score</h3>
          <p className="metric-value score">{summary.averageFraudScore.toFixed(1)}</p>
        </div>
      </div>

      <div className="charts-grid">
        <div className="chart-card">
          <h3>Spending by Category</h3>
          {summary.spendingByCategory.length > 0 ? (
            <Pie data={spendingData} />
          ) : (
            <p>No spending data</p>
          )}
        </div>
        
        <div className="chart-card">
          <h3>Fraud by Category</h3>
          {summary.fraudByCategory.length > 0 ? (
            <Bar data={fraudData} />
          ) : (
            <p>No fraud data</p>
          )}
        </div>
      </div>
    </div>
  )
}

export default Dashboard
