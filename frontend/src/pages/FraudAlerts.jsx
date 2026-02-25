import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { getFraudAlerts, resolveFraudAlert } from '../services/api'
import './FraudAlerts.css'

function FraudAlerts() {
  const { user } = useAuth()
  const [alerts, setAlerts] = useState([])
  const [loading, setLoading] = useState(true)
  const [filters, setFilters] = useState({
    resolved: 'false',
    severity: ''
  })

  useEffect(() => {
    loadAlerts()
  }, [filters])

  const loadAlerts = async () => {
    try {
      const params = { userId: user.id, ...filters }
      Object.keys(params).forEach(key => !params[key] && delete params[key])
      const response = await getFraudAlerts(params)
      setAlerts(response.data)
    } catch (err) {
      console.error('Failed to load alerts', err)
    } finally {
      setLoading(false)
    }
  }

  const handleResolve = async (id) => {
    try {
      await resolveFraudAlert(id)
      loadAlerts()
    } catch (err) {
      console.error('Failed to resolve alert', err)
    }
  }

  const getSeverityBadge = (severity) => {
    const classes = {
      LOW: 'badge-low',
      MEDIUM: 'badge-medium',
      HIGH: 'badge-high'
    }
    return <span className={`badge ${classes[severity]}`}>{severity}</span>
  }

  if (loading) return <div className="loading">Loading fraud alerts...</div>

  return (
    <div className="fraud-alerts">
      <h1>Fraud Alerts</h1>

      <div className="filters-panel card">
        <div className="filters-grid">
          <div className="form-group">
            <label>Status</label>
            <select
              className="form-control"
              value={filters.resolved}
              onChange={(e) => setFilters({ ...filters, resolved: e.target.value })}
            >
              <option value="">All</option>
              <option value="false">Unresolved</option>
              <option value="true">Resolved</option>
            </select>
          </div>

          <div className="form-group">
            <label>Severity</label>
            <select
              className="form-control"
              value={filters.severity}
              onChange={(e) => setFilters({ ...filters, severity: e.target.value })}
            >
              <option value="">All</option>
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
            </select>
          </div>
        </div>
      </div>

      <div className="alerts-grid">
        {alerts.length === 0 ? (
          <div className="card">
            <p style={{ textAlign: 'center', padding: '20px' }}>No fraud alerts found</p>
          </div>
        ) : (
          alerts.map(alert => (
            <div key={alert.id} className="alert-card card">
              <div className="alert-header">
                {getSeverityBadge(alert.severity)}
                {alert.resolved && <span className="badge badge-low">Resolved</span>}
              </div>
              
              <p className="alert-message">{alert.message}</p>
              
              <div className="alert-details">
                <div className="detail-row">
                  <span className="label">Transaction:</span>
                  <span>{alert.transactionDescription}</span>
                </div>
                <div className="detail-row">
                  <span className="label">Amount:</span>
                  <span className="amount">${alert.transactionAmount?.toFixed(2)}</span>
                </div>
                <div className="detail-row">
                  <span className="label">Date:</span>
                  <span>{new Date(alert.createdAt).toLocaleString()}</span>
                </div>
              </div>

              {!alert.resolved && (
                <button
                  className="btn btn-success btn-block"
                  onClick={() => handleResolve(alert.id)}
                >
                  Resolve Alert
                </button>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  )
}

export default FraudAlerts
