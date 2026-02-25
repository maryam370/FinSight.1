import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { getSubscriptions, ignoreSubscription, getDueSoonSubscriptions, detectSubscriptions } from '../services/api'
import './Subscriptions.css'

function Subscriptions() {
  const { user } = useAuth()
  const [subscriptions, setSubscriptions] = useState([])
  const [dueSoon, setDueSoon] = useState([])
  const [loading, setLoading] = useState(true)
  const [detecting, setDetecting] = useState(false)

  useEffect(() => {
    loadSubscriptions()
    loadDueSoon()
  }, [])

  const loadSubscriptions = async () => {
    try {
      const response = await getSubscriptions({ userId: user.id, status: 'ACTIVE' })
      setSubscriptions(response.data)
    } catch (err) {
      console.error('Failed to load subscriptions', err)
    } finally {
      setLoading(false)
    }
  }

  const loadDueSoon = async () => {
    try {
      const response = await getDueSoonSubscriptions(user.id, 7)
      setDueSoon(response.data)
    } catch (err) {
      console.error('Failed to load due soon subscriptions', err)
    }
  }

  const handleDetect = async () => {
    setDetecting(true)
    try {
      await detectSubscriptions(user.id)
      await loadSubscriptions()
      await loadDueSoon()
    } catch (err) {
      console.error('Failed to detect subscriptions', err)
    } finally {
      setDetecting(false)
    }
  }

  const handleIgnore = async (id) => {
    try {
      await ignoreSubscription(id)
      loadSubscriptions()
      loadDueSoon()
    } catch (err) {
      console.error('Failed to ignore subscription', err)
    }
  }

  if (loading) return <div className="loading">Loading subscriptions...</div>

  return (
    <div className="subscriptions">
      <div className="page-header">
        <h1>Subscriptions</h1>
        <button className="btn btn-primary" onClick={handleDetect} disabled={detecting}>
          {detecting ? 'Detecting...' : 'Detect Subscriptions'}
        </button>
      </div>

      {dueSoon.length > 0 && (
        <div className="due-soon-banner alert alert-info">
          <strong>⚠️ {dueSoon.length} subscription(s) due soon!</strong>
          <div className="due-soon-list">
            {dueSoon.map(sub => (
              <div key={sub.id}>
                {sub.merchant} - ${sub.avgAmount.toFixed(2)} due on {new Date(sub.nextDueDate).toLocaleDateString()}
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="subscriptions-grid">
        {subscriptions.length === 0 ? (
          <div className="card">
            <p style={{ textAlign: 'center', padding: '20px' }}>
              No active subscriptions found. Click "Detect Subscriptions" to scan your transactions.
            </p>
          </div>
        ) : (
          subscriptions.map(sub => (
            <div key={sub.id} className="subscription-card card">
              <div className="sub-header">
                <h3>{sub.merchant}</h3>
                <span className="badge badge-low">{sub.status}</span>
              </div>

              <div className="sub-details">
                <div className="detail-row">
                  <span className="label">Average Amount:</span>
                  <span className="amount">${sub.avgAmount.toFixed(2)}</span>
                </div>
                <div className="detail-row">
                  <span className="label">Last Paid:</span>
                  <span>{new Date(sub.lastPaidDate).toLocaleDateString()}</span>
                </div>
                <div className="detail-row">
                  <span className="label">Next Due:</span>
                  <span>{new Date(sub.nextDueDate).toLocaleDateString()}</span>
                </div>
                <div className="detail-row">
                  <span className="label">Created:</span>
                  <span>{new Date(sub.createdAt).toLocaleDateString()}</span>
                </div>
              </div>

              <button
                className="btn btn-danger btn-block"
                onClick={() => handleIgnore(sub.id)}
              >
                Ignore Subscription
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  )
}

export default Subscriptions
