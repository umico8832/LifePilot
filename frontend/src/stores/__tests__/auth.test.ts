import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// Mock the API module before importing the store
vi.mock('@/api/auth', () => ({
  login: vi.fn(),
  register: vi.fn(),
  fetchCurrentUser: vi.fn(),
}))

import { useAuthStore } from '../auth'
import { login, register, fetchCurrentUser } from '@/api/auth'

const mockedLogin = vi.mocked(login)
const mockedRegister = vi.mocked(register)
const mockedFetchCurrentUser = vi.mocked(fetchCurrentUser)

const mockUser = {
  id: 1,
  email: 'test@example.com',
  displayName: 'Test User',
}

const mockAuthResponse = {
  tokenType: 'Bearer' as const,
  accessToken: 'mock-jwt-token',
  user: mockUser,
}

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('starts with no token and no user', () => {
    const store = useAuthStore()
    expect(store.token).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })

  it('reads existing token from localStorage', () => {
    localStorage.setItem('lifepilot.accessToken', 'existing-token')
    const store = useAuthStore()
    expect(store.token).toBe('existing-token')
    expect(store.isAuthenticated).toBe(true)
  })

  it('login sets token and user', async () => {
    mockedLogin.mockResolvedValue(mockAuthResponse)
    const store = useAuthStore()

    await store.login({ email: 'test@example.com', password: 'password' })

    expect(store.token).toBe('mock-jwt-token')
    expect(store.user).toEqual(mockUser)
    expect(store.isAuthenticated).toBe(true)
    expect(localStorage.getItem('lifepilot.accessToken')).toBe('mock-jwt-token')
  })

  it('register sets token and user', async () => {
    mockedRegister.mockResolvedValue(mockAuthResponse)
    const store = useAuthStore()

    await store.register({ email: 'new@example.com', password: 'password', displayName: 'New' })

    expect(store.token).toBe('mock-jwt-token')
    expect(store.user).toEqual(mockUser)
    expect(store.isAuthenticated).toBe(true)
    expect(localStorage.getItem('lifepilot.accessToken')).toBe('mock-jwt-token')
  })

  it('logout clears token and user', async () => {
    localStorage.setItem('lifepilot.accessToken', 'existing-token')
    const store = useAuthStore()
    store.setSession('tok', mockUser)

    store.logout()

    expect(store.token).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(localStorage.getItem('lifepilot.accessToken')).toBeNull()
  })

  it('loadCurrentUser fetches and sets user', async () => {
    mockedFetchCurrentUser.mockResolvedValue(mockUser)
    const store = useAuthStore()
    store.setSession('tok', mockUser)

    await store.loadCurrentUser()

    expect(store.user).toEqual(mockUser)
    expect(mockedFetchCurrentUser).toHaveBeenCalled()
  })

  it('loadCurrentUser does nothing without token', async () => {
    const store = useAuthStore()
    await store.loadCurrentUser()
    expect(mockedFetchCurrentUser).not.toHaveBeenCalled()
  })

  it('loadCurrentUser logs out on error', async () => {
    mockedFetchCurrentUser.mockRejectedValue(new Error('401'))
    const store = useAuthStore()
    store.setSession('tok', mockUser)

    await store.loadCurrentUser()

    expect(store.token).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })

  it('login sets loading flag', async () => {
    let resolveLogin: (v: typeof mockAuthResponse) => void
    mockedLogin.mockImplementation(
      () => new Promise((resolve) => { resolveLogin = resolve }),
    )
    const store = useAuthStore()

    const promise = store.login({ email: 'a@b.com', password: 'pw' })
    expect(store.loading).toBe(true)

    resolveLogin!(mockAuthResponse)
    await promise
    expect(store.loading).toBe(false)
  })
})