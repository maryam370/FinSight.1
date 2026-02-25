import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { getTransactions, createTransaction } from '../services/api'
import './Transactions.css'

function Transactions() {
  const { user } = useAuth()
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(true)
  const [filters, setFilters] = useState({
    type: '',
    category: '',
    fraudulent: '',
    sortBy: 'transactionDate',
    sortDir: 'desc'
  })
  const [showAddModal, setShowAddModal] = useState(false)
  const [liveRefresh, setLiveRefresh] = useState(false)

  useEffect(() => {
    loadTransactions()
  }, [filters])

  useEffect(() => {
    let interval
    if (liveRefresh) {
      interval = setInterval(loadTransactions, 5000)
    }
    return () => clearInterval(interval)
  }, [liveRefresh, filters])

  const loadTransactions = async () => {
    try {
      const params = { userId: user.id, ...filters }
      Object.keys(params).forEach(key => !params[key] && delete params[key])
      const response = await getTransactions(params)
      setTransactions(response.data.content || response.data)
    } catch (err) {
      console.error('Failed to load transactions', err)
    } finally {
      setLoading(false)
    }
  }

  const handleFilterChange = (e) => {
    setFilters({ ...filters, [e.target.name]: e.target.value })
  }

  const getRiskBadge = (riskLevel) => {
    const classes = {
      LOW: 'badge-low',
      MEDIUM: 'badge-medium',
      HIGH: 'badge-high'
    }
    return <span className={`badge ${classes[riskLevel]}`}>{riskLevel}</span>
  }

  if (loading) return <div className="loading">Loading transactions...</div>

  return (
    <div className="transactions">
      <div className="page-header">
        <h1>Transactions</h1>
        <div className="header-actions">
          <label className="live-refresh">
            <input
              type="checkbox"
              checked={liveRefresh}
              onChange={(e) => setLiveRefresh(e.target.checked)}
            />
            Live Refresh
          </label>
          <button className="btn btn-primary" onClick={() => setShowAddModal(true)}>
            Add Transaction
          </button>
        </div>
      </div>

      <div className="filters-panel card">
        <div className="filters-grid">
          <div className="form-group">
            <label>Type</label>
            <select name="type" className="form-control" value={filters.type} onChange={handleFilterChange}>
              <option value="">All</option>
              <option value="INCOME">Income</option>
              <option value="EXPENSE">Expense</option>
            </select>
          </div>

          <div className="form-group">
            <label>Category</label>
            <input
              type="text"
              name="category"
              className="form-control"
              placeholder="e.g., groceries"
              value={filters.category}
              onChange={handleFilterChange}
            />
          </div>

          <div className="form-group">
            <label>Fraudulent</label>
            <select name="fraudulent" className="form-control" value={filters.fraudulent} onChange={handleFilterChange}>
              <option value="">All</option>
              <option value="true">Yes</option>
              <option value="false">No</option>
            </select>
          </div>

          <div className="form-group">
            <label>Sort By</label>
            <select name="sortBy" className="form-control" value={filters.sortBy} onChange={handleFilterChange}>
              <option value="transactionDate">Date</option>
              <option value="amount">Amount</option>
              <option value="fraudScore">Fraud Score</option>
            </select>
          </div>

          <div className="form-group">
            <label>Direction</label>
            <select name="sortDir" className="form-control" value={filters.sortDir} onChange={handleFilterChange}>
              <option value="desc">Descending</option>
              <option value="asc">Ascending</option>
            </select>
          </div>
        </div>
      </div>

      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>Date</th>
              <th>Description</th>
              <th>Category</th>
              <th>Type</th>
              <th>Amount</th>
              <th>Location</th>
              <th>Risk</th>
              <th>Score</th>
            </tr>
          </thead>
          <tbody>
            {transactions.length === 0 ? (
              <tr>
                <td colSpan="8" style={{ textAlign: 'center' }}>No transactions found</td>
              </tr>
            ) : (
              transactions.map(tx => (
                <tr key={tx.id} className={tx.fraudulent ? 'fraudulent-row' : ''}>
                  <td>{new Date(tx.transactionDate).toLocaleDateString()}</td>
                  <td>{tx.description}</td>
                  <td>{tx.category}</td>
                  <td>
                    <span className={`badge ${tx.type === 'INCOME' ? 'badge-low' : 'badge-medium'}`}>
                      {tx.type}
                    </span>
                  </td>
                  <td className={tx.type === 'INCOME' ? 'amount-income' : 'amount-expense'}>
                    ${tx.amount.toFixed(2)}
                  </td>
                  <td>{tx.location}</td>
                  <td>{getRiskBadge(tx.riskLevel)}</td>
                  <td>{tx.fraudScore}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {showAddModal && (
        <AddTransactionModal
          userId={user.id}
          onClose={() => setShowAddModal(false)}
          onSuccess={() => {
            setShowAddModal(false)
            loadTransactions()
          }}
        />
      )}
    </div>
  )
}

function AddTransactionModal({ userId, onClose, onSuccess }) {
  const [formData, setFormData] = useState({
    userId,
    amount: '',
    type: 'EXPENSE',
    category: '',
    description: '',
    location: '',
    transactionDate: new Date().toISOString().split('T')[0]
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    try {
      // Convert date string to LocalDateTime format (ISO 8601 with time)
      const dateTime = formData.transactionDate + 'T12:00:00'
      
      // Prepare payload with correct types
      const payload = {
        userId: formData.userId,
        amount: parseFloat(formData.amount),
        type: formData.type,
        category: formData.category,
        description: formData.description,
        location: formData.location,
        transactionDate: dateTime
      }
      
      await createTransaction(payload)
      onSuccess()
    } catch (err) {
      console.error('Transaction creation error:', err)
      setError(err.response?.data?.message || 'Failed to create transaction')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <h2>Add Transaction</h2>
        {error && <div className="alert alert-error">{error}</div>}
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Amount</label>
            <input
              type="number"
              step="0.01"
              className="form-control"
              value={formData.amount}
              onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label>Type</label>
            <select
              className="form-control"
              value={formData.type}
              onChange={(e) => setFormData({ ...formData, type: e.target.value })}
            >
              <option value="INCOME">Income</option>
              <option value="EXPENSE">Expense</option>
            </select>
          </div>

          <div className="form-group">
            <label>Category</label>
            <input
              type="text"
              className="form-control"
              value={formData.category}
              onChange={(e) => setFormData({ ...formData, category: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label>Description</label>
            <input
              type="text"
              className="form-control"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label>Location</label>
            <input
              type="text"
              className="form-control"
              value={formData.location}
              onChange={(e) => setFormData({ ...formData, location: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label>Date</label>
            <input
              type="date"
              className="form-control"
              value={formData.transactionDate}
              onChange={(e) => setFormData({ ...formData, transactionDate: e.target.value })}
              required
            />
          </div>

          <div className="modal-actions">
            <button type="button" className="btn" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creating...' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default Transactions
