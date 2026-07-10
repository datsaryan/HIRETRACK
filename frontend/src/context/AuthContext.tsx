import { createContext, useContext, useEffect, useState, ReactNode } from 'react'
import { apiClient } from '../api/client'
import { AuthResponse, Role } from '../types'

interface AuthUser {
  userId: string
  email: string
  name: string
  role: Role
  organizationId: string
}

interface AuthContextValue {
  user: AuthUser | null
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (organizationName: string, name: string, email: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const stored = localStorage.getItem('hiretrack_user')
    if (stored) {
      setUser(JSON.parse(stored))
    }
    setLoading(false)
  }, [])

  function persist(data: AuthResponse) {
    const authUser: AuthUser = {
      userId: data.userId,
      email: data.email,
      name: data.name,
      role: data.role,
      organizationId: data.organizationId,
    }
    localStorage.setItem('hiretrack_token', data.accessToken)
    localStorage.setItem('hiretrack_user', JSON.stringify(authUser))
    setUser(authUser)
  }

  async function login(email: string, password: string) {
    const res = await apiClient.post<AuthResponse>('/auth/login', { email, password })
    persist(res.data)
  }

  async function register(organizationName: string, name: string, email: string, password: string) {
    const res = await apiClient.post<AuthResponse>('/auth/register', {
      organizationName,
      name,
      email,
      password,
    })
    persist(res.data)
  }

  function logout() {
    localStorage.removeItem('hiretrack_token')
    localStorage.removeItem('hiretrack_user')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
