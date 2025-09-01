import { useState } from 'react'
import { useAuth } from '../context/AuthContext'

const LoginForm = ({ onSwitchToRegister, onClose }) => {
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  })
  const [loading, setLoading] = useState(false)
  const { login, error, skipLogin } = useAuth()

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)

    const result = await login(formData.username, formData.password)

    if (result.success) {
      onClose()
    }

    setLoading(false)
  }

  return (
    <div className="auth-form">
      <h2>Login to Digital Twin</h2>

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="username">Username</label>
          <input
            type="text"
            id="username"
            name="username"
            value={formData.username}
            onChange={handleChange}
            required
            placeholder="Enter your username"
            disabled={loading}
          />
        </div>

        <div className="form-group">
          <label htmlFor="password">Password</label>
          <input
            type="password"
            id="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required
            placeholder="Enter your password"
            disabled={loading}
          />
        </div>

        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        <button
          type="submit"
          className="auth-button"
          disabled={loading}
        >
          {loading ? 'Logging in...' : 'Login'}
        </button>

        <button
          type="button"
          onClick={async () => {
            const result = await skipLogin()
            if (result.success) {
              onClose()
            }
          }}
          className="auth-button"
          style={{ background: '#95a5a6', marginTop: '10px' }}
          disabled={loading}
        >
          Skip Login (Test)
        </button>
      </form>

      <div className="auth-switch">
        <p>
          Don't have an account?{' '}
          <button
            type="button"
            onClick={onSwitchToRegister}
            className="link-button"
            disabled={loading}
          >
            Register here
          </button>
        </p>
      </div>
    </div>
  )
}

export default LoginForm