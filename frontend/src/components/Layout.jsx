import { Outlet, Link, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import './Layout.css'

function Layout() {
  const { user, logout } = useAuth()
  const location = useLocation()

  const isActive = (path) => location.pathname === path ? 'active' : ''

  return (
    <div className="layout">
      <nav className="navbar">
        <div className="nav-brand">
          <h2>FinSight</h2>
        </div>
        <div className="nav-links">
          <Link to="/dashboard" className={isActive('/dashboard')}>Dashboard</Link>
          <Link to="/transactions" className={isActive('/transactions')}>Transactions</Link>
          <Link to="/fraud-alerts" className={isActive('/fraud-alerts')}>Fraud Alerts</Link>
          <Link to="/subscriptions" className={isActive('/subscriptions')}>Subscriptions</Link>
        </div>
        <div className="nav-user">
          <span>{user?.fullName || user?.username}</span>
          <button onClick={logout} className="btn btn-sm">Logout</button>
        </div>
      </nav>
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  )
}

export default Layout
