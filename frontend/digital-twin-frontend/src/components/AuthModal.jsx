import { useState } from 'react'
import LoginForm from './LoginForm'
import RegisterForm from './RegisterForm'

const AuthModal = ({ isOpen, onClose }) => {
  const [isLogin, setIsLogin] = useState(true)

  if (!isOpen) return null

  const handleSwitchToRegister = () => {
    setIsLogin(false)
  }

  const handleSwitchToLogin = () => {
    setIsLogin(true)
  }

  const handleClose = () => {
    setIsLogin(true) // Reset to login form
    onClose()
  }

  return (
    <div className="auth-modal-overlay" onClick={handleClose}>
      <div className="auth-modal" onClick={(e) => e.stopPropagation()}>
        <button
          className="auth-modal-close"
          onClick={handleClose}
          aria-label="Close"
        >
          ×
        </button>

        <div className="auth-modal-content">
          {isLogin ? (
            <LoginForm
              onSwitchToRegister={handleSwitchToRegister}
              onClose={handleClose}
            />
          ) : (
            <RegisterForm
              onSwitchToLogin={handleSwitchToLogin}
              onClose={handleClose}
            />
          )}
        </div>
      </div>
    </div>
  )
}

export default AuthModal