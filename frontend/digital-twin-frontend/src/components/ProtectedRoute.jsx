import { useAuth } from '../context/AuthContext'
import AuthModal from './AuthModal'
import { useState } from 'react'

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth()
  const [showAuthModal, setShowAuthModal] = useState(false)

  // Show loading spinner while checking authentication
  if (loading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>Loading...</p>
      </div>
    )
  }

  // If not authenticated, show auth modal
  if (!isAuthenticated()) {
    return (
      <div className="unauthenticated-container">
        <div className="welcome-section">
          <h1>Welcome to Digital Twin Ontology System</h1>
          <p>Please login or register to access the system.</p>
          <button
            className="auth-button"
            onClick={() => setShowAuthModal(true)}
          >
            Get Started
          </button>
        </div>

        <AuthModal
          isOpen={showAuthModal}
          onClose={() => setShowAuthModal(false)}
        />
      </div>
    )
  }

  // If authenticated, render the protected content
  return children
}

export default ProtectedRoute