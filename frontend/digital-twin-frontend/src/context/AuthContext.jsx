import React, { createContext, useContext, useState, useEffect } from 'react'
import jwtDecode from 'jwt-decode'
import { authService } from '../services/authService'

const AuthContext = createContext()

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // Initialize auth state from localStorage
  useEffect(() => {
    const initializeAuth = async () => {
      const token = localStorage.getItem('accessToken')
      const refreshToken = localStorage.getItem('refreshToken')

      if (token && refreshToken) {
        try {
          const decoded = jwtDecode(token)
          const currentTime = Date.now() / 1000

          if (decoded.exp > currentTime) {
            setUser({
              id: decoded.userId,
              username: decoded.sub,
              email: decoded.email,
              roles: decoded.roles || []
            })
          } else {
            // Token expired, try refresh
            await refreshAccessToken()
          }
        } catch (error) {
          console.error('Error decoding token:', error)
          logout()
        }
      }

      setLoading(false)
    }

    initializeAuth()
  }, [])

  const login = async (username, password) => {
    try {
      setError(null)
      setLoading(true)

      const response = await authService.login(username, password)

      const { accessToken, refreshToken } = response.data
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)

      const decoded = jwtDecode(accessToken)
      setUser({
        id: decoded.userId,
        username: decoded.sub,
        email: decoded.email,
        roles: decoded.roles || []
      })

      return { success: true }
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Login failed'
      setError(errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      setLoading(false)
    }
  }

  const register = async (username, email, password) => {
    try {
      setError(null)
      setLoading(true)

      const response = await authService.register(username, email, password)

      const { accessToken, refreshToken } = response.data
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)

      const decoded = jwtDecode(accessToken)
      setUser({
        id: decoded.userId,
        username: decoded.sub,
        email: decoded.email,
        roles: decoded.roles || []
      })

      return { success: true }
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Registration failed'
      setError(errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      setLoading(false)
    }
  }

  const refreshAccessToken = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken')
      if (!refreshToken) {
        throw new Error('No refresh token available')
      }

      const response = await authService.refreshToken(refreshToken)

      const { accessToken, newRefreshToken } = response.data
      localStorage.setItem('accessToken', accessToken)
      if (newRefreshToken) {
        localStorage.setItem('refreshToken', newRefreshToken)
      }

      const decoded = jwtDecode(accessToken)
      setUser({
        id: decoded.userId,
        username: decoded.sub,
        email: decoded.email,
        roles: decoded.roles || []
      })

      return accessToken
    } catch (error) {
      console.error('Token refresh failed:', error)
      logout()
      throw error
    }
  }

  const logout = () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    setUser(null)
    setError(null)
  }

  const getAccessToken = () => {
    return localStorage.getItem('accessToken')
  }

  const isAuthenticated = () => {
    return user !== null
  }

  const hasRole = (role) => {
    return user?.roles?.includes(role) || false
  }

  const value = {
    user,
    loading,
    error,
    login,
    register,
    logout,
    refreshAccessToken,
    getAccessToken,
    isAuthenticated,
    hasRole
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}